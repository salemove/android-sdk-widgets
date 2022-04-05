package com.glia.widgets.view.header;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.header.button.GliaEndButton;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;

public class AppBarView extends AppBarLayout {
    private final GliaEndButton gliaEndButton;
    private final MaterialToolbar materialToolbar;
    private final TextView titleView;
    private UiTheme theme;

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
        gliaEndButton = view.findViewById(R.id.end_button);

        materialToolbar.setNavigationContentDescription(context.getString(R.string.glia_top_app_bar_navigate_up_content_description));

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AppBarView);
        Integer backIconResId = Utils.getTypedArrayIntegerValue(
                typedArray,
                context,
                R.styleable.AppBarView_backIcon,
                R.attr.gliaIconAppBarBack);
        materialToolbar.setNavigationIcon(backIconResId);
        setDefaultTitleTintColor(typedArray, context);
        setDefaultHomeButtonTintColor(typedArray, context);
        setDefaultBackgroundTintColor(typedArray, context);

        String title = Utils.getTypedArrayStringValue(typedArray, R.styleable.AppBarView_titleText);
        if (title != null) {
            titleView.setText(title);
        }

        TypedArray typedArrayView = context.obtainStyledAttributes(attrs, R.styleable.GliaView);
        this.theme = Utils.getThemeFromTypedArray(typedArrayView, this.getContext());
        setTypeface();
        DrawableCompat.setTint(materialToolbar.getMenu().findItem(R.id.leave_queue_button).getIcon(),
                ResourcesCompat.getColor(
                        getResources(),
                        Utils.getTypedArrayIntegerValue(typedArrayView,
                                context,
                                R.styleable.GliaView_chatHeaderExitQueueButtonTintColor,
                                R.attr.gliaChatHeaderExitQueueButtonTintColor
                        ),
                        this.getContext().getTheme())
        );
    }

    private void setDefaultBackgroundTintColor(TypedArray typedArray, Context context) {
        ColorStateList backgroundTintList = ContextCompat.getColorStateList(
                context,
                Utils.getTypedArrayIntegerValue(typedArray,
                        context,
                        R.styleable.AppBarView_android_backgroundTint,
                        R.attr.gliaBrandPrimaryColor
                )
        );
        materialToolbar.setBackgroundTintList(backgroundTintList);
    }

    private void setDefaultHomeButtonTintColor(TypedArray typedArray, Context context) {
        int homeButtonTintColor = ContextCompat.getColor(
                context,
                Utils.getTypedArrayIntegerValue(typedArray,
                        context,
                        R.styleable.AppBarView_lightTint,
                        R.attr.gliaChatHeaderHomeButtonTintColor
                )
        );
        materialToolbar.getNavigationIcon().setTint(homeButtonTintColor);
    }

    private void setDefaultTitleTintColor(TypedArray typedArray, Context context) {
        int titleTintColor = ContextCompat.getColor(
                context,
                Utils.getTypedArrayIntegerValue(typedArray,
                        context,
                        R.styleable.AppBarView_lightTint,
                        R.attr.gliaChatHeaderTitleTintColor
                )
        );
        titleView.setTextColor(titleTintColor);
    }


    private void setTypeface() {
        if (this.theme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(
                    this.getContext(),
                    this.theme.getFontRes()
            );
            titleView.setTypeface(fontFamily);
        }
    }

    public void setTheme(UiTheme uiTheme) {
        if (uiTheme == null) return;
        this.theme = Utils.getFullHybridTheme(uiTheme, this.theme);

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

        titleView.setTextColor(ContextCompat.getColorStateList(getContext(), theme.getGliaChatHeaderTitleTintColor()));
        materialToolbar.getNavigationIcon().setTint(ContextCompat.getColor(this.getContext(), theme.getGliaChatHeaderHomeButtonTintColor()));
        gliaEndButton.setTheme(theme);
        DrawableCompat.setTint(
                materialToolbar.getMenu().findItem(R.id.leave_queue_button).getIcon(),
                ContextCompat.getColor(getContext(), theme.getGliaChatHeaderExitQueueButtonTintColor())
        );
        setTypeface();
    }

    public void setTitle(String title) {
        titleView.setText(title);
    }

    public void showToolbar() {
        setVisibility(VISIBLE);
    }

    public void showXButton() {
        gliaEndButton.setVisibility(GONE);
        materialToolbar.getMenu().findItem(R.id.leave_queue_button).setVisible(true);
    }

    public void showEndButton() {
        gliaEndButton.setVisibility(VISIBLE);
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
        gliaEndButton.setOnClickListener(v -> onEndChatClickedListener.onEnd());
    }

    public void hideLeaveButtons() {
        gliaEndButton.setVisibility(GONE);
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
