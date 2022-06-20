package com.glia.widgets.filepreview.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.helper.FileHelper;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.header.AppBarView;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

import java.io.File;

public class FilePreviewView extends ConstraintLayout implements FilePreviewContract.View {
    private static final String TAG = FilePreviewView.class.getSimpleName();
    private static final int WRITE_PERMISSION_REQUEST = 110011;

    private AppBarView appBar;

    private ImageView previewImageView;

    private UiTheme theme;

    private FilePreviewController filePreviewController;

    private OnEndListener onEndListener;

    public FilePreviewView(Context context) {
        this(context, null);
    }

    public FilePreviewView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.gliaChatStyle);
    }

    public FilePreviewView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat);
    }

    public FilePreviewView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(
                MaterialThemeOverlay.wrap(
                        context,
                        attrs,
                        defStyleAttr,
                        defStyleRes),
                attrs,
                defStyleAttr,
                defStyleRes
        );
        initView();
        setDefaultThemeFromTypedArray(attrs, defStyleAttr, defStyleRes);
        initCallbacks();
        setupViewAppearance();
    }

    private void initCallbacks() {
        appBar.setOnBackClickedListener(() -> {
            Activity activity = Utils.getActivity(getContext());
            if (activity != null) activity.finish();
        });
    }

    private void setDefaultThemeFromTypedArray(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        @SuppressLint("CustomViewStyleable") TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes);
        this.theme = Utils.getThemeFromTypedArray(typedArray, this.getContext());
        typedArray.recycle();
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.file_preview_view, this);
        appBar = view.findViewById(R.id.app_bar_view);
        appBar.setTitle(getContext().getString(R.string.glia_preview_activity_toolbar_title));
        appBar.hideLeaveButtons();
        appBar.setMenuImagePreview();
        appBar.setImagePreviewButtonListener(new AppBarView.OnImagePreviewMenuListener() {
            @Override
            public void onShareClicked() {
                filePreviewController.onSharePressed();
            }

            @Override
            public void onDownloadClicked() {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    filePreviewController.onDownloadPressed();
                } else {
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        filePreviewController.onDownloadPressed();
                    } else {
                        Utils.getActivity(getContext()).requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
                    }
                }
            }
        });
        previewImageView = findViewById(R.id.preview_iv);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == WRITE_PERMISSION_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            filePreviewController.onDownloadPressed();
        }
    }

    public UiTheme setTheme(UiTheme uiTheme) {
        if (uiTheme != null) this.theme = Utils.getFullHybridTheme(uiTheme, this.theme);
        setupViewAppearance();
        return this.theme;
    }

    public void init(String bitmapId, String bitmapName) {
        filePreviewController.onImageDataReceived(bitmapId, bitmapName);
        filePreviewController.onImageRequested();
    }

    private void setupViewAppearance() {
        setBackgroundColor(ContextCompat.getColor(getContext(), theme.getBaseLightColor()));
        appBar.setTheme(this.theme, null);
    }

    @Override
    public void onStateUpdated(State state) {
        appBar.setImagePreviewButtonsVisible(state.getIsShowDownloadButton(), state.getIsShowShareButton());
        previewImageView.setImageBitmap(state.getLoadedImage());
    }

    @Override
    public void setController(FilePreviewController controller) {
        this.filePreviewController = controller;
        this.filePreviewController.setView(this);
    }

    @Override
    public void shareImageFile(String fileName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), fileName);
        Uri contentUri = FileProvider.getUriForFile(getContext(), FileHelper.getFileProviderAuthority(getContext()), file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.setType("image/jpeg");
        getContext().startActivity(shareIntent);
    }

    @Override
    public void showOnImageSaveSuccess() {
        Toast.makeText(getContext(), getContext().getString(R.string.glia_preview_activity_image_save_success_msg), Toast.LENGTH_LONG).show();
    }

    @Override
    public void showOnImageSaveFailed() {
        Toast.makeText(getContext(), getContext().getString(R.string.glia_preview_activity_image_save_fail_msg), Toast.LENGTH_LONG).show();
    }

    @Override
    public void showOnImageLoadingFailed() {
        Toast.makeText(getContext(), getContext().getString(R.string.glia_preview_activity_preview_failed_msg), Toast.LENGTH_LONG).show();
    }

    @Override
    public void engagementEnded() {
        if (onEndListener != null) this.onEndListener.onEnd();
    }

    public void onDestroyView() {
        filePreviewController.onDestroy();
        onEndListener = null;
    }

    public void setOnEndListener(OnEndListener onEndListener) {
        this.onEndListener = onEndListener;
    }

    public interface OnEndListener {
        void onEnd();
    }
}
