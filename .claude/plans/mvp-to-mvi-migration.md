# MVP to MVI + Clean Architecture Migration - Complete Implementation Plan

**Ticket**: [MOB-5043](https://glia.atlassian.net/browse/MOB-5043)

## Overview

Migrate the GliaWidgets Android SDK from MVP architecture (Activities + Views + Controllers) to **Single Transparent Activity + Fragments + ViewModels + MVI pattern**.

**Key approach**:
- One transparent `HostActivity` serves as container for all SDK screens
- Full-screen content (Chat, Call) shown as regular Fragments
- Overlay content (Survey, dialogs) shown as BottomSheetDialogFragment/DialogFragment
- Custom Views retain ONLY UI drawing/rendering logic
- ALL business logic moves to Fragments/ViewModels

## Unified Transparent Activity Architecture

### The Solution

A single transparent `HostActivity` that hosts everything:
- Main screens (Chat, Call, MessageCenter) as regular Fragments
- Survey as `BottomSheetDialogFragment`
- All dialogs as `DialogFragment`
- All permission requests

```
┌───────────────────────────────────────────────────────────────┐
│  HostActivity (Transparent background)                         │
│                                                                │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │  Fragment Container                                       │ │
│  │  ┌────────────────────────────────────────────────────┐  │ │
│  │  │  ChatFragment / CallFragment / MessageCenterFragment│  │ │
│  │  │  (full-screen with own background)                  │  │ │
│  │  └────────────────────────────────────────────────────┘  │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                │
│  BottomSheets & Dialogs (shown over content):                  │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │  SurveyBottomSheetFragment                                │ │
│  │  VisitorCodeDialogFragment                                │ │
│  │  ConfirmationDialogFragment                               │ │
│  │  EndEngagementDialogFragment                              │ │
│  │  MediaUpgradeDialogFragment                               │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                │
│  Permission requests use this Activity's context               │
└───────────────────────────────────────────────────────────────┘
        │ transparent when no full-screen fragment
        ▼
┌───────────────────────────────────────────────────────────────┐
│  Client's Activity (Flutter/RN/Unity/Native)                   │
│  Visible through transparent areas!                            │
└───────────────────────────────────────────────────────────────┘
```

### Why This Works for Cross-Platform

| Concern | Solution |
|---------|----------|
| **Cross-platform clients** (Flutter, RN, Unity) | We launch our own AndroidX Activity - doesn't matter what host uses |
| **Overlay screens** (Survey, dialogs) | BottomSheetDialogFragment over transparent activity - client UI visible |
| **Full-screen flows** (Chat, Call) | Regular Fragments with own backgrounds |
| **Permission handling** | Single Activity context for all permissions |
| **State sharing** | Shared ViewModels possible across fragments |
| **Smooth transitions** | Fragment animations within same Activity |

### Flow Examples

**Scenario 1: Full Engagement Flow**
```
1. Client starts chat → HostActivity launches
2. ChatFragment shown (has its own background - covers screen)
3. Operator requests video → MediaUpgradeDialogFragment shown over Chat
4. User accepts → CallFragment replaces ChatFragment
5. Call ends → SurveyBottomSheetFragment shown over Call (or transparent)
6. Survey submitted → HostActivity finishes
```

**Scenario 2: Just Survey (no engagement)**
```
1. Survey needed → HostActivity launches (transparent)
2. SurveyBottomSheetFragment shown immediately
3. Client's UI visible through transparent activity!
4. Survey dismissed → HostActivity finishes
```

**Scenario 3: Visitor Code Dialog**
```
1. Show visitor code → HostActivity launches (transparent)
2. VisitorCodeDialogFragment shown
3. Client's UI visible behind
4. Dialog dismissed → HostActivity finishes
```

## Architecture Principles

### Clear Separation of Concerns

| Layer | Responsibility | Examples |
|-------|---------------|----------|
| **ViewModel** | Business logic, state management, use case orchestration | Validation, data transformation, API calls, analytics logging |
| **Fragment** | Lifecycle coordination, navigation, user intent dispatch, state observation | Activity results, dialog management, permission requests |
| **Custom View** | Pure UI rendering, styling, animations | Theme application, RecyclerView setup, visual state updates |

### What Stays in Custom Views (UI Drawing Only)
- Theme and style application
- View binding and layout inflation
- RecyclerView/adapter setup
- Animations and transitions
- Rendering state to visual elements
- Window insets handling
- Visual feedback (scrolling, focus states)

### What Moves to Fragments
- Activity result launchers (camera, file picker)
- Dialog lifecycle and result handling
- Navigation coordination with Activity
- Listener callbacks to Activity (onEnd, onMinimize)
- Controller/ViewModel lifecycle management
- User action forwarding to ViewModel

### What Moves to ViewModels
- State management (StateFlow)
- Business logic and validation
- Use case orchestration
- Analytics/telemetry logging
- Data transformation (e.g., Question → QuestionItem)
- Error handling and retry logic
- One-time effects (Channel for navigation, toasts)

## Current State Analysis

| Component | Count | Technology |
|-----------|-------|------------|
| Activities | 11 | MVP pattern with Contract interfaces |
| Controllers | 20+ | Business logic, RxJava subscriptions |
| Custom Views | 8 | SurveyView, ChatView, CallView, MessageCenterView, etc. |
| Fragments | 1 | EntryWidgetFragment (BottomSheetDialogFragment) |
| ViewModels | 0 | None - to be introduced |
| Reactive | RxJava 3 | BehaviorProcessor, CompositeDisposable |

### Screen Inventory by Complexity

| Screen | Current Activity | View | Controller | Fragment Type |
|--------|-----------------|------|------------|---------------|
| Survey | SurveyActivity | SurveyView | SurveyController (Java) | BottomSheetDialogFragment |
| WebBrowser | WebBrowserActivity | WebBrowserView | None | Regular Fragment |
| ImagePreview | ImagePreviewActivity | ImagePreviewView | ImagePreviewController | DialogFragment |
| VisitorCode | DialogHolderActivity | VisitorCodeView | VisitorCodeController | DialogFragment |
| MessageCenter | MessageCenterActivity | MessageCenterView | MessageCenterController | Regular Fragment |
| EntryWidget | EntryWidgetActivity | EntryWidgetView | EntryWidgetController | BottomSheetDialogFragment |
| Call | CallActivity | CallView | CallController (649 lines) | Regular Fragment |
| Chat | ChatActivity | ChatView | ChatController (1140 lines) | Regular Fragment |

## Desired End State

After completing this plan:
- **Single `HostActivity`** with transparent background hosts all SDK screens
- All screens migrated to Fragment + ViewModel + MVI architecture
- Survey shown as `BottomSheetDialogFragment` (proper bottom sheet behavior)
- All dialogs as `DialogFragment` instances
- Custom Views contain ONLY UI rendering code (no Controllers, no business logic)
- Business logic in ViewModels with StateFlow for state, Channel for effects
- Cross-platform compatible (works with Flutter, React Native, Unity, etc.)
- All tests pass with new ViewModel unit tests

### Key Patterns to Use:
- **Flow Collection**: `flow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { }.launchIn(lifecycleScope)`
- **State**: `StateFlow<UiState>` for UI state
- **Effects**: `Channel<Effect>` for one-time events (navigation, toasts)
- **Intents**: Sealed interface for user actions
- **RxJava Bridge**: Use `asFlow()` to consume existing RxJava streams in ViewModels
- **Fragment Factory**: Centralized `FragmentFactory` class for creating Fragment instances

## What We're NOT Doing

- NOT rewriting custom Views (reusing SurveyView, ChatView, etc.)
- NOT converting all repositories from RxJava to Flow (using `asFlow()` bridge)
- NOT breaking public APIs (`EngagementLauncher`, `EntryWidget`, `GliaWidgets`)
- NOT using Jetpack Compose (traditional View system only)
- NOT using Navigation Component (custom FragmentManager navigation)

## Kotlin Coding Style Guidelines

**CRITICAL - All new Kotlin code must follow these rules:**

### Naming Conventions
- **DO NOT use "Glia" prefix for internal classes** - Only public API classes should have the "Glia" prefix (e.g., `GliaWidgets`, `GliaWidgetsConfig`)
- Internal classes use simple names: `BaseViewModel`, `BaseFragment`, `HostActivity`, `Navigator`
- This keeps internal code clean while maintaining brand visibility in the public API

### Type Declarations
- **ALWAYS specify types explicitly for class-level properties**
  ```kotlin
  // ✅ CORRECT
  private val _state: MutableStateFlow<SurveyUiState> = MutableStateFlow(initialState)
  val state: StateFlow<SurveyUiState> = _state.asStateFlow()

  // ❌ WRONG
  private val _state = MutableStateFlow(initialState)
  val state = _state.asStateFlow()
  ```

- **ALWAYS specify return types for functions with block bodies** (except for `Unit` which can be omitted)
  - Functions returning `Unit` - return type is **optional** and should be **omitted**
  - Single-expression functions - Kotlin can infer, but explicit is preferred for class-level functions
  - Functions with block bodies that return non-Unit - return type is **required**

  ```kotlin
  // ✅ CORRECT - Unit return type omitted
  fun processIntent(intent: SurveyIntent) { }
  protected suspend fun handleIntent(intent: SurveyIntent) { }

  // ✅ CORRECT - explicit return type for non-Unit returns
  protected fun currentState(): SurveyUiState = _state.value
  fun getQuestions(): List<QuestionItem> {
      return currentState.questions
  }

  // ❌ WRONG - missing return type for non-Unit function with block body
  fun getQuestions() {
      return currentState.questions
  }
  protected fun currentState() = _state.value  // Prefer explicit type for class-level
  ```

- **Exception**: Type inference is OK for local variables inside functions
  ```kotlin
  // ✅ OK for local variables
  fun example() {
      val result = calculateSomething() // OK - local variable
      val items = listOf(1, 2, 3)      // OK - local variable
  }
  ```

---

## Phase 1: Add Dependencies ✅ COMPLETED

### Overview
Add Coroutines, Lifecycle ViewModel, and RxJava-Coroutines interop dependencies.

### Success Criteria:
- [x] Project syncs: `./gradlew widgetssdk:dependencies`
- [x] Build completes: `./gradlew widgetssdk:assembleDebug`

---

## Phase 2: Create Base Infrastructure Classes ✅ COMPLETED (Enhanced)

### Overview
Create reusable base classes for Fragments and ViewModels that implement MVI pattern.

### Changes Completed:
- ✅ `MviContracts.kt` - Marker interfaces: `UiState`, `UiIntent`, `UiEffect`
- ✅ `BaseViewModel.kt` - Generic MVI ViewModel with type bounds for State, Intent, Effect
- ✅ `BaseFragment.kt` - Generic Fragment that auto-collects state/effects, delegates to `handleState()`/`handleEffect()`
- ✅ `ViewModelFactory.kt` - Factory for ViewModel creation
- ✅ `Dependencies.kt` - Added `viewModelFactory` property

### Key Design Pattern:
BaseFragment is generic and handles Flow collection internally:
```kotlin
abstract class BaseFragment<State : UiState, Effect : UiEffect, VM : BaseViewModel<State, *, Effect>>(
    @LayoutRes contentLayoutId: Int
) : Fragment(contentLayoutId) {

    protected abstract val viewModel: VM

    // Called when new state is emitted
    protected abstract fun handleState(state: State)

    // Called when one-time effect is emitted
    protected abstract fun handleEffect(effect: Effect)
}
```

Subclasses just provide the ViewModel and implement handlers - no manual Flow collection needed.

### Success Criteria:
- [x] Build completes: `./gradlew widgetssdk:assembleDebug`
- [x] Unit tests pass: `./gradlew widgetssdk:testDebugUnitTest`

---

## Phase 3: Create HostActivity and Navigator

### Overview
Create the unified transparent `HostActivity` that serves as container for all SDK screens, plus `Navigator` for fragment management.

**Key Design Decisions:**
- **Theme**: Use existing `Application.Glia.Translucent.Activity` which already inherits from `Application.Glia.Activity.Style` - this preserves integrator theme configuration
- **Edge-to-edge**: Extend `FadeTransitionActivity` which already calls `enableEdgeToEdge()` and handles insets properly
- **Insets**: Let `FadeTransitionActivity` handle edge-to-edge setup; Fragments/Views use `SimpleWindowInsetsAndAnimationHandler` for content padding

### Changes Required:

#### 1. HostActivity Theme
**NO NEW THEME NEEDED** - Use existing `Application.Glia.Translucent.Activity`

The existing theme (`widgetssdk/src/main/res/values/themes.xml:85-92`) already:
- Inherits from `Application.Glia.Activity.Style` (preserves integrator theming)
- Has transparent window background
- Has translucent window
- Has transparent status and navigation bars
- Has `windowDrawsSystemBarBackgrounds=true` for edge-to-edge

```xml
<!-- Already exists - no changes needed -->
<style name="Application.Glia.Translucent.Activity" parent="Application.Glia.Activity.Style">
    <item name="android:windowBackground">@android:color/transparent</item>
    <item name="android:windowIsTranslucent">true</item>
    <item name="android:windowContentOverlay">@null</item>
    <item name="android:windowDrawsSystemBarBackgrounds">true</item>
    <item name="android:statusBarColor">@android:color/transparent</item>
    <item name="android:navigationBarColor">@android:color/transparent</item>
</style>
```

#### 2. Destination sealed class
**File**: `widgetssdk/src/main/java/com/glia/widgets/navigation/Destination.kt`

```kotlin
package com.glia.widgets.navigation

import android.os.Parcelable
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.chat.Intention
import kotlinx.parcelize.Parcelize

/**
 * Represents navigation destinations within the SDK.
 */
internal sealed interface Destination : Parcelable {

    @Parcelize
    data class Chat(val intention: Intention) : Destination

    @Parcelize
    data class Call(val mediaType: String?) : Destination

    @Parcelize
    data class Survey(val survey: com.glia.androidsdk.engagement.Survey) : Destination

    @Parcelize
    data class MessageCenter(val queueIds: List<String>?) : Destination

    @Parcelize
    data class WebBrowser(val title: String, val url: String) : Destination

    @Parcelize
    data class ImagePreview(
        val imageId: String? = null,
        val imageName: String? = null,
        val localImageUri: String? = null
    ) : Destination

    @Parcelize
    data object VisitorCode : Destination
}
```

#### 3. Navigator
**File**: `widgetssdk/src/main/java/com/glia/widgets/navigation/Navigator.kt`

```kotlin
package com.glia.widgets.navigation

import androidx.fragment.app.FragmentManager
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies

/**
 * Handles fragment navigation within HostActivity.
 *
 * Manages:
 * - Full-screen fragment transactions (Chat, Call, MessageCenter)
 * - Dialog/BottomSheet display (Survey, VisitorCode, confirmations)
 * - Back stack management
 */
internal class Navigator(
    private val fragmentManager: FragmentManager,
    private val containerId: Int = R.id.fragment_container
) {
    private val fragmentFactory: FragmentFactory = Dependencies.fragmentFactory

    val hasContent: Boolean
        get() = fragmentManager.findFragmentById(containerId) != null

    val hasDialogs: Boolean
        get() = fragmentManager.fragments.any { it.isAdded && it.tag?.startsWith("dialog_") == true }

    val isEmpty: Boolean
        get() = !hasContent && !hasDialogs

    // Full-screen fragments

    fun showChat(intention: Intention) {
        val fragment = fragmentFactory.createChatFragment(intention)
        fragmentManager.beginTransaction()
            .replace(containerId, fragment, "chat")
            .commit()
    }

    fun showCall(mediaType: String?) {
        val fragment = fragmentFactory.createCallFragment(mediaType)
        fragmentManager.beginTransaction()
            .replace(containerId, fragment, "call")
            .commit()
    }

    fun showMessageCenter(queueIds: List<String>?) {
        val fragment = fragmentFactory.createMessageCenterFragment(queueIds?.let { ArrayList(it) })
        fragmentManager.beginTransaction()
            .replace(containerId, fragment, "message_center")
            .commit()
    }

    fun showWebBrowser(title: String, url: String) {
        val fragment = fragmentFactory.createWebBrowserFragment(title, url)
        fragmentManager.beginTransaction()
            .replace(containerId, fragment, "web_browser")
            .addToBackStack(null)
            .commit()
    }

    fun showImagePreview(imageId: String?, imageName: String?, localImageUri: String?) {
        val fragment = fragmentFactory.createImagePreviewFragment(imageId, imageName, localImageUri)
        fragment.show(fragmentManager, "dialog_image_preview")
    }

    // Bottom sheets and dialogs

    fun showSurvey(survey: Survey) {
        val fragment = fragmentFactory.createSurveyBottomSheet(survey)
        fragment.show(fragmentManager, "dialog_survey")
    }

    fun showVisitorCode() {
        val fragment = fragmentFactory.createVisitorCodeDialog()
        fragment.show(fragmentManager, "dialog_visitor_code")
    }

    fun showEndEngagementConfirmation(onConfirm: () -> Unit) {
        val fragment = fragmentFactory.createEndEngagementDialog(onConfirm)
        fragment.show(fragmentManager, "dialog_end_engagement")
    }

    fun showMediaUpgradeDialog(mediaType: String, onAccept: () -> Unit, onDecline: () -> Unit) {
        val fragment = fragmentFactory.createMediaUpgradeDialog(mediaType, onAccept, onDecline)
        fragment.show(fragmentManager, "dialog_media_upgrade")
    }

    // Navigation helpers

    fun popBackStack(): Boolean {
        return if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            true
        } else {
            false
        }
    }

    fun dismissAllDialogs() {
        fragmentManager.fragments
            .filter { it.tag?.startsWith("dialog_") == true }
            .forEach { (it as? androidx.fragment.app.DialogFragment)?.dismissAllowingStateLoss() }
    }

    fun clearContent() {
        fragmentManager.findFragmentById(containerId)?.let {
            fragmentManager.beginTransaction()
                .remove(it)
                .commit()
        }
    }
}
```

#### 4. HostActivity
**File**: `widgetssdk/src/main/java/com/glia/widgets/HostActivity.kt`

**Key changes from original plan:**
- Extends `FadeTransitionActivity` instead of `AppCompatActivity` for edge-to-edge and animation support
- Edge-to-edge is handled by parent class (calls `enableEdgeToEdge()`)
- No need for additional insets setup - Fragments handle their own content insets

```kotlin
package com.glia.widgets

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.core.os.BundleCompat
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.helper.Logger
import com.glia.widgets.navigation.Destination
import com.glia.widgets.navigation.Navigator

/**
 * Single transparent Activity that hosts all SDK screens.
 *
 * This Activity:
 * - Has a transparent background (client's UI visible when no content)
 * - Hosts full-screen Fragments (Chat, Call, MessageCenter)
 * - Shows BottomSheets and Dialogs (Survey, VisitorCode, confirmations)
 * - Handles all permission requests
 * - Manages unified back navigation
 * - Supports edge-to-edge display (via FadeTransitionActivity)
 *
 * Edge-to-edge handling:
 * - FadeTransitionActivity.onCreate() calls enableEdgeToEdge()
 * - Individual Fragments/Views apply their own content padding using SimpleWindowInsetsAndAnimationHandler
 *
 * Usage:
 * ```
 * HostActivity.start(context, Destination.Chat(intention))
 * HostActivity.start(context, Destination.Survey(survey))
 * ```
 */
internal class HostActivity : FadeTransitionActivity() {

    companion object {
        private const val TAG = "HostActivity"
        internal const val EXTRA_DESTINATION = "extra_destination"

        fun start(context: Context, destination: Destination) {
            val intent: Intent = Intent(context, HostActivity::class.java).apply {
                putExtra(EXTRA_DESTINATION, destination)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        fun createIntent(context: Context, destination: Destination): Intent {
            return Intent(context, HostActivity::class.java).apply {
                putExtra(EXTRA_DESTINATION, destination)
            }
        }
    }

    private lateinit var navigator: Navigator

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleBack()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // FadeTransitionActivity handles edge-to-edge
        setContentView(R.layout.host_activity)

        navigator = Navigator(supportFragmentManager)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        if (savedInstanceState == null) {
            handleIntent(intent)
        }

        Logger.i(TAG, "HostActivity created")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val destination = BundleCompat.getParcelable(
            intent.extras ?: return,
            EXTRA_DESTINATION,
            Destination::class.java
        ) ?: return

        Logger.i(TAG, "Navigating to: ${destination::class.simpleName}")

        when (destination) {
            is Destination.Chat -> navigator.showChat(destination.intention)
            is Destination.Call -> navigator.showCall(destination.mediaType)
            is Destination.Survey -> navigator.showSurvey(destination.survey)
            is Destination.MessageCenter -> navigator.showMessageCenter(destination.queueIds)
            is Destination.WebBrowser -> navigator.showWebBrowser(destination.title, destination.url)
            is Destination.ImagePreview -> navigator.showImagePreview(
                destination.imageId,
                destination.imageName,
                destination.localImageUri
            )
            Destination.VisitorCode -> navigator.showVisitorCode()
        }
    }

    private fun handleBack() {
        // First try to pop back stack
        if (navigator.popBackStack()) {
            return
        }

        // Then dismiss dialogs
        if (navigator.hasDialogs) {
            navigator.dismissAllDialogs()
            finishIfEmpty()
            return
        }

        // Finally finish activity
        finishAndRemoveTask()
    }

    /**
     * Called by Fragments when they complete (e.g., Survey submitted, engagement ended).
     * Finishes the activity if no content remains.
     */
    fun finishIfEmpty() {
        if (navigator.isEmpty) {
            Logger.i(TAG, "No content remaining, finishing")
            finishAndRemoveTask()
        }
    }

    /**
     * Called by Fragments to navigate to another destination.
     */
    fun navigateTo(destination: Destination) {
        when (destination) {
            is Destination.Chat -> navigator.showChat(destination.intention)
            is Destination.Call -> navigator.showCall(destination.mediaType)
            is Destination.Survey -> navigator.showSurvey(destination.survey)
            is Destination.MessageCenter -> navigator.showMessageCenter(destination.queueIds)
            is Destination.WebBrowser -> navigator.showWebBrowser(destination.title, destination.url)
            is Destination.ImagePreview -> navigator.showImagePreview(
                destination.imageId,
                destination.imageName,
                destination.localImageUri
            )
            Destination.VisitorCode -> navigator.showVisitorCode()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onBackPressedCallback.remove()
        Logger.i(TAG, "HostActivity destroyed")
    }
}
```

#### 5. HostActivity Layout
**File**: `widgetssdk/src/main/res/layout/host_activity.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/fragment_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

#### 6. Update AndroidManifest.xml
Add HostActivity with existing translucent theme (inherits from `Application.Glia.Activity.Style`):
```xml
<activity
    android:name=".HostActivity"
    android:theme="@style/Application.Glia.Translucent.Activity"
    android:configChanges="orientation|screenSize|keyboardHidden"
    android:windowSoftInputMode="adjustResize"
    android:exported="false" />
```

#### 7. Update ActivityLauncher
**File**: `widgetssdk/src/main/java/com/glia/widgets/launcher/ActivityLauncher.kt`

Update to use `HostActivity`:
```kotlin
fun launchChat(context: Context, intention: Intention) {
    HostActivity.start(context, Destination.Chat(intention))
}

fun launchCall(context: Context, mediaType: String?) {
    HostActivity.start(context, Destination.Call(mediaType))
}

fun launchSurvey(context: Context, survey: Survey) {
    HostActivity.start(context, Destination.Survey(survey))
}

// ... etc
```

### Success Criteria:

#### Automated Verification:
- [x] Build completes: `./gradlew widgetssdk:assembleDebug`
- [x] Unit tests pass: `./gradlew widgetssdk:testDebugUnitTest`

#### Manual Verification:
- [ ] HostActivity launches with transparent background
- [ ] Client's UI visible when no fragment content
- [ ] Full-screen fragments show with proper backgrounds
- [ ] Edge-to-edge works: content extends behind system bars
- [ ] Status bar and navigation bar are transparent
- [ ] Fragment content has proper padding for system bars (not cut off)

---

## Phase 4: Create FragmentFactory for Centralized Fragment Creation

### Overview
Create a centralized `FragmentFactory` class instead of using companion object `newInstance()` methods.

### Changes Required:

#### 1. FragmentFactory
**File**: `widgetssdk/src/main/java/com/glia/widgets/di/FragmentFactory.kt`

```kotlin
package com.glia.widgets.di

import android.os.Bundle
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.call.CallFragment
import com.glia.widgets.chat.ChatFragment
import com.glia.widgets.chat.Intention
import com.glia.widgets.filepreview.ui.ImagePreviewDialogFragment
import com.glia.widgets.messagecenter.MessageCenterFragment
import com.glia.widgets.survey.SurveyBottomSheetFragment
import com.glia.widgets.view.dialog.VisitorCodeDialogFragment
import com.glia.widgets.webbrowser.WebBrowserFragment

/**
 * Factory for creating Fragment instances with proper arguments.
 *
 * This centralizes Fragment creation logic, making it easier to:
 * - Maintain consistent argument handling
 * - Test Fragment creation
 * - Discover available Fragments
 */
internal class FragmentFactory {

    // Full-screen fragments

    fun createChatFragment(intention: Intention): ChatFragment {
        return ChatFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ChatFragment.ARG_INTENTION, intention)
            }
        }
    }

    fun createCallFragment(mediaType: String?): CallFragment {
        return CallFragment().apply {
            arguments = Bundle().apply {
                mediaType?.let { putString(CallFragment.ARG_MEDIA_TYPE, it) }
            }
        }
    }

    fun createMessageCenterFragment(queueIds: ArrayList<String>?): MessageCenterFragment {
        return MessageCenterFragment().apply {
            arguments = Bundle().apply {
                queueIds?.let { putStringArrayList(MessageCenterFragment.ARG_QUEUE_IDS, it) }
            }
        }
    }

    fun createWebBrowserFragment(title: String, url: String): WebBrowserFragment {
        return WebBrowserFragment().apply {
            arguments = Bundle().apply {
                putString(WebBrowserFragment.ARG_TITLE, title)
                putString(WebBrowserFragment.ARG_URL, url)
            }
        }
    }

    // Bottom sheets

    fun createSurveyBottomSheet(survey: Survey): SurveyBottomSheetFragment {
        return SurveyBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putParcelable(SurveyBottomSheetFragment.ARG_SURVEY, survey)
            }
        }
    }

    // Dialogs

    fun createImagePreviewFragment(
        imageId: String? = null,
        imageName: String? = null,
        localImageUri: String? = null
    ): ImagePreviewDialogFragment {
        return ImagePreviewDialogFragment().apply {
            arguments = Bundle().apply {
                imageId?.let { putString(ImagePreviewDialogFragment.ARG_IMAGE_ID, it) }
                imageName?.let { putString(ImagePreviewDialogFragment.ARG_IMAGE_NAME, it) }
                localImageUri?.let { putString(ImagePreviewDialogFragment.ARG_LOCAL_IMAGE_URI, it) }
            }
        }
    }

    fun createVisitorCodeDialog(): VisitorCodeDialogFragment {
        return VisitorCodeDialogFragment()
    }

    fun createEndEngagementDialog(onConfirm: () -> Unit): EndEngagementDialogFragment {
        return EndEngagementDialogFragment().apply {
            this.onConfirmCallback = onConfirm
        }
    }

    fun createMediaUpgradeDialog(
        mediaType: String,
        onAccept: () -> Unit,
        onDecline: () -> Unit
    ): MediaUpgradeDialogFragment {
        return MediaUpgradeDialogFragment().apply {
            arguments = Bundle().apply {
                putString(MediaUpgradeDialogFragment.ARG_MEDIA_TYPE, mediaType)
            }
            this.onAcceptCallback = onAccept
            this.onDeclineCallback = onDecline
        }
    }
}
```

#### 2. Update Dependencies.kt
Add after `viewModelFactory`:
```kotlin
val fragmentFactory: FragmentFactory by lazy { FragmentFactory() }
```

### Success Criteria:

#### Automated Verification:
- [x] Build completes: `./gradlew widgetssdk:assembleDebug`
- [x] Unit tests pass: `./gradlew widgetssdk:testDebugUnitTest`

---

## Phase 5: Survey Screen Migration (Priority 1)

### Overview
Migrate Survey to `BottomSheetDialogFragment` with proper bottom sheet behavior. Move business logic from SurveyController to SurveyViewModel.

### Logic Distribution

**From SurveyController → SurveyViewModel:**
- State management and mutations (`SurveyController.java:32, 198-203`)
- Survey initialization and equality checks (`SurveyController.java:41-93`)
- Question-to-QuestionItem mapping (`SurveyController.java:95-111`)
- Answer collection and validation triggering (`SurveyController.java:119-157`)
- Submit orchestration and error handling (`SurveyController.java:168-196`)
- Cancel logic (`SurveyController.java:160-165`)
- Telemetry logging (currently in `SurveyView.kt:220, 225`)

**From SurveyView → SurveyBottomSheetFragment:**
- Button click intent dispatching
- State observation and rendering delegation to View
- One-time event handling (finish, scroll, toast)
- ViewModel lifecycle management

**Stays in SurveyView (UI drawing only):**
- Theme and style application (`SurveyView.kt:102-187`)
- View binding and references (`SurveyView.kt:61-71`)
- RecyclerView adapter setup (`SurveyView.kt:203-214`)
- Window insets handling (`SurveyView.kt:83`)
- State rendering to UI elements (`SurveyView.kt:238-242`)
- View commands execution (`SurveyView.kt:244-258` - scroll, hide keyboard)

### Changes Required:

#### 1. Survey MVI Contracts
**File**: `widgetssdk/src/main/java/com/glia/widgets/survey/SurveyMvi.kt`

Note: State, Intent, and Effect implement the marker interfaces from `base/MviContracts.kt`:

```kotlin
package com.glia.widgets.survey

