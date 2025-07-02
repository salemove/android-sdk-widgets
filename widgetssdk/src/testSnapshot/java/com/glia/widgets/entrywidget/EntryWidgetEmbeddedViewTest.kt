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

    // MARK: Tests for available engagement items without unread count badge

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

    // MARK: Tests for available engagement items with unread count badge

    private val mediaTypesWithUnreadMessaging = listOf(
        EntryWidgetContract.ItemType.VideoCall,
        EntryWidgetContract.ItemType.AudioCall,
        EntryWidgetContract.ItemType.Chat,
        EntryWidgetContract.ItemType.Messaging(5),
        EntryWidgetContract.ItemType.PoweredBy
    )

    private val mediaTypesWithUnreadMessagingWhiteLabel = listOf(
        EntryWidgetContract.ItemType.VideoCall,
        EntryWidgetContract.ItemType.AudioCall,
        EntryWidgetContract.ItemType.Chat,
        EntryWidgetContract.ItemType.Messaging(5)
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
    fun contactsWithBadgeWhiteLabelDefaultTheme() {
        snapshot(
            setupView(
                items = mediaTypesWithUnreadMessagingWhiteLabel,
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
    fun contactsWithBadgeWhiteLabelWithUnifiedTheme() {
        snapshot(
            setupView(
                items = mediaTypesWithUnreadMessagingWhiteLabel,
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
    fun contactsWithBadgeWhiteLabelWithUnifiedThemeWithGlobalColors() {
        snapshot(
            setupView(
                items = mediaTypesWithUnreadMessagingWhiteLabel,
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

    @Test
    fun contactsWithBadgeWhiteLabelWithUnifiedThemeWithoutEntryWidget() {
        snapshot(
            setupView(
                items = mediaTypesWithUnreadMessagingWhiteLabel,
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

    private val loadingItemsWhiteLabel = listOf(
        EntryWidgetContract.ItemType.LoadingState,
        EntryWidgetContract.ItemType.LoadingState,
        EntryWidgetContract.ItemType.LoadingState,
        EntryWidgetContract.ItemType.LoadingState
    )

    @Test
    fun loadingDefaultTheme() {
        snapshot(
            setupView(items = loadingItems)
        )
    }

    @Test
    fun loadingWhiteLabelDefaultTheme() {
        snapshot(
            setupView(items = loadingItemsWhiteLabel)
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
    fun loadingWhiteLabelWithUnifiedTheme() {
        snapshot(
            setupView(
                items = loadingItemsWhiteLabel,
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
    fun loadingWithUnifiedThemeWhiteLabelWithGlobalColors() {
        snapshot(
            setupView(
                items = loadingItemsWhiteLabel,
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

    @Test
    fun loadingWithUnifiedThemeWhiteLabelWithoutEntryWidget() {
        snapshot(
            setupView(
                items = loadingItemsWhiteLabel,
                unifiedTheme = unifiedThemeWithoutEntryWidget()
            )
        )
    }

    // MARK: Empty state tests

    private val emptyItems = listOf(
        EntryWidgetContract.ItemType.EmptyState,
        EntryWidgetContract.ItemType.PoweredBy
    )

    private val emptyItemsWhiteLabel = listOf(
        EntryWidgetContract.ItemType.EmptyState
    )

    @Test
    fun emptyDefaultTheme() {
        snapshot(
            setupView(items = emptyItems)
        )
    }

    @Test
    fun emptyWhiteLabelDefaultTheme() {
        snapshot(
            setupView(items = emptyItemsWhiteLabel)
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
    fun emptyWhiteLabelWithUnifiedTheme() {
        snapshot(
            setupView(
                items = emptyItemsWhiteLabel,
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
    fun emptyWhiteLabelWithUnifiedThemeWithGlobalColors() {
        snapshot(
            setupView(
                items = emptyItemsWhiteLabel,
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

    @Test
    fun emptyWhiteLabelWithUnifiedThemeWithoutEntryWidget() {
        snapshot(
            setupView(
                items = emptyItemsWhiteLabel,
                unifiedTheme = unifiedThemeWithoutEntryWidget()
            )
        )
    }


    // MARK: SDK not initialized state tests

    private val sdkNotInitializedItems = listOf(
        EntryWidgetContract.ItemType.SdkNotInitializedState,
        EntryWidgetContract.ItemType.PoweredBy
    )

    private val sdkNotInitializedItemsWhiteLabel = listOf(
        EntryWidgetContract.ItemType.SdkNotInitializedState
    )

    @Test
    fun sdkNotInitializedItemsDefaultTheme() {
        snapshot(
            setupView(items = sdkNotInitializedItems)
        )
    }

    @Test
    fun sdkNotInitializedItemsWhiteLabelDefaultTheme() {
        snapshot(
            setupView(items = sdkNotInitializedItemsWhiteLabel)
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
    fun sdkNotInitializedItemsWhiteLabelWithUnifiedTheme() {
        snapshot(
            setupView(
                items = sdkNotInitializedItemsWhiteLabel,
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
    fun sdkNotInitializedItemsWhiteLabelWithUnifiedThemeWithGlobalColors() {
        snapshot(
            setupView(
                items = sdkNotInitializedItemsWhiteLabel,
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

    @Test
    fun sdkNotInitializedItemsWhiteLabelWithUnifiedThemeWithoutEntryWidget() {
        snapshot(
            setupView(
                items = sdkNotInitializedItemsWhiteLabel,
                unifiedTheme = unifiedThemeWithoutEntryWidget()
            )
        )
    }

    // MARK: Error state tests

    private val errorItems = listOf(
        EntryWidgetContract.ItemType.ErrorState,
        EntryWidgetContract.ItemType.PoweredBy
    )

    private val errorItemsWhiteLabel = listOf(
        EntryWidgetContract.ItemType.ErrorState
    )

    @Test
    fun errorDefaultTheme() {
        snapshot(
            setupView(items = errorItems)
        )
    }

    @Test
    fun errorWhiteLabelDefaultTheme() {
        snapshot(
            setupView(items = errorItemsWhiteLabel)
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
    fun errorWhiteLabelWithUnifiedTheme() {
        snapshot(
            setupView(
                items = errorItemsWhiteLabel,
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
    fun errorWhiteLabelWithUnifiedThemeWithGlobalColors() {
        snapshot(
            setupView(
                items = errorItemsWhiteLabel,
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

    @Test
    fun errorWhiteLabelWithUnifiedThemeWithoutEntryWidget() {
        snapshot(
            setupView(
                items = errorItemsWhiteLabel,
                unifiedTheme = unifiedThemeWithoutEntryWidget()
            )
        )
    }

    // MARK: Tests for VideoCallOngoing engagement item

    private val videoCallOngoing = listOf(
        EntryWidgetContract.ItemType.VideoCallOngoing,
    )

    @Test
    fun videoCallOngoingDefaultTheme() {
        snapshot(
            setupView(items = videoCallOngoing)
        )
    }

    @Test
    fun videoCallOngoingWithUnifiedTheme() {
        snapshot(
            setupView(
                items = videoCallOngoing,
                unifiedTheme = unifiedTheme()
            )
        )
    }

    @Test
    fun videoCallOngoingWithUnifiedThemeWithGlobalColors() {
        snapshot(
            setupView(
                items = videoCallOngoing,
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun videoCallOngoingWithUnifiedThemeWithoutEntryWidget() {
        snapshot(
            setupView(
                items = videoCallOngoing,
                unifiedTheme = unifiedThemeWithoutEntryWidget()
            )
        )
    }

    // MARK: Tests for AudioCallOngoing engagement item

    private val audioCallOngoing = listOf(
        EntryWidgetContract.ItemType.AudioCallOngoing,
    )

    @Test
    fun audioCallOngoingDefaultTheme() {
        snapshot(
            setupView(items = audioCallOngoing)
        )
    }

    @Test
    fun audioCallOngoingWithUnifiedTheme() {
        snapshot(
            setupView(
                items = audioCallOngoing,
                unifiedTheme = unifiedTheme()
            )
        )
    }

    @Test
    fun audioCallOngoingWithUnifiedThemeWithGlobalColors() {
        snapshot(
            setupView(
                items = audioCallOngoing,
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun audioCallOngoingWithUnifiedThemeWithoutEntryWidget() {
        snapshot(
            setupView(
                items = audioCallOngoing,
                unifiedTheme = unifiedThemeWithoutEntryWidget()
            )
        )
    }

    // MARK: Tests for ChatOngoing engagement item

    private val chatOngoing = listOf(
        EntryWidgetContract.ItemType.ChatOngoing,
    )

    @Test
    fun chatOngoingDefaultTheme() {
        snapshot(
            setupView(items = chatOngoing)
        )
    }

    @Test
    fun chatOngoingWithUnifiedTheme() {
        snapshot(
            setupView(
                items = chatOngoing,
                unifiedTheme = unifiedTheme()
            )
        )
    }

    @Test
    fun chatOngoingWithUnifiedThemeWithGlobalColors() {
        snapshot(
            setupView(
                items = chatOngoing,
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun chatOngoingWithUnifiedThemeWithoutEntryWidget() {
        snapshot(
            setupView(
                items = chatOngoing,
                unifiedTheme = unifiedThemeWithoutEntryWidget()
            )
        )
    }

    // MARK: Tests for MessagingOngoing engagement item without unread count badge

    private val messagingOngoing = listOf(
        EntryWidgetContract.ItemType.MessagingOngoing(0),
    )

    @Test
    fun messagingOngoingDefaultTheme() {
        snapshot(
            setupView(items = messagingOngoing)
        )
    }

    @Test
    fun messagingOngoingWithUnifiedTheme() {
        snapshot(
            setupView(
                items = messagingOngoing,
                unifiedTheme = unifiedTheme()
            )
        )
    }

    @Test
    fun messagingOngoingWithUnifiedThemeWithGlobalColors() {
        snapshot(
            setupView(
                items = messagingOngoing,
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun messagingOngoingWithUnifiedThemeWithoutEntryWidget() {
        snapshot(
            setupView(
                items = messagingOngoing,
                unifiedTheme = unifiedThemeWithoutEntryWidget()
            )
        )
    }

    // MARK: Tests for MessagingOngoing engagement item with unread count badge

    private val messagingOngoingWithUnread = listOf(
        EntryWidgetContract.ItemType.MessagingOngoing(5),
    )

    @Test
    fun messagingOngoingWithUnreadDefaultTheme() {
        snapshot(
            setupView(items = messagingOngoingWithUnread)
        )
    }

    @Test
    fun messagingOngoingWithUnreadWithUnifiedTheme() {
        snapshot(
            setupView(
                items = messagingOngoingWithUnread,
                unifiedTheme = unifiedTheme()
            )
        )
    }

    @Test
    fun messagingOngoingWithUnreadWithUnifiedThemeWithGlobalColors() {
        snapshot(
            setupView(
                items = messagingOngoingWithUnread,
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun messagingOngoingWithUnreadWithUnifiedThemeWithoutEntryWidget() {
        snapshot(
            setupView(
                items = messagingOngoingWithUnread,
                unifiedTheme = unifiedThemeWithoutEntryWidget()
            )
        )
    }

    // MARK: utils for tests

    open fun setupView(
        items: List<EntryWidgetContract.ItemType> = listOf(EntryWidgetContract.ItemType.PoweredBy),
        viewType: EntryWidgetContract.ViewType = EntryWidgetContract.ViewType.EMBEDDED_VIEW,
        unifiedTheme: UnifiedTheme? = null
    ): View {

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
