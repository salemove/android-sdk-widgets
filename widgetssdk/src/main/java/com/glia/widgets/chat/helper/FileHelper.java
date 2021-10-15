package com.glia.widgets.chat.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Environment;

import com.glia.androidsdk.chat.AttachmentFile;

import java.io.File;
import java.io.InputStream;

import io.reactivex.Maybe;

public class FileHelper {
    private static final String TAG = FileHelper.class.getSimpleName();
    private static final String FILE_PROVIDER_AUTHORITY = "com.glia.widgets.fileprovider";
    private static final int DESIRED_IMAGE_SIZE = 640;

    public static Maybe<Bitmap> decodeSampledBitmapFromInputStream(InputStream inputStream) {
        return Maybe.create(emitter -> {
                    Bitmap rawBitmap = BitmapFactory.decodeStream(inputStream);
                    int rawHeight = rawBitmap.getHeight();
                    int rawWidth = rawBitmap.getWidth();

                    double ratio = ((double) rawWidth) / ((double) rawHeight);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(rawBitmap, (int) (DESIRED_IMAGE_SIZE * ratio), DESIRED_IMAGE_SIZE, false);
                    if (scaledBitmap != null)
                        emitter.onSuccess(rotateImage(scaledBitmap, 90));
                    else {
                        emitter.onError(new Exception());
                    }
                }
        );
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

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
}
