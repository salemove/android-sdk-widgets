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
    composeIfAtLeastOneNotNull(darkColorTheme, shadeColorTheme, neutralColorTheme) {
        val attachmentItem = AttachmentItemTheme(
            text = BaseDarkColorTextTheme(this),
            iconColor = darkColorTheme
        )
        AttachmentsPopupTheme(
            photoLibrary = attachmentItem,
            takePhoto = attachmentItem,
            browse = attachmentItem,
            dividerColor = shadeColorTheme,
            background = neutralColorTheme
        )
    }
}

/**
 * Default theme for File upload bar item
 */
internal fun DefaultUploadFileTheme(pallet: ColorPallet): UploadFileTheme? = pallet.run {
    composeIfAtLeastOneNotNull(normalColorTheme, darkColorTheme) {
        UploadFileTheme(text = BaseNormalColorTextTheme(pallet), info = BaseDarkColorTextTheme(pallet))
    }
}
