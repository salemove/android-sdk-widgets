package com.glia.widgets.chat

import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.view.ViewCompat
import com.glia.widgets.di.Dependencies
import io.reactivex.rxjava3.core.Observable
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
internal class AttachmentPopupTest {

    private lateinit var popup: AttachmentPopup

    @Before
    fun setUp() {
        val localeProvider = mock<com.glia.widgets.locale.LocaleProvider>()
        whenever(localeProvider.getLocaleObservable()) doReturn Observable.never()
        whenever(localeProvider.getString(any<Int>(), any<List<com.glia.widgets.locale.StringKeyPair>>())) doReturn "label"
        Dependencies.localeProvider = localeProvider

        val context = RuntimeEnvironment.getApplication()
        popup = AttachmentPopup(context, null) { view ->
            view.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            mock<PopupWindow>()
        }
        popup.show(android.view.View(context), {}, {}, {})
    }

    @Test
    fun `photoLibraryItem has contentDescription set after show`() {
        val binding = popup.getBindingForTest()
        assertTrue(binding.photoLibraryItem.contentDescription?.isNotEmpty() == true)
    }

    @Test
    fun `photoItem has contentDescription set after show`() {
        val binding = popup.getBindingForTest()
        assertTrue(binding.photoItem.contentDescription?.isNotEmpty() == true)
    }

    @Test
    fun `browseItem has contentDescription set after show`() {
        val binding = popup.getBindingForTest()
        assertTrue(binding.browseItem.contentDescription?.isNotEmpty() == true)
    }

    @Test
    fun `photoLibraryItem has accessibility delegate set`() {
        val binding = popup.getBindingForTest()
        assertNotNull(ViewCompat.getAccessibilityDelegate(binding.photoLibraryItem))
    }

    @Test
    fun `photoItem has accessibility delegate set`() {
        val binding = popup.getBindingForTest()
        assertNotNull(ViewCompat.getAccessibilityDelegate(binding.photoItem))
    }

    @Test
    fun `browseItem has accessibility delegate set`() {
        val binding = popup.getBindingForTest()
        assertNotNull(ViewCompat.getAccessibilityDelegate(binding.browseItem))
    }
}

// Test helper to expose binding — used only in tests
private fun AttachmentPopup.getBindingForTest(): com.glia.widgets.databinding.ChatAttachmentPopupBinding {
    val field = AttachmentPopup::class.java.getDeclaredField("binding\$delegate")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val lazy = field.get(this) as Lazy<com.glia.widgets.databinding.ChatAttachmentPopupBinding>
    return lazy.value
}