import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.base.UiEffect
import com.glia.widgets.base.UiIntent
import com.glia.widgets.base.UiState

/**
 * UI state for Survey screen.
 */
internal data class SurveyUiState(
    val title: String? = null,
    val questions: List<QuestionItem> = emptyList(),
    val isSubmitting: Boolean = false
) : UiState

/**
 * User intents for Survey screen.
 */
internal sealed interface SurveyIntent : UiIntent {
    data class Initialize(val survey: Survey) : SurveyIntent
    data class AnswerQuestion(val answer: Survey.Answer) : SurveyIntent
    data object SubmitSurvey : SurveyIntent
    data object CancelSurvey : SurveyIntent
}

/**
 * One-time effects for Survey screen.
 */
internal sealed interface SurveyEffect : UiEffect {
    data object Dismiss : SurveyEffect
    data object HideSoftKeyboard : SurveyEffect
    data class ScrollToQuestion(val index: Int) : SurveyEffect
    data object ShowNetworkError : SurveyEffect
}
```

#### 2. SurveyViewModel
**File**: `widgetssdk/src/main/java/com/glia/widgets/survey/SurveyViewModel.kt`

```kotlin
package com.glia.widgets.survey

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.base.BaseViewModel
import com.glia.widgets.core.survey.SurveyAnswerUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.LogEvents
import com.glia.widgets.helper.EventAttribute
import com.glia.widgets.helper.ButtonNames

