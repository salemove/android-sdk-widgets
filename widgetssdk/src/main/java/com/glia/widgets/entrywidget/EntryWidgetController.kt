package com.glia.widgets.entrywidget

internal class EntryWidgetController : EntryWidgetContract.Controller {
    private lateinit var view: EntryWidgetContract.View

    override fun setView(view: EntryWidgetContract.View) {
        this.view = view

        // TODO: Remove this hardcoded list of items
        val items = listOf(
            EntryWidgetContract.ItemType.VIDEO_CALL,
            EntryWidgetContract.ItemType.AUDIO_CALL,
            EntryWidgetContract.ItemType.CHAT,
            EntryWidgetContract.ItemType.SECURE_MESSAGE,
            EntryWidgetContract.ItemType.PROVIDED_BY
        )
        view.showItems(items)
    }

    override fun onItemClicked(itemType: EntryWidgetContract.ItemType) {
        // TODO: Handle item click

        // Dismiss the widget
        view.dismiss()
    }

    override fun onDestroy() {
        // Clean up
    }
}
