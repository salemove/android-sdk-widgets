package com.glia.widgets.view.floatingvisitorvideoview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.glia.androidsdk.comms.VideoView;
import com.glia.widgets.R;

public class FloatingVisitorVideoView extends ConstraintLayout {
    private VideoView videoView;
    private FrameLayout videoContainer;
    private ConstraintLayout onHoldOverlay;

    public FloatingVisitorVideoView(@NonNull Context context) {
        this(context, null);
    }

    public FloatingVisitorVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingVisitorVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FloatingVisitorVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void onResume() {
        resumeVideoStream();
    }

    public void onPause() {
        pauseVideoStream();
    }

    public void onDestroy() {
        releaseVideoStream();
    }

    public void showVisitorVideo(VideoView newVideoView) {
        videoView = newVideoView;
        videoView.setZOrderMediaOverlay(true);
        videoContainer.addView(videoView);
        videoContainer.invalidate();
    }

    public void hideVisitorVideo() {
        releaseVideoStream();
        videoContainer.removeAllViews();
        videoContainer.invalidate();
    }

    public void showOnHold() {
        pauseVideoStream();
        onHoldOverlay.setVisibility(VISIBLE);
    }

    public void hideOnHold() {
        resumeVideoStream();
        onHoldOverlay.setVisibility(GONE);
    }

    public boolean hasVideo() {
        return videoView != null;
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.visitor_video_floating_view, this);
        videoContainer = findViewById(R.id.visitor_video_container);
        onHoldOverlay = findViewById(R.id.on_hold_overlay);
    }

    private void releaseVideoStream() {
        if (videoView != null) {
            videoView.release();
            videoView = null;
        }
    }

    private void pauseVideoStream() {
        if (videoView != null) {
            videoView.pauseRendering();
        }
    }

    private void resumeVideoStream() {
        if (videoView != null) {
            videoView.resumeRendering();
        }
    }
}