internal class SurveyViewModel(
    private val surveyAnswerUseCase: SurveyAnswerUseCase
) : BaseViewModel<SurveyUiState, SurveyIntent, SurveyEffect>(SurveyUiState()) {

    private var survey: Survey? = null

    override suspend fun handleIntent(intent: SurveyIntent) {
        when (intent) {
            is SurveyIntent.Initialize -> handleInitialize(intent.survey)
            is SurveyIntent.AnswerQuestion -> handleAnswer(intent.answer)
            SurveyIntent.SubmitSurvey -> handleSubmit()
            SurveyIntent.CancelSurvey -> handleCancel()
        }
    }

    private fun handleInitialize(survey: Survey) {
        if (isAlreadyInit(survey)) return

        this.survey = survey
        val questions = survey.questions?.map { makeQuestionItem(it) } ?: emptyList()
        updateState {
            copy(title = survey.title, questions = questions)
        }
    }

    private fun isAlreadyInit(newSurvey: Survey): Boolean {
        return survey != null && survey?.id == newSurvey.id
    }

    private fun makeQuestionItem(question: Survey.Question): QuestionItem {
        var answer: Survey.Answer? = null
        if (question.type == Survey.Question.QuestionType.SINGLE_CHOICE) {
            question.options
                ?.firstOrNull { it.isDefault }
                ?.let { option ->
                    answer = Survey.Answer.makeAnswer(question.id, option.id)
                }
        }
        return QuestionItem(question, answer)
    }

    private fun handleAnswer(answer: Survey.Answer) {
        val questions = currentState.questions.toMutableList()
        val index = questions.indexOfFirst { it.question.id == answer.questionId }
        if (index >= 0) {
            val item = questions[index]
            questions[index] = item.copy(answer = answer, showError = false)
            updateState { copy(questions = questions) }

            if (item.question.type != Survey.Question.QuestionType.TEXT) {
                sendEffect(SurveyEffect.HideSoftKeyboard)
            }
        }
    }

    private suspend fun handleSubmit() {
        Logger.i(LogEvents.SURVEY_SCREEN_BUTTON_CLICKED, mapOf(EventAttribute.ButtonName to ButtonNames.SUBMIT))

        val questions = currentState.questions
        val survey = this.survey ?: return

        updateState { copy(isSubmitting = true) }

        surveyAnswerUseCase.submit(questions, survey) { exception ->
            updateState { copy(isSubmitting = false) }

            if (exception == null) {
                sendEffect(SurveyEffect.Dismiss)
                return@submit
            }

            if (exception is GliaException && exception.cause == GliaException.Cause.NETWORK_TIMEOUT) {
                sendEffect(SurveyEffect.ShowNetworkError)
            }

            val updatedQuestions = questions.map { item ->
                val hasError = item.question.isRequired && item.answer == null
                item.copy(showError = hasError)
            }
            updateState { copy(questions = updatedQuestions) }

            val firstErrorIndex = updatedQuestions.indexOfFirst { it.showError }
            if (firstErrorIndex >= 0) {
                sendEffect(SurveyEffect.ScrollToQuestion(firstErrorIndex))
            }
        }
    }

    private fun handleCancel() {
        Logger.i(LogEvents.SURVEY_SCREEN_BUTTON_CLICKED, mapOf(EventAttribute.ButtonName to ButtonNames.CANCEL))
        sendEffect(SurveyEffect.Dismiss)
    }
}
```

#### 3. SurveyBottomSheetFragment
**File**: `widgetssdk/src/main/java/com/glia/widgets/survey/SurveyBottomSheetFragment.kt`

Note the simplified implementation - no manual Flow collection needed, just implement `handleState` and `handleEffect`:

```kotlin
package com.glia.widgets.survey

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.fragment.app.viewModels
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.HostActivity
import com.glia.widgets.R
import com.glia.widgets.base.BaseBottomSheetFragment
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.insetsController
import com.glia.widgets.helper.showToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * BottomSheetDialogFragment hosting SurveyView with MVI architecture.
 *
 * Displayed as a bottom sheet over the current content (Chat, Call, or transparent).
 * Provides proper bottom sheet behavior (dragging, collapsing, etc.).
 */
