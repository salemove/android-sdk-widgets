package com.glia.widgets.view.floatingvisitorvideoview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;

import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.widgets.R;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.ViewHelpers;

public class FloatingVisitorVideoConstraintLayout extends ConstraintLayout implements FloatingVisitorVideoContract.View {
    private FloatingVisitorVideoContract.Controller controller;
    private FloatingVisitorVideoView floatingVisitorVideoContainer;

    public FloatingVisitorVideoConstraintLayout(@NonNull Context context) {
        this(context, null);
    }

    public FloatingVisitorVideoConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingVisitorVideoConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void setController(FloatingVisitorVideoContract.Controller controller) {
        this.controller = controller;
    }

    @Override
    public void show(VisitorMediaState state) {
        post(() -> {
            if (!floatingVisitorVideoContainer.hasVideo()) {
                floatingVisitorVideoContainer.showVisitorVideo(
                        state.getVideo().createVideoView(Utils.getActivity(getContext()))
                );
            }
            setVisibility(VISIBLE);
        });
    }

    @Override
    public void hide() {
        post(() -> {
            floatingVisitorVideoContainer.hideVisitorVideo();
            setVisibility(GONE);
        });
    }

    @Override
    public void onResume() {
        floatingVisitorVideoContainer.onResume();
        controller.onResume();
    }

    @Override
    public void onPause() {
        floatingVisitorVideoContainer.onPause();
        controller.onPause();
    }

    @Override
    public void onDestroy() {
        floatingVisitorVideoContainer.onDestroy();
        controller.onDestroy();
    }

    @Override
    public void showOnHold() {
        post(() -> floatingVisitorVideoContainer.showOnHold());
    }

    @Override
    public void hideOnHold() {
        post(() -> floatingVisitorVideoContainer.hideOnHold());
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.visitor_video_layout_view, this);
        floatingVisitorVideoContainer = findViewById(R.id.visitor_video_card);
        setController(Dependencies.getControllerFactory().getFloatingVisitorVideoController());
        controller.setView(this);
        setVisitorVideoContainerTouchListener();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setVisitorVideoContainerTouchListener() {
        floatingVisitorVideoContainer.setOnTouchListener(
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
                Float.valueOf(floatingVisitorVideoContainer.getX()).intValue(),
                Float.valueOf(floatingVisitorVideoContainer.getY()).intValue()
        );
    }

    private void onViewDragged(float newPositionX, float newPositionY) {
        if (newPositionX < 0) {
            newPositionX = 0;
        }
        if (newPositionY < 0) {
            newPositionY = 0;
        }
        if (newPositionX > getWidth() - floatingVisitorVideoContainer.getWidth()) {
            newPositionX = getWidth() - floatingVisitorVideoContainer.getWidth();
        }
        if (newPositionY > getHeight() - floatingVisitorVideoContainer.getHeight()) {
            newPositionY = getHeight() - floatingVisitorVideoContainer.getHeight();
        }
        setFloatingViewPosition(newPositionX, newPositionY);
    }

    private void setFloatingViewPosition(float newPositionX, float newPositionY) {
        floatingVisitorVideoContainer.setX(newPositionX);
        floatingVisitorVideoContainer.setY(newPositionY);
        floatingVisitorVideoContainer.invalidate();
    }
}
