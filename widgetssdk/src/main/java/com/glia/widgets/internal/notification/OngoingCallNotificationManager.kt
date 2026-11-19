package com.glia.widgets.internal.notification

import android.Manifest
import android.app.Activity
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.base.SimpleActivityLifecycleCallbacks
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.OperatorMediaUseCase
import com.glia.widgets.engagement.domain.VisitorMediaUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.internal.notification.device.INotificationManager
import com.glia.widgets.internal.notification.domain.CallNotification
import com.glia.widgets.internal.notification.domain.CallNotification.Phase
import com.glia.widgets.internal.notification.domain.CallNotificationUseCase
import com.glia.widgets.internal.permissions.PermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.reactive.asFlow

/**
 * Owns the call notification and its foreground service for the whole app, whichever screen is open.
 *
 * The service starts when the visitor queues for an audio or video engagement or accepts a media upgrade.
 * Android 14+ allows creating a microphone or camera service only while an activity is visible:
 * https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start#wiu-restrictions
 * A start that is not allowed or was refused waits for the next activity start.
 * Later changes only update the notification text, which is allowed in the background.
 *
 * Every input is an [Event]. One flow folds the events into a [CallState] on the main thread, one event at a time.
 * It counts started activities itself. ProcessLifecycleOwner reports the app as stopped only about 700 ms
 * after its last activity stops, so it can't tell when Android allows the service to start.
 */