internal class SurveyBottomSheetFragment :
    BaseBottomSheetFragment<SurveyUiState, SurveyEffect, SurveyViewModel>() {

    internal companion object {
        internal const val ARG_SURVEY = "arg_survey"
    }

    private var surveyView: SurveyView? = null
    override val viewModel: SurveyViewModel by viewModels { Dependencies.viewModelFactory }
    private val localeProvider by lazy { Dependencies.localeProvider }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: BottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            isDraggable = true
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        surveyView = SurveyView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return surveyView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Initialize survey from arguments before base class calls setupViews/handleState/handleEffect
        arguments?.let { args ->
            BundleCompat.getParcelable(args, ARG_SURVEY, Survey::class.java)?.let { survey ->
                viewModel.processIntent(SurveyIntent.Initialize(survey))
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun setupViews() {
        surveyView?.apply {
            setOnAnswerListener { answer ->
                viewModel.processIntent(SurveyIntent.AnswerQuestion(answer))
            }
            setOnSubmitClickListener {
                viewModel.processIntent(SurveyIntent.SubmitSurvey)
            }
            setOnCancelClickListener {
                viewModel.processIntent(SurveyIntent.CancelSurvey)
            }
        }
    }

    override fun handleState(state: SurveyUiState) {
        surveyView?.renderState(
            SurveyState.Builder()
                .setTitle(state.title)
                .setQuestions(state.questions)
                .createSurveyState()
        )
    }

    override fun handleEffect(effect: SurveyEffect) {
        when (effect) {
            SurveyEffect.Dismiss -> {
                dismissAllowingStateLoss()
                (activity as? HostActivity)?.finishIfEmpty()
            }
            SurveyEffect.HideSoftKeyboard -> {
                surveyView?.insetsController?.hideKeyboard()
            }
            is SurveyEffect.ScrollToQuestion -> {
                surveyView?.scrollTo(effect.index)
            }
            SurveyEffect.ShowNetworkError -> {
                context?.showToast(localeProvider.getString(R.string.glia_survey_network_unavailable))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        surveyView = null
    }
}
```

#### 4. BaseBottomSheetFragment
**File**: `widgetssdk/src/main/java/com/glia/widgets/base/BaseBottomSheetFragment.kt`

**Design**: Generic BottomSheetDialogFragment that mirrors `BaseFragment` pattern - auto-collects state/effects and delegates to `handleState()`/`handleEffect()`.

**Edge-to-edge support**: BottomSheetDialogFragment creates its own window, so we need to ensure edge-to-edge is properly configured. The child View (SurveyView) handles its own insets via `SimpleWindowInsetsAndAnimationHandler`.

```kotlin
package com.glia.widgets.base

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Base BottomSheetDialogFragment with MVI architecture and edge-to-edge support.
 *
 * Automatically collects state and effects from the ViewModel and delegates to
 * [handleState] and [handleEffect] methods.
 *
 * Edge-to-edge handling:
 * - Dialog window is configured for edge-to-edge in onCreateDialog
 * - Child Views should use SimpleWindowInsetsAndAnimationHandler for content padding
 *
 * @param State UI state type implementing [UiState]
 * @param Effect One-time effect type implementing [UiEffect]
 * @param VM ViewModel type extending [BaseViewModel]
 */
internal abstract class BaseBottomSheetFragment<State : UiState, Effect : UiEffect, VM : BaseViewModel<State, *, Effect>> :
    BottomSheetDialogFragment() {

    /**
     * The ViewModel instance for this Fragment.
     * Subclasses should use `by viewModels { Dependencies.viewModelFactory }` to initialize.
     */
    protected abstract val viewModel: VM

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            // Enable edge-to-edge for the bottom sheet dialog
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        collectState()
        collectEffects()
    }

    /**
     * Called after view is created. Override to set up view listeners and initial configuration.
     */
    protected open fun setupViews() {}

    /**
     * Called when new state is emitted from the ViewModel.
     * Override to render the state to the UI.
     */
    protected abstract fun handleState(state: State)

    /**
     * Called when a one-time effect is emitted from the ViewModel.
     * Override to handle navigation, toasts, dialogs, etc.
     */
    protected abstract fun handleEffect(effect: Effect)

    private fun collectState() {
        viewModel.state.collectWithLifecycle { state ->
            handleState(state)
        }
    }

    private fun collectEffects() {
        viewModel.effect.collectWithLifecycle { effect ->
            handleEffect(effect)
        }
    }

    /**
     * Extension function for lifecycle-aware Flow collection.
     */
    protected fun <T> Flow<T>.collectWithLifecycle(
        state: Lifecycle.State = Lifecycle.State.STARTED,
        action: suspend (T) -> Unit
    ) {
        flowWithLifecycle(viewLifecycleOwner.lifecycle, state)
            .onEach(action)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
```

#### 5. Update SurveyView
Remove business logic, keep only UI rendering. Add new listener interfaces as described in earlier phases.

#### 6. Update ViewModelFactory
Add SurveyViewModel case.

#### 7. Remove SurveyActivity
Mark as `@Deprecated` - calls now go through `HostActivity` with `Destination.Survey`.

### Success Criteria:

#### Automated Verification:
- [ ] Build completes: `./gradlew widgetssdk:assembleDebug`
- [ ] Unit tests pass: `./gradlew widgetssdk:testDebugUnitTest`
- [ ] SurveyViewModel unit tests added and passing

#### Manual Verification:
- [ ] Survey shows as proper bottom sheet
- [ ] Bottom sheet can be dragged
- [ ] Client's UI visible behind (when no other content)
- [ ] All question types work
- [ ] Submit/cancel work
- [ ] Keyboard behavior correct
- [ ] State survives rotation

---

## Phase 6: WebBrowser Screen Migration (Priority 1)

### Overview
Migrate WebBrowser to a regular Fragment hosted in `HostActivity`.

### Changes Required:
- Create `WebBrowserFragment` extending `BaseFragment`
- Create minimal `WebBrowserViewModel`
- Update `Navigator` to handle WebBrowser destination
- Mark `WebBrowserActivity` as `@Deprecated`

### Success Criteria:
- [ ] Build completes
- [ ] WebBrowser works through HostActivity

---

## Phase 7: ImagePreview Screen Migration (Priority 1)

### Overview
Migrate ImagePreview to `DialogFragment` hosted in `HostActivity`.

### Changes Required:
- Create `ImagePreviewDialogFragment`
- Create `ImagePreviewViewModel` from controller logic
- Update `Navigator` to handle ImagePreview destination
- Mark `ImagePreviewActivity` as `@Deprecated`

### Success Criteria:
- [ ] Build completes
- [ ] ImagePreview works as dialog through HostActivity

---

## Phase 8: VisitorCode Migration (Priority 2)

### Overview
Migrate VisitorCode to `DialogFragment` hosted in `HostActivity`.

### Changes Required:
- Create `VisitorCodeDialogFragment`
- Create `VisitorCodeViewModel` from controller logic
- Update `Navigator` to handle VisitorCode destination
- Mark `DialogHolderActivity` as `@Deprecated`

### Success Criteria:
- [ ] Build completes
- [ ] VisitorCode works as dialog through HostActivity

---

## Phase 9: MessageCenter Screen Migration (Priority 2)

### Overview
Migrate MessageCenter to a regular Fragment with proper file picker handling.

### Logic Distribution

**From MessageCenterController → MessageCenterViewModel:**
- Message composition state
- Attachment validation
- Send message orchestration
- 13 use case coordinations

**From MessageCenterView → MessageCenterFragment:**
- Activity result launchers for file picking (camera, gallery, files)
- Permission requests
- Navigation callbacks

**Stays in MessageCenterView:**
- Theme application, RecyclerView setup, visual state rendering

### Changes Required:
- Create `MessageCenterFragment`
- Create `MessageCenterViewModel`
- Move activity result launchers to Fragment
- Mark `MessageCenterActivity` as `@Deprecated`

### Success Criteria:
- [ ] Build completes
- [ ] Message sending works
- [ ] Attachment picking works

---

## Phase 10: EntryWidget Migration (Priority 2)

### Overview
EntryWidget already uses a Fragment. Migrate controller to ViewModel.

### Changes Required:
- Create `EntryWidgetViewModel`
- Update `EntryWidgetFragment` to use ViewModel
- Remove controller reference from `EntryWidgetView`

### Success Criteria:
- [ ] Build completes
- [ ] EntryWidget works with ViewModel

---

## Phase 11: Call Screen Migration (Priority 3 - HIGH)

### Overview
Call screen is complex with video handling. Create `CallFragment` with careful video lifecycle management.

### Logic Distribution

**From CallController → CallViewModel:**
- Media state debouncing → Flow debounce operator
- Timer management → ViewModel-scoped timers
- All business logic from `CallController.kt`

**From CallView → CallFragment:**
- Video lifecycle callbacks (onResume/onPause)
- Dialog management (media upgrade, end engagement)
- Navigation callbacks

**Stays in CallView:**
- State rendering, button states, theme application, video UI rendering

### Changes Required:
- Create `CallFragment`
- Create `CallViewModel`
- Move dialog handling to Fragment
- Mark `CallActivity` as `@Deprecated`

### Key Challenges:
- WebRTC VideoView lifecycle management
- 200ms media state debounce critical
- Multiple concurrent subscriptions

### Success Criteria:
- [ ] Audio/video calls work
- [ ] Media upgrade works
- [ ] Video survives rotation

---

## Phase 12: Chat Screen Migration (Priority 3 - VERY HIGH)

### Overview
Chat is the most complex screen. Create `ChatFragment` with careful state management.

### Logic Distribution

**From ChatController → ChatViewModel:**
- 50+ use case dependencies
- ChatManager integration
- Media upgrade handling
- File attachment uploads

**From ChatView → ChatFragment:**
- Activity result launchers (camera, gallery, files)
- Dialog management
- Navigation

**Stays in ChatView:**
- State rendering, RecyclerView management, theme application

### Changes Required:
- Create `ChatFragment`
- Create `ChatViewModel`
- Move activity result launchers to Fragment
- Mark `ChatActivity` as `@Deprecated`

### Key Challenges:
- 50+ use case dependencies
- Complex adapter with WebView items
- ChatManager state integration

### Success Criteria:
- [ ] Messaging works
- [ ] File attachments work
- [ ] GVA cards render correctly
- [ ] State survives rotation

---

## Phase 13: Cleanup and Legacy Activity Removal

### Overview
Remove deprecated Activities and clean up.

### Changes Required:

1. **Remove deprecated Activities** after migration validated:
   - `SurveyActivity`
   - `WebBrowserActivity`
   - `ImagePreviewActivity`
   - `DialogHolderActivity`
   - `MessageCenterActivity`
   - `CallActivity`
   - `ChatActivity`

2. **Update ActivityLauncher** to only use `HostActivity`

3. **Update AndroidManifest** to remove old activity declarations

4. **Update unit tests** - Controller tests → ViewModel tests

5. **Update CLAUDE.md** documentation

### Success Criteria:
- [ ] All deprecated Activities removed
- [ ] Only `HostActivity` remains
- [ ] All tests pass

---

## Testing Strategy

### Unit Tests (JUnit + MockK + Turbine)
- Test all ViewModel intents and state transitions
- Test effect emissions
- Use Turbine for Flow testing
- Use StandardTestDispatcher for coroutine control

### Snapshot Tests (Paparazzi)
- Keep existing View snapshot tests
- Add Fragment state snapshot tests

### Manual Testing Checklist per Screen
1. Basic functionality works
2. State survives rotation
3. Keyboard behavior correct
4. TalkBack accessibility works
5. Theme styling applies
6. Error handling works

---

## RxJava to Flow Bridge Pattern

For ViewModels consuming existing RxJava repositories:

```kotlin
// In ViewModel
private fun observeEngagementState() {
    viewModelScope.launch {
        engagementRepository.engagementState
            .asFlow() // RxJava Flowable → Kotlin Flow
            .collect { state ->
                updateState { copy(engagementState = state) }
            }
    }
}
```

This allows gradual migration without rewriting repositories.

---

## Summary of Architectural Improvements

1. **Unified Transparent HostActivity** - Single entry point for all SDK screens
2. **Cross-platform compatible** - Works with Flutter, React Native, Unity, etc.
3. **Proper BottomSheet behavior** - Survey slides up with native bottom sheet UX
4. **Centralized FragmentFactory** - Aligns with existing ControllerFactory pattern
5. **Clear separation of concerns** - Views ONLY render, Fragments coordinate, ViewModels own business logic
6. **No "Glia" prefix on internal classes** - Clean internal code, branded public API

---

## References

- Research: `~/.claude/research/2026-02-02-architecture-migration-mvp-to-mvi-clean.md`
- Survey: `widgetssdk/src/main/java/com/glia/widgets/survey/`
- WebBrowser: `widgetssdk/src/main/java/com/glia/widgets/webbrowser/`
- ImagePreview: `widgetssdk/src/main/java/com/glia/widgets/filepreview/ui/`
- VisitorCode: `widgetssdk/src/main/java/com/glia/widgets/view/VisitorCodeView.kt`
- MessageCenter: `widgetssdk/src/main/java/com/glia/widgets/messagecenter/`
- EntryWidget: `widgetssdk/src/main/java/com/glia/widgets/entrywidget/`
- Call: `widgetssdk/src/main/java/com/glia/widgets/call/`
- Chat: `widgetssdk/src/main/java/com/glia/widgets/chat/`
- Dependencies: `widgetssdk/src/main/java/com/glia/widgets/di/Dependencies.kt`