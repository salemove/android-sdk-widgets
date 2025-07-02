package com.glia.widgets.locale

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @hide
 */
@Parcelize
data class StringKeyPair(val key: StringKey, val value: String) : Parcelable {
    constructor(deprecatedStringKeyPair: com.glia.widgets.StringKeyPair) : this(
        StringKey.from(deprecatedStringKeyPair.key),
        deprecatedStringKeyPair.value
    )
}

/**
 * @hide
 */
enum class StringKey(val value: String) {
    COMPANY_NAME("companyName"),
    OPERATOR_NAME("operatorName"),
    MESSAGE("message"),
    NAME("name"),
    SIZE("size"),
    NUMBER("number"),
    STATUS("status"),
    VISITOR_CODE("visitorCode"),
    BADGE_VALUE("badgeValue"),
    FILE_SENDER("fileSender"),
    BUTTON_TITLE("buttonTitle");

    /**
     * @hide
     */
    companion object {
        fun from(deprecatedStringKey: com.glia.widgets.StringKey): StringKey {
            return when (deprecatedStringKey) {
                com.glia.widgets.StringKey.COMPANY_NAME -> COMPANY_NAME
                com.glia.widgets.StringKey.OPERATOR_NAME -> OPERATOR_NAME
                com.glia.widgets.StringKey.MESSAGE -> MESSAGE
                com.glia.widgets.StringKey.NAME -> NAME
                com.glia.widgets.StringKey.SIZE -> SIZE
                com.glia.widgets.StringKey.NUMBER -> NUMBER
                com.glia.widgets.StringKey.STATUS -> STATUS
                com.glia.widgets.StringKey.VISITOR_CODE -> VISITOR_CODE
                com.glia.widgets.StringKey.BADGE_VALUE -> BADGE_VALUE
                com.glia.widgets.StringKey.FILE_SENDER -> FILE_SENDER
                com.glia.widgets.StringKey.BUTTON_TITLE -> BUTTON_TITLE
            }
        }
    }
}
