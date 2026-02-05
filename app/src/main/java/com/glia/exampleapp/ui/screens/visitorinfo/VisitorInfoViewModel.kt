package com.glia.exampleapp.ui.screens.visitorinfo

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.glia.widgets.GliaWidgets
import com.glia.widgets.callbacks.OnComplete
import com.glia.widgets.callbacks.OnError
import com.glia.widgets.visitor.VisitorInfoUpdateRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NotesMode { REPLACE, APPEND }
enum class CustomAttributesMode { REPLACE, MERGE }

data class CustomAttribute(
    val key: String = "",
    val value: String = ""
)

data class VisitorInfoUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val externalId: String = "",
    val notes: String = "",
    val notesMode: NotesMode = NotesMode.REPLACE,
    val customAttributes: List<CustomAttribute> = emptyList(),
    val customAttributesMode: CustomAttributesMode = CustomAttributesMode.REPLACE,
    val hasChanges: Boolean = false,
    val isSaving: Boolean = false
)

class VisitorInfoViewModel(
    private val applicationContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisitorInfoUiState())
    val uiState: StateFlow<VisitorInfoUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, hasChanges = true)
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, hasChanges = true)
    }

    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone, hasChanges = true)
    }

    fun updateExternalId(externalId: String) {
        _uiState.value = _uiState.value.copy(externalId = externalId, hasChanges = true)
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes, hasChanges = true)
    }

    fun updateNotesMode(mode: NotesMode) {
        _uiState.value = _uiState.value.copy(notesMode = mode)
    }

    fun updateCustomAttributesMode(mode: CustomAttributesMode) {
        _uiState.value = _uiState.value.copy(customAttributesMode = mode)
    }

    fun addCustomAttribute() {
        val currentAttributes = _uiState.value.customAttributes.toMutableList()
        currentAttributes.add(CustomAttribute())
        _uiState.value = _uiState.value.copy(
            customAttributes = currentAttributes,
            hasChanges = true
        )
    }

    fun updateCustomAttributeKey(index: Int, key: String) {
        val currentAttributes = _uiState.value.customAttributes.toMutableList()
        if (index in currentAttributes.indices) {
            currentAttributes[index] = currentAttributes[index].copy(key = key)
            _uiState.value = _uiState.value.copy(
                customAttributes = currentAttributes,
                hasChanges = true
            )
        }
    }

    fun updateCustomAttributeValue(index: Int, value: String) {
        val currentAttributes = _uiState.value.customAttributes.toMutableList()
        if (index in currentAttributes.indices) {
            currentAttributes[index] = currentAttributes[index].copy(value = value)
            _uiState.value = _uiState.value.copy(
                customAttributes = currentAttributes,
                hasChanges = true
            )
        }
    }

    fun removeCustomAttribute(index: Int) {
        val currentAttributes = _uiState.value.customAttributes.toMutableList()
        if (index in currentAttributes.indices) {
            currentAttributes.removeAt(index)
            _uiState.value = _uiState.value.copy(
                customAttributes = currentAttributes,
                hasChanges = true
            )
        }
    }

    fun save(onSuccess: () -> Unit) {
        if (!GliaWidgets.isInitialized()) {
            showToast("SDK not initialized")
            return
        }

        _uiState.value = _uiState.value.copy(isSaving = true)

        val state = _uiState.value

        // Build custom attributes map
        val customAttrsMap = state.customAttributes
            .filter { it.key.isNotBlank() }
            .associate { it.key to it.value }

        // Determine update methods
        val noteUpdateMethod = when (state.notesMode) {
            NotesMode.REPLACE -> VisitorInfoUpdateRequest.NoteUpdateMethod.REPLACE
            NotesMode.APPEND -> VisitorInfoUpdateRequest.NoteUpdateMethod.APPEND
        }

        val customAttrsUpdateMethod = when (state.customAttributesMode) {
            CustomAttributesMode.REPLACE -> VisitorInfoUpdateRequest.CustomAttributesUpdateMethod.REPLACE
            CustomAttributesMode.MERGE -> VisitorInfoUpdateRequest.CustomAttributesUpdateMethod.MERGE
        }

        val request = VisitorInfoUpdateRequest(
            name = state.name.ifBlank { null },
            email = state.email.ifBlank { null },
            phone = state.phone.ifBlank { null },
            externalId = state.externalId.ifBlank { null },
            note = state.notes.ifBlank { null },
            customAttributes = customAttrsMap.ifEmpty { null },
            noteUpdateMethod = noteUpdateMethod,
            customAttrsUpdateMethod = customAttrsUpdateMethod
        )

        val onComplete = OnComplete {
            _uiState.value = _uiState.value.copy(isSaving = false, hasChanges = false)
            showToast("Visitor info updated")
            onSuccess()
        }

        val onError = OnError { exception ->
            _uiState.value = _uiState.value.copy(isSaving = false)
            showToast("Error: ${exception?.message ?: "Unknown error"}")
        }

        GliaWidgets.updateVisitorInfo(request, onComplete, onError)
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    class Factory(
        private val applicationContext: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VisitorInfoViewModel(applicationContext) as T
        }
    }
}
