package com.glia.widgets.entrywidget.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.core.secureconversations.domain.ObserveUnreadMessagesCountUseCase
import com.glia.widgets.databinding.EntryWidgetAuthenticatedContactBinding
import com.glia.widgets.databinding.EntryWidgetErrorItemBinding
import com.glia.widgets.databinding.EntryWidgetPoweredByItemBinding
import com.glia.widgets.databinding.EntryWidgetUnauthenticatedContactBinding
import com.glia.widgets.entrywidget.EntryWidgetContract
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.entrywidget.EntryWidgetTheme
import com.glia.widgets.view.unifiedui.theme.entrywidget.MediaTypeItemsTheme
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

internal class EntryWidgetAdapter(
    val viewType: EntryWidgetContract.ViewType,
    private val mediaTypeItemsTheme: MediaTypeItemsTheme? = null,
    private val errorTitleTheme: TextTheme? = null,
    private val errorMessageTheme: TextTheme? = null,
    private val errorButtonTheme: ButtonTheme? = null,
    private val observeUnreadMessagesCountUseCase: ObserveUnreadMessagesCountUseCase
) : ListAdapter<EntryWidgetContract.ItemType, EntryWidgetAdapter.ViewHolder>(DIFF_CALLBACK) {

    constructor(
        viewType: EntryWidgetContract.ViewType,
        entryWidgetTheme: EntryWidgetTheme?,
        observeUnreadMessagesCountUseCase: ObserveUnreadMessagesCountUseCase
    ) : this(
        viewType,
        entryWidgetTheme?.mediaTypeItems,
        entryWidgetTheme?.errorTitle,
        entryWidgetTheme?.errorMessage,
        entryWidgetTheme?.errorButton,
        observeUnreadMessagesCountUseCase
    )

    init {
        when (viewType) {
            EntryWidgetContract.ViewType.BOTTOM_SHEET,
            EntryWidgetContract.ViewType.EMBEDDED_VIEW -> Logger.d(TAG, "Creating an EntryWidget for $viewType")

            else -> { /* No need to send a log */
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<EntryWidgetContract.ItemType>() {
            override fun areItemsTheSame(
                oldItem: EntryWidgetContract.ItemType,
                newItem: EntryWidgetContract.ItemType
            ): Boolean {
                // Whether items are the same
                return oldItem.ordinal == newItem.ordinal
            }

            override fun areContentsTheSame(
                oldItem: EntryWidgetContract.ItemType,
                newItem: EntryWidgetContract.ItemType
            ): Boolean {
                // Whether content is the same
                return oldItem.ordinal == newItem.ordinal
            }
        }
    }

    enum class ViewType {
        AUTHENTICATED_CONTACT_ITEM,
        UNAUTHENTICATED_CONTACT_ITEM,
        ERROR_ITEM,
        PROVIDED_BY_ITEM
    }

    var onItemClickListener: ((EntryWidgetContract.ItemType) -> Unit)? = null
    val disposable: CompositeDisposable = CompositeDisposable()

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
            ViewType.AUTHENTICATED_CONTACT_ITEM.ordinal -> EntryWidgetAuthenticatedContactViewHolder(
                EntryWidgetAuthenticatedContactBinding.inflate(parent.layoutInflater, parent, false),
                itemTheme = mediaTypeItemsTheme?.mediaTypeItem
            )
            else -> EntryWidgetUnauthenticatedContactViewHolder(
                EntryWidgetUnauthenticatedContactBinding.inflate(parent.layoutInflater, parent, false),
                itemTheme = mediaTypeItemsTheme?.mediaTypeItem
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { item ->
            holder.bind(item) {
                onItemClickListener?.invoke(item)
            }
        }

        if (holder is EntryWidgetAuthenticatedContactViewHolder) {
            disposable.add(observeUnreadMessagesCountUseCase()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { count ->
                    holder.updateUnreadMessageCount(count)
                })
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            EntryWidgetContract.ItemType.EMPTY_STATE,
            EntryWidgetContract.ItemType.SDK_NOT_INITIALIZED_STATE,
            EntryWidgetContract.ItemType.ERROR_STATE -> ViewType.ERROR_ITEM.ordinal
            EntryWidgetContract.ItemType.PROVIDED_BY -> ViewType.PROVIDED_BY_ITEM.ordinal
            EntryWidgetContract.ItemType.SECURE_MESSAGE -> ViewType.AUTHENTICATED_CONTACT_ITEM.ordinal
            else -> ViewType.UNAUTHENTICATED_CONTACT_ITEM.ordinal
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposable.clear()
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        open fun bind(itemType: EntryWidgetContract.ItemType, onClickListener: View.OnClickListener) {}
    }

}
