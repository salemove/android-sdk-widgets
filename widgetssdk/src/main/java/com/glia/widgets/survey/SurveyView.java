package com.glia.widgets.survey;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.configuration.ButtonConfiguration;
import com.glia.widgets.view.configuration.TextConfiguration;
import com.glia.widgets.view.configuration.survey.SurveyStyle;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

public class SurveyView extends FrameLayout
        implements SurveyContract.View, SurveyAdapter.SurveyAdapterListener {
    private static final String TAG = SurveyView.class.getSimpleName();

    private OnTitleUpdatedListener onTitleUpdatedListener;
    private OnFinishListener onFinishListener;

    private SurveyContract.Controller controller;

    private UiTheme theme;

    private CardView cardView;
    private TextView title;
    private RecyclerView recyclerView;
    private LinearLayout buttonPanel;
    private MaterialButton submitButton;
    private MaterialButton cancelButton;

    private SurveyAdapter surveyAdapter;

    public SurveyView(Context context) {
        this(context, null);
    }

    public SurveyView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.gliaChatStyle);
    }

    public SurveyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat);
    }

    public SurveyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
        readTypedArray(attrs, defStyleAttr, defStyleRes);
        initCallbacks();
        initAdapter();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        applyStyle(theme.getSurveyStyle());
    }

    public void setOnTitleUpdatedListener(OnTitleUpdatedListener onTitleUpdatedListener) {
        this.onTitleUpdatedListener = onTitleUpdatedListener;
    }

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    private void applyStyle(SurveyStyle surveyStyle) {
        setupCardView(surveyStyle);

        TextConfiguration titleStyle = surveyStyle.getTitle();
        this.title.setTextColor(titleStyle.getTextColor());
        float textSize = titleStyle.getTextSize();
        this.title.setTextSize(textSize);
        if (Boolean.TRUE.equals(titleStyle.isBold())) {
            this.title.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            this.title.setTypeface(Typeface.DEFAULT);
        }

        // The elevated view (buttonPanel) needs to have a background to cast a shadow
        buttonPanel.setBackgroundColor(Color.parseColor(surveyStyle.getLayer().getBackgroundColor()));

        applyButtonStyle(surveyStyle.getSubmitButton(), submitButton);
        applyButtonStyle(surveyStyle.getCancelButton(), cancelButton);
    }

    private void setupCardView(SurveyStyle surveyStyle) {
        ResourceProvider resourceProvider = Dependencies.getResourceProvider();
        float cornerRadius = resourceProvider.convertDpToPixel(surveyStyle.getLayer().getCornerRadius());
        ShapeAppearanceModel.Builder cardViewShapeBuilder = new ShapeAppearanceModel().toBuilder();
        cardViewShapeBuilder.setTopLeftCorner(
                CornerFamily.ROUNDED,
                bounds -> cornerRadius);
        cardViewShapeBuilder.setTopRightCorner(
                CornerFamily.ROUNDED,
                bounds -> cornerRadius);

        MaterialShapeDrawable background = new MaterialShapeDrawable(cardViewShapeBuilder.build());
        int backgroundColor = Color.parseColor(surveyStyle.getLayer().getBackgroundColor());
        background.setFillColor(ColorStateList.valueOf(backgroundColor));
        cardView.setBackground(background);
    }

    private void applyButtonStyle(ButtonConfiguration configuration, MaterialButton button) {
        if (configuration == null) {
            // Default attributes from
            // "Application.GliaAndroidSdkWidgetsExample.Button" styles
            // will be in use
            return;
        }
        ColorStateList backgroundColor = configuration.getBackgroundColor();
        button.setBackgroundTintList(backgroundColor);
        ColorStateList textColor = configuration.getTextConfiguration().getTextColor();
        button.setTextColor(textColor);
        button.setTextSize(configuration.getTextConfiguration().getTextSize());
        button.setStrokeColor(configuration.getStrokeColor());
        if (configuration.getStrokeWidth() != null) button.setStrokeWidth(configuration.getStrokeWidth());
        if (Boolean.TRUE.equals(configuration.getTextConfiguration().isBold())) {
            button.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            button.setTypeface(Typeface.DEFAULT);
        }
    }

    private void readTypedArray(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        @SuppressLint("CustomViewStyleable") TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes);
        setDefaultTheme(attrs, defStyleAttr, defStyleRes);
        typedArray.recycle();
    }

    private void setDefaultTheme(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        @SuppressLint("CustomViewStyleable") TypedArray typedArray =
                this.getContext().obtainStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes);
        this.theme = Utils.getThemeFromTypedArray(typedArray, this.getContext());
        typedArray.recycle();
    }

    public void setTheme(UiTheme uiTheme) {
        if (uiTheme == null) return;
        this.theme = Utils.getFullHybridTheme(uiTheme, this.theme);
        surveyAdapter.setStyle(theme.getSurveyStyle());
    }

    private void initAdapter() {
        surveyAdapter = new SurveyAdapter(this);
        recyclerView.setAdapter(surveyAdapter);
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.survey_view, this);

        cardView = view.findViewById(R.id.card_view);
        title = view.findViewById(R.id.survey_title);
        recyclerView = view.findViewById(R.id.survey_list);
        buttonPanel = view.findViewById(R.id.button_panel);
        submitButton = view.findViewById(R.id.btn_submit);
        cancelButton = view.findViewById(R.id.btn_cancel);
    }

    private void initCallbacks() {
        submitButton.setOnClickListener((view) -> {
            if (controller != null) {
                controller.onSubmitClicked();
            }
        });
        cancelButton.setOnClickListener((view) -> {
            if (controller != null) {
                controller.onCancelClicked();
            }
        });
    }

    @Override
    public void setController(SurveyContract.Controller controller) {
        this.controller = controller;
        this.controller.setView(this);
    }

    @Override
    public void onAnswer(@NonNull Survey.Answer answer) {
        if (controller != null) {
            controller.onAnswer(answer);
        }
    }

    @Override
    public void onStateUpdated(SurveyState state) {
        if (onTitleUpdatedListener != null) {
            onTitleUpdatedListener.onTitleUpdated(state.title);
        }
        title.setText(state.title);
        surveyAdapter.submitList(state.questions);
    }

    @Override
    public void scrollTo(int index) {
        recyclerView.scrollToPosition(index);
    }

    @Override
    public void hideSoftKeyboard() {
        Utils.hideSoftKeyboard(getContext(), getWindowToken());
    }

    @Override
    public void onNetworkTimeout() {
        Toast.makeText(getContext(), R.string.glia_survey_network_unavailable, Toast.LENGTH_LONG).show();
    }

    @Override
    public void finish() {
        if (onFinishListener != null) {
            onFinishListener.onFinish();
        }
    }

    public void onDestroyView() {
        if (controller != null) {
            controller.onDestroy();
            controller = null;
        }
    }

    public interface OnTitleUpdatedListener {
        void onTitleUpdated(String title);
    }

    public interface OnFinishListener {
        void onFinish();
    }
}
