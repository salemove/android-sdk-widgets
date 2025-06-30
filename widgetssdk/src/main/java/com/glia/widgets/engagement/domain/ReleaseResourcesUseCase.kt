package com.glia.widgets.engagement.domain

import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.internal.dialog.DialogContract
import com.glia.widgets.internal.fileupload.FileAttachmentRepository
import com.glia.widgets.internal.notification.domain.CallNotificationUseCase
import com.glia.widgets.di.Dependencies

internal interface ReleaseResourcesUseCase {
    operator fun invoke()
}

internal class ReleaseResourcesUseCaseImpl(
    private val callNotificationUseCase: CallNotificationUseCase,
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val updateFromCallScreenUseCase: UpdateFromCallScreenUseCase,
    private val dialogController: DialogContract.Controller
) : ReleaseResourcesUseCase {
    override fun invoke() {
        dialogController.dismissDialogs()
        fileAttachmentRepository.detachAllFiles()
        callNotificationUseCase.removeAllNotifications()
        updateFromCallScreenUseCase(false)
        Dependencies.destroyControllers()
    }
}
