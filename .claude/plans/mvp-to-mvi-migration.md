# MVP to MVI + Clean Architecture Migration - Complete Implementation Plan

## Overview

Migrate the GliaWidgets Android SDK from MVP architecture (Activities + Views + Controllers) to Single Activity + Fragments + ViewModels + MVI pattern. **Key approach**: Reuse existing custom Views (SurveyView, ChatView, CallView, etc.) inside Fragments while moving business logic from Controllers to ViewModels.

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

| Screen | Activity | View | Controller | Priority |
|--------|----------|------|------------|----------|
| Survey | SurveyActivity | SurveyView | SurveyController (Java) | 1 - LOW |
| WebBrowser | WebBrowserActivity | WebBrowserView | None | 1 - LOW |
| ImagePreview | ImagePreviewActivity | ImagePreviewView | ImagePreviewController | 1 - LOW |
| VisitorCode | DialogHolderActivity | VisitorCodeView | VisitorCodeController | 2 - LOW |
| MessageCenter | MessageCenterActivity | MessageCenterView | MessageCenterController | 2 - MEDIUM |
| EntryWidget | EntryWidgetActivity | EntryWidgetView | EntryWidgetController | 2 - MEDIUM |
| Call | CallActivity | CallView | CallController (649 lines) | 3 - HIGH |
| Chat | ChatActivity | ChatView | ChatController (1140 lines) | 3 - VERY HIGH |

## Desired End State

After completing this plan:
- All screens migrated to Fragment + ViewModel + MVI architecture
- Existing custom Views (SurveyView, ChatView, etc.) reused inside Fragments
- Business logic moved from Controllers to ViewModels
- Foundation infrastructure supports incremental migration
- All tests pass with new ViewModel unit tests

### Key Patterns to Use:
- **Flow Collection**: `flow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { }.launchIn(lifecycleScope)`
- **State**: `StateFlow<UiState>` for UI state
- **Effects**: `Channel<Effect>` for one-time events (navigation, toasts)
- **Intents**: Sealed interface for user actions
- **RxJava Bridge**: Use `asFlow()` to consume existing RxJava streams in ViewModels

## What We're NOT Doing

- NOT rewriting custom Views (reusing SurveyView, ChatView, etc.)
- NOT converting all repositories from RxJava to Flow (using `asFlow()` bridge)
- NOT breaking public APIs (`EngagementLauncher`, `EntryWidget`, `GliaWidgets`)
- NOT using Jetpack Compose (traditional View system only)
- NOT using Navigation Component (custom FragmentManager navigation for now)

---

## Phase 1: Add Dependencies

### Overview
Add Coroutines, Lifecycle ViewModel, and RxJava-Coroutines interop dependencies.

### Changes Required:

#### 1. Version Catalog
**File**: `gradle/libs.versions.toml`

Add to `[versions]` section:
```toml
testTurbineVersion = "1.2.0"
```

Add to `[libraries]` section (after line 69):
```toml
lifecycle-viewmodel-ktx = { module = "androidx.lifecycle:lifecycle-viewmodel-ktx", version.ref = "javaLifecycleProcessVersion" }
lifecycle-runtime-ktx = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "javaLifecycleProcessVersion" }
java-coroutines-rx3 = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-rx3", version.ref = "javaCoroutinesVersion" }
test-turbine = { module = "app.cash.turbine:turbine", version.ref = "testTurbineVersion" }
test-coroutines = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "javaCoroutinesVersion" }
```

#### 2. Module Build File
**File**: `widgetssdk/build.gradle`

Add after line 178:
```groovy
implementation libs.java.coroutines
implementation libs.lifecycle.viewmodel.ktx
implementation libs.lifecycle.runtime.ktx
implementation libs.java.coroutines.rx3
```

Add to test dependencies (after line 226):
```groovy
testImplementation libs.test.turbine
testImplementation libs.test.coroutines
```

### Success Criteria:

#### Automated Verification:
- [x] Project syncs: `./gradlew widgetssdk:dependencies`
- [x] Build completes: `./gradlew widgetssdk:assembleDebug`

---

## Phase 2: Create Base Infrastructure Classes

### Overview
Create reusable base classes for Fragments and ViewModels that implement MVI pattern.

### Changes Required:

