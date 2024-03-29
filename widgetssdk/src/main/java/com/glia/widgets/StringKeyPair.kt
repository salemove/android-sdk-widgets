package com.glia.widgets

/**
 * Retrieve custom locales strings that should have placeholders replaced with real values.
 */
data class StringKeyPair(val key: StringKey, val value: String)

/**
 * List placeholders for [StringKeyPair].
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
    BUTTON_TITLE("buttonTitle")
}
