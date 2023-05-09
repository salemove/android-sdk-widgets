@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.chat.AttachmentItemTheme
import com.glia.widgets.view.unifiedui.theme.chat.AttachmentsPopupTheme

/**
 * Default theme for Attachments popup
 */
internal fun DefaultAttachmentsPopupTheme(pallet: ColorPallet): AttachmentsPopupTheme? =
    pallet.run {
        composeIfAtLeastOneNotNull(baseDarkColorTheme, baseShadeColorTheme) {
            val attachmentItem = AttachmentItemTheme(
                text = BaseDarkColorTextTheme(this),
                iconColor = baseDarkColorTheme
            )
            AttachmentsPopupTheme(
                photoLibrary = attachmentItem,
                takePhoto = attachmentItem,
                browse = attachmentItem,
                dividerColor = baseShadeColorTheme
            )
        }
    }
