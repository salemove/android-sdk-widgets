package com.glia.widgets.call

import com.glia.androidsdk.engagement.Survey

internal interface CallViewCallback {
    fun emitState(callState: CallState)
    fun navigateToChat()
    fun navigateToSurvey(survey: Survey)
    fun destroyView()
    fun minimizeView()
    fun showMissingPermissionsDialog()
    fun showLiveObservationOptInDialog(companyName: String)
}
