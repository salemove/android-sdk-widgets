package com.glia.widgets.head;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import androidx.core.util.Pair;

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.ViewHelpers;

public class ChatHeadService extends Service {

    private final String TAG = "ChatHeadService";

    private WindowManager windowManager;
    private ChatHeadView chatHeadView;
    private ChatHeadsController controller;
    private String returnDestination;
    private final ChatHeadsController.OnChatheadSettingsChangedListener visibilityChangedListener =
            new ChatHeadsController.OnChatheadSettingsChangedListener() {

                @Override
                public void emitState(ChatHeadState chatHeadState) {
                    if (chatHeadState.theme != null) {
                        chatHeadView.setTheme(chatHeadState.theme);
                    }
                    chatHeadView.setMessageBadgeCount(
                            chatHeadState.showMessageCount ?
                                    chatHeadState.messageCount :
                                    0);
                    chatHeadView.updateImage(chatHeadState.operatorProfileImgUrl);
                    chatHeadView.setVisibility(chatHeadState.isOverlayVisible ? View.VISIBLE : View.GONE);
                    returnDestination = chatHeadState.returnDestination;
                }
            };

    public ChatHeadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d(TAG, "onCreate");
        //Add the view to the window.
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = width - ((int) getResources().getDimension(R.dimen.chat_head_size))
                - Float.valueOf(Utils.pxFromDp(this, 16)).intValue();
        params.y = height / 10 * 8;

        //Add the view to the window
        chatHeadView = new ChatHeadView(this);
        windowManager.addView(chatHeadView, params);
        chatHeadView.setOnTouchListener(new ViewHelpers.ChatHeadOnTouchListener(
                () -> new Pair(params.x, params.y),
                (x, y) -> {
                    params.x = Float.valueOf(x).intValue();
                    params.y = Float.valueOf(y).intValue();

                    //Update the layout with new X & Y coordinate
                    windowManager.updateViewLayout(chatHeadView, params);
                },
                (v) -> {
                    if (controller != null) {
                        Intent navigationIntent = Utils.getReturnToEngagementIntent(
                                getApplicationContext(),
                                controller.chatHeadClicked(),
                                returnDestination
                        );
                        startActivity(navigationIntent);
                    }
                }));

        controller = GliaWidgets.getControllerFactory().getChatHeadsController();
        controller.addOverlayListener(visibilityChangedListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatHeadView != null) windowManager.removeView(chatHeadView);
        GliaWidgets.getControllerFactory().getChatHeadsController().clearOverlayListener();
        Logger.d(TAG, "onDestroy");
    }
}
