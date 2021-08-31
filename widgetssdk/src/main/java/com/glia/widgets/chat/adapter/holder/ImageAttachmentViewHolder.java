package com.glia.widgets.chat.adapter.holder;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.R;
import com.glia.widgets.chat.helper.FileHelper;
import com.glia.widgets.filepreview.data.source.local.InAppBitmapCache;
import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.helper.Logger;
import com.google.android.material.imageview.ShapeableImageView;

public class ImageAttachmentViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = ChatAdapter.class.getSimpleName();

    private Handler mainHandler;
    private Runnable runnable = null;

    private final ShapeableImageView imageView;

    public ImageAttachmentViewHolder(@NonNull View itemView) {
        super(itemView);

        mainHandler = new Handler(Looper.getMainLooper());
        imageView = itemView.findViewById(R.id.incoming_image_attachment);
    }

    public void bind(AttachmentFile attachmentFile) {
        Bitmap cachedBitmap = InAppBitmapCache.getInstance().getBitmapById(attachmentFile.getId() + "." + attachmentFile.getName());

        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap);
            Logger.d(TAG, "Image loaded from the in app file cache.");
        } else {
            Logger.d(TAG, "Image load from in app file cache failed, trying downloads folder.");
            FileHelper.loadImageFromDownloadsFolder(attachmentFile, imageView, new FileHelper.BitmapCallback() {
                @Override
                public void onBitmapSuccess(Bitmap bitmap) {
                    onFileSaveSuccess(bitmap, imageView);
                }

                @Override
                public void onBitmapFail() {

                }
            });
        }
    }

    private void onFileSaveSuccess(Bitmap bitmap, ImageView imageView) {
        if (mainHandler != null) {
            runnable = () -> imageView.setImageBitmap(bitmap);
            mainHandler.post(runnable);
        }
    }

    public void onStopView() {
        if (mainHandler != null) {
            mainHandler.removeCallbacks(runnable);
            runnable = null;
            mainHandler = null;
        }
    }
}
