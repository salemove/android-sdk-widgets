package com.glia.widgets.engagement.domain

import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.core.dialog.DialogContract
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.core.notification.domain.CallNotificationUseCase
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase
import com.glia.widgets.di.Dependencies

internal interface ReleaseResourcesUseCase {
    operator fun invoke()
}

internal class ReleaseResourcesUseCaseImpl(
    private val removeScreenSharingNotificationUseCase: RemoveScreenSharingNotificationUseCase,
    private val callNotificationUseCase: CallNotificationUseCase,
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val gliaEngagementConfigRepository: GliaEngagementConfigRepository,
    private val updateFromCallScreenUseCase: UpdateFromCallScreenUseCase,
    private val dialogController: DialogContract.Controller
) : ReleaseResourcesUseCase {
    override fun invoke() {
        dialogController.dismissDialogs()
        fileAttachmentRepository.clearObservers()
        fileAttachmentRepository.detachAllFiles()
        removeScreenSharingNotificationUseCase()
        callNotificationUseCase.removeAllNotifications()
        gliaEngagementConfigRepository.reset()
        updateFromCallScreenUseCase(false)
        Dependencies.destroyControllers()
    }
}