internal class OngoingCallNotificationManager(
    engagementStateUseCase: EngagementStateUseCase,
    visitorMediaUseCase: VisitorMediaUseCase,
    operatorMediaUseCase: OperatorMediaUseCase,
    acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase,
    private val callNotificationUseCase: CallNotificationUseCase,
    private val permissionManager: PermissionManager,
    private val notificationManager: INotificationManager,
    // The manager lives as long as the process, so nothing cancels this scope.
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : SimpleActivityLifecycleCallbacks() {
    // Unlimited, so a send never fails and no event is lost before the flow is collected.
    private val deviceEvents: Channel<Event> = Channel(Channel.UNLIMITED)

    init {
        merge<Event>(
            engagementStateUseCase().asFlow().map { Event.EngagementChanged(it) },
            liveCallChanges(visitorMediaUseCase().asFlow(), operatorMediaUseCase().asFlow()),
            acceptMediaUpgradeOfferUseCase.result.asFlow().map { Event.UpgradeAccepted(it) },
            deviceEvents.receiveAsFlow()
        )
            .runningFold(CallState(), ::next)
            .catch { Logger.w(TAG, "Call notification updates stopped: ${it.javaClass.simpleName}") }
            .launchIn(scope)
    }

    override fun onActivityStarted(activity: Activity) {
        deviceEvents.trySend(Event.ActivityStarted)
    }

    override fun onActivityStopped(activity: Activity) {
        deviceEvents.trySend(Event.ActivityStopped)
    }

    // Called when the service stopped without being asked, so the next activity start brings it back.
    fun onCallServiceStopped() {
        deviceEvents.trySend(Event.ServiceStopped)
    }

    private fun liveCallChanges(visitorMedia: Flow<MediaState>, operatorMedia: Flow<MediaState>): Flow<Event> =
        combine(visitorMedia, operatorMedia, ::liveCallChange).filterNotNull()

    private fun liveCallChange(visitorMedia: MediaState, operatorMedia: MediaState): Event? = try {
        Event.LiveCallChanged(callNotificationUseCase(visitorMedia, operatorMedia))
    } catch (error: IllegalStateException) {
        Logger.d(TAG, "Skipping call notification update for unsupported media: ${error.message}")
        null
    }

    private fun next(state: CallState, event: Event): CallState {
        val reduced = reduce(state, event)
        return try {
            show(reduced)
        } catch (error: RuntimeException) {
            // An error that ends the flow would leave the notification up for good. The next event tries again.
            Logger.w(TAG, "Failed to update the call notification: ${error.javaClass.simpleName}")
            reduced
        }
    }

    private fun reduce(state: CallState, event: Event): CallState {
        val reduced = when (event) {
            is Event.EngagementChanged -> state.copy(requestedCall = requestedCall(state.requestedCall, event.state))
            // Live media replaces the request it came from.
            is Event.LiveCallChanged -> state.copy(liveCall = event.call, requestedCall = state.requestedCall.takeIf { event.call == null })
            is Event.UpgradeAccepted -> state.copy(requestedCall = connectingCall(event.offer))
            is Event.ActivityStarted -> state.copy(startedActivities = state.startedActivities + 1, isWaitingForActivityStart = false)
            is Event.ActivityStopped -> state.copy(startedActivities = maxOf(state.startedActivities - 1, 0))
            is Event.ServiceStopped -> state.copy(shown = Shown(), isWaitingForActivityStart = true)
        }
        // A refused start holds back only the call it was for.
        return if (reduced.call == null) reduced.copy(isWaitingForActivityStart = false) else reduced
    }

    private fun requestedCall(current: CallNotification?, state: State): CallNotification? = when (state) {
        is State.PreQueuing, is State.Queuing -> state.queueingMediaType?.let(::waitingCall)
        is State.EngagementStarted, is State.Update -> current?.copy(phase = Phase.CONNECTING)
        // The queue was left or failed, the engagement ended, or it moved to secure conversations.
        else -> null
    }

    // The only place with side effects. It acts only where the state differs from what the system shows.
    private fun show(state: CallState): CallState {
        val shown = state.shown
        val call = state.call ?: return state.copy(shown = hide(shown))
        // A requested upgrade adds its types while the current call is still live, so they are ready once the upgrade connects.
        val types = shown.serviceTypes + typesFor(state.liveCall) + typesFor(state.requestedCall)

        return when {
            types == shown.serviceTypes || !state.canStartService -> state.copy(shown = update(shown, call))
            notificationManager.startCallService(call, types.microphone, types.camera) -> state.copy(shown = Shown(call, types))
            else -> state.copy(shown = update(shown, call), isWaitingForActivityStart = true)
        }
    }

    private fun update(shown: Shown, call: CallNotification): Shown {
        if (call == shown.call) return shown
        notificationManager.showCallNotification(call)
        return shown.copy(call = call)
    }

    private fun hide(shown: Shown): Shown {
        if (shown.call != null) notificationManager.removeCallNotification()
        return Shown()
    }

    // Asking for a type without its permission makes the service start fail on Android 14+.
    private fun typesFor(call: CallNotification?): ServiceTypes = ServiceTypes(
        microphone = call?.hasAudio == true && permissionManager.hasPermission(Manifest.permission.RECORD_AUDIO),
        camera = call?.video == MediaDirection.TWO_WAY && permissionManager.hasPermission(Manifest.permission.CAMERA)
    )

    private fun waitingCall(mediaType: MediaType): CallNotification? = when (mediaType) {
        MediaType.AUDIO -> CallNotification(Phase.WAITING, MediaDirection.NONE, hasAudio = true)
        MediaType.VIDEO -> CallNotification(Phase.WAITING, MediaDirection.TWO_WAY, hasAudio = true)
        else -> null
    }

    private fun connectingCall(offer: MediaUpgradeOffer): CallNotification =
        CallNotification(Phase.CONNECTING, offer.video, hasAudio = offer.audio == MediaDirection.TWO_WAY)

    private sealed interface Event {
        data class EngagementChanged(val state: State) : Event
        data class LiveCallChanged(val call: CallNotification?) : Event
        data class UpgradeAccepted(val offer: MediaUpgradeOffer) : Event
        data object ActivityStarted : Event
        data object ActivityStopped : Event
        data object ServiceStopped : Event
    }

    private data class CallState(
        // A call the visitor asked for that has no live media yet.
        val requestedCall: CallNotification? = null,
        val liveCall: CallNotification? = null,
        val startedActivities: Int = 0,
        // Set after the system refused a start or the service stopped on its own. A retry before the next activity start would fail again.
        val isWaitingForActivityStart: Boolean = false,
        val shown: Shown = Shown()
    ) {
        val call: CallNotification? get() = liveCall ?: requestedCall
        val canStartService: Boolean get() = startedActivities > 0 && !isWaitingForActivityStart
    }

    // What the system shows now. The service types are NONE while no service runs, and only grow while it runs.
    private data class Shown(val call: CallNotification? = null, val serviceTypes: ServiceTypes = ServiceTypes.NONE)

    private data class ServiceTypes(val microphone: Boolean, val camera: Boolean) {
        operator fun plus(other: ServiceTypes): ServiceTypes = ServiceTypes(microphone || other.microphone, camera || other.camera)

        companion object {
            val NONE: ServiceTypes = ServiceTypes(microphone = false, camera = false)
        }
    }
}
