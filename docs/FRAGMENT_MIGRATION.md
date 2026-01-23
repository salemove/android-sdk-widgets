# Fragment Migration Guide

## Overview

The GliaWidgets Android SDK has been migrated to a Fragment-based architecture while maintaining full backwards compatibility. This document explains the new architecture and how to work with it.

## Architecture

### Before (Activity-based)
```
┌─────────────────┐
│   ChatActivity  │  ← Entry point, contains all logic
│   - ChatView    │
│   - Lifecycle   │
│   - Permissions │
└─────────────────┘
```

### After (Fragment-based)
```
┌──────────────────┐
│  ChatActivity    │  ← Thin wrapper (backwards compatible)
│  (Host)          │
└────────┬─────────┘
         │ hosts
         ▼
┌──────────────────┐
│  ChatFragment    │  ← Contains all logic
│  - ChatView      │
│  - Lifecycle     │
│  - Permissions   │
└──────────────────┘
```

## Benefits

### 1. **Modular UI Components**
Fragments can be reused and composed in different contexts without duplicating code.

### 2. **Better Lifecycle Management**
Fragment lifecycle is more granular and better suited for managing view state across configuration changes.

### 3. **Navigation Component Ready**
The architecture is prepared for future integration with Jetpack Navigation Component.

### 4. **Improved Testability**
Fragments can be tested in isolation using `FragmentScenario` without launching full Activities.

### 5. **Backwards Compatible**
All existing Intent-based Activity launches continue to work without any changes required.

## Migration Details

### Activities Migrated

| Activity | Fragment | Status |
|----------|----------|--------|
| ChatActivity | ChatFragment | ✅ Complete |
| CallActivity | CallFragment | ✅ Complete |
| MessageCenterActivity | MessageCenterFragment | ✅ Complete |
| ImagePreviewActivity | ImagePreviewFragment | ✅ Complete |
| WebBrowserActivity | WebBrowserFragment | ✅ Complete |
| SurveyActivity | SurveyFragment | ✅ Complete |

### Activities Not Migrated

- **EntryWidgetActivity** - Already Fragment-based
- **DialogHolderActivity** - Programmatic, no UI
- **PushClickHandlerActivity** - Routing only, no UI

## Key Components

### 1. GliaFragment Base Class

All Fragments extend `GliaFragment`:

```kotlin
internal abstract class GliaFragment : Fragment() {
    protected val compositeDisposable = CompositeDisposable()
    abstract val gliaView: View

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }
}
```

**Features:**
- RxJava subscription management
- Common lifecycle patterns
- Reference to the main Glia view

### 2. GliaFragmentContract

Defines communication between Fragments and host Activities:

```kotlin
internal interface GliaFragmentContract {
    interface Host {
        fun setHostTitle(locale: LocaleString?)
        fun finish()
    }
}
```

**Usage:**
- Fragments call `host?.setHostTitle()` to update Activity title
- Fragments call `host?.finish()` to close the Activity

### 3. FragmentArgumentHelper

Type-safe Fragment argument creation:

```kotlin
// Creating arguments
val args = FragmentArgumentHelper.chatArguments(Intention.LIVE_CHAT)

// Extracting arguments
val intention = arguments?.getEnumArgument<Intention>(
    FragmentArgumentKeys.OPEN_CHAT_INTENTION
)
```

**Benefits:**
- Type safety
- Centralized key management
- Consistent patterns

## Fragment Implementation Patterns

### Pattern 1: View Binding Lifecycle

```kotlin
class ChatFragment : GliaFragment() {
    private var _binding: ChatFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(...): View {
        _binding = ChatFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null // Prevent memory leaks
        super.onDestroyView()
    }
}
```

### Pattern 2: Host Communication

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    host = activity as? GliaFragmentContract.Host

    // Update title
    chatView.setOnTitleUpdatedListener { title ->
        host?.setHostTitle(title)
    }

    // Close Activity
    chatView.setOnEndListener {
        host?.finish()
    }
}

override fun onDetach() {
    super.onDetach()
    host = null // Clear reference
}
```

### Pattern 3: Back Press Handling

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // Handle back press
            chatView.onBackPressed()
        }
    }

    requireActivity().onBackPressedDispatcher.addCallback(
        viewLifecycleOwner, // Automatically removed when view destroyed
        onBackPressedCallback
    )
}
```

### Pattern 4: Activity Result APIs

```kotlin
class MessageCenterFragment : GliaFragment() {
    // Register in Fragment, not Activity
    private val pickContentLauncher = registerForActivityResult(
        PickVisualMediaMultipleMimeTypes()
    ) { uri: Uri? ->
        uri?.let { messageCenterView.onContentChosen(it) }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) controller.onTakePhotoClicked()
    }
}
```

**Benefits:**
- Survives configuration changes automatically
- Cleaner than `onActivityResult()`

### Pattern 5: Options Menu (ImagePreviewFragment)

```kotlin
class ImagePreviewFragment : GliaFragment(), MenuProvider {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup toolbar
        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)

        // Add menu provider
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_file_preview, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.save_item -> { /* ... */ true }
            R.id.share_item -> { /* ... */ true }
            else -> false
        }
    }
}
```

## Activity Wrapper Pattern

All Activities follow this thin wrapper pattern:

