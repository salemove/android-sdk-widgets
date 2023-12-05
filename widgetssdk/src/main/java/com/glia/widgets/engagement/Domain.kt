package com.glia.widgets.engagement

import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.core.notification.domain.CallNotificationUseCase
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase
import com.glia.widgets.core.operator.GliaOperatorMediaRepository
import com.glia.widgets.core.visitor.GliaVisitorMediaRepository
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.OneTimeEvent
import io.reactivex.Flowable

internal class EndEngagementUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(silently: Boolean = false) = engagementRepository.endEngagement(silently)
}

internal class HasOngoingEngagementUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Boolean = engagementRepository.hasOngoingEngagement
}

internal class IsCurrentEngagementCallVisualizer(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Boolean = engagementRepository.isCallVisualizerEngagement
}

internal class SurveyUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Flowable<OneTimeEvent<EngagementRepository.SurveyState>> = engagementRepository.survey.map(::OneTimeEvent)
}

internal class EngagementStateUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Flowable<EngagementRepository.State> = engagementRepository.engagementState
}

internal class ReleaseResourcesUseCase(
    private val removeScreenSharingNotificationUseCase: RemoveScreenSharingNotificationUseCase,
    private val callNotificationUseCase: CallNotificationUseCase,
    private val operatorMediaRepository: GliaOperatorMediaRepository,
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val gliaVisitorMediaRepository: GliaVisitorMediaRepository,
    private val gliaEngagementConfigRepository: GliaEngagementConfigRepository,
    private val updateFromCallScreenUseCase: UpdateFromCallScreenUseCase,
) {
    operator fun invoke() {
        operatorMediaRepository.onEngagementEnded()
        fileAttachmentRepository.clearObservers()
        fileAttachmentRepository.detachAllFiles()
        removeScreenSharingNotificationUseCase()
        callNotificationUseCase.removeAllNotifications()
        gliaVisitorMediaRepository.onEngagementEnded()
        gliaEngagementConfigRepository.reset()
        updateFromCallScreenUseCase.updateFromCallScreen(false)
        Dependencies.destroyControllers()
    }
}
