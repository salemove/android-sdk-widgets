package com.glia.widgets.view.button

import android.content.Context
import android.util.AttributeSet
import com.glia.widgets.R

/**
 * Option button for the boolean and scale survey questions.
 *
 * Nothing is styled here: `?attr/buttonSurveyOptionButtonStyle` points at
 * `Application.Glia.Chat.Button.SurveyOption`, whose fill, stroke and label are colour state lists
 * over the composed Glia theme. Both of this button's own states are therefore expressed as drawable
 * states - selection as `android:state_selected`, the required-answer error as
 * `android:state_activated` - so changing either is enough to repaint it.
 *
 * @hide
 */
class GliaSurveyOptionButton(context: Context, attrs: AttributeSet?) :
    BaseConfigurableButton(context, attrs, R.attr.buttonSurveyOptionButtonStyle) {

    /**
     * Whether the question this button belongs to is currently showing its validation error.
     *
     * Backed by `isActivated` rather than a field of its own, so the state list and this flag cannot
     * disagree. The JSON unified theme reads it back in `applyOptionButtonTheme` to pick which of its
     * three layers to apply.
     */
    var isError: Boolean
        get() = isActivated
        set(value) {
            isActivated = value
        }
}
