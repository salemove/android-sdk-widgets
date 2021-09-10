package com.glia.widgets.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.FontRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.helper.Utils;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;

public class AppBarView extends AppBarLayout {

    private final MaterialToolbar materialToolbar;
    private final TextView titleView;
    private final Button endButton;

    public AppBarView(@NonNull Context context) {
        this(context, null);
    }

    public AppBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.gliaChatStyle);
    }

    public AppBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = View.inflate(context, R.layout.app_bar, this);

        materialToolbar = view.findViewById(R.id.toolbar);
        titleView = view.findViewById(R.id.title);
        endButton = view.findViewById(R.id.end_button);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AppBarView);
        Integer backIconResId = Utils.getTypedArrayIntegerValue(
                typedArray,
                context,
                R.styleable.AppBarView_backIcon,
                R.attr.gliaIconAppBarBack);
        materialToolbar.setNavigationIcon(backIconResId);

        ColorStateList backgroundTintList = ContextCompat.getColorStateList(
                context,
                Utils.getTypedArrayIntegerValue(typedArray,
                        context,
                        R.styleable.AppBarView_android_backgroundTint,
                        R.attr.gliaBrandPrimaryColor));
        int lightColor = ContextCompat.getColor(
                context,
                Utils.getTypedArrayIntegerValue(typedArray,
                        context,
                        R.styleable.AppBarView_lightTint,
                        R.attr.gliaBaseLightColor));
        ColorStateList negativeColorStateList = ContextCompat.getColorStateList(this.getContext(),
                Utils.getTypedArrayIntegerValue(typedArray,
                        context,
                        R.styleable.AppBarView_negativeTint,
                        R.attr.gliaSystemNegativeColor));
        materialToolbar.setBackgroundTintList(backgroundTintList);
        titleView.setTextColor(lightColor);
        materialToolbar.getNavigationIcon().setTint(lightColor);
        DrawableCompat.setTint(materialToolbar.getMenu().findItem(R.id.leave_queue_button).getIcon(),
                ResourcesCompat.getColor(
                        getResources(),
                        Utils.getTypedArrayIntegerValue(typedArray,
                                context,
                                R.styleable.AppBarView_lightTint,
                                R.attr.gliaBaseLightColor),
                        this.getContext().getTheme()));
        endButton.setBackgroundTintList(negativeColorStateList);
        endButton.setTextColor(lightColor);

        String title = Utils.getTypedArrayStringValue(typedArray, R.styleable.AppBarView_titleText);
        if (title != null) {
            titleView.setText(title);
        }
    }

    public void setTheme(UiTheme theme) {
        // icons
        materialToolbar.setNavigationIcon(theme.getIconAppBarBack());
        materialToolbar.getMenu().findItem(R.id.leave_queue_button).setIcon(theme.getIconLeaveQueue());

        // colors
        materialToolbar.setBackgroundTintList(
                ContextCompat.getColorStateList(
                        this.getContext(),
                        theme.getBrandPrimaryColor()));
        DrawableCompat.setTint(materialToolbar.getMenu().findItem(R.id.leave_queue_button).getIcon(),
                ResourcesCompat.getColor(
                        getResources(),
                        theme.getBaseLightColor(),
                        this.getContext().getTheme()));
        titleView.setTextColor(ResourcesCompat.getColor(
                getResources(),
                theme.getBaseLightColor(),
                this.getContext().getTheme()));
        materialToolbar.getNavigationIcon().setTint(
                ContextCompat.getColor(this.getContext(), theme.getBaseLightColor()));
        endButton.setBackgroundTintList(
                ContextCompat.getColorStateList(
                        this.getContext(),
                        theme.getSystemNegativeColor()));
        endButton.setTextColor(ResourcesCompat.getColor(
                getResources(),
                theme.getBaseLightColor(),
                this.getContext().getTheme()));
        // fonts
        if (theme.getFontRes() != null) {
            changeFontFamily(theme.getFontRes());
        }
    }

    public void changeFontFamily(@FontRes int fontRes) {
        Typeface fontFamily = ResourcesCompat.getFont(
                this.getContext(),
                fontRes);
        titleView.setTypeface(fontFamily);
        endButton.setTypeface(fontFamily);
    }

    public void setTitle(String title) {
        titleView.setText(title);
    }

    public void showToolbar() {
        setVisibility(VISIBLE);
    }

    public void showXButton() {
        endButton.setVisibility(GONE);
        materialToolbar.getMenu().findItem(R.id.leave_queue_button).setVisible(true);
    }

    public void showEndButton() {
        endButton.setVisibility(VISIBLE);
        materialToolbar.getMenu().findItem(R.id.leave_queue_button).setVisible(false);
    }

    public void setOnBackClickedListener(OnBackClickedListener onBackClickedListener) {
        materialToolbar.setNavigationOnClickListener(view -> onBackClickedListener.onBackClicked());
    }

    public void setOnXClickedListener(OnXClickedListener onXClickedListener) {
        materialToolbar.setOnMenuItemClickListener(item -> {
            onXClickedListener.onXClicked();
            return true;
        });
    }

    public void setOnEndChatClickedListener(OnEndChatClickedListener onEndChatClickedListener) {
        endButton.setOnClickListener(v -> onEndChatClickedListener.onEnd());
    }

    public void hideLeaveButtons() {
        endButton.setVisibility(GONE);
        materialToolbar.getMenu().findItem(R.id.leave_queue_button).setVisible(false);
    }

    private MenuItem saveItem;
    private MenuItem shareItem;

    public void setMenuImagePreview() {
        materialToolbar.inflateMenu(R.menu.menu_file_preview);
        Menu menu = materialToolbar.getMenu();
        saveItem = menu.findItem(R.id.save_item);
        shareItem = menu.findItem(R.id.share_item);
    }

    public void setImagePreviewButtonListener(OnImagePreviewMenuListener listener) {
        materialToolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.share_item) {
                listener.onShareClicked();
            } else if (itemId == R.id.save_item) {
                listener.onDownloadClicked();
            }
            return true;
        });
    }

    public void setImagePreviewButtonsVisible(boolean saveItem, boolean shareItem) {
        this.saveItem.setVisible(saveItem);
        this.shareItem.setVisible(shareItem);
    }

    public interface OnImagePreviewMenuListener {
        void onShareClicked();

        void onDownloadClicked();
    }

    public interface OnBackClickedListener {
        void onBackClicked();
    }

    public interface OnXClickedListener {
        void onXClicked();
    }

    public interface OnEndChatClickedListener {
        void onEnd();
    }
}
