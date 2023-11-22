package com.glia.widgets.engagement

import com.glia.androidsdk.Operator
import com.glia.androidsdk.comms.Media
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.core.notification.domain.CallNotificationUseCase
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Data
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.hasMedia
import io.reactivex.Flowable

internal class EndEngagementUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(silently: Boolean = false) = engagementRepository.endEngagement(silently)
}

internal class EngagementRequestUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke() = engagementRepository.engagementRequest

    fun accept(visitorContextAssetId: String) = engagementRepository.acceptCurrentEngagementRequest(visitorContextAssetId)
    fun decline() = engagementRepository.declineCurrentEngagementRequest()
}

internal class HasOngoingEngagementUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Boolean = engagementRepository.hasOngoingEngagement
}

internal class IsCurrentEngagementCallVisualizer(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Boolean = engagementRepository.isCallVisualizerEngagement
}

internal class SurveyUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Flowable<OneTimeEvent<SurveyState>> = engagementRepository.survey.map(::OneTimeEvent)
}

internal class EngagementStateUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Flowable<State> = engagementRepository.engagementState
}

internal class OperatorTypingUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Flowable<Boolean> = engagementRepository.operatorTypingStatus
}

internal class CurrentOperatorUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Flowable<Operator> = engagementRepository.currentOperator
        .filter(Data<Operator>::hasValue)
        .map { it as Data.Value }
        .map(Data.Value<Operator>::result)
}

internal class IsOperatorPresentUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Boolean = engagementRepository.isOperatorPresent
}

internal class MediaUpgradeOfferUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Flowable<MediaUpgradeOffer> = engagementRepository.mediaUpgradeOffer
}

internal class AcceptMediaUpgradeOfferUseCase(
    private val engagementRepository: EngagementRepository,
    private val permissionManager: PermissionManager
) {
    val result: Flowable<MediaUpgradeOffer> = engagementRepository.mediaUpgradeOfferAcceptResult
        .filter { it.isSuccess }
        .map { it.getOrThrow() }

    operator fun invoke(offer: MediaUpgradeOffer) = permissionManager.apply {
        val permissions = getPermissionsForMediaUpgradeOffer(offer)
        handlePermissions(permissions.requiredPermissions, permissions.additionalPermissions, {
            if (it) engagementRepository.acceptMediaUpgradeRequest(offer)
        })
    }
}

internal class DeclineMediaUpgradeOfferUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(offer: MediaUpgradeOffer) = engagementRepository.declineMediaUpgradeRequest(offer)
}

internal class OperatorMediaUseCase(private val engagementRepository: EngagementRepository) {
    private val operatorMediaState: Flowable<MediaState> = engagementRepository.operatorMediaState
        .filter(Data<MediaState>::hasValue)
        .map { it as Data.Value }
        .map(Data.Value<MediaState>::result)

    val hasMedia: Boolean get() = engagementRepository.operatorCurrentMediaState?.hasMedia ?: false
    operator fun invoke(): Flowable<MediaState> = operatorMediaState
}

internal class VisitorMediaUseCase(private val engagementRepository: EngagementRepository) {
    val hasMedia: Boolean get() = engagementRepository.operatorCurrentMediaState?.hasMedia ?: false

    val onHoldState: Flowable<Boolean> = engagementRepository.onHoldState

    private val visitorMediaState: Flowable<MediaState> = engagementRepository.visitorMediaState
        .filter(Data<MediaState>::hasValue)
        .map { it as Data.Value }
        .map(Data.Value<MediaState>::result)

    operator fun invoke(): Flowable<MediaState> = visitorMediaState
}

internal class ToggleVisitorAudioMediaStateUseCase(private val repository: EngagementRepository) {
    operator fun invoke() {
        val status: Media.Status = repository.visitorCurrentMediaState?.audio?.status ?: return
        when (status) {
            Media.Status.PLAYING -> repository.muteVisitorAudio()
            Media.Status.PAUSED -> repository.unMuteVisitorAudio()
            Media.Status.DISCONNECTED -> Logger.d(TAG, "Visitor Audio is disconnected")
        }
    }
}

internal class ToggleVisitorVideoMediaStateUseCase(private val repository: EngagementRepository) {
    operator fun invoke() {
        val status: Media.Status = repository.visitorCurrentMediaState?.video?.status ?: return
        when (status) {
            Media.Status.PLAYING -> repository.pauseVisitorVideo()
            Media.Status.PAUSED -> repository.resumeVisitorVideo()
            Media.Status.DISCONNECTED -> Logger.d(TAG, "Visitor Video is disconnected")
        }
    }
}

internal class EngagementTypeUseCase(
    private val hasOngoingEngagementUseCase: HasOngoingEngagementUseCase,
    private val isCurrentEngagementCallVisualizer: IsCurrentEngagementCallVisualizer,
    private val operatorMediaUseCase: OperatorMediaUseCase,
    private val visitorMediaUseCase: VisitorMediaUseCase,
    private val isOperatorPresentUseCase: IsOperatorPresentUseCase
) {
    val isMediaEngagement: Boolean get() = hasOngoingEngagementUseCase() && isOperatorPresentUseCase() && hasAnyMedia

    val isChatEngagement: Boolean
        get() = hasOngoingEngagementUseCase() && !isCurrentEngagementCallVisualizer() && isOperatorPresentUseCase() && !hasAnyMedia

    private val hasAnyMedia: Boolean get() = visitorMediaUseCase.hasMedia || operatorMediaUseCase.hasMedia

    val isCallVisualizerScreenSharing: Boolean get() = isCurrentEngagementCallVisualizer.invoke() && !hasAnyMedia
}

internal class ReleaseResourcesUseCase(
    private val removeScreenSharingNotificationUseCase: RemoveScreenSharingNotificationUseCase,
    private val callNotificationUseCase: CallNotificationUseCase,
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val gliaEngagementConfigRepository: GliaEngagementConfigRepository,
    private val updateFromCallScreenUseCase: UpdateFromCallScreenUseCase,
) {
    operator fun invoke() {
        fileAttachmentRepository.clearObservers()
        fileAttachmentRepository.detachAllFiles()
        removeScreenSharingNotificationUseCase()
        callNotificationUseCase.removeAllNotifications()
        gliaEngagementConfigRepository.reset()
        updateFromCallScreenUseCase.updateFromCallScreen(false)
        Dependencies.destroyControllers()
    }
}
