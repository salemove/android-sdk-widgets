package com.glia.widgets.survey;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.core.dialog.DialogController;
import com.glia.widgets.core.dialog.DialogsState;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.Dialogs;
import com.glia.widgets.view.header.AppBarView;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

public class SurveyView extends ConstraintLayout implements SurveyContract.View {
    private static final String TAG = SurveyView.class.getSimpleName();

    private SurveyContract.Controller controller;
    private DialogController.Callback dialogCallback = dialogsState -> {
        if (dialogsState instanceof DialogsState.SubmitSurveyAnswersErrorDialog) {
            post(this::showSubmitSurveyAnswersErrorDialog);
        }
    };

    private UiTheme theme;

    private AlertDialog alertDialog;
    private AppBarView appBar;
    private RecyclerView recyclerView;
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

    private void readTypedArray(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        @SuppressLint("CustomViewStyleable") TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes);
        setDefaultTheme(typedArray);
        typedArray.recycle();
    }

    private void setDefaultTheme(TypedArray typedArray) {
        this.theme = Utils.getThemeFromTypedArray(typedArray, this.getContext());
    }

    public void setTheme(UiTheme uiTheme) {
        if (uiTheme == null) return;
        this.theme = Utils.getFullHybridTheme(uiTheme, this.theme);
    }

    private void initAdapter() {
        surveyAdapter = new SurveyAdapter( );
        recyclerView.setAdapter(surveyAdapter);
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.survey_view, this);
        appBar = view.findViewById(R.id.app_bar_view);
        appBar.setTitle(getContext().getString(R.string.glia_survey_engagement_ended_title));
        appBar.hideLeaveButtons();

        recyclerView = view.findViewById(R.id.survey_list);
        submitButton = view.findViewById(R.id.btn_submit);
        cancelButton = view.findViewById(R.id.btn_cancel);
    }

    private void initCallbacks() {
        appBar.setOnBackClickedListener(() -> {
            if (controller != null) {
                controller.onBackClicked();
            }
        });
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

        controller.setDialogCallback(dialogCallback);
    }

    private void showSubmitSurveyAnswersErrorDialog() {
        showAlertDialog(
                R.string.glia_dialog_submit_survey_answers_error_title,
                R.string.glia_dialog_submit_survey_answers_error_message,
                v -> {
                    dismissAlertDialog();
                    if (controller != null) {
                        controller.submitSurveyAnswersErrorDialogDismissed();
                    }
                }
        );
    }

    private void showAlertDialog(@StringRes int title, @StringRes int message,
                                 View.OnClickListener buttonClickListener) {
        dismissAlertDialog();
        alertDialog = Dialogs.showAlertDialog(
                this.getContext(),
                this.theme,
                title,
                message,
                buttonClickListener);
    }

    private void dismissAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    @Override
    public void onStateUpdated(SurveyState state) {
        surveyAdapter.setItems(state.questions);
    }

    @Override
    public void finish() {
        Activity activity = Utils.getActivity(getContext());
        if (activity != null) activity.finish();
    }

    public void onDestroyView() {
        if (controller != null) {
            controller.removeDialogCallback(dialogCallback);
            controller.onDestroy();
            controller = null;
        }
    }
}
