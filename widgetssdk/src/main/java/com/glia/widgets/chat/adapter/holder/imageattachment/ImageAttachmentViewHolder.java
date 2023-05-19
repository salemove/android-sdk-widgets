package com.glia.widgets.chat.adapter.holder.imageattachment;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.R;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase;
import com.glia.widgets.helper.FileHelper;
import com.glia.widgets.helper.Logger;
import com.google.android.material.imageview.ShapeableImageView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ImageAttachmentViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = ImageAttachmentViewHolder.class.getSimpleName();

    private final ShapeableImageView imageView;

    private final GetImageFileFromCacheUseCase getImageFileFromCacheUseCase;
    private final GetImageFileFromDownloadsUseCase getImageFileFromDownloadsUseCase;
    private final GetImageFileFromNetworkUseCase getImageFileFromNetworkUseCase;

    private Disposable disposable = null;

    public ImageAttachmentViewHolder(
            @NonNull View itemView,
            GetImageFileFromCacheUseCase getImageFileFromCacheUseCase,
            GetImageFileFromDownloadsUseCase getImageFileFromDownloadsUseCase,
            GetImageFileFromNetworkUseCase getImageFileFromNetworkUseCase
    ) {
        super(itemView);
        this.imageView = itemView.findViewById(R.id.incoming_image_attachment);
        this.getImageFileFromCacheUseCase = getImageFileFromCacheUseCase;
        this.getImageFileFromDownloadsUseCase = getImageFileFromDownloadsUseCase;
        this.getImageFileFromNetworkUseCase = getImageFileFromNetworkUseCase;
    }

    public void bind(AttachmentFile attachmentFile) {
        imageView.setImageResource(android.R.color.transparent); // clear the previous view state

        String imageName = FileHelper.getFileName(attachmentFile);
        disposable = getImageFileFromCacheUseCase.invoke(imageName)
                .doOnError(error -> Logger.e(TAG, "failed loading from cache: " + imageName + " reason: " + error.getMessage()))
                .doOnSuccess(_b -> Logger.d(TAG, "loaded from cache: " + imageName))
                .onErrorResumeNext(getImageFileFromDownloadsUseCase.invoke(imageName))
                .doOnError(error -> Logger.e(TAG, imageName + " failed loading from downloads: " + error.getMessage()))
                .doOnSuccess(_b -> Logger.d(TAG, "loaded from downloads: " + imageName))
                .onErrorResumeNext(getImageFileFromNetworkUseCase.invoke(attachmentFile))
                .doOnError(error -> Logger.e(TAG, imageName + " failed loading from network: " + error.getMessage()))
                .doOnSuccess(_b -> Logger.d(TAG, "loaded from network: " + imageName))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(imageView::setImageBitmap, error -> {
                            Logger.e(TAG, error.getMessage());
                            imageView.setBackgroundColor(Color.BLACK);
                        }
                );

        setAccessibilityActions();
    }

    private void setAccessibilityActions() {
        ViewCompat.setAccessibilityDelegate(itemView, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(@NonNull View host, @NonNull AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);

                String actionLabel = host.getResources().getString(R.string.glia_chat_attachment_open_button_label);

                AccessibilityNodeInfoCompat.AccessibilityActionCompat actionClick
                        = new AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                        AccessibilityNodeInfoCompat.ACTION_CLICK, actionLabel);
                info.addAction(actionClick);
            }
        });
    }

    public void onStopView() {
        if (disposable != null) disposable.dispose();
    }
}
