package com.glia.widgets.filepreview.data.source.local;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.helper.FileHelper;
import com.glia.widgets.helper.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;

/**
 * @hide
 */
public class DownloadsFolderDataSource {
    private final static String TAG = DownloadsFolderDataSource.class.getSimpleName();
    private final Context context;

    public DownloadsFolderDataSource(Context appContext) {
        this.context = appContext;
    }

    public boolean isDownloaded(AttachmentFile attachmentFile) {
        return FileHelper.isDownloaded(attachmentFile, context);
    }

    public Maybe<Bitmap> getImageFromDownloadsFolder(@Nullable String imageName) {
        if (imageName == null) return Maybe.error(new NullPointerException("Image name cannot be null"));

        return Maybe.create(emitter -> {
            Uri uri = FileHelper.getContentUriCompat(
                imageName,
                context
            );
            Bitmap bitmap = BitmapFactory.decodeStream(
                context.getContentResolver().openInputStream(uri)
            );
            if (bitmap == null) {
                emitter.onError(new FileNotFoundException());
            } else {
                emitter.onSuccess(bitmap);
            }
        });
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private Completable putImageToDownloadsAPI29(String imageName, Bitmap bitmap) {
        return Completable.create(emitter -> {
            OutputStream fos = null;
            try {
                ContentResolver resolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri imageUri = resolver.insert(MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues);
                fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
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

    private Completable putImageToDownloadsOld(String imageName, Bitmap bitmap) {
        return Completable.create(emitter -> {
            OutputStream fos = null;
            try {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                File image = new File(imagesDir, imageName);
                fos = new FileOutputStream(image);
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

    public Completable putImageToDownloads(String fileName, Bitmap bitmap) {
        if (fileName == null) return Completable.error(new NullPointerException("File name cannot be null"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return putImageToDownloadsAPI29(fileName, bitmap);
        } else {
            return putImageToDownloadsOld(fileName, bitmap);
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private Completable downloadFileToDownloadsAPI29(String fileName, String contentType, InputStream inputStream) {
        return Completable.create(emitter -> {
            OutputStream fos = null;
            try {
                ContentResolver resolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, contentType);
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri fileUri = resolver.insert(MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues);
                fos = resolver.openOutputStream(Objects.requireNonNull(fileUri));

                byte[] buffer = new byte[10 * 1024]; // or other buffer size
                int read;

                while ((read = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }

                fos.flush();
                emitter.onComplete();
            } catch (FileNotFoundException ex) {
                Logger.e(TAG, "File saving to downloads folder failed: " + ex.getMessage());
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

    private Completable downloadFileToDownloadsOld(String fileName, InputStream inputStream) {
        return Completable.create(emitter -> {
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            File file = new File(imagesDir, fileName);
            try (OutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[10 * 1024]; // or other buffer size
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                emitter.onComplete();
            } catch (FileNotFoundException ex) {
                Logger.e(TAG, "File saving to downloads folder failed: " + ex.getMessage());
                emitter.onError(ex);
            }
            emitter.onComplete();
        });
    }

    public Completable downloadFileToDownloads(String fileName, String contentType, InputStream inputStream) {
        if (fileName == null) return Completable.error(new NullPointerException("File name cannot be null"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return downloadFileToDownloadsAPI29(fileName, contentType, inputStream);
        } else {
            return downloadFileToDownloadsOld(fileName, inputStream);
        }
    }
}
