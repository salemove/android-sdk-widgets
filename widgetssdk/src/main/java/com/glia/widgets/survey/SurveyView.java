package com.glia.widgets.survey;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.configuration.survey.SurveyStyle;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

public class SurveyView extends ConstraintLayout implements SurveyContract.View, SurveyAdapter.SurveyAdapterListener {
    private static final String TAG = SurveyView.class.getSimpleName();

    private SurveyContract.Controller controller;

    private UiTheme theme;

    private CardView cardView;
    private TextView title;
    private RecyclerView recyclerView;
    private LinearLayout buttonPanel;
    private Button submitButton;
    private Button cancelButton;

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

        SurveyStyle surveyStyle = theme.getSurveyStyle();

        cardView.setCardBackgroundColor(Color.parseColor(surveyStyle.getBgColor()));
        String titleColorString = surveyStyle.getTitle().normalColor;
        title.setTextColor(Color.parseColor(titleColorString));
        // The elevated view (buttonPanel) needs to have a background to cast a shadow
        buttonPanel.setBackgroundColor(Color.parseColor(surveyStyle.getBgColor()));
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
    }

    private void initAdapter() {
        surveyAdapter = new SurveyAdapter(this, theme.getSurveyStyle());
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
        surveyAdapter.submitList(state.questions);
    }

    @Override
    public void scrollTo(int index) {
        recyclerView.scrollToPosition(index);
    }

    @Override
    public void finish() {
        Activity activity = Utils.getActivity(getContext());
        if (activity != null) activity.finish();
    }

    public void onDestroyView() {
        if (controller != null) {
            controller.onDestroy();
            controller = null;
        }
    }
}
