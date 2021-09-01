package com.glia.widgets.chat;

import static com.glia.widgets.chat.helper.FileHelper.loadImageFromDownloadsFolder;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.glia.widgets.R;
import com.glia.widgets.chat.helper.FileHelper;
import com.glia.widgets.chat.helper.InAppBitmapCache;
import com.glia.widgets.helper.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class FilePreviewActivity extends AppCompatActivity implements FileHelper.PicassoCallback {

    private static final String TAG = FilePreviewActivity.class.getSimpleName();

    private static final String IMAGE_ID_KEY = "image_id";
    private static final String IMAGE_ID_NAME = "image_name";

    private ImageView previewImageView;
    private MenuItem saveItem;
    private MenuItem shareItem;

    private Handler mainHandler = null;
    private Runnable runnable = null;

    @Override
    protected void onStart() {
        super.onStart();

        if (mainHandler == null) {
            mainHandler = new Handler(Looper.getMainLooper());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_preview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.preview_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        previewImageView = findViewById(R.id.preview_iv);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mainHandler != null) {
            mainHandler.removeCallbacks(runnable);
            runnable = null;
            mainHandler = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_file_preview, menu);
        saveItem = menu.findItem(R.id.save_item);
        shareItem = menu.findItem(R.id.share_item);
        setPreviewImage();

        return true;
    }

    private void setPreviewImage() {
        String bitmapName = getInAppCacheBitmapIdFromIntent() + "." + getInAppCacheBitmapNameFromIntent();
        loadImageFromDownloadsFolder(getApplicationContext(), bitmapName, previewImageView, this);
    }

    private Bitmap getBitmapFromInAppCache() {
        String bitmapId = getInAppCacheBitmapIdFromIntent() + "." + getInAppCacheBitmapNameFromIntent();
        return InAppBitmapCache.getInstance().getBitmapById(bitmapId);
    }

    private String getInAppCacheBitmapIdFromIntent() {
        Intent incoming = getIntent();
        return incoming.getStringExtra(IMAGE_ID_KEY);
    }

    private String getInAppCacheBitmapNameFromIntent() {
        Intent incoming = getIntent();
        return incoming.getStringExtra(IMAGE_ID_NAME);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.share_item) {
            shareImage();
            return true;
        } else if (itemId == R.id.save_item) {
            saveFileToDownloadsFolder();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareImage() {
        String imageName = getInAppCacheBitmapIdFromIntent() + "." + getInAppCacheBitmapNameFromIntent();

        File file = new File(getApplicationContext().getFilesDir(), imageName);
        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), FileHelper.getFileProviderAuthority(getApplicationContext()), file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.glia_preview_activity_image_share_title)));
    }

    private void saveFileToDownloadsFolder() {
        Bitmap bitmap = getBitmapFromInAppCache();
        String imageName = getInAppCacheBitmapIdFromIntent() + "." + getInAppCacheBitmapNameFromIntent();

        new Thread(() -> {
            FileOutputStream fos = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentResolver contentResolver = getApplicationContext().getContentResolver();
                    if (contentResolver != null) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.MediaColumns.DISPLAY_NAME, imageName);
                        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                        Uri imageUri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                        fos = (FileOutputStream) contentResolver.openOutputStream(imageUri);
                    }
                } else {
                    File downloadsDir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    File image = new File(downloadsDir, imageName);
                    fos = new FileOutputStream(image);
                }

                if (fos != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                }
                onImageSaveSuccess();
            } catch (FileNotFoundException ex) {
                Logger.e(TAG, "Image saving to downloads folder failed: " + ex.getMessage());
                onImageSaveFail();
            } finally {
                if (fos != null) {
                    try {
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        Logger.e(TAG, "Closing FileOutputStream failed: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    private void onImageSaveSuccess() {
        runnable = createRunnable(getString(R.string.glia_preview_activity_image_save_success_msg), false, true);
        mainHandler.post(runnable);
    }

    private void onImageSaveFail() {
        runnable = createRunnable(getString(R.string.glia_preview_activity_image_save_fail_msg), true, false);
        mainHandler.post(runnable);
    }

    private Runnable createRunnable(String message, boolean isSaveItemVisible, boolean isShareItemVisible) {
        return () -> {
            saveItem.setVisible(isSaveItemVisible);
            shareItem.setVisible(isShareItemVisible);
            Toast.makeText(FilePreviewActivity.this, message, Toast.LENGTH_LONG).show();
        };
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    protected static Intent intent(Context context, String itemId, String itemName) {
        Intent intent = new Intent(context, FilePreviewActivity.class);
        intent.putExtra(IMAGE_ID_KEY, itemId);
        intent.putExtra(IMAGE_ID_NAME, itemName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return intent;
    }

    @Override
    public void onImageLoadSuccess() {
        Log.d(TAG, "Image is successfully loaded.");
        saveItem.setVisible(false);
        shareItem.setVisible(true);
    }

    @Override
    public void onImageLoadFail() {
        Bitmap bitmap = getBitmapFromInAppCache();

        if (bitmap != null) {
            previewImageView.setImageBitmap(bitmap);
            saveItem.setVisible(true);
            shareItem.setVisible(false);
        } else {
            Toast.makeText(this, getString(R.string.glia_preview_activity_preview_failed_msg), Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
