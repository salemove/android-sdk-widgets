package com.glia.widgets.engagement.domain

import android.annotation.SuppressLint
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.di.Dependencies
import com.glia.widgets.internal.dialog.DialogContract
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.internal.fileupload.FileAttachmentRepository
import com.glia.widgets.internal.fileupload.domain.GetShareableLocalAttachmentUriUseCase
import com.glia.widgets.internal.notification.domain.CallNotificationUseCase

internal interface ReleaseResourcesUseCase {
    operator fun invoke()
}

internal class ReleaseResourcesUseCaseImpl(
    private val callNotificationUseCase: CallNotificationUseCase,
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val updateFromCallScreenUseCase: UpdateFromCallScreenUseCase,
    private val dialogController: DialogContract.Controller,
    private val getShareableLocalAttachmentUriUseCase: GetShareableLocalAttachmentUriUseCase
) : ReleaseResourcesUseCase {
    @SuppressLint("CheckResult")
    override fun invoke() {
        dialogController.dismissDialogs()
        fileAttachmentRepository.detachAllFiles()
        getShareableLocalAttachmentUriUseCase.clearCache().subscribe({}, {
            Logger.w(TAG, "Failed to clear shared attachments cache: ${it.javaClass.simpleName}")
        })
        callNotificationUseCase.removeAllNotifications()
        updateFromCallScreenUseCase(false)
        Dependencies.destroyControllers()
    }
}
