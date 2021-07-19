package com.glia.widgets.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.chat.adapter.InAppFileCache;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.InputStream;

public class PicassoUtils {

    private static final String TAG = PicassoUtils.class.getSimpleName();

    public static final String PNG_EXTENSION = ".png";

    public static void loadImageFromDownloadsFolder(Context context, AttachmentFile attachmentFile, ImageView imageView) {
        File imageFile = new File(context.getFilesDir(), attachmentFile.getName());
        Picasso.get()
                .load(imageFile)
                .resize(0, 720)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Logger.d(TAG, "Image loaded from the downloads folder.");
                    }

                    @Override
                    public void onError(Exception e) {
                        Logger.e(TAG, "Image load from the downloads folder failed, trying network load: " + e.getMessage());
                        LoadImageFromNetwork(attachmentFile, imageView);
                    }
                });
    }

    public static void loadImageFromDownloadsFolder(Context context, String imageName, ImageView imageView, PicassoCallback picassoCallback) {
        File imageFile = new File(context.getFilesDir(), imageName);
        Picasso.get()
                .load(imageFile)
                .resize(0, 720)
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

    private static void LoadImageFromNetwork(AttachmentFile attachmentFile, ImageView imageView) {
        Context context = imageView.getContext();

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

            getCustomPicasso(fileInputStream, context)
                    .load(attachmentFile.getName())
                    .resize(0, 720)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            Logger.d(TAG, "Image loaded from the network.");
                            imageView.setImageBitmap(bitmap);
                            InAppFileCache.getInstance().putBitmap(attachmentFile.getId() + "." + attachmentFile.getName(), bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            Logger.e(TAG, "Image load from the network failed: " + e.getMessage());
                            e.printStackTrace();
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
        });
    }

    public static Picasso getCustomPicasso(InputStream fileInputStream, Context context) {
        return new Picasso.Builder(context)
                .listener((picasso, uri, exception) -> exception.printStackTrace())
                .addRequestHandler(new RequestHandler() {
                    @Override
                    public boolean canHandleRequest(Request data) {
                        return true;
                    }

                    @Override
                    public Result load(Request request, int networkPolicy) {
                        return new Result(BitmapFactory.decodeStream(fileInputStream), Picasso.LoadedFrom.NETWORK);
                    }
                }).build();
    }

    public interface PicassoCallback {
        void onImageLoadSuccess();

        void onImageLoadFail();
    }
}
