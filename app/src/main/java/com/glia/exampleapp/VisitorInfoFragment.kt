package com.glia.exampleapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.glia.androidsdk.Glia
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.visitor.VisitorInfo
import com.glia.androidsdk.visitor.VisitorInfoUpdateRequest
import com.glia.widgets.helper.SimpleTextWatcher
import java.util.UUID

class VisitorInfoFragment : Fragment() {

    private lateinit var generatedNameTextView: TextView
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var noteEditText: EditText
    private lateinit var noteModeSwitch: SwitchCompat
    private lateinit var customAttributesModeSwitch: SwitchCompat
    private lateinit var saveButton: Button

    private val customAttributesAdapter = CustomAttributesAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.visitor_info_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        generatedNameTextView = view.findViewById(R.id.generated_name_text_view)
        nameEditText = view.findViewById(R.id.name_edit_text)
        emailEditText = view.findViewById(R.id.email_edit_text)
        phoneEditText = view.findViewById(R.id.phone_edit_text)
        noteEditText = view.findViewById(R.id.note_edit_text)
        noteModeSwitch = view.findViewById(R.id.note_mode_switch)
        customAttributesModeSwitch = view.findViewById(R.id.custom_attributes_mode_switch)

        view.findViewById<RecyclerView>(R.id.custom_attributes_recycler_view).apply {
            adapter = customAttributesAdapter
            isNestedScrollingEnabled = false
        }

        view.findViewById<View>(R.id.add_attribute_button).setOnClickListener {
            customAttributesAdapter.addAttribute()
        }

        view.findViewById<Button>(R.id.save_button).apply {
            saveButton = this
            setOnClickListener {
                saveVisitorInfo(obtainVisitorInfoUpdateRequest())
            }
        }

        getVisitorInfo()
    }

    private fun showVisitorInfo(visitorInfo: VisitorInfo) {
        generatedNameTextView.text = visitorInfo.generatedName
        nameEditText.setText(visitorInfo.name)
        emailEditText.setText(visitorInfo.email)
        phoneEditText.setText(visitorInfo.phone)
        noteEditText.setText(visitorInfo.note)
        customAttributesAdapter.setAttributes(visitorInfo.customAttributesMap)
    }

    private fun showError(exception: GliaException) {
        Toast.makeText(context, exception.message ?: exception.debugMessage, Toast.LENGTH_LONG).show()
    }

    private fun obtainVisitorInfoUpdateRequest(): VisitorInfoUpdateRequest {
        return VisitorInfoUpdateRequest.Builder()
            .setName(nameEditText.text.toString())
            .setEmail(emailEditText.text.toString())
            .setPhone(phoneEditText.text.toString())
            .setNote(noteEditText.text.toString())
            .setCustomAttributes(obtainCustomAttributes())
            .setCustomAttrsUpdateMethod(obtainCustomAttributesUpdateMethod())
            .setNoteUpdateMethod(obtainNoteUpdateMethod())
            .build()
    }

    private fun obtainNoteUpdateMethod() = if (noteModeSwitch.isChecked) {
        VisitorInfoUpdateRequest.NoteUpdateMethod.REPLACE
    } else {
        VisitorInfoUpdateRequest.NoteUpdateMethod.APPEND
    }

    private fun obtainCustomAttributesUpdateMethod() = if (customAttributesModeSwitch.isChecked) {
        VisitorInfoUpdateRequest.CustomAttributesUpdateMethod.REPLACE
    } else {
        VisitorInfoUpdateRequest.CustomAttributesUpdateMethod.MERGE
    }

    private fun obtainCustomAttributes(): Map<String, String> {
        return customAttributesAdapter.getAttributes()
    }

    private fun getVisitorInfo() {
        saveButton.text = getString(R.string.visitor_info_loading)
        Glia.getVisitorInfo { visitorInfo, error ->
            view?.post {
                saveButton.text = getString(R.string.visitor_info_save)
                visitorInfo?.let { showVisitorInfo(it) }
                error?.let { showError(it) }
            }
        }
    }

    private fun saveVisitorInfo(visitorInfo: VisitorInfoUpdateRequest) {
        saveButton.text = getString(R.string.visitor_info_saving)
        Glia.updateVisitorInfo(visitorInfo) { error ->
            view?.post {
                saveButton.text = getString(R.string.visitor_info_save)
                error?.let { showError(it) }
                getVisitorInfo()
            }
        }
    }
}

