package com.glia.widgets.filepreview.data.source.local;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.glia.widgets.chat.helper.FileHelper;
import com.glia.widgets.helper.Logger;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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

    @RequiresApi(Build.VERSION_CODES.Q)
    private Maybe<Bitmap> getImageFromDownloadsFolderAPI29(String imageName) {
        return Maybe.create(emitter -> {
            Uri downloadsContentUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL);
            String[] projection = new String[]{
                    MediaStore.Downloads._ID,
                    MediaStore.Downloads.DISPLAY_NAME,
                    MediaStore.Downloads.SIZE
            };
            String selection = MediaStore.Downloads.DISPLAY_NAME +
                    " == ?";
            String[] selectionArgs = new String[]{imageName};
            String sortOrder = MediaStore.Downloads.DISPLAY_NAME + " ASC";
            try (Cursor cursor = context.getContentResolver().query(
                    downloadsContentUri,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
            )) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID);
                cursor.moveToFirst();
                long id = cursor.getLong(idColumn);
                cursor.close();

                Uri contentUri = ContentUris.withAppendedId(downloadsContentUri, id);
                Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(contentUri));
                if (bitmap != null) emitter.onSuccess(bitmap);
                else emitter.onError(new FileNotFoundException());
            } catch (FileNotFoundException e) {
                emitter.onError(e);
            }
        });
    }

    private Maybe<Bitmap> getImageFromDownloadsFolderOld(String imageName) {
        return Maybe.create(emitter -> {
            File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), imageName);
            Uri uri = FileProvider.getUriForFile(context, FileHelper.getFileProviderAuthority(context), imageFile);
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
            if (bitmap == null) emitter.onError(new FileNotFoundException());
            else emitter.onSuccess(bitmap);
        });
    }

    public Maybe<Bitmap> getImageFromDownloadsFolder(String imageName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return getImageFromDownloadsFolderAPI29(imageName);
        } else {
            return getImageFromDownloadsFolderOld(imageName);
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return putImageToDownloadsAPI29(fileName, bitmap);
        } else {
            return putImageToDownloadsOld(fileName, bitmap);
        }
    }
}
