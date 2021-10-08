package com.glia.widgets.chat.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.exifinterface.media.ExifInterface;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.helper.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
                        emitter.onSuccess(scaledBitmap);
                    else {
                        emitter.onError(new Exception());
                    }
                }
        );
    }

    public static Bitmap getRotatedBitmapFromUri(Uri cameraImgUri, Context context) {
        Bitmap capturedImage = null;

        try {
            capturedImage = FileHelper.handleSamplingAndRotationBitmap(context, cameraImgUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return capturedImage;
    }

    /*
     Refer to the related question on Stackoverflow if necessary
     https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a
    */
    private static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage) throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            inSampleSize = Math.min(heightRatio, widthRatio);

            final float totalPixels = width * height;
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        ei = new ExifInterface(input);

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public static Uri getUriFromBitmap(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, Utils.generatePhotoFileName(), null);
        return Uri.parse(path);
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