private class CustomAttributesAdapter: RecyclerView.Adapter<CustomAttributesAdapter.CustomAttributeViewHolder>() {
    private val attributes = mutableListOf<Attribute>()

    fun setAttributes(attributes: Map<String, String>) {
        this.attributes.clear()
        this.attributes.addAll(attributes.entries.map { Attribute(id = it.key, key = it.key, value = it.value) })
        notifyDataSetChanged()
    }

    fun getAttributes(): Map<String, String> {
        return attributes.associate { Pair(it.key, it.value) }
    }

    fun addAttribute() {
        val uniqueKey = generateNewUniqueKey()
        if (attributes.add(Attribute(id = uniqueKey, key = uniqueKey))) {
            notifyItemInserted(attributes.size - 1)
        }
    }

    private fun generateNewUniqueKey(): String {
        val ids = attributes.map { it.id }
        var keyIndex = 1
        var newKey: String
        do {
            newKey = "key$keyIndex"
            keyIndex += 1
        } while (ids.contains(newKey))
        return newKey
    }

    class CustomAttributeViewHolder(
        itemView: View,
        val keyEditText: EditText,
        val valueEditText: EditText,
        val deleteButton: Button
    ): RecyclerView.ViewHolder(itemView) {
        private var onTextChanged: (() -> Unit)? = null

        init {
            val textWatcher: TextWatcher = object : SimpleTextWatcher() {
                override fun afterTextChanged(editable: Editable) {
                    onTextChanged?.let { it() }
                }
            }
            keyEditText.addTextChangedListener(textWatcher)
            valueEditText.addTextChangedListener(textWatcher)
        }

        fun bind(
            attribute: Attribute,
            onItemChanged: ((Attribute) -> Unit),
            onItemRemove: ((Attribute) -> Unit)
        ) {
            onTextChanged = null
            keyEditText.setText(attribute.key)
            valueEditText.setText(attribute.value)
            deleteButton.setOnClickListener { onItemRemove(attribute) }
            onTextChanged = {
                val key = keyEditText.text.toString()
                val value = valueEditText.text.toString()
                if (key != attribute.key || value != attribute.value) {
                    onItemChanged(attribute.copy(key = key, value = value))
                }
            }

            itemView.contentDescription = attribute.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAttributeViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = layoutInflater.inflate(R.layout.custom_attribute_item, parent, false)
        return CustomAttributeViewHolder(
            itemView,
            itemView.findViewById(R.id.key_edit_text),
            itemView.findViewById(R.id.value_edit_text),
            itemView.findViewById(R.id.delete_button)
        )
    }

    override fun getItemCount(): Int {
        return attributes.count()
    }

    override fun onBindViewHolder(holder: CustomAttributeViewHolder, position: Int) {
        holder.bind(attributes[position],
            { attribute ->
                val index = attributes.indexOfFirst { it.id == attribute.id }
                if (index < 0) return@bind
                attributes.add(index, attribute)
                attributes.removeAt(index + 1)
            }, { attribute ->
                val index = attributes.indexOfFirst { it.id == attribute.id }
                if (index < 0) return@bind
                attributes.removeAt(index)
                notifyItemRemoved(index)
            })
    }

    data class Attribute(
        val id: String = UUID.randomUUID().toString(),
        val key: String = "",
        val value: String = ""
    )
}
