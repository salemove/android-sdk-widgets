@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.chat.AttachmentItemTheme
import com.glia.widgets.view.unifiedui.theme.chat.AttachmentsPopupTheme
import com.glia.widgets.view.unifiedui.theme.chat.UploadFileTheme

/**
 * Default theme for Attachments popup
 */
internal fun DefaultAttachmentsPopupTheme(pallet: ColorPallet): AttachmentsPopupTheme? = pallet.run {
    composeIfAtLeastOneNotNull(baseDarkColorTheme, baseShadeColorTheme, baseNeutralColorTheme) {
        val attachmentItem = AttachmentItemTheme(
            text = BaseDarkColorTextTheme(this),
            iconColor = baseDarkColorTheme
        )
        AttachmentsPopupTheme(
            photoLibrary = attachmentItem,
            takePhoto = attachmentItem,
            browse = attachmentItem,
            dividerColor = baseShadeColorTheme,
            background = baseNeutralColorTheme
        )
    }
}

/**
 * Default theme for File upload bar item
 */
internal fun DefaultUploadFileTheme(pallet: ColorPallet): UploadFileTheme? = pallet.run {
    composeIfAtLeastOneNotNull(baseNormalColorTheme, baseDarkColorTheme) {
        UploadFileTheme(text = BaseNormalColorTextTheme(pallet), info = BaseDarkColorTextTheme(pallet))
    }
}
