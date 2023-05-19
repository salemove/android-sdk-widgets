package com.glia.widgets.chat;

import android.view.View;

import androidx.annotation.NonNull;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.chat.model.ChatState;
import com.glia.widgets.chat.model.history.ChatItem;
import com.glia.widgets.core.fileupload.model.FileAttachment;

import java.util.List;

public interface ChatViewCallback {

    void emitUploadAttachments(List<FileAttachment> attachments);

    void emitState(ChatState chatState);

    void emitItems(List<ChatItem> items);

    void navigateToCall(String requestedMediaType);

    void backToCall();

    void navigateToSurvey(@NonNull Survey survey);

    void destroyView();

    void minimizeView();

    void smoothScrollToBottom();

    void scrollToBottomImmediate();

    void fileDownloadError(AttachmentFile attachmentFile, Throwable error);

    void fileDownloadSuccess(AttachmentFile attachmentFile);

    void clearMessageInput();

    void navigateToPreview(AttachmentFile attachmentFile, View view);

    void fileIsNotReadyForPreview();
}
