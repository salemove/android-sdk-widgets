package com.glia.widgets.core.visitor;

import com.glia.androidsdk.visitor.VisitorInfoUpdateRequest;
import com.glia.widgets.helper.Logger;
import com.google.gson.internal.LinkedTreeMap;

import java.util.Map;

/**
 * @deprecated since 1.9.0 use @see {@link VisitorInfoUpdateRequest}
 */
@Deprecated
public class VisitorInfoUpdate implements VisitorInfoUpdateRequest {
    private final String TAG = VisitorInfoUpdate.class.getSimpleName();

    public String name;
    public String email;
    public String phone;
    public String note;
    public Map<String, String> customAttributes;
    public VisitorInfoUpdateRequest.NoteUpdateMethod noteUpdateMethod = VisitorInfoUpdateRequest.NoteUpdateMethod.APPEND;
    public VisitorInfoUpdateRequest.CustomAttributesUpdateMethod customAttrsUpdateMethod = VisitorInfoUpdateRequest.CustomAttributesUpdateMethod.MERGE;
    public String externalId;

    public VisitorInfoUpdate() {
        Logger.logDeprecatedClassUse(TAG);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setCustomAttributes(Map<String, String> customAttributes) {
        this.customAttributes = customAttributes;
    }

    public void setNoteUpdateMethod(NoteUpdateMethod noteUpdateMethod) {
        this.noteUpdateMethod = noteUpdateMethod;
    }

    public void setCustomAttrsUpdateMethod(CustomAttributesUpdateMethod customAttrsUpdateMethod) {
        this.customAttrsUpdateMethod = customAttrsUpdateMethod;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
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
    public NoteUpdateMethod getNoteUpdateMethod() {
        return noteUpdateMethod;
    }

    @Override
    public CustomAttributesUpdateMethod getCustomAttrsUpdateMethod() {
        return customAttrsUpdateMethod;
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
    public String getExternalId() {
        return externalId;
    }
}