#### 1. Base ViewModel
**File**: `widgetssdk/src/main/java/com/glia/widgets/base/GliaBaseViewModel.kt`

```kotlin
package com.glia.widgets.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel implementing MVI pattern.
 * @param State Immutable UI state data class
 * @param Intent User actions/events
 * @param Effect One-time side effects (navigation, toasts)
 */
internal abstract class GliaBaseViewModel<State, Intent, Effect>(
    initialState: State
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    protected val currentState: State get() = _state.value

    fun processIntent(intent: Intent) {
        viewModelScope.launch { handleIntent(intent) }
    }

    protected abstract suspend fun handleIntent(intent: Intent)

    protected fun updateState(reducer: State.() -> State) {
        _state.value = currentState.reducer()
    }

    protected suspend fun emitEffect(effect: Effect) {
        _effect.send(effect)
    }

    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
```

#### 2. Base Fragment
**File**: `widgetssdk/src/main/java/com/glia/widgets/base/GliaBaseFragment.kt`

```kotlin
package com.glia.widgets.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Base Fragment for Glia SDK screens with lifecycle-aware Flow collection.
 */
internal abstract class GliaBaseFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    protected fun <T> Flow<T>.collectWithLifecycle(
        state: Lifecycle.State = Lifecycle.State.STARTED,
        action: suspend (T) -> Unit
    ) {
        flowWithLifecycle(viewLifecycleOwner.lifecycle, state)
            .onEach(action)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeState()
        observeEffects()
    }

    protected open fun setupViews() {}
    protected abstract fun observeState()
    protected abstract fun observeEffects()
}
```

#### 3. ViewModelFactory
**File**: `widgetssdk/src/main/java/com/glia/widgets/di/GliaViewModelFactory.kt`

```kotlin
package com.glia.widgets.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for creating ViewModels with dependencies from the SDK's DI system.
 */
internal class GliaViewModelFactory(
    private val useCaseFactory: UseCaseFactory,
    private val repositoryFactory: RepositoryFactory
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // ViewModels will be added as screens are migrated
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
```

#### 4. Update Dependencies.kt
**File**: `widgetssdk/src/main/java/com/glia/widgets/di/Dependencies.kt`

Add after line 85:
```kotlin
@JvmStatic
val viewModelFactory: ViewModelProvider.Factory by lazy {
    GliaViewModelFactory(useCaseFactory, repositoryFactory)
}
```

### Success Criteria:

#### Automated Verification:
- [ ] Build completes: `./gradlew widgetssdk:assembleDebug`
- [ ] Unit tests pass: `./gradlew widgetssdk:testDebugUnitTest`

---

## Phase 3: Survey Screen Migration (Priority 1)

### Overview
Migrate Survey screen: Create SurveyFragment that hosts existing SurveyView, move business logic from SurveyController to SurveyViewModel.

### Changes Required:

#### 1. Survey MVI Contracts
**File**: `widgetssdk/src/main/java/com/glia/widgets/survey/SurveyMvi.kt`

```kotlin
package com.glia.widgets.survey

import com.glia.androidsdk.engagement.Survey

internal data class SurveyUiState(
    val title: String? = null,
    val questions: List<QuestionItem> = emptyList(),
    val isSubmitting: Boolean = false
)

internal sealed interface SurveyIntent {
    data class Initialize(val survey: Survey) : SurveyIntent
    data class AnswerQuestion(val answer: Survey.Answer) : SurveyIntent
    data object SubmitSurvey : SurveyIntent
    data object CancelSurvey : SurveyIntent
}

internal sealed interface SurveyEffect {
    data object NavigateBack : SurveyEffect
    data object HideSoftKeyboard : SurveyEffect
    data class ScrollToQuestion(val index: Int) : SurveyEffect
    data object ShowNetworkError : SurveyEffect
}
```

#### 2. SurveyViewModel
**File**: `widgetssdk/src/main/java/com/glia/widgets/survey/SurveyViewModel.kt`

Port business logic from `SurveyController.java` to ViewModel:
- `handleInitialize()` - from `init()` and `setQuestions()`
- `handleAnswer()` - from `onAnswer()` and `setAnswer()`
- `handleSubmit()` - from `onSubmitClicked()`
- `handleCancel()` - from `onCancelClicked()`

