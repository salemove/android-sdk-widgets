package com.glia.widgets.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.glia.widgets.filepreview.ui.ImagePreviewViewModel
import com.glia.widgets.messagecenter.MessageCenterConfirmationViewModel
import com.glia.widgets.messagecenter.MessageCenterWelcomeViewModel
import com.glia.widgets.survey.SurveyViewModel
import com.glia.widgets.webbrowser.WebBrowserViewModel

/**
 * Factory for creating ViewModels with dependencies from the SDK's DI system.
 */
internal class ViewModelFactory(
    private val useCaseFactory: UseCaseFactory,
    private val repositoryFactory: RepositoryFactory
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            SurveyViewModel::class.java -> SurveyViewModel(
                useCaseFactory.surveyAnswerUseCase
            ) as T
            WebBrowserViewModel::class.java -> WebBrowserViewModel() as T
            ImagePreviewViewModel::class.java -> ImagePreviewViewModel(
                useCaseFactory.createGetImageFileFromDownloadsUseCase(),
                useCaseFactory.createGetImageFileFromCacheUseCase(),
                useCaseFactory.createPutImageFileToDownloadsUseCase()
            ) as T
            MessageCenterWelcomeViewModel::class.java -> MessageCenterWelcomeViewModel(
                useCaseFactory.createSendSecureMessageUseCase(),
                useCaseFactory.createAddFileAttachmentsObserverUseCase(),
                useCaseFactory.createAddSecureFileToAttachmentAndUploadUseCase(),
                useCaseFactory.createGetFileAttachmentsUseCase(),
                useCaseFactory.createRemoveFileAttachmentUseCase(),
                useCaseFactory.createIsAuthenticatedUseCase(),
                useCaseFactory.createSupportedUploadFileTypesUseCase(),
                useCaseFactory.createOnNextMessageUseCase(),
                useCaseFactory.createEnableSendMessageButtonUseCase(),
                useCaseFactory.createShowMessageLimitErrorUseCase(),
                useCaseFactory.createResetMessageCenterUseCase(),
                useCaseFactory.takePictureUseCase,
                useCaseFactory.uriToFileAttachmentUseCase,
                useCaseFactory.requestNotificationPermissionIfPushNotificationsSetUpUseCase,
                useCaseFactory.createIsMessagingAvailableUseCase(),
                useCaseFactory.isQueueingOrEngagementUseCase
            ) as T
            MessageCenterConfirmationViewModel::class.java -> MessageCenterConfirmationViewModel(
                useCaseFactory.createResetMessageCenterUseCase(),
                useCaseFactory.isQueueingOrEngagementUseCase
            ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}