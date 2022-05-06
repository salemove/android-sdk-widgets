package com.glia.widgets.view.floatingvisitorvideoview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.comms.VideoView;
import com.glia.widgets.R;
import com.google.android.material.card.MaterialCardView;

public class FloatingVisitorVideoView extends MaterialCardView {
    private VideoView videoView;
    private TextView onHoldOverlay;

    public FloatingVisitorVideoView(@NonNull Context context) {
        this(context, null);
    }

    public FloatingVisitorVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingVisitorVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releaseVideoStream();
    }

    public void onResume() {
        resumeVideoStream();
    }

    public void onPause() {
        pauseVideoStream();
    }

    public void showVisitorVideo(VideoView newVideoView) {
        videoView = newVideoView;
        videoView.setZOrderMediaOverlay(true);
        addView(videoView, 0);
    }

    public void hideVisitorVideo() {
        removeView(videoView);
        releaseVideoStream();
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
        onHoldOverlay = findViewById(R.id.on_hold_textview);
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
