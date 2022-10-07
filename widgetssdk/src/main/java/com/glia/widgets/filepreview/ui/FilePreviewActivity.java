package com.glia.widgets.filepreview.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.di.Dependencies;

public class FilePreviewActivity extends AppCompatActivity {

    private static final String TAG = FilePreviewActivity.class.getSimpleName();
    private static final String IMAGE_ID_KEY = "image_id";
    private static final String IMAGE_ID_NAME = "image_name";

    private final FilePreviewView.OnEndListener onEndListener = this::finish;

    private FilePreviewView filePreviewView;
    private UiTheme theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_preview_activity);

        filePreviewView = findViewById(R.id.file_preview_view);

        filePreviewView.setController(
                Dependencies
                        .getControllerFactory()
                        .getImagePreviewController()
        );

        Intent intent = getIntent();
        UiTheme runtimeTheme = intent.getParcelableExtra(GliaWidgets.UI_THEME);
        String bitmapId = getInAppCacheBitmapIdFromIntent(intent);
        String bitmapName = getInAppCacheBitmapNameFromIntent(intent);

        filePreviewView.init(
                bitmapId,
                bitmapName
        );

        this.theme = filePreviewView.setTheme(runtimeTheme);
        filePreviewView.setOnEndListener(onEndListener);
        handleStatusBarColor();

        setTitle(R.string.glia_preview_activity_toolbar_title);
    }

    private void handleStatusBarColor() {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, this.theme.getBrandPrimaryColor()));
    }

    @Override
    protected void onDestroy() {
        filePreviewView.onDestroyView();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        filePreviewView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public static Intent intent(Context context, String itemId, String itemName, UiTheme theme) {
        Intent intent = new Intent(context, FilePreviewActivity.class);
        intent.putExtra(IMAGE_ID_KEY, itemId);
        intent.putExtra(IMAGE_ID_NAME, itemName);
//        intent.putExtra(GliaWidgets.REMOTE_CONFIGURATION, ); // TODO
        intent.putExtra(GliaWidgets.UI_THEME, theme);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    private String getInAppCacheBitmapIdFromIntent(Intent incoming) {
        return incoming.getStringExtra(IMAGE_ID_KEY);
    }

    private String getInAppCacheBitmapNameFromIntent(Intent incoming) {
        return incoming.getStringExtra(IMAGE_ID_NAME);
    }
}
