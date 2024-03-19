package com.glia.widgets.view.floatingvisitorvideoview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;

import com.glia.androidsdk.comms.MediaState;
import com.glia.widgets.R;
import com.glia.widgets.StringProvider;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.ContextExtensionsKt;
import com.glia.widgets.view.ViewHelpers;

/**
 * @hide
 */
public class FloatingVisitorVideoContainer extends ConstraintLayout
        implements FloatingVisitorVideoContract.View {
    private FloatingVisitorVideoContract.Controller controller;
    private FloatingVisitorVideoView floatingVisitorVideoView;

    public FloatingVisitorVideoContainer(@NonNull Context context) {
        this(context, null);
    }

    public FloatingVisitorVideoContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingVisitorVideoContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private StringProvider stringProvider = Dependencies.getStringProvider();

    @Override
    public void setController(FloatingVisitorVideoContract.Controller controller) {
        this.controller = controller;
    }

    @Override
    public void show(MediaState state) {
        if (!floatingVisitorVideoView.hasVideo() && state != null) {
            floatingVisitorVideoView.showVisitorVideo(
                    state.getVideo().createVideoView(ContextExtensionsKt.asActivity(getContext()))
            );
        }
        setVisibility(VISIBLE);
    }

    @Override
    public void hide() {
        floatingVisitorVideoView.hideVisitorVideo();
        setVisibility(GONE);
    }

    @Override
    public void onResume() {
        floatingVisitorVideoView.onResume();
    }

    @Override
    public void onPause() {
        floatingVisitorVideoView.onPause();
    }

    @Override
    public void showOnHold() {
        floatingVisitorVideoView.showOnHold();
    }

    @Override
    public void hideOnHold() {
        floatingVisitorVideoView.hideOnHold();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        controller.onDestroy();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.visitor_video_layout_view, this);
        floatingVisitorVideoView = findViewById(R.id.visitor_video_card);
        floatingVisitorVideoView.setContentDescription(stringProvider.getRemoteString(R.string.call_visitor_video_accessibility_label));
        setController(Dependencies.getControllerFactory().getFloatingVisitorVideoController());
        controller.setView(this);
        setVisitorVideoContainerTouchListener();
        hide();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setVisitorVideoContainerTouchListener() {
        floatingVisitorVideoView.setOnTouchListener(
                new ViewHelpers.OnTouchListener(
                        this::getViewLocation,
                        this::onViewDragged,
                        (view) -> { // no-op
                        }
                )
        );
    }

    private Pair<Integer, Integer> getViewLocation() {
        return new Pair<>(
                Float.valueOf(floatingVisitorVideoView.getX()).intValue(),
                Float.valueOf(floatingVisitorVideoView.getY()).intValue()
        );
    }

    private void onViewDragged(float newPositionX, float newPositionY) {
        if (newPositionX < 0) {
            newPositionX = 0;
        }
        if (newPositionY < 0) {
            newPositionY = 0;
        }
        if (newPositionX > getWidth() - floatingVisitorVideoView.getWidth()) {
            newPositionX = getWidth() - floatingVisitorVideoView.getWidth();
        }
        if (newPositionY > getHeight() - floatingVisitorVideoView.getHeight()) {
            newPositionY = getHeight() - floatingVisitorVideoView.getHeight();
        }
        setFloatingViewPosition(newPositionX, newPositionY);
    }

    private void setFloatingViewPosition(float newPositionX, float newPositionY) {
        floatingVisitorVideoView.setX(newPositionX);
        floatingVisitorVideoView.setY(newPositionY);
        floatingVisitorVideoView.invalidate();
    }
}
