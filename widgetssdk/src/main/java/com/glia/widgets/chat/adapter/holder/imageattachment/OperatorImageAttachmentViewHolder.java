package com.glia.widgets.chat.adapter.holder.imageattachment;

import android.view.View;

import androidx.annotation.NonNull;

import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase;

public class OperatorImageAttachmentViewHolder extends ImageAttachmentViewHolder {
    public OperatorImageAttachmentViewHolder(
            @NonNull View itemView,
            GetImageFileFromCacheUseCase getImageFileFromCacheUseCase,
            GetImageFileFromDownloadsUseCase getImageFileFromDownloadsUseCase,
            GetImageFileFromNetworkUseCase getImageFileFromNetworkUseCase
    ) {
        super(itemView, getImageFileFromCacheUseCase, getImageFileFromDownloadsUseCase, getImageFileFromNetworkUseCase);
    }
}
