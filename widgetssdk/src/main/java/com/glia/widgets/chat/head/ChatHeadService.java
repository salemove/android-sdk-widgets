package com.glia.widgets.chat.head;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.call.CallActivity;
import com.glia.widgets.chat.ChatActivity;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;
import com.google.android.material.imageview.ShapeableImageView;

public class ChatHeadService extends Service {

    private final String TAG = "ChatHeadService";
    public static final String IS_VISIBLE = "is_visible";

    private WindowManager windowManager;
    private UiTheme uiTheme;
    private String companyName;
    private String queueId;
    private String contextUrl;
    private String lastTypedText;
    private View floatingView;
    private ShapeableImageView profilePictureView;
    private ShapeableImageView placeholderView;
    private boolean isVisible = true;
    private String returnDestination;

    public ChatHeadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String companyName = intent.getStringExtra(GliaWidgets.COMPANY_NAME);
        if (companyName != null) {
            this.companyName = companyName;
        }
        String queueId = intent.getStringExtra(GliaWidgets.QUEUE_ID);
        if (queueId != null) {
            this.queueId = queueId;
        }
        String contextUrl = intent.getStringExtra(GliaWidgets.CONTEXT_URL);
        if (contextUrl != null) {
            this.contextUrl = contextUrl;
        }
        String lastTypedText = intent.getStringExtra(ChatActivity.LAST_TYPED_TEXT);
        if (lastTypedText != null) {
            this.lastTypedText = lastTypedText;
        }
        UiTheme uiTheme = intent.getParcelableExtra(GliaWidgets.UI_THEME);
        if (uiTheme != null) {
            this.uiTheme = uiTheme;
            useTheme();
        }
        returnDestination = intent.getStringExtra(GliaWidgets.RETURN_DESTINATION);
        isVisible = intent.getBooleanExtra(IS_VISIBLE, false);
        Logger.d(TAG, "companyName: " + this.companyName + ", queueId: " + this.queueId +
                ", contextUrl: " + this.contextUrl + "lastTypedText: " + this.lastTypedText +
                ", uiTheme: " + this.uiTheme.toString() + "returnDestination: " + returnDestination +
                ", isVisible: " + isVisible);
        updateVisibility();
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateVisibility() {
        if (floatingView != null) {
            if (isVisible) {
                floatingView.setVisibility(View.VISIBLE);
            } else {
                floatingView.setVisibility(View.GONE);
            }
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public void onCreate() {
        super.onCreate();
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_chat_head, null);
        profilePictureView = floatingView.findViewById(R.id.profile_picture_view);
        placeholderView = floatingView.findViewById(R.id.placeholder_view);
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
        windowManager.addView(floatingView, params);

        final View floatingChatHead = floatingView.findViewById(R.id.floating_chat_head_layout);

        //Drag and move floating view using user's touch action.
        floatingChatHead.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int xDiff = (int) (event.getRawX() - initialTouchX);
                        int yDiff = (int) (event.getRawY() - initialTouchY);

                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (xDiff < 10 && yDiff < 10) {
                            returnToEngagement();
                            floatingView.setVisibility(View.GONE);
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        //Update the layout with new X & Y coordinate
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
        updateVisibility();
    }

    private void useTheme() {
        if (uiTheme != null) {
            ColorStateList backgroundColor =
                    ContextCompat.getColorStateList(this, uiTheme.getBaseLightColor());
            int primaryColor = ContextCompat.getColor(this, uiTheme.getBrandPrimaryColor());
            profilePictureView.setBackgroundColor(primaryColor);
            placeholderView.setBackgroundColor(primaryColor);
            placeholderView.setImageTintList(backgroundColor);
        }
    }

    private void returnToEngagement() {
        if (returnDestination.equals(GliaWidgets.DESTINATION_CHAT)) {
            startActivity(ChatActivity.class);
        } else {
            startActivity(CallActivity.class);
        }
    }

    private void startActivity(Class<?> cls) {
        Intent newIntent = new Intent(getApplicationContext(), cls);
        newIntent.putExtra(GliaWidgets.COMPANY_NAME, companyName);
        newIntent.putExtra(GliaWidgets.QUEUE_ID, queueId);
        newIntent.putExtra(GliaWidgets.CONTEXT_URL, contextUrl);
        newIntent.putExtra(GliaWidgets.UI_THEME, uiTheme);
        newIntent.putExtra(ChatActivity.LAST_TYPED_TEXT, lastTypedText);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(newIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) windowManager.removeView(floatingView);
    }
}
