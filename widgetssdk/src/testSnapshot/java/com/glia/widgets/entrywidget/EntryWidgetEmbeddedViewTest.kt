package com.glia.widgets.entrywidget

import android.view.View
import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.entrywidget.adapter.EntryWidgetAdapter
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal open class EntryWidgetEmbeddedViewTest : SnapshotTest(
    renderingMode = fullWidthRenderMode
), SnapshotProviders {

    override fun setUp() {
        super.setUp()

        val controllerFactoryMock = mock<ControllerFactory>()
        val entryWidgetControllerMock = mock<EntryWidgetContract.Controller>()
        whenever(controllerFactoryMock.entryWidgetController).thenReturn(entryWidgetControllerMock)
        Dependencies.controllerFactory = controllerFactoryMock
    }

    // MARK: Contacts Tests without unread count badge

    private val mediaTypesWithoutUnreadMessaging = listOf(
        EntryWidgetContract.ItemType.VideoCall,
        EntryWidgetContract.ItemType.AudioCall,
        EntryWidgetContract.ItemType.Chat,
        EntryWidgetContract.ItemType.Messaging(0),
        EntryWidgetContract.ItemType.PoweredBy
    )

    @Test
    fun contactsDefaultTheme() {
        snapshot(
            setupView(items = mediaTypesWithoutUnreadMessaging)
        )
    }

    @Test
    fun contactsWithUnifiedTheme() {
        snapshot(
            setupView(
                items = mediaTypesWithoutUnreadMessaging,
                unifiedTheme = unifiedTheme()
            )
        )
    }

    @Test
    fun contactsWithUnifiedThemeWithGlobalColors() {
        snapshot(
            setupView(
                items = mediaTypesWithoutUnreadMessaging,
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun contactsWithUnifiedThemeWithoutEntryWidget() {
        snapshot(
            setupView(
                items = mediaTypesWithoutUnreadMessaging,
                unifiedTheme = unifiedThemeWithoutEntryWidget()
            )
        )
    }

    // MARK: Contacts Tests with unread count badge

    private val mediaTypesWithUnreadMessaging = listOf(
        EntryWidgetContract.ItemType.VideoCall,
        EntryWidgetContract.ItemType.AudioCall,
        EntryWidgetContract.ItemType.Chat,
        EntryWidgetContract.ItemType.Messaging(5),
        EntryWidgetContract.ItemType.PoweredBy
    )

    @Test
    fun contactsWithBadgeDefaultTheme() {
        snapshot(
            setupView(
                items = mediaTypesWithUnreadMessaging,
            )
        )
    }

    @Test
    fun contactsWithBadgeWithUnifiedTheme() {
        snapshot(
            setupView(
                items = mediaTypesWithUnreadMessaging,
                unifiedTheme = unifiedTheme()
            )
        )
    }

    @Test
    fun contactsWithBadgeWithUnifiedThemeWithGlobalColors() {
        snapshot(
            setupView(
                items = mediaTypesWithUnreadMessaging,
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun contactsWithBadgeWithUnifiedThemeWithoutEntryWidget() {
        snapshot(
            setupView(
                items = mediaTypesWithUnreadMessaging,
                unifiedTheme = unifiedThemeWithoutEntryWidget()
            )
        )
    }

    // MARK: Loading state tests

    private val loadingItems = listOf(
        EntryWidgetContract.ItemType.LoadingState,
        EntryWidgetContract.ItemType.LoadingState,
        EntryWidgetContract.ItemType.LoadingState,
        EntryWidgetContract.ItemType.LoadingState,
        EntryWidgetContract.ItemType.PoweredBy
    )

    @Test
    fun loadingDefaultTheme() {
        snapshot(
            setupView(items = loadingItems)
        )
    }

    @Test
    fun loadingWithUnifiedTheme() {
        snapshot(
            setupView(
                items = loadingItems,
                unifiedTheme = unifiedTheme()
            )
        )
    }

    @Test
    fun loadingWithUnifiedThemeWithGlobalColors() {
        snapshot(
            setupView(
                items = loadingItems,
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun loadingWithUnifiedThemeWithoutEntryWidget() {
        snapshot(
            setupView(
                items = loadingItems,
                unifiedTheme = unifiedThemeWithoutEntryWidget()
            )
        )
    }

    // MARK: Empty state tests

    private val emptyItems = listOf(
        EntryWidgetContract.ItemType.EmptyState,
        EntryWidgetContract.ItemType.PoweredBy
    )

    @Test
    fun emptyDefaultTheme() {
        snapshot(
            setupView(items = emptyItems)
        )
    }

    @Test
    fun emptyWithUnifiedTheme() {
        snapshot(
            setupView(
                items = emptyItems,
                unifiedTheme = unifiedTheme()
            )
        )
    }

    @Test
    fun emptyWithUnifiedThemeWithGlobalColors() {
        snapshot(
            setupView(
                items = emptyItems,
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun emptyWithUnifiedThemeWithoutEntryWidget() {
        snapshot(
            setupView(
                items = emptyItems,
                unifiedTheme = unifiedThemeWithoutEntryWidget()
            )
        )
    }


    // MARK: SDK not initialized state tests

    private val sdkNotInitializedItems = listOf(
        EntryWidgetContract.ItemType.SdkNotInitializedState,
        EntryWidgetContract.ItemType.PoweredBy
    )

    @Test
    fun sdkNotInitializedItemsDefaultTheme() {
        snapshot(
            setupView(items = sdkNotInitializedItems)
        )
    }

    @Test
    fun sdkNotInitializedItemsWithUnifiedTheme() {
        snapshot(
            setupView(
                items = sdkNotInitializedItems,
                unifiedTheme = unifiedTheme()
            )
        )
    }

    @Test
    fun sdkNotInitializedItemsWithUnifiedThemeWithGlobalColors() {
        snapshot(
            setupView(
                items = sdkNotInitializedItems,
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun sdkNotInitializedItemsWithUnifiedThemeWithoutEntryWidget() {
        snapshot(
            setupView(
                items = sdkNotInitializedItems,
                unifiedTheme = unifiedThemeWithoutEntryWidget()
            )
        )
    }

    // MARK: Error state tests

    private val errorItems = listOf(
        EntryWidgetContract.ItemType.ErrorState,
        EntryWidgetContract.ItemType.PoweredBy
    )

    @Test
    fun errorDefaultTheme() {
        snapshot(
            setupView(items = errorItems)
        )
    }

    @Test
    fun errorWithUnifiedTheme() {
        snapshot(
            setupView(
                items = errorItems,
                unifiedTheme = unifiedTheme()
            )
        )
    }

    @Test
    fun errorWithUnifiedThemeWithGlobalColors() {
        snapshot(
            setupView(
                items = errorItems,
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun errorWithUnifiedThemeWithoutEntryWidget() {
        snapshot(
            setupView(
                items = errorItems,
                unifiedTheme = unifiedThemeWithoutEntryWidget()
            )
        )
    }

    // MARK: utils for tests

    open fun setupView(
        items: List<EntryWidgetContract.ItemType> = listOf(EntryWidgetContract.ItemType.PoweredBy),
        viewType: EntryWidgetContract.ViewType = EntryWidgetContract.ViewType.EMBEDDED_VIEW,
        unifiedTheme: UnifiedTheme? = null
    ) : View {

        val entryWidgetTheme = unifiedTheme?.entryWidgetTheme

        return EntryWidgetView(context).apply {
            setAdapter(EntryWidgetAdapter(viewType, entryWidgetTheme))
            setEntryWidgetTheme(entryWidgetTheme)
            showItems(items)
        }
    }

    private fun unifiedThemeWithoutEntryWidget(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config) { unifiedTheme ->
        unifiedTheme.remove("entryWidget")
    }
}
