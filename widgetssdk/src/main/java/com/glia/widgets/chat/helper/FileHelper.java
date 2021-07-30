package com.glia.widgets.chat.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.helper.Logger;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.InputStream;

public class FileHelper {

    private static final String TAG = FileHelper.class.getSimpleName();

    private static final int DESIRED_IMAGE_SIZE = 640;

    public static void loadImageFromDownloadsFolder(Context context, AttachmentFile attachmentFile, ImageView imageView, BitmapCallback bitmapCallback) {
        File imageFile = new File(context.getFilesDir(), attachmentFile.getName());
        Picasso.get()
                .load(imageFile)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Logger.d(TAG, "Image loaded from the downloads folder.");
                    }

                    @Override
                    public void onError(Exception e) {
                        Logger.e(TAG, "Image load from the downloads folder failed, trying network load: " + e.getMessage());
                        LoadImageFromNetwork(attachmentFile, bitmapCallback);
                    }
                });
    }

    public static void loadImageFromDownloadsFolder(Context context, String imageName, ImageView imageView, PicassoCallback picassoCallback) {
        File imageFile = new File(context.getFilesDir(), imageName);
        Picasso.get()
                .load(imageFile)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        picassoCallback.onImageLoadSuccess();
                    }

                    @Override
                    public void onError(Exception e) {
                        picassoCallback.onImageLoadFail();
                    }
                });
    }

    private static void LoadImageFromNetwork(AttachmentFile attachmentFile, BitmapCallback bitmapCallback) {
        if (attachmentFile.isDeleted()) {
            Logger.d(TAG, "Image loaded from the network failed image is deleted");
            return;
        }

        Glia.fetchFile(attachmentFile, (fileInputStream, gliaException) -> {
            if (gliaException != null) {
                Logger.e(TAG, "Image loaded from the network failed." + gliaException.getMessage());
                gliaException.printStackTrace();
                return;
            }

            new Thread(() -> {
                try {
                    Bitmap bitmap = decodeSampledBitmapFromInputStream(fileInputStream);
                    InAppBitmapCache.getInstance().putBitmap(attachmentFile.getId() + "." + attachmentFile.getName(), bitmap);
                    bitmapCallback.onBitmapSuccess(bitmap);
                } catch (Exception e) {
                    bitmapCallback.onBitmapFail();
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private static Bitmap decodeSampledBitmapFromInputStream(InputStream inputStream) {
        Bitmap rawBitmap = BitmapFactory.decodeStream(inputStream);
        int rawHeight = rawBitmap.getHeight();
        int rawWidth = rawBitmap.getWidth();

        double ratio = ((double) rawWidth) / ((double) rawHeight);

        return Bitmap.createScaledBitmap(rawBitmap, (int) (DESIRED_IMAGE_SIZE * ratio), DESIRED_IMAGE_SIZE, false);
    }

    public interface PicassoCallback {
        void onImageLoadSuccess();

        void onImageLoadFail();
    }

    public interface BitmapCallback {
        void onBitmapSuccess(Bitmap bitmap);

        void onBitmapFail();
    }
}
