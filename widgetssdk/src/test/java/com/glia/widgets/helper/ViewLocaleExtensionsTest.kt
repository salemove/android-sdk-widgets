package com.glia.widgets.helper

import android.app.Activity
import android.widget.FrameLayout
import android.widget.TextView
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies
import com.glia.widgets.locale.LocaleString
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ViewLocaleExtensionsTest {

    private val localeChanges = PublishSubject.create<String>()
    private var subscriptions = 0
    private var yesTranslation = "Yes"

    private lateinit var parent: FrameLayout
    private lateinit var view: TextView

    @Before
    fun setUp() {
        Dependencies.localeProvider = mockk {
            every { getLocaleObservable() } returns localeChanges
                .doOnSubscribe { subscriptions++ }
                .doOnDispose { subscriptions-- }
            every { getString(R.string.general_yes, emptyList()) } answers { yesTranslation }
            every { getString(R.string.general_no, emptyList()) } returns "No"
        }
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        parent = FrameLayout(activity).also(activity::setContentView)
        view = TextView(activity)
    }

    @Test
    fun `setLocaleText sets the translation without subscribing when the view is not attached`() {
        view.setLocaleText(R.string.general_yes)

        assertEquals("Yes", view.text.toString())
        assertEquals(0, subscriptions)
    }

    @Test
    fun `setLocaleText follows locale changes while the view is attached`() {
        parent.addView(view)
        view.setLocaleText(R.string.general_yes)

        changeYesTranslation("Jah")

        assertTrue(view.isAttachedToWindow)
        assertEquals("Jah", view.text.toString())
        assertEquals(1, subscriptions)
    }

    @Test
    fun `setLocaleText stops following locale changes after the view is detached`() {
        parent.addView(view)
        view.setLocaleText(R.string.general_yes)

        parent.removeView(view)
        changeYesTranslation("Jah")

        assertEquals("Yes", view.text.toString())
        assertEquals(0, subscriptions)
    }

    @Test
    fun `setLocaleText applies the locale that changed while the view was detached`() {
        view.setLocaleText(R.string.general_yes)
        changeYesTranslation("Jah")

        parent.addView(view)

        assertEquals("Jah", view.text.toString())
        assertEquals(1, subscriptions)
    }

    @Test
    fun `setLocaleText replaces the previous listener of the same view`() {
        parent.addView(view)
        view.setLocaleText(R.string.general_yes)
        view.setLocaleText(R.string.general_no)

        changeYesTranslation("Jah")

        assertEquals("No", view.text.toString())
        assertEquals(1, subscriptions)
    }

    @Test
    fun `setText with null removes the previous listener`() {
        parent.addView(view)
        view.setLocaleText(R.string.general_yes)
        view.setText(null as LocaleString?)

        changeYesTranslation("Jah")

        assertEquals("", view.text.toString())
        assertEquals(0, subscriptions)
    }

    @Test
    fun `text and content description keep separate listeners`() {
        parent.addView(view)
        view.setLocaleText(R.string.general_yes)
        view.setLocaleContentDescription(R.string.general_no)

        changeYesTranslation("Jah")

        assertEquals("Jah", view.text.toString())
        assertEquals("No", view.contentDescription.toString())
        assertEquals(2, subscriptions)
    }

    private fun changeYesTranslation(translation: String) {
        yesTranslation = translation
        localeChanges.onNext("et")
    }
}
