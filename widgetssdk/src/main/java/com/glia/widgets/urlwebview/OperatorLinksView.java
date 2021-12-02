package com.glia.widgets.urlwebview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.header.AppBarView;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

public class OperatorLinksView extends ConstraintLayout {

    private AppBarView appBar;
    private WebView operatorUrlWebView;
    private UiTheme theme;

    public OperatorLinksView(Context context) {
        this(context, null);
    }

    public OperatorLinksView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.gliaChatStyle);
    }

    public OperatorLinksView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat);
    }

    public OperatorLinksView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(
                MaterialThemeOverlay.wrap(
                        context,
                        attrs,
                        defStyleAttr,
                        defStyleRes),
                attrs,
                defStyleAttr,
                defStyleRes
        );

        initView();
        setDefaultThemeFromTypedArray(attrs, defStyleAttr, defStyleRes);
        initCallbacks();
        setupViewAppearance();
        configureWebViewSettings();
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.operator_links_view, this);
        appBar = view.findViewById(R.id.app_bar_view);
        appBar.setTitle(getContext().getString(R.string.operator_link_title));
        appBar.hideLeaveButtons();
        operatorUrlWebView = view.findViewById(R.id.operator_url_web_view);
    }

    private void setDefaultThemeFromTypedArray(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        @SuppressLint("CustomViewStyleable") TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes);
        this.theme = Utils.getThemeFromTypedArray(typedArray, this.getContext());
        typedArray.recycle();
    }

    private void initCallbacks() {
        appBar.setOnBackClickedListener(() -> {
            Activity activity = Utils.getActivity(getContext());
            if (activity != null) activity.finish();
        });
    }

    private void setupViewAppearance() {
        setBackgroundColor(ContextCompat.getColor(getContext(), theme.getBaseLightColor()));
        appBar.setTheme(this.theme);
    }

    public void loadWebView(String url) {
        operatorUrlWebView.loadUrl(url);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebViewSettings() {
        operatorUrlWebView.getSettings().setJavaScriptEnabled(true);
        operatorUrlWebView.setWebViewClient(new WebViewClient());
    }

    public UiTheme setTheme(UiTheme uiTheme) {
        if (uiTheme != null) this.theme = Utils.getFullHybridTheme(uiTheme, this.theme);
        setupViewAppearance();
        return this.theme;
    }
}
