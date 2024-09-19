package com.glia.widgets.entrywidget.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.databinding.EntryWidgetErrorItemBinding
import com.glia.widgets.databinding.EntryWidgetMediaTypeItemBinding
import com.glia.widgets.databinding.EntryWidgetPoweredByItemBinding
import com.glia.widgets.entrywidget.EntryWidgetContract
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.entrywidget.EntryWidgetTheme
import com.glia.widgets.view.unifiedui.theme.entrywidget.MediaTypeItemsTheme

internal class EntryWidgetAdapter(
    private val viewType: EntryWidgetContract.ViewType,
    private val mediaTypeItemsTheme: MediaTypeItemsTheme? = null,
    private val errorTitleTheme: TextTheme? = null,
    private val errorMessageTheme: TextTheme? = null,
    private val errorButtonTheme: ButtonTheme? = null
) : RecyclerView.Adapter<EntryWidgetAdapter.ViewHolder>() {

    constructor(viewType: EntryWidgetContract.ViewType, entryWidgetTheme: EntryWidgetTheme?): this(
        viewType,
        entryWidgetTheme?.mediaTypeItems,
        entryWidgetTheme?.errorTitle,
        entryWidgetTheme?.errorMessage,
        entryWidgetTheme?.errorButton
    )

    enum class ViewType {
        CONTACT_ITEM,
        ERROR_ITEM,
        PROVIDED_BY_ITEM
    }

    var items: List<EntryWidgetContract.ItemType>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onItemClickListener: ((EntryWidgetContract.ItemType) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            ViewType.ERROR_ITEM.ordinal -> EntryWidgetErrorStateViewHolder(
                EntryWidgetErrorItemBinding.inflate(parent.layoutInflater, parent, false),
                viewType = this.viewType,
                errorTitleTheme = errorTitleTheme,
                errorMessageTheme = errorMessageTheme,
                errorButtonTheme = errorButtonTheme
            )
            ViewType.PROVIDED_BY_ITEM.ordinal -> EntryWidgetPoweredByViewHolder(
                EntryWidgetPoweredByItemBinding.inflate(parent.layoutInflater, parent, false)
            )
            else -> EntryWidgetMediaTypeItemViewHolder(
                EntryWidgetMediaTypeItemBinding.inflate(parent.layoutInflater, parent, false),
                itemTheme = mediaTypeItemsTheme?.mediaTypeItem
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items?.get(position)?.let { item ->
            holder.bind(item) {
                onItemClickListener?.invoke(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items?.get(position)) {
            EntryWidgetContract.ItemType.EMPTY_STATE,
            EntryWidgetContract.ItemType.ERROR_STATE -> ViewType.ERROR_ITEM.ordinal
            EntryWidgetContract.ItemType.PROVIDED_BY -> ViewType.PROVIDED_BY_ITEM.ordinal
            else -> ViewType.CONTACT_ITEM.ordinal
        }
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        open fun bind(itemType: EntryWidgetContract.ItemType, onClickListener: View.OnClickListener) {}
    }

}
