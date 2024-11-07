package com.glia.widgets.entrywidget

import android.view.View
import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.core.secureconversations.domain.ObserveUnreadMessagesCountUseCase
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.entrywidget.adapter.EntryWidgetAdapter
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import io.reactivex.rxjava3.subjects.BehaviorSubject

internal open class EntryWidgetEmbeddedViewTest : SnapshotTest(
    renderingMode = fullWidthRenderMode
), SnapshotProviders {

    private val observeUnreadMessagesCountZeroUseCaseMock = mock<ObserveUnreadMessagesCountUseCase>()
    private val observeUnreadMessagesCountFiveUseCaseMock = mock<ObserveUnreadMessagesCountUseCase>()

    override fun setUp() {
        super.setUp()

        val controllerFactoryMock = mock<ControllerFactory>()
        val entryWidgetControllerMock = mock<EntryWidgetContract.Controller>()
        whenever(controllerFactoryMock.entryWidgetController).thenReturn(entryWidgetControllerMock)
        Dependencies.controllerFactory = controllerFactoryMock
        whenever(observeUnreadMessagesCountZeroUseCaseMock()).thenReturn(BehaviorSubject.createDefault(0))
        whenever(observeUnreadMessagesCountFiveUseCaseMock()).thenReturn(BehaviorSubject.createDefault(5))
    }

    // MARK: Contacts Tests without unread count badge

    private val contactsItems = listOf(
        EntryWidgetContract.ItemType.VIDEO_CALL,
        EntryWidgetContract.ItemType.AUDIO_CALL,
        EntryWidgetContract.ItemType.CHAT,
        EntryWidgetContract.ItemType.SECURE_MESSAGE,
        EntryWidgetContract.ItemType.PROVIDED_BY
    )

    @Test
    fun contactsDefaultTheme() {
        snapshot(
            setupView(items = contactsItems)
        )
    }

    @Test
    fun contactsWithUnifiedTheme() {
        snapshot(
            setupView(
                items = contactsItems,
                unifiedTheme = unifiedTheme()
            )
        )
    }

    @Test
    fun contactsWithUnifiedThemeWithGlobalColors() {
        snapshot(
            setupView(
                items = contactsItems,
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun contactsWithUnifiedThemeWithoutEntryWidget() {
        snapshot(
            setupView(
                items = contactsItems,
                unifiedTheme = unifiedThemeWithoutEntryWidget()
            )
        )
    }

    // MARK: Contacts Tests with unread count badge

    @Test
    fun contactsWithBadgeDefaultTheme() {
        snapshot(
            setupView(
                items = contactsItems,
                observeUnreadMessagesCountUseCase = observeUnreadMessagesCountFiveUseCaseMock,
            )
        )
    }

    @Test
    fun contactsWithBadgeWithUnifiedTheme() {
        snapshot(
            setupView(
                items = contactsItems,
                observeUnreadMessagesCountUseCase = observeUnreadMessagesCountFiveUseCaseMock,
                unifiedTheme = unifiedTheme()
            )
        )
    }

    @Test
    fun contactsWithBadgeWithUnifiedThemeWithGlobalColors() {
        snapshot(
            setupView(
                items = contactsItems,
                observeUnreadMessagesCountUseCase = observeUnreadMessagesCountFiveUseCaseMock,
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun contactsWithBadgeWithUnifiedThemeWithoutEntryWidget() {
        snapshot(
            setupView(
                items = contactsItems,
                observeUnreadMessagesCountUseCase = observeUnreadMessagesCountFiveUseCaseMock,
                unifiedTheme = unifiedThemeWithoutEntryWidget()
            )
        )
    }

    // MARK: Loading state tests

    private val loadingItems = listOf(
        EntryWidgetContract.ItemType.LOADING_STATE,
        EntryWidgetContract.ItemType.LOADING_STATE,
        EntryWidgetContract.ItemType.LOADING_STATE,
        EntryWidgetContract.ItemType.LOADING_STATE,
        EntryWidgetContract.ItemType.PROVIDED_BY
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
        EntryWidgetContract.ItemType.EMPTY_STATE,
        EntryWidgetContract.ItemType.PROVIDED_BY
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
        EntryWidgetContract.ItemType.SDK_NOT_INITIALIZED_STATE,
        EntryWidgetContract.ItemType.PROVIDED_BY
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
        EntryWidgetContract.ItemType.ERROR_STATE,
        EntryWidgetContract.ItemType.PROVIDED_BY
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
        items: List<EntryWidgetContract.ItemType> = listOf(EntryWidgetContract.ItemType.PROVIDED_BY),
        viewType: EntryWidgetContract.ViewType = EntryWidgetContract.ViewType.EMBEDDED_VIEW,
        observeUnreadMessagesCountUseCase: ObserveUnreadMessagesCountUseCase = observeUnreadMessagesCountZeroUseCaseMock,
        unifiedTheme: UnifiedTheme? = null
    ) : View {

        val entryWidgetTheme = unifiedTheme?.entryWidgetTheme

        return EntryWidgetView(
            context,
            viewAdapter = EntryWidgetAdapter(
                viewType,
                entryWidgetTheme,
                observeUnreadMessagesCountUseCase,
            ),
            entryWidgetTheme = entryWidgetTheme
        ).apply {
            showItems(items)
        }
    }

    private fun unifiedThemeWithoutEntryWidget(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config) { unifiedTheme ->
        unifiedTheme.remove("entryWidget")
    }
}
