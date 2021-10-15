package com.glia.widgets.urlwebview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.glia.widgets.Constants;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.di.Dependencies;

public class OperatorLinksActivity extends AppCompatActivity {

    private static final String URL_KEY = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_links);
        Dependencies.addActivityToBackStack(Constants.URL_WEB_VIEW_ACTIVITY);

        OperatorLinksView operatorLinksView = findViewById(R.id.operator_links_view);

        Intent intent = getIntent();
        UiTheme theme = intent.getParcelableExtra(GliaWidgets.UI_THEME);
        String url = intent.getStringExtra(URL_KEY);

        operatorLinksView.setTheme(theme);
        handleStatusBarColor(theme);
        operatorLinksView.loadWebView(url);
    }

    public static Intent intent(Context context, String url, UiTheme theme) {
        Intent intent = new Intent(context, OperatorLinksActivity.class);
        intent.putExtra(URL_KEY, url);
        intent.putExtra(GliaWidgets.UI_THEME, theme);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    private void handleStatusBarColor(UiTheme uiTheme) {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, uiTheme.getBrandPrimaryColor()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Dependencies.removeActivityFromBackStack(Constants.URL_WEB_VIEW_ACTIVITY);
    }
}
