package com.glia.widgets.chat;

import static com.glia.widgets.chat.helper.FileHelper.loadImageFromDownloadsFolder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class FilePreviewActivity extends AppCompatActivity implements FileHelper.PicassoCallback {

    private static final String TAG = FilePreviewActivity.class.getSimpleName();

    private static final String IMAGE_ID_KEY = "image_id";

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
        String bitmapName = getInAppCacheBitmapIdFromIntent().split("\\.")[1];
        if (bitmapName == null || bitmapName.isEmpty()) {
            Toast.makeText(this, getString(R.string.preview_activity_preview_failed_msg), Toast.LENGTH_LONG).show();
            return;
        }

        loadImageFromDownloadsFolder(getApplicationContext(), bitmapName, previewImageView, this);
    }

    private Bitmap getBitmapFromInAppCache() {
        String bitmapId = getInAppCacheBitmapIdFromIntent();
        return InAppBitmapCache.getInstance().getBitmapById(bitmapId);
    }

    private String getInAppCacheBitmapIdFromIntent() {
        Intent incoming = getIntent();
        return incoming.getStringExtra(IMAGE_ID_KEY);
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
        String imageName = getInAppCacheBitmapIdFromIntent().split("\\.")[1];

        if (imageName == null || imageName.isEmpty()) {
            Toast.makeText(this, getString(R.string.preview_activity_image_share_fail_msg), Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(getApplicationContext().getFilesDir(), imageName);
        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.glia.widgets.fileprovider", file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.preview_activity_image_share_title)));
    }

    private void saveFileToDownloadsFolder() {
        Bitmap bitmap = getBitmapFromInAppCache();
        String imageName = getInAppCacheBitmapIdFromIntent().split("\\.")[1];

        if (bitmap == null || imageName == null || imageName.isEmpty()) {
            onImageSaveFail();
            return;
        }

        new Thread(() -> {
            final File imageFile = new File(getApplicationContext().getFilesDir(), imageName);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                onImageSaveSuccess();
            } catch (IOException e) {
                Logger.e(TAG, "Image saving to downloads folder failed: " + e.getMessage());
                e.printStackTrace();
                onImageSaveFail();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    Logger.e(TAG, "Closing FileOutputStream failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void onImageSaveSuccess() {
        runnable = createRunnable(getString(R.string.preview_activity_image_save_success_msg), false, true);
        mainHandler.post(runnable);
    }

    private void onImageSaveFail() {
        runnable = createRunnable(getString(R.string.preview_activity_image_save_fail_msg), true, false);
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

    protected static Intent intent(Context context, String itemId) {
        Intent intent = new Intent(context, FilePreviewActivity.class);
        intent.putExtra(IMAGE_ID_KEY, itemId);
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
            Toast.makeText(this, getString(R.string.preview_activity_preview_failed_msg), Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
