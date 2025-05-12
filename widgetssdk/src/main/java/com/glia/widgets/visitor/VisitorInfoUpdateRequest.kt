package com.glia.widgets.visitor

import com.glia.androidsdk.visitor.VisitorInfoUpdateRequest

/**
 * Object that can be used for updating the visitor's information on the backend.
 * <p>
 *
 * @see {@link GliaWidgets#updateVisitorInfo(VisitorInfoUpdateRequest, OnWidgetsSuccess, OnWidgetsError)}
 */
data class VisitorInfoUpdateRequest(
    var name: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var externalId: String? = null,
    var note: String? = null,
    var noteUpdateMethod: NoteUpdateMethod? = null,
    var customAttributes: Map<String, String>? = null,
    var customAttrsUpdateMethod: CustomAttributesUpdateMethod? = null
) {

    internal fun toCoreType(): VisitorInfoUpdateRequest {
        return VisitorInfoUpdateRequest.Builder()
            .setName(name)
            .setEmail(email)
            .setPhone(phone)
            .setExternalId(externalId)
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
