package com.glia.widgets.call;

import androidx.annotation.NonNull;

import com.glia.androidsdk.engagement.Survey;

public interface CallViewCallback {

    void emitState(CallState callState);

    void navigateToChat();

    void navigateToSurvey(@NonNull Survey survey);

    void destroyView();
}