#### 3. Update GliaViewModelFactory
Add SurveyViewModel case to `create()` method.

#### 4. SurveyFragment
**File**: `widgetssdk/src/main/java/com/glia/widgets/survey/SurveyFragment.kt`

```kotlin
package com.glia.widgets.survey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.fragment.app.viewModels
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.R
import com.glia.widgets.base.GliaBaseFragment
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.insetsController

internal class SurveyFragment : GliaBaseFragment(R.layout.survey_fragment) {

    private var surveyView: SurveyView? = null
    private val viewModel: SurveyViewModel by viewModels { Dependencies.viewModelFactory }
    private val localeProvider by lazy { Dependencies.localeProvider }

    interface OnFinishListener {
        fun onSurveyFinish()
    }

    companion object {
        private const val ARG_SURVEY = "arg_survey"

        fun newInstance(survey: Survey): SurveyFragment {
            return SurveyFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_SURVEY, survey)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Reuse existing SurveyView
        surveyView = SurveyView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        return surveyView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize with survey from arguments
        arguments?.let { args ->
            BundleCompat.getParcelable(args, ARG_SURVEY, Survey::class.java)?.let { survey ->
                viewModel.processIntent(SurveyIntent.Initialize(survey))
            }
        }
    }

    override fun setupViews() {
        surveyView?.apply {
            // Set up callbacks that route to ViewModel
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

    override fun observeState() {
        viewModel.state.collectWithLifecycle { state ->
            surveyView?.onStateUpdated(
                SurveyState.Builder()
                    .setTitle(state.title)
                    .setQuestions(state.questions)
                    .createSurveyState()
            )
        }
    }

    override fun observeEffects() {
        viewModel.effect.collectWithLifecycle { effect ->
            when (effect) {
                SurveyEffect.NavigateBack -> {
                    (activity as? OnFinishListener)?.onSurveyFinish()
                }
                SurveyEffect.HideSoftKeyboard -> {
                    surveyView?.insetsController?.hideKeyboard()
                }
                is SurveyEffect.ScrollToQuestion -> {
                    surveyView?.scrollTo(effect.index)
                }
                SurveyEffect.ShowNetworkError -> {
                    surveyView?.onNetworkTimeout()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        surveyView = null
    }
}
```

#### 5. SurveyFragment Layout
**File**: `widgetssdk/src/main/res/layout/survey_fragment.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/survey_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/glia_black_color_opacity_60" />
```

#### 6. Update SurveyActivity
**File**: `widgetssdk/src/main/java/com/glia/widgets/survey/SurveyActivity.kt`

Refactor to host SurveyFragment instead of SurveyView:
- Change layout to use fragment container
- Add SurveyFragment in onCreate if savedInstanceState == null
- Implement SurveyFragment.OnFinishListener
- Keep existing touch-outside-to-dismiss and animation logic

### Success Criteria:

#### Automated Verification:
- [ ] Build completes: `./gradlew widgetssdk:assembleDebug`
- [ ] Unit tests pass: `./gradlew widgetssdk:testDebugUnitTest`

#### Manual Verification:
- [ ] Survey displays correctly
- [ ] All question types work
- [ ] Submit/cancel work
- [ ] Keyboard behavior correct
- [ ] State survives rotation
- [ ] TalkBack works

---

## Phase 4: WebBrowser Screen Migration (Priority 1)

### Overview
WebBrowser is the simplest screen - no Controller exists. Create WebBrowserFragment with minimal ViewModel.

### Changes Required:

#### 1. WebBrowser MVI Contracts
**File**: `widgetssdk/src/main/java/com/glia/widgets/webbrowser/WebBrowserMvi.kt`

```kotlin
internal data class WebBrowserUiState(
    val title: String = "",
    val url: String = ""
)

internal sealed interface WebBrowserIntent {
    data class Initialize(val title: String, val url: String) : WebBrowserIntent
    data class OnLinkClicked(val url: String) : WebBrowserIntent
}

internal sealed interface WebBrowserEffect {
    data object NavigateBack : WebBrowserEffect
    data class OpenExternalLink(val url: String) : WebBrowserEffect
}
```

