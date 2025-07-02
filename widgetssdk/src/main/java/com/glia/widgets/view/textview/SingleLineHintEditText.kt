package com.glia.widgets.view.textview

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

/**
 * This class is here to solve next bug:
 * See video in this comment to understand initial bug: https://glia.atlassian.net/browse/MOB-3860?focusedCommentId=459636
 *
 * The issue was caused by default EditText behavior. The hint height is effecting the EditText height even when the hint is not displayed.
 * This means that if for some reason your EditText view has property maxLines = 4 and your hint is so long that it takes 4 lines and
 * then when user write just 1 letter into your EditText it would still occupy as much height as if it had 4 lines of code.
 *
 * Another related problem is that for some reason at least in ConstraintLayout multi-line hint messes up layout rules. See next comment
 * in the same ticket: https://glia.atlassian.net/browse/MOB-3860?focusedCommentId=459642.
 * To prevent this from happening this implementation forces hint to always be one-line
 */
internal class SingleLineHintEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatEditText(context, attrs, defStyleAttr) {

    private var hintOverride: CharSequence? = null
    var maxLinesOverride: Int = 0
    var ellipsizeOverride: TextUtils.TruncateAt? = null

    init {
        hintOverride = hint           // Retrieves existing value if it was set in XML
        maxLinesOverride = maxLines   // Retrieves existing value if it was set in XML
        ellipsizeOverride = ellipsize // Retrieves existing value if it was set in XML

        if (shouldShowHint()) {
            showHintAndMakeItOneLine()
        }
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /* no-op */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                /* no-op */
            }

            override fun afterTextChanged(text: Editable?) {
                if (shouldShowHint()) {
                    showHintAndMakeItOneLine()
                } else if (!text.isNullOrEmpty() && !hint.isNullOrEmpty()) {
                    removeHintAndRestoreEditTextSettings()
                }
            }

        })
    }

    /**
     * Since the native EditText#setHint(..) method is final and can't be override in subclasses I had to introduce a separate method
     */
    fun setHintOverride(s: CharSequence?) {
        hintOverride = s
        if (shouldShowHint()) {
            showHintAndMakeItOneLine()
        }
    }

    /**
     * Some clarifications about this method implementation:
     * - 'hint' here is the native EditText#getHint() method
     * - When the user has written anything into this EditText then the hint is set to be empty (e.g.: "") to prevent a bug with height but later we need to add it back when there is no user text in this EditText
     * - Technically I could have used just the part text.isNullOrEmpty() but I wanted to make sure that it is not triggered unnecessarily if the hint is already displayed
     * - Also it is possible that the hint has been changed at runtime using the setHintOverride (e.g. custom locales finished loading and need hint update) while there was an old hint displayed needing refresh
     */
    private fun shouldShowHint(): Boolean = text.isNullOrEmpty() && hint != hintOverride

    private fun removeHintAndRestoreEditTextSettings() {
        hint = ""
        maxLines = maxLinesOverride
        ellipsize = ellipsizeOverride
    }

    private fun showHintAndMakeItOneLine() {
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        hint = hintOverride
    }
}
