package com.glia.widgets.core.visitor;

import com.glia.androidsdk.visitor.VisitorInfoUpdateRequest;
import com.google.gson.internal.LinkedTreeMap;

public class VisitorInfoUpdate implements VisitorInfoUpdateRequest {

    public String name;
    public String email;
    public String phone;
    public String note;
    public LinkedTreeMap<String, String> customAttributes;
    public VisitorInfoUpdateRequest.NoteUpdateMethod noteUpdateMethod = VisitorInfoUpdateRequest.NoteUpdateMethod.APPEND;
    public VisitorInfoUpdateRequest.CustomAttributesUpdateMethod customAttrsUpdateMethod = VisitorInfoUpdateRequest.CustomAttributesUpdateMethod.MERGE;

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

    public void setCustomAttributes(LinkedTreeMap<String, String> customAttributes) {
        this.customAttributes = customAttributes;
    }

    public void setNoteUpdateMethod(NoteUpdateMethod noteUpdateMethod) {
        this.noteUpdateMethod = noteUpdateMethod;
    }

    public void setCustomAttrsUpdateMethod(CustomAttributesUpdateMethod customAttrsUpdateMethod) {
        this.customAttrsUpdateMethod = customAttrsUpdateMethod;
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
        return customAttributes;
    }
}
