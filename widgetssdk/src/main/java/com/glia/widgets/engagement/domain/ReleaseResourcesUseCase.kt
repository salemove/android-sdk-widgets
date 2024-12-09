package com.glia.widgets.engagement.domain

import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.core.dialog.DialogContract
import com.glia.widgets.core.fileupload.EngagementFileAttachmentRepository
import com.glia.widgets.core.notification.domain.CallNotificationUseCase
import com.glia.widgets.di.Dependencies

internal interface ReleaseResourcesUseCase {
    operator fun invoke()
}

internal class ReleaseResourcesUseCaseImpl(
    private val releaseScreenSharingResourcesUseCase: ReleaseScreenSharingResourcesUseCase,
    private val callNotificationUseCase: CallNotificationUseCase,
    private val fileAttachmentRepository: EngagementFileAttachmentRepository,
    private val updateFromCallScreenUseCase: UpdateFromCallScreenUseCase,
    private val dialogController: DialogContract.Controller
) : ReleaseResourcesUseCase {
    override fun invoke() {
        dialogController.dismissDialogs()
        fileAttachmentRepository.clearObservers()
        fileAttachmentRepository.detachAllFiles()
        releaseScreenSharingResourcesUseCase()
        callNotificationUseCase.removeAllNotifications()
        updateFromCallScreenUseCase(false)
        Dependencies.destroyControllers()
    }
}
