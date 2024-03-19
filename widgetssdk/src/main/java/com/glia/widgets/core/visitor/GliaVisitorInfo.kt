package com.glia.widgets.core.visitor

import com.glia.androidsdk.visitor.VisitorInfo
import com.glia.widgets.helper.Logger.logDeprecatedClassUse
import com.glia.widgets.helper.TAG
import com.google.gson.internal.LinkedTreeMap

/**
 * Deprecated. Use [VisitorInfo] instead
 */
@Deprecated("since 1.9.0 use @see {@link VisitorInfo}")
class GliaVisitorInfo(visitorInfo: VisitorInfo) : VisitorInfo {
    private var name: String
    private var email: String
    private var phone: String
    private var note: String
    private var customAttributes: Map<String, String>
    private var generatedName: String
    private var banned: Boolean
    private var href: String
    private var id: String
    private var externalId: String?

    init {
        logDeprecatedClassUse(TAG)
        name = visitorInfo.name
        email = visitorInfo.email
        phone = visitorInfo.phone
        note = visitorInfo.note
        customAttributes = visitorInfo.customAttributesMap
        generatedName = visitorInfo.generatedName
        banned = visitorInfo.banned
        href = visitorInfo.href
        id = visitorInfo.id
        externalId = visitorInfo.externalId
    }

    override fun getName(): String {
        return name
    }

    override fun getEmail(): String {
        return email
    }

    override fun getPhone(): String {
        return phone
    }

    override fun getNote(): String {
        return note
    }

    /** @deprecated */
    @Deprecated("Will be removed")
    @Suppress("removal")
    override fun getCustomAttributes(): LinkedTreeMap<String, String> {
        val attrs = LinkedTreeMap<String, String>()
        attrs.putAll(customAttributes)
        return attrs
    }

    override fun getCustomAttributesMap(): Map<String, String> {
        return customAttributes
    }

    override fun getGeneratedName(): String {
        return generatedName
    }

    override fun getBanned(): Boolean {
        return banned
    }

    override fun getHref(): String {
        return href
    }

    override fun getId(): String {
        return id
    }

    override fun getExternalId(): String? {
        return externalId
    }
}
