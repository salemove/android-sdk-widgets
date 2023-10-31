package com.glia.widgets.chat.adapter

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.view.unifiedui.applyButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.google.android.material.button.MaterialButton

internal class GvaButtonsAdapter(
    private val buttonsClickListener: ChatAdapter.OnGvaButtonsClickListener,
    private val uiTheme: UiTheme,
    private val buttonTheme: ButtonTheme?
) : RecyclerView.Adapter<GvaButtonsAdapter.ButtonViewHolder>() {
    private var options: List<GvaButton>? = null

    fun setOptions(options: List<GvaButton>) {
        this.options = options
        notifyDataSetChanged()
    }

    class ButtonViewHolder(
        private val buttonView: MaterialButton
    ) : RecyclerView.ViewHolder(buttonView) {
        fun bind(button: GvaButton, buttonsClickListener: ChatAdapter.OnGvaButtonsClickListener) {
            buttonView.contentDescription = button.text
            buttonView.text = button.text
            buttonView.setOnClickListener {
                buttonsClickListener.onGvaButtonClicked(button)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val styleResId = Utils.getAttrResourceId(parent.context, R.attr.gvaOptionButtonStyle)
        val button = MaterialButton(ContextThemeWrapper(parent.context, styleResId), null, 0).also {
            it.id = View.generateViewId()

            uiTheme.fontRes?.let(parent::getFontCompat)?.also(it::setTypeface)

            buttonTheme?.also(it::applyButtonTheme)
        }
        button.layoutParams = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        return ButtonViewHolder(button)
    }

    override fun getItemCount(): Int = options?.size ?: 0

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        options?.get(position)?.let { holder.bind(it, buttonsClickListener) }
    }
}