```kotlin
internal class ChatActivity :
    GliaActivity<ChatView>,
    FadeTransitionActivity(),
    GliaFragmentContract.Host {

    private var chatFragment: ChatFragment? = null

    override val gliaView: ChatView
        get() = chatFragment?.gliaView as? ChatView
            ?: error("Fragment not initialized")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity_host)

        if (savedInstanceState == null) {
            // Extract Intent extras
            val intention = intent.getEnumExtra<Intention>(
                ExtraKeys.OPEN_CHAT_INTENTION
            )
            checkNotNull(intention) { "Intention must be provided" }

            // Create and host Fragment
            chatFragment = ChatFragment.newInstance(intention)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, chatFragment!!)
                .commit()
        } else {
            // Restore Fragment after configuration change
            chatFragment = supportFragmentManager
                .findFragmentById(R.id.fragment_container) as? ChatFragment
        }
    }

    // Implement Host interface
    override fun setHostTitle(locale: LocaleString?) {
        setTitle(locale)
    }

    override fun finish() = super.finish()
}
```

## Backwards Compatibility

### Intent-based Launches Still Work

```kotlin
// Before (still works)
val intent = Intent(context, ChatActivity::class.java)
intent.putExtra(ExtraKeys.OPEN_CHAT_INTENTION, Intention.LIVE_CHAT)
startActivity(intent)

// After (internally creates Fragment)
// No changes required to existing code
```

### Public API Unchanged

All public APIs remain identical:
- `GliaWidgets.getEngagementLauncher()` works as before
- Activity Intent extras are preserved
- Lifecycle behavior is identical
- View hierarchy is the same

## Testing

### Unit Testing Fragments

```kotlin
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class ChatFragmentTest {
    @Test
    fun `fragment creation with valid arguments succeeds`() {
        val args = FragmentArgumentHelper.chatArguments(Intention.LIVE_CHAT)

        val scenario = launchFragmentInContainer<ChatFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.onFragment { fragment ->
            assertNotNull(fragment.gliaView)
            assertTrue(fragment.gliaView is ChatView)
        }
    }
}
```

### Testing Host Callbacks

```kotlin
@Test
fun `fragment calls host finish on end listener`() {
    val mockHost = mock<GliaFragmentContract.Host>()
    val scenario = launchFragmentInContainer<ChatFragment>(...)

    scenario.onFragment { fragment ->
        // Inject mock host
        fragment.host = mockHost

        // Trigger end listener
        fragment.chatView.onEndListener?.invoke()

        // Verify host callback
        verify(mockHost).finish()
    }
}
```

## Future Enhancements (Phase 2)

The current implementation is **Phase 1** - Fragment-based architecture with Activity wrappers.

**Phase 2** (Future) will include:
- Single host Activity
- Jetpack Navigation Component integration
- Fragment-to-Fragment navigation
- Deep linking via navigation graph
- Shared element transitions
- Public API updates for Fragment-based launches

## Common Patterns

### Configuration Changes

Fragments automatically handle configuration changes:
- `savedInstanceState` is managed by the Fragment
- View state is restored automatically
- No need for `android:configChanges` in manifest

### Memory Leaks Prevention

```kotlin
override fun onDestroyView() {
    // Clear view binding
    _binding = null

    // Clear RxJava subscriptions
    compositeDisposable.clear()

    // Clear custom view lifecycle
    customView.onDestroyView()

    super.onDestroyView()
}

override fun onDetach() {
    super.onDetach()
    // Clear host reference
    host = null
}
```

### ViewLifecycleOwner

Always use `viewLifecycleOwner` for view-related observers:

```kotlin
// ❌ WRONG - uses Fragment lifecycle
requireActivity().onBackPressedDispatcher.addCallback(this, callback)

// ✅ CORRECT - uses View lifecycle
requireActivity().onBackPressedDispatcher.addCallback(
    viewLifecycleOwner,
    callback
)
```

## Troubleshooting

### Issue: Fragment not found after rotation

**Solution:** Ensure Fragment is retrieved in `onCreate()` after `savedInstanceState`:

```kotlin
if (savedInstanceState == null) {
    // Create new Fragment
    fragment = ChatFragment.newInstance(...)
    // ...
} else {
    // Restore existing Fragment
    fragment = supportFragmentManager
        .findFragmentById(R.id.fragment_container) as? ChatFragment
}
```

### Issue: View binding null pointer exception

**Solution:** Always null-check binding or use `viewLifecycleOwner`:

```kotlin
private var _binding: ChatFragmentBinding? = null
private val binding get() = _binding!!

// In onDestroyView
_binding = null
```

### Issue: Activity Result not working

**Solution:** Register launchers in Fragment, not Activity:

```kotlin
// ✅ CORRECT - in Fragment
class MyFragment : Fragment() {
    private val launcher = registerForActivityResult(...) { ... }
}

// ❌ WRONG - in Activity
class MyActivity : AppCompatActivity() {
    private val launcher = registerForActivityResult(...) { ... }
}
```

## Summary

The Fragment migration provides:
- ✅ Better code organization
- ✅ Improved testability
- ✅ Better lifecycle management
- ✅ Navigation Component readiness
- ✅ Full backwards compatibility

All existing code continues to work without changes. The migration is internal and transparent to SDK users.