#### 2. WebBrowserViewModel
**File**: `widgetssdk/src/main/java/com/glia/widgets/webbrowser/WebBrowserViewModel.kt`

Minimal ViewModel - just holds title/URL state.

#### 3. WebBrowserFragment
**File**: `widgetssdk/src/main/java/com/glia/widgets/webbrowser/WebBrowserFragment.kt`

Host existing WebBrowserView inside fragment.

#### 4. Update WebBrowserActivity
Host fragment instead of view directly.

### Success Criteria:

#### Automated Verification:
- [ ] Build completes: `./gradlew widgetssdk:assembleDebug`
- [ ] Unit tests pass: `./gradlew widgetssdk:testDebugUnitTest`

---

## Phase 5: ImagePreview Screen Migration (Priority 1)

### Overview
Migrate ImagePreviewController logic to ImagePreviewViewModel.

### Changes Required:

#### 1. ImagePreview MVI Contracts
**File**: `widgetssdk/src/main/java/com/glia/widgets/filepreview/ui/ImagePreviewMvi.kt`

```kotlin
internal data class ImagePreviewUiState(
    val imageLoadingState: ImageLoadingState = ImageLoadingState.INITIAL,
    val isShowShareButton: Boolean = false,
    val isShowDownloadButton: Boolean = false,
    val imageName: String = "",
    val loadedImage: Bitmap? = null,
    val localImageUri: Uri? = null
)

internal sealed interface ImagePreviewIntent {
    data class OnRemoteImageReceived(val imageId: String, val imageName: String) : ImagePreviewIntent
    data class OnLocalImageReceived(val uri: Uri) : ImagePreviewIntent
    data object OnSharePressed : ImagePreviewIntent
    data object OnDownloadPressed : ImagePreviewIntent
    data object OnImageRequested : ImagePreviewIntent
}

internal sealed interface ImagePreviewEffect {
    data class ShareImage(val uri: Uri, val imageName: String) : ImagePreviewEffect
    data object ShowSaveSuccess : ImagePreviewEffect
    data object ShowSaveFailed : ImagePreviewEffect
    data object ShowLoadingFailed : ImagePreviewEffect
}
```

#### 2. ImagePreviewViewModel
Port logic from `ImagePreviewController.kt`.

#### 3. ImagePreviewFragment
Host existing ImagePreviewView, wire to ViewModel.

#### 4. Update ImagePreviewActivity
Host fragment.

### Success Criteria:

#### Automated Verification:
- [ ] Build completes: `./gradlew widgetssdk:assembleDebug`
- [ ] Unit tests pass: `./gradlew widgetssdk:testDebugUnitTest`

---

## Phase 6: VisitorCode Component Migration (Priority 2)

### Overview
VisitorCode is used both embedded and in dialogs. Migrate VisitorCodeController to ViewModel.

### Changes Required:

#### 1. VisitorCode MVI Contracts
#### 2. VisitorCodeViewModel
#### 3. Update VisitorCodeView to work with ViewModel
#### 4. Create VisitorCodeFragment for dialog usage

### Success Criteria:

#### Automated Verification:
- [ ] Build completes
- [ ] Unit tests pass

---

## Phase 7: MessageCenter Screen Migration (Priority 2)

### Overview
MessageCenter has medium complexity with attachment handling. Port MessageCenterController to ViewModel.

### Changes Required:

#### 1. MessageCenter MVI Contracts
**File**: `widgetssdk/src/main/java/com/glia/widgets/messagecenter/MessageCenterMvi.kt`

State will use existing `MessageCenterState` structure.

#### 2. MessageCenterViewModel
Port logic from `MessageCenterController.kt` (13 use cases).

#### 3. MessageCenterFragment
Host existing MessageCenterView, handle activity result launchers for file picking.

#### 4. Update MessageCenterActivity
Host fragment.

### Success Criteria:

#### Automated Verification:
- [ ] Build completes
- [ ] Unit tests pass

#### Manual Verification:
- [ ] Message sending works
- [ ] Attachment picking works (gallery, camera, files)
- [ ] Confirmation screen displays

---

## Phase 8: EntryWidget Screen Migration (Priority 2)

### Overview
EntryWidget already uses a Fragment. Migrate EntryWidgetController to ViewModel.

### Changes Required:

