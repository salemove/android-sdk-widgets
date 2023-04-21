package com.glia.widgets.chat.helper;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.glia.androidsdk.chat.AttachmentFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.Maybe;

public class FileHelper {
    private static final String FILE_PROVIDER_AUTHORITY = "com.glia.widgets.fileprovider";
    private static final int DESIRED_IMAGE_SIZE = 640;

    public static String getFileProviderAuthority(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return String.join(".", context.getPackageName(), FILE_PROVIDER_AUTHORITY);
        } else {
            return String.format("%s.%s", context.getPackageName(), FILE_PROVIDER_AUTHORITY);
        }
    }

    public static boolean isFileDownloaded(AttachmentFile attachmentFile) {
        String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File file = new File(imagesDir, FileHelper.getFileName(attachmentFile));
        return file.exists();
    }

    public static String getFileName(AttachmentFile attachmentFile) {
        return getFileName(attachmentFile.getId(), attachmentFile.getName());
    }

    public static String getFileName(String fileId, String fileName) {
        return fileId + getFileExtension(fileName);
    }

    public static String getFileExtension(String fullName) {
        if (fullName == null) return "";
        String fileName = new File(fullName).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    public static void fixCapturedPhotoRotation(Context context, Uri uri) {
        int rotation = getRotationFromExif(context, uri);

        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, os);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Uri getContentUri(AttachmentFile attachment, Context context) {
        Uri contentUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Uri downloadsContentUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL);
            String[] projection = new String[]{
                    MediaStore.Downloads._ID,
                    MediaStore.Downloads.DISPLAY_NAME,
                    MediaStore.Downloads.SIZE
            };
            String selection = MediaStore.Downloads.DISPLAY_NAME + " == ?";
            String[] selectionArgs = new String[]{getFileName(attachment)};
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
                contentUri = ContentUris.withAppendedId(downloadsContentUri, id);
            }
        } else {
            File file = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(),
                    getFileName(attachment)
            );
            contentUri = FileProvider.getUriForFile(context, getFileProviderAuthority(context), file);
        }

        return contentUri;
    }

    private static int getRotationFromExif(Context context, Uri uri) {
        int rotation = 0;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            int orientation = new ExifInterface(in).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rotation;
    }

    public Maybe<Bitmap> decodeSampledBitmapFromInputStream(InputStream inputStream) {
        return Maybe.create(emitter -> {
                    Bitmap rawBitmap = BitmapFactory.decodeStream(inputStream);
                    if (rawBitmap == null) {
                        emitter.onError(
                                new IOException("InputStream could not be decoded")
                        );
                        return;
                    }

                    int rawHeight = rawBitmap.getHeight();
                    int rawWidth = rawBitmap.getWidth();
                    double ratio = ((double) rawWidth) / ((double) rawHeight);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                            rawBitmap,
                            (int) (DESIRED_IMAGE_SIZE * ratio),
                            DESIRED_IMAGE_SIZE,
                            false
                    );
                    if (scaledBitmap != null) {
                        emitter.onSuccess(scaledBitmap);
                    } else {
                        emitter.onError(new Exception());
                    }

                    if (!rawBitmap.equals(scaledBitmap)) {
                        rawBitmap.recycle();
                    }
                }
        );
    }
}
