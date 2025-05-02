package com.glia.widgets.visitor

import com.glia.androidsdk.visitor.VisitorInfoUpdateRequest

/**
 * Object that can be used for updating the visitor's information on the backend.
 * <p>
 *
 * @see {@link GliaWidgets#updateVisitorInfo(VisitorInfoUpdateRequest, OnWidgetsSuccess, OnWidgetsError)}
 */
data class VisitorInfoUpdateRequest(
    val name: String?,
    val email: String?,
    val phone: String?,
    val note: String?,
    val noteUpdateMethod: NoteUpdateMethod?,
    val customAttributes: Map<String, String>?,
    val customAttrsUpdateMethod: CustomAttributesUpdateMethod?
) {

    internal fun toCoreType(): VisitorInfoUpdateRequest {
        return VisitorInfoUpdateRequest.Builder()
            .setName(name)
            .setEmail(email)
            .setPhone(phone)
            .setNote(note)
            .setCustomAttributes(customAttributes)
            .setCustomAttrsUpdateMethod(customAttrsUpdateMethod?.toCoreType())
            .setNoteUpdateMethod(noteUpdateMethod?.toCoreType())
            .build()
    }

    /**
     * Specifies a method for updating the visitor's note.
     */
    enum class NoteUpdateMethod {
        /**
         * The notes for the visitor will be overwritten with the specified in the request
         */
        REPLACE,

        /**
         * The line break (\n) will be added and specified in the request notes will be appended to the existing
         * visitor’s notes.
         */
        APPEND;

        internal fun toCoreType(): VisitorInfoUpdateRequest.NoteUpdateMethod? {
            return com.glia.androidsdk.visitor.VisitorInfoUpdateRequest.NoteUpdateMethod
                .entries.firstOrNull { it.name == name }
        }
    }

    /**
     * Specifies the method for updating custom attributes.
     */
    enum class CustomAttributesUpdateMethod {
        /**
         * All custom attributes for the visitor will be overwritten with the specified in the request
         */
        REPLACE,

        /**
         * Only custom attributes present in the request will be added or updated. In case of merge it is
         * possible to remove a custom attribute by setting its value to `null`.
         */
        MERGE;

        internal fun toCoreType(): VisitorInfoUpdateRequest.CustomAttributesUpdateMethod? {
            return com.glia.androidsdk.visitor.VisitorInfoUpdateRequest.CustomAttributesUpdateMethod
                .entries.firstOrNull { it.name == name }
        }
    }
}
