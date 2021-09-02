package com.glia.widgets.chat.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.io.InputStream;

public class FileHelper {

    private static final String TAG = FileHelper.class.getSimpleName();
    private static final String FILE_PROVIDER_AUTHORITY = "com.glia.widgets.fileprovider";
    private static final int DESIRED_IMAGE_SIZE = 640;

    public static Bitmap decodeSampledBitmapFromInputStream(InputStream inputStream) {
        Bitmap rawBitmap = BitmapFactory.decodeStream(inputStream);
        int rawHeight = rawBitmap.getHeight();
        int rawWidth = rawBitmap.getWidth();

        double ratio = ((double) rawWidth) / ((double) rawHeight);

        return Bitmap.createScaledBitmap(rawBitmap, (int) (DESIRED_IMAGE_SIZE * ratio), DESIRED_IMAGE_SIZE, false);
    }

    public static String getFileProviderAuthority(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return String.join(".", context.getPackageName(), FILE_PROVIDER_AUTHORITY);
        } else {
            return String.format("%s.%s", context.getPackageName(), FILE_PROVIDER_AUTHORITY);
        }
    }
}
