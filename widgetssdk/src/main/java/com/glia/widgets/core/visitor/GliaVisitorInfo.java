package com.glia.widgets.core.visitor;

import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.visitor.VisitorInfo;
import com.google.gson.internal.LinkedTreeMap;

import java.util.Map;

/**
 * @deprecated since 1.9.0 use @see {@link VisitorInfo}
 */
@Deprecated
public class GliaVisitorInfo implements VisitorInfo {
    public String name;
    public String email;
    public String phone;
    public String note;
    public Map<String, String> customAttributes;
    public String generatedName;
    public boolean banned;
    public String href;
    public String id;
    public String externalId;

    public GliaVisitorInfo(VisitorInfo visitorInfo) {
        this.name = visitorInfo.getName();
        this.email = visitorInfo.getEmail();
        this.phone = visitorInfo.getPhone();
        this.note = visitorInfo.getNote();
        this.customAttributes = visitorInfo.getCustomAttributesMap();
        this.generatedName = visitorInfo.getGeneratedName();
        this.banned = visitorInfo.getBanned();
        this.href = visitorInfo.getHref();
        this.id = visitorInfo.getId();
        this.externalId = visitorInfo.getExternalId();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    @Override
    public String getNote() {
        return note;
    }

    @Override
    public LinkedTreeMap<String, String> getCustomAttributes() {
        LinkedTreeMap<String, String> attrs = new LinkedTreeMap<>();
        attrs.putAll(customAttributes);
        return attrs;
    }

    @Override
    public Map<String, String> getCustomAttributesMap() {
        return customAttributes;
    }

    @Override
    public String getGeneratedName() {
        return generatedName;
    }

    @Override
    public Boolean getBanned() {
        return banned;
    }

    @Override
    public String getHref() {
        return href;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getExternalId() {
        return externalId;
    }
}
