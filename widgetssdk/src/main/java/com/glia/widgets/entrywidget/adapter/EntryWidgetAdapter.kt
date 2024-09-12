package com.glia.widgets.entrywidget.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.databinding.EntryWidgetErrorItemBinding
import com.glia.widgets.databinding.EntryWidgetMediaTypeItemBinding
import com.glia.widgets.databinding.EntryWidgetPoweredByItemBinding
import com.glia.widgets.entrywidget.EntryWidgetContract
import com.glia.widgets.helper.layoutInflater

internal class EntryWidgetAdapter(
    private val viewType: EntryWidgetContract.ViewType
) : RecyclerView.Adapter<EntryWidgetAdapter.ViewHolder>() {

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
                viewType = this.viewType
            )
            ViewType.PROVIDED_BY_ITEM.ordinal -> EntryWidgetPoweredByViewHolder(
                EntryWidgetPoweredByItemBinding.inflate(parent.layoutInflater, parent, false)
            )
            else -> EntryWidgetMediaTypeItemViewHolder(
                EntryWidgetMediaTypeItemBinding.inflate(parent.layoutInflater, parent, false)
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