#### 1. EntryWidget MVI Contracts
#### 2. EntryWidgetViewModel

Port logic from `EntryWidgetController.kt` including:
- 5-way `Flowable.combineLatest()` → convert to Flow
- Item mapping logic
- Engagement launching

#### 3. Update EntryWidgetFragment
Use ViewModel instead of Controller.

#### 4. Update EntryWidgetView
Remove controller reference, work with Fragment callbacks.

### Success Criteria:

#### Automated Verification:
- [ ] Build completes
- [ ] Unit tests pass

---

## Phase 9: Call Screen Migration (Priority 3 - HIGH)

### Overview
Call screen is complex with video handling and multiple timers. Requires careful migration.

### Changes Required:

#### 1. Call MVI Contracts
Use existing `CallState.java` converted to Kotlin data class or wrapped.

#### 2. CallViewModel
Port logic from `CallController.kt` (649 lines):
- 3 CompositeDisposables → viewModelScope coroutines
- Media state debouncing → Flow debounce operator
- Timer management → ViewModel-scoped timers
- Video lifecycle callbacks

#### 3. CallFragment
Host existing CallView with careful video lifecycle handling:
- Register activity result launchers in Fragment
- Forward lifecycle events to View for video rendering

#### 4. Update CallActivity
Host fragment, maintain video view lifecycle.

### Key Challenges:
- WebRTC VideoView lifecycle management
- 200ms media state debounce critical for audio call errors
- Inactivity timer for landscape UI auto-hide
- Multiple concurrent subscriptions

### Success Criteria:

#### Automated Verification:
- [ ] Build completes
- [ ] Unit tests pass

#### Manual Verification:
- [ ] Audio calls work
- [ ] Video calls work (both directions)
- [ ] Media upgrade works
- [ ] Video survives rotation
- [ ] Timer displays correctly
- [ ] Landscape UI auto-hides

---

## Phase 10: Chat Screen Migration (Priority 3 - VERY HIGH)

### Overview
Chat is the most complex screen (1140 line controller). Requires careful, methodical migration.

### Changes Required:

#### 1. Chat MVI Contracts
Use existing `ChatState.kt` (already immutable Kotlin data class).

#### 2. ChatViewModel
Port logic from `ChatController.kt`:
- 50+ constructor dependencies
- 3 CompositeDisposables → viewModelScope
- ChatManager integration
- Media upgrade handling
- Operator typing indicators
- File attachment uploads

#### 3. ChatFragment
Host existing ChatView:
- Register activity result launchers for camera/gallery/files
- Handle file upload callbacks
- Manage ChatAdapter lifecycle

#### 4. Update ChatActivity
Host fragment.

### Key Challenges:
- 50+ use case dependencies
- Activity result launchers for file operations
- Complex adapter with WebView items
- Synchronized state emission
- ChatManager state integration

### Success Criteria:

#### Automated Verification:
- [ ] Build completes
- [ ] Unit tests pass

#### Manual Verification:
- [ ] Messaging works (send/receive)
- [ ] File attachments work (all sources)
- [ ] GVA cards render correctly
- [ ] Quick replies work
- [ ] Operator typing indicator shows
- [ ] Unread indicator works
- [ ] State survives rotation
- [ ] History loads correctly

---

## Phase 11: Single Activity Host (Optional Future)

### Overview
After all screens migrated, create optional GliaHostActivity for true single-activity architecture.

### Changes Required:

#### 1. GliaHostActivity
**File**: `widgetssdk/src/main/java/com/glia/widgets/GliaHostActivity.kt`

Single activity hosting all fragments with GliaNavigator.

#### 2. GliaNavigator Implementation
FragmentManager-based navigation with back stack management.

#### 3. Update ActivityLauncher
Option to launch single activity instead of individual activities.

### This phase is optional and can be done after validating Fragment migrations work correctly.

---

## Phase 12: Cleanup

### Overview
Remove deprecated code and update tests.

### Changes Required:

1. **Mark Controllers as @Deprecated** (keep for backwards compatibility initially)
2. **Update unit tests** - Controller tests → ViewModel tests
3. **Update snapshot tests** for Fragment-based UI
4. **Remove unused Contract interfaces** after full migration
5. **Update CLAUDE.md** documentation

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
