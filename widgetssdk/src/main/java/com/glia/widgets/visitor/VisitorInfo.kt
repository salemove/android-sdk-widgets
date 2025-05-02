package com.glia.widgets.visitor

/**
 * Provide Visitor information
 *
 * @see com.glia.widgets.GliaWidgets.getVisitorInfo()
 */
data class VisitorInfo(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val note: String? = null,
    val customAttributesMap: Map<String, String>,
    val generatedName: String? = null,
    val banned: Boolean? = null,
    val href: String? = null,
    val id: String? = null,
    val externalId: String? = null,
) {
    constructor(visitorInfo: com.glia.androidsdk.visitor.VisitorInfo) : this(
        visitorInfo.name,
        visitorInfo.email,
        visitorInfo.phone,
        visitorInfo.note,
        visitorInfo.customAttributesMap,
        visitorInfo.generatedName,
        visitorInfo.banned,
        visitorInfo.href,
        visitorInfo.id,
        visitorInfo.externalId
    )
}
