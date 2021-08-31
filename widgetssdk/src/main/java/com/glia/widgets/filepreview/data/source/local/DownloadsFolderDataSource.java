package com.glia.widgets.filepreview.data.source.local;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.glia.widgets.helper.Logger;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import io.reactivex.Completable;
import io.reactivex.Maybe;

public class DownloadsFolderDataSource {
    private final static String TAG = DownloadsFolderDataSource.class.getSimpleName();
    private final Context context;

    public DownloadsFolderDataSource(Context appContext) {
        this.context = appContext;
    }

    public Maybe<Bitmap> getImageFromDownloads(String imageName) {
        return Maybe.create(
                emitter -> {
                    File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), imageName);
                    Picasso.get()
                            .load(imageFile)
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    emitter.onSuccess(bitmap);
                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                    emitter.onError(e);
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });
                }
        );
    }

    public Completable putImageToDownloads(String fileName, Bitmap bitmap) {
        return Completable.create(emitter -> {
            OutputStream fos = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentResolver resolver = context.getContentResolver();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                    Uri imageUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                    fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
                } else {
                    String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                    File image = new File(imagesDir, fileName);
                    fos = new FileOutputStream(image);
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            } catch (FileNotFoundException ex) {
                Logger.e(TAG, "Image saving to downloads folder failed: " + ex.getMessage());
                emitter.onError(ex);
            } finally {
                if (fos != null) {
                    try {
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        Logger.e(TAG, e.getMessage());
                    }
                }
            }
            emitter.onComplete();
        });
    }
}
