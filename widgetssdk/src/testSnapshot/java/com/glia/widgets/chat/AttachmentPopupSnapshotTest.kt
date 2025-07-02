package com.glia.widgets.chat

import android.view.View
import android.widget.LinearLayout
import com.glia.widgets.SnapshotTest
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.snapshotutils.SnapshotTheme
import com.glia.widgets.view.unifiedui.theme.chat.AttachmentsPopupTheme
import org.junit.Test
import org.mockito.kotlin.mock

internal class AttachmentPopupSnapshotTest : SnapshotTest(), SnapshotTheme, SnapshotProviders {

    // MARK: Tests

    @Test
    fun defaultTheme() {
        setupView {
            snapshot(it)
        }
    }

    @Test
    fun withGlobalColors() {
        setupView(
            unifiedThemeWithGlobalColors().chatTheme?.attachmentsPopup
        ) {
            snapshot(it)
        }
    }

    @Test
    fun chatWithUnifiedTheme() {
        setupView(
            unifiedTheme().chatTheme?.attachmentsPopup
        ) {
            snapshot(it)
        }
    }

    @Test
    fun secureMessagingWithUnifiedTheme() {
        setupView(
            unifiedTheme().secureMessagingWelcomeScreenTheme?.pickMediaTheme
        ) {
            snapshot(it)
        }
    }

    // MARK: utils for tests

    private fun setupView(
        unifiedTheme: AttachmentsPopupTheme? = null,
        viewCallback: (View) -> Unit
    ): AttachmentPopup {

        return AttachmentPopup(
            context,
            unifiedTheme
        ) {
            it.layoutParams = LinearLayout.LayoutParams(650, LinearLayout.LayoutParams.WRAP_CONTENT)
            viewCallback(it)
            mock()
        }.apply {
            show(View(context), mock(), mock(), mock())
        }
    }
}
