package com.glia.widgets.chat.adapter

import android.net.Uri
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.annotation.ColorRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.glia.widgets.R
import com.glia.widgets.databinding.ChatAttachmentUploadedItemBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.applyGliaThemeOverlays
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import io.reactivex.rxjava3.core.Observable
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * `updateExtensionType`, `updateTitleAndStatusText` and `setProgressIndicatorState` used to fall
 * back to hardcoded `R.color.glia_*` literals whenever there is no JSON theme, instead of the
 * composed `?attr/glia*` value - so a legacy overlay or `GliaTheme` customization changed every
 * other brand-coloured surface except this one. Locks the fix: the card background, the progress
 * indicator and the title text all follow the composed theme now.
 */
@RunWith(RobolectricTestRunner::class)
internal class UploadAttachmentAdapterTest {

    @Before
    fun setUp() {
        val localeProvider = mock<com.glia.widgets.locale.LocaleProvider>()
        whenever(localeProvider.getLocaleObservable()) doReturn Observable.never()
        whenever(localeProvider.getString(any<Int>())) doReturn "label"
        whenever(localeProvider.getString(any<Int>(), any<List<com.glia.widgets.locale.StringKeyPair>>())) doReturn "label"
        Dependencies.localeProvider = localeProvider
        Dependencies.gliaThemeManager.theme = null
    }

    private fun themed(@StyleRes customizationStyle: Int): android.content.Context =
        ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_Glia_Internal).also {
            it.applyGliaThemeOverlays(customizationStyle = customizationStyle)
        }

    private fun android.content.Context.color(@ColorRes colorRes: Int): Int = ContextCompat.getColor(this, colorRes)

    private fun viewHolder(context: android.content.Context): ViewHolder {
        val binding = ChatAttachmentUploadedItemBinding.inflate(LayoutInflater.from(context))
        return ViewHolder(binding, isMessageCenter = false)
    }

    private fun bind(holder: ViewHolder, status: LocalAttachment.Status): ChatAttachmentUploadedItemBinding {
        val attachment = LocalAttachment(
            uri = Uri.EMPTY,
            mimeType = "application/pdf",
            displayName = "Document.pdf",
            size = 1024,
            attachmentStatus = status
        )
        holder.onBind(attachment, callback = null)
        return bindingOf(holder)
    }

    private fun bindingOf(holder: ViewHolder): ChatAttachmentUploadedItemBinding {
        val field = ViewHolder::class.java.getDeclaredField("binding")
        field.isAccessible = true
        return field.get(holder) as ChatAttachmentUploadedItemBinding
    }

    @Test
    fun `extension card background follows the brand colour when customized`() {
        val context = themed(R.style.Test_Glia_Customization)
        val binding = bind(viewHolder(context), LocalAttachment.Status.READY_TO_SEND)

        assertEquals(
            context.color(R.color.glia_test_customization_brand),
            binding.typeIndicatorView.cardBackgroundColor.defaultColor
        )
    }

    @Test
    fun `progress indicator colour follows the brand colour when customized`() {
        val context = themed(R.style.Test_Glia_Customization)
        val binding = bind(viewHolder(context), LocalAttachment.Status.UPLOADING)

        assertEquals(
            context.color(R.color.glia_test_customization_brand),
            binding.progressIndicator.indicatorColor.first()
        )
    }

    @Test
    fun `title text colour follows the base normal colour when customized`() {
        val context = themed(R.style.Test_Glia_Customization_NormalColor)
        val binding = bind(viewHolder(context), LocalAttachment.Status.READY_TO_SEND)

        assertEquals(context.color(R.color.glia_test_customization_normal), binding.itemTitle.currentTextColor)
    }

    @Test
    fun `defaults resolve to the SDK colours when nothing is customized`() {
        val context = ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_Glia_Internal)
        val binding = bind(viewHolder(context), LocalAttachment.Status.UPLOADING)

        assertEquals(context.color(R.color.glia_primary_color), binding.progressIndicator.indicatorColor.first())
        assertEquals(context.color(R.color.glia_primary_color), binding.typeIndicatorView.cardBackgroundColor.defaultColor)
    }
}
