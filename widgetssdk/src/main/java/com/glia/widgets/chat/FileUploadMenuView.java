package com.glia.widgets.chat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.glia.widgets.R;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

public class FileUploadMenuView extends ConstraintLayout {
    private Callback callback;

    private View photoLibraryItem;
    private View capturePhotoItem;
    private View browseItem;

    private Animation openAnimation;
    private Animation closeAnimation;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void show() {
        if (this.getVisibility() != VISIBLE) {
            this.setVisibility(VISIBLE);
            this.startAnimation(openAnimation);
        }
    }

    public void hide() {
        if (this.getVisibility() != GONE) {
            closeAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.startAnimation(closeAnimation);
        }
    }

    public FileUploadMenuView(@NonNull Context context) {
        this(context, null);
    }

    public FileUploadMenuView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.gliaChatStyle);
    }

    public FileUploadMenuView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat);
    }

    public FileUploadMenuView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(
                MaterialThemeOverlay.wrap(
                        context,
                        attrs,
                        defStyleAttr,
                        defStyleRes
                ),
                attrs,
                defStyleAttr,
                defStyleRes
        );

        View view = inflate(context, R.layout.chat_attachment_select_item_menu, this);

        findItems(view);
        setupListeners();
        setupAnimations();
    }

    private void findItems(View view) {
        photoLibraryItem = view.findViewById(R.id.photo_library_item);
        capturePhotoItem = view.findViewById(R.id.photo_or_video_item);
        browseItem = view.findViewById(R.id.browse_item);
    }

    private void setupListeners() {
        photoLibraryItem.setOnClickListener(view -> {
            if (callback != null) callback.onGalleryClicked();
        });
        capturePhotoItem.setOnClickListener(view -> {
            if (callback != null) callback.onTakePhotoClicked();
        });
        browseItem.setOnClickListener(view -> {
            if (callback != null) callback.onBrowseClicked();
        });
    }

    private void setupAnimations() {
        openAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.opening_fab_menu);
        closeAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.closing_fab_menu);
    }

    public interface Callback {
        void onGalleryClicked();

        void onTakePhotoClicked();

        void onBrowseClicked();
    }
}
