package com.glia.widgets

import android.os.Parcelable
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import kotlinx.parcelize.Parcelize

@Deprecated("Feature was removed. It's for internal use only now.")
@Parcelize
data class StringKeyPair(val key: StringKey, val value: String) : Parcelable {
    init {
        Logger.logDeprecatedClassUse(TAG)
    }
}

@Deprecated("Feature was removed. It's for internal use only now.")
enum class StringKey(val value: String) {
    @Deprecated("Feature was removed. It's for internal use only now.")
    COMPANY_NAME("companyName"),

    @Deprecated("Feature was removed. It's for internal use only now.")
    OPERATOR_NAME("operatorName"),

    @Deprecated("Feature was removed. It's for internal use only now.")
    MESSAGE("message"),

    @Deprecated("Feature was removed. It's for internal use only now.")
    NAME("name"),

    @Deprecated("Feature was removed. It's for internal use only now.")
    SIZE("size"),

    @Deprecated("Feature was removed. It's for internal use only now.")
    NUMBER("number"),

    @Deprecated("Feature was removed. It's for internal use only now.")
    STATUS("status"),

    @Deprecated("Feature was removed. It's for internal use only now.")
    VISITOR_CODE("visitorCode"),

    @Deprecated("Feature was removed. It's for internal use only now.")
    BADGE_VALUE("badgeValue"),

    @Deprecated("Feature was removed. It's for internal use only now.")
    FILE_SENDER("fileSender"),

    @Deprecated("Feature was removed. It's for internal use only now.")
    BUTTON_TITLE("buttonTitle");

    init {
        Logger.logDeprecatedClassUse(TAG)
    }
}
