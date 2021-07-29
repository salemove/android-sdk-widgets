package com.glia.widgets.core.chathead;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.glia.widgets.R;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.ViewHelpers;
import com.glia.widgets.view.head.controller.ServiceChatBubbleController;
import com.glia.widgets.view.head.ChatHeadView;

public class ChatHeadService extends Service {
    private static final String TAG = ChatHeadService.class.getSimpleName();

    private WindowManager.LayoutParams layoutParams;
    private ChatHeadView chatHeadView;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, ChatHeadService.class);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d(TAG, "onCreate");
        //Add the view to the window.
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        chatHeadView = ChatHeadView.getInstance(this);
        ServiceChatBubbleController controller = Dependencies
                .getControllerFactory()
                .getChatHeadController();

        layoutParams = getLayoutParams();
        chatHeadView.setOnTouchListener(
                new ViewHelpers.ChatHeadOnTouchListener(
                        () -> new Pair<>(layoutParams.x, layoutParams.y),
                        (x, y) -> {
                            layoutParams.x = Float.valueOf(x).intValue();
                            layoutParams.y = Float.valueOf(y).intValue();
                            windowManager.updateViewLayout(chatHeadView, layoutParams);
                        },
                        (v) -> controller.onChatHeadClicked()
                ));

        chatHeadView.setController(controller);
        controller.onSetChatHeadView(chatHeadView);
        controller.updateChatHeadView();
        windowManager.addView(chatHeadView, layoutParams);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (chatHeadView != null) windowManager.removeView(chatHeadView);
        Logger.d(TAG, "onDestroy");
    }


    private WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getLayoutFlag(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        DisplayMetrics displayMetrics = getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = getDefaultXPosition(screenWidth);
        params.y = getDefaultYPosition(screenHeight);
        return params;
    }

    private int getLayoutFlag() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            return WindowManager.LayoutParams.TYPE_PHONE;
        }
    }

    private DisplayMetrics getDisplayMetrics() {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    private int getDefaultXPosition(int screenWidth) {
        return screenWidth - ((int) getResources().getDimension(R.dimen.glia_chat_head_size)) - Float.valueOf(Utils.pxFromDp(this, 16)).intValue();
    }

    private int getDefaultYPosition(int screenHeight) {
        return screenHeight / 10 * 8;
    }
}
