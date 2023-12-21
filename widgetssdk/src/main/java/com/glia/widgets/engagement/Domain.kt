package com.glia.widgets.engagement

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.Operator
import com.glia.androidsdk.comms.Media
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.core.notification.domain.CallNotificationUseCase
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Data
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.hasMedia
import io.reactivex.Flowable

internal class EndEngagementUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(silently: Boolean = false) {
        if (engagementRepository.isQueueing) {
            engagementRepository.cancelQueuing()
        } else {
            engagementRepository.endEngagement(silently)
        }
    }
}

internal class EngagementRequestUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke() = engagementRepository.engagementRequest

    fun accept(visitorContextAssetId: String) = engagementRepository.acceptCurrentEngagementRequest(visitorContextAssetId)
    fun decline() = engagementRepository.declineCurrentEngagementRequest()
}

internal class IsQueueingOrEngagementUseCase(private val engagementRepository: EngagementRepository) {
    val hasOngoingEngagement: Boolean get() = engagementRepository.hasOngoingEngagement
    val isQueueingForMedia: Boolean get() = engagementRepository.isQueueingForMedia
    val isQueueingForChat: Boolean get() = engagementRepository.isQueueing && !isQueueingForMedia
    operator fun invoke(): Boolean = engagementRepository.isQueueingOrEngagement
}

internal class IsCurrentEngagementCallVisualizerUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Boolean = engagementRepository.isCallVisualizerEngagement
}

internal class SurveyUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Flowable<SurveyState> = engagementRepository.survey
}

internal class EngagementStateUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Flowable<State> = engagementRepository.engagementState
}

internal class EnqueueForEngagementUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(queueId: String, mediaType: Engagement.MediaType? = null, visitorContextAssetId: String? = null) {
        if (mediaType == null) {
            engagementRepository.queueForChatEngagement(queueId, visitorContextAssetId)
        } else {
            engagementRepository.queueForMediaEngagement(queueId, mediaType, visitorContextAssetId)
        }
    }
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

    val resultForCallVisualizer: Flowable<MediaUpgradeOffer> = result.filter { engagementRepository.isCallVisualizerEngagement }

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
    private val isQueueingOrEngagementUseCase: IsQueueingOrEngagementUseCase,
    private val isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase,
    private val operatorMediaUseCase: OperatorMediaUseCase,
    private val visitorMediaUseCase: VisitorMediaUseCase,
    private val isOperatorPresentUseCase: IsOperatorPresentUseCase
) {
    private val hasOngoingEngagement get() = isQueueingOrEngagementUseCase.hasOngoingEngagement
    val isMediaEngagement: Boolean get() = hasOngoingEngagement && isOperatorPresentUseCase() && hasAnyMedia
    val isChatEngagement: Boolean get() = hasOngoingEngagement && !isCurrentEngagementCallVisualizerUseCase() && isOperatorPresentUseCase() && !hasAnyMedia
    private val hasAnyMedia: Boolean get() = visitorMediaUseCase.hasMedia || operatorMediaUseCase.hasMedia
    val isCallVisualizerScreenSharing: Boolean get() = isCurrentEngagementCallVisualizerUseCase.invoke() && !hasAnyMedia
}

internal class ReleaseResourcesUseCase(
    private val removeScreenSharingNotificationUseCase: RemoveScreenSharingNotificationUseCase,
    private val callNotificationUseCase: CallNotificationUseCase,
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val gliaEngagementConfigRepository: GliaEngagementConfigRepository,
    private val updateFromCallScreenUseCase: UpdateFromCallScreenUseCase,
    private val dialogController: DialogController
) {
    operator fun invoke() {
        dialogController.dismissDialogs()
        fileAttachmentRepository.clearObservers()
        fileAttachmentRepository.detachAllFiles()
        removeScreenSharingNotificationUseCase()
        callNotificationUseCase.removeAllNotifications()
        gliaEngagementConfigRepository.reset()
        updateFromCallScreenUseCase.updateFromCallScreen(false)
        Dependencies.destroyControllers()
    }
}
