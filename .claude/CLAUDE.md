# Claude Code: GliaWidgets Android SDK

This repository contains the **GliaWidgets Android SDK** - a UI/UX framework for Android that provides pre-built, customizable widgets for customer engagement. It consumes the Glia Core Android SDK and provides out-of-the-box views for video/audio calls, chat, and secure conversations. The SDK is designed for banking and financial services customer support. This SDK serves as the Android counterpart to the iOS GliaWidgets SDK.

---

## Agentic Development Tools

This repository is configured with **Claude Code custom commands** to streamline development workflows.

### Atlassian Integration (Jira & Confluence)

Claude has access to Atlassian services with **tool selection by service type**:

**🔴 IMPORTANT: Tool Selection Rules**
- **Jira:** ALWAYS use Atlassian CLI (`acli`) - NO EXCEPTIONS
- **Confluence:** ALWAYS use Atlassian MCP Server
- **Reason:** CLI is more token-efficient for Jira, MCP provides better Confluence content access

**Atlassian CLI (`acli`) - For Jira ONLY:**
- **Use for:** All Jira operations (viewing tickets, searching issues, etc.)
- **View tickets:** `acli jira workitem view MOB-XXXX`
- **Search issues:** `acli jira workitem list --jql "project = MOB AND status = Open"`
- **Authentication:** Run `acli auth login --web` in terminal for browser-based OAuth
- **Pre-approved commands:** Configured in [.claude/settings.json](.claude/settings.json)

**Atlassian MCP Server - For Confluence ONLY:**
- **Use for:** All Confluence operations (searching pages, reading docs, listing spaces)
- **Pre-configured:** Set up in [.mcp.json](.mcp.json)
- **Pre-approved tools:** Configured in [.claude/settings.json](.claude/settings.json)
- **Capabilities:** Search pages, read full content, list spaces, CQL queries
- **No additional auth needed:** Uses same credentials as CLI

**Usage Examples:**
- "Search Confluence for Android UI design guidelines" → Use MCP
- "Get details on Jira ticket MOB-1234" → Use CLI (`acli`)
- "Find all open issues assigned to me" → Use CLI (`acli`)
- "Search Confluence pages in the ENG space" → Use MCP
- "What's the status of sprint issues?" → Use CLI (`acli`)

### GitHub Integration

Claude has direct access to GitHub repositories via GitHub CLI (`gh`) MCP integration:

**Setup:**
- **Pre-configured:** GitHub MCP server is already set up in [.mcp.json](.mcp.json)
- **Auto-enabled:** Automatically enabled for all team members via [.claude/settings.json](.claude/settings.json)
- **Uses GitHub CLI:** Leverages `gh` authentication (browser-based OAuth)

**Authentication (One-time per machine):**

1. **Install GitHub CLI** (if not already installed):
   ```bash
   brew install gh  # macOS
   # Or download from: https://cli.github.com/
   ```

2. **Authenticate with GitHub** (browser-based OAuth):
   ```bash
   gh auth login
   ```
   - Select: **GitHub.com**
   - Select: **HTTPS**
   - Authenticate Git: **Yes**
   - How to authenticate: **Login with a web browser**
   - Copy the one-time code → press Enter → browser opens → paste code → authorize

3. **Install gh-mcp extension:**
   ```bash
   gh extension install shuymn/gh-mcp
   ```

4. **Verify it works:**
   - Run `claude mcp list` to see GitHub server status
   - Or run `/mcp` in Claude Code

**Usage Examples:**
- "Review PR #456 and provide feedback"
- "Create an issue for the bug we just found"
- "Show me all open PRs assigned to me"
- "List recent commits on the master branch"
- "What's the status of issue MOB-1234?"
- "Search for code using 'Engagement' in the repository"

**Benefits:**
- ✅ Browser-based OAuth (no manual token creation)
- ✅ No tokens stored in files (uses `gh` credentials)
- ✅ Automatic token refresh handled by `gh`
- ✅ Same authentication used for git operations
- ✅ Easy team onboarding

### Android Emulator Integration

Claude has direct access to Android Emulators via the Mobile MCP server for UI testing and debugging:

**Mobile MCP (`mobile-mcp`):**
- **Pre-configured:** Set up in [.mcp.json](.mcp.json)
- **Capabilities:** Advanced device control, app management, orientation changes, screenshots
- **Use for:** Comprehensive mobile testing automation on Android emulators

**Usage Examples:**
- "Take a screenshot of the current emulator screen"
- "List all running Android emulators"
- "Install the debug APK on the emulator"
- "Change emulator orientation to landscape"
- "Tap at specific coordinates on the screen"

**Benefits:**
- ✅ No manual emulator navigation needed
- ✅ Automated UI testing workflows
- ✅ Quick visual debugging
- ✅ Integration with test automation

---

## Parallel Execution Best Practices

Claude Code performs better when you request multiple independent operations in parallel. This reduces context switching and speeds up analysis.

### Recommended Patterns

**File Exploration** - Request multiple file reads together:
```
Read these files to understand the engagement system:
- widgetssdk/src/main/java/com/glia/widgets/GliaWidgets.kt
- widgetssdk/src/main/java/com/glia/widgets/launcher/EngagementLauncher.kt
- widgetssdk/src/main/java/com/glia/widgets/di/Dependencies.kt
```

**Codebase Search** - Parallel grep for related patterns:
```
Find all usages of: GliaWidgets, UiTheme, ChatActivity, EngagementLauncher
```

**Build Validation** - Run checks concurrently:
```
In parallel: Run lint, build the SDK, and execute unit tests
```

### Benefits
- **Faster responses**: Multiple tool calls in single message
- **Lower token usage**: Less context switching overhead
- **Better workflow**: Complete analysis before making changes

---

## Project Architecture

### Framework Type
- **Distribution:** Android Library (AAR) published to Maven Central
- **Group ID:** `com.glia`
- **Artifact ID:** `android-widgets`
- **Min SDK:** API 24 (Android 7.0)
- **Target/Compile SDK:** API 35 (Android 15)
- **Java Toolchain:** Version 17
- **Languages:** Kotlin (preferred for new code)

### Core Architectural Patterns
- **MVP Architecture:** Views, Controllers (Presenters), and Contracts define interactions
- **Unified UI Theme System:** Centralized styling via remote JSON configuration
- **Dependency Injection:** Centralized `Dependencies` object manages all SDK dependencies
- **Dependency on Core SDK:** Consumes Glia Core Android SDK as API dependency
- **Repository Pattern:** Data access abstraction for configuration and state
- **RxJava Streams:** Reactive state management and async operations

### Primary Entry Point
- **GliaWidgets.init():** Main SDK initialization with `GliaWidgetsConfig`
- **Main Features:**
  - `getEngagementLauncher()` - Launch engagement UI for chat/audio/video
  - `getEntryWidget()` - Floating bubble widget for engagement access
  - `getSecureConversations()` - Secure async messaging
  - `getCallVisualizer()` - Call visualizer controls
  - Event callbacks for engagement lifecycle
- **Engagement Types:** Chat, Audio Call, Video Call, Call Visualizer, Secure Conversations

### SDK Module Structure
```
android-sdk-widgets/
├── widgetssdk/              # Main SDK module (published library)
│   ├── src/main/java/com/glia/widgets/
│   │   ├── GliaWidgets.kt       # Main SDK entry point
│   │   ├── GliaWidgetsConfig.kt # SDK configuration
│   │   ├── UiTheme.kt           # Legacy theme system (deprecated)
│   │   ├── launcher/            # Engagement launcher
│   │   ├── entrywidget/         # Floating bubble widget
│   │   ├── chat/                # Chat UI components
│   │   ├── call/                # Call UI components
│   │   ├── callvisualizer/      # Call visualizer
│   │   ├── secureconversations/ # Secure messaging (Message Center)
│   │   ├── engagement/          # Engagement management
│   │   ├── di/                  # Dependency injection
│   │   ├── core/                # Core utilities
│   │   ├── view/                # Custom views and Unified UI theme
│   │   └── internal/            # Internal implementations
│   ├── src/test/java/           # Unit tests
│   └── src/testSnapshot/        # Snapshot tests (Paparazzi)
├── app/                         # Demo application
└── lint_checker/                # Custom Lint rules
```

---

## Tech Stack

### Languages
**Kotlin-First Codebase:**
- **Kotlin:** Preferred for all new code (modern, null-safe, concise)
- **Java:** Legacy code exists, but new features should be in Kotlin
- **Interop:** Seamless Java-Kotlin interoperability with `@JvmStatic`, `@JvmOverloads`

### UI Frameworks
**Android Views (Traditional)**
- **View Binding** - Type-safe view access
- Material Design Components
- Custom Views for engagement UI
- ConstraintLayout for complex layouts
- RecyclerView for chat messages
- **No Jetpack Compose** - Uses traditional View system

### Reactive Programming
**Primary:** RxJava 3
- `Observable` and `Single` for async operations
- `BehaviorSubject` for state management
- `Consumer<T>` for callbacks
- `io.reactivex.rxjava3.*` namespace
- RxAndroid for main thread scheduling

### Concurrency Model
- **RxJava Schedulers:** Primary async pattern (`io()`, `mainThread()`)
- **Callbacks:** `OnComplete`, `OnError`, `OnResult<T>` for async results
- **Handlers:** Main thread coordination
- **Coroutines:** Minimal use (some Kotlin files)

### Styling System
**Unified UI Theme System**

Location: `widgetssdk/src/main/java/com/glia/widgets/view/unifiedui/`

Implementation:
- **Remote Configuration:** JSON-based theme configuration via `GliaWidgetsConfig.setUiJsonRemoteConfig()`
- **UnifiedTheme:** Centralized styling for all UI components
- **ColorPallet:** Base colors with semantic naming
- **Typography:** Font styles with dynamic type support
- **Component Themes:** Chat, Call, Alert, Entry Widget, etc.

Components styled via Unified Theme:
- Chat UI (messages, attachments, input, operator status)
- Call UI (buttons, operator view, video layout)
- Call Visualizer (visitor code, controls)
- Entry Widget (floating bubble)
- Alerts and dialogs
- Secure conversations UI (Message Center)

**Legacy UiTheme (Deprecated):**
- `UiTheme.kt` - Deprecated in favor of remote configuration
- Still supported for backwards compatibility
- Strongly encourage migration to remote JSON config

### Testing Frameworks
**Snapshot Testing** (Primary for UI validation)
- **Paparazzi** - JVM-based snapshot testing (no emulator required)
- Tests in `widgetssdk/src/testSnapshot/` directory
- Renders Views on JVM using Robolectric
- Fast execution compared to instrumentation tests
- PNG snapshots stored in repository

**Unit Testing**
- **JUnit 4.13.2** - Test framework
- **Mockito Kotlin 6.0.0** - Mocking framework
- **MockK 1.14.6** - Kotlin-native mocking
- **Robolectric 4.16** - Android framework mocking (runs tests on JVM)
- Tests in `widgetssdk/src/test/java/` directory

**Instrumentation Testing**
- **Espresso 3.7.0** - UI testing framework
- **AndroidX Test** - Testing utilities
- Tests in `app/src/androidTest/` directory (demo app)

### Dependencies
External libraries (via Gradle version catalog):
- **Glia Core SDK** - Core business logic and networking (API dependency)
- **Glia Telemetry SDK** - Instrumentation and observability
- **AndroidX Lifecycle 2.9.3** - Lifecycle-aware components
- **AndroidX Core KTX 1.16.0** - Kotlin extensions
- **Material Components 1.12.0** - UI components
- **Coil** - Image loading
- **Lottie** - Animation
- **GSON** - JSON serialization
- **AudioSwitch** - Audio device management
- **Firebase Cloud Messaging (FCM)** - Push notifications (optional dependency)
- **ExifInterface** - Image metadata

---

## Security & Compliance Standards

### Data Storage
- **No Direct PII Storage:** All sensitive data managed by Core SDK
- **SharedPreferences:** Only for non-sensitive UI configuration
- **Session Scoped:** In-memory state cleared on logout
- **Core SDK Delegation:** All secure storage via Glia Core SDK

### Networking
- **No Direct Networking:** All API calls routed through Core SDK
- **Core SDK Dependency:** Use Core SDK's networking layer exclusively
- **HTTPS Only:** All network communication encrypted (enforced by Core SDK)
- **Token-based Auth:** Handled by Core SDK

### Logging Compliance
- **NEVER use Android Log directly** - Use `GliaLogger` and `GliaTracer` from telemetry SDK
- **PII Handling:** **ALWAYS** avoid logging user input, operator names, or chat content
- **UI Events Only:** Log UI interactions, navigation, and errors only
- **Telemetry Attributes:** Use structured logging with `EventAttribute`

### UI-Specific Security
- **Screen Recording:** Handle screen recording state changes
- **Screenshot Prevention:** Respect sensitive content flags for secure conversations
- **Background Mode:** Hide sensitive UI when app enters background
- **Permissions:** Request CAMERA and RECORD_AUDIO at runtime for media engagements

---

## Testing Infrastructure

### Snapshot Testing (Primary)
**Framework:** Paparazzi (JVM-based snapshot testing)
**Location:** `widgetssdk/src/testSnapshot/` directory

**Workflow:**
```bash
# Run snapshot tests (record mode to generate new snapshots)
./gradlew widgetssdk:recordPaparazziSnapshot

# Run snapshot tests (verify mode to compare against existing)
./gradlew widgetssdk:verifyPaparazziSnapshot

# View snapshot test results
open widgetssdk/build/reports/paparazzi/snapshot/index.html
```

**Test Coverage:**
- All UI components across multiple device configurations
- Theme variations (colors, fonts)
- Different screen sizes (phone, tablet)
- Various UI states (loading, error, success)

**Best Practices:**
- ALWAYS update snapshots when changing UI
- Review snapshot diffs carefully before committing
- Use descriptive test names indicating component and variant
- Test multiple device configurations for responsive layouts

### Unit Testing
**Frameworks:**
- **JUnit 4** - Test framework
- **Mockito Kotlin** - Mocking framework for Java/Kotlin interop
- **MockK** - Kotlin-native mocking (preferred for Kotlin code)
- **Robolectric** - Android framework mocking on JVM

**Test Organization:**
```
widgetssdk/src/test/java/com/glia/widgets/
├── GliaWidgetsTest.kt           # Main SDK tests
├── chat/                        # Chat component tests
├── call/                        # Call component tests
├── engagement/                  # Engagement tests
├── internal/                    # Internal implementation tests
└── launcher/                    # Launcher tests
```

**Mocking Strategy:**
- Mock external dependencies via dependency injection
- Use MockK for Kotlin code
- Use Mockito for Java code
- Robolectric for Android framework classes

### Instrumentation Testing
**Framework:** Espresso for UI testing
**Location:** Demo app (`app/src/androidTest/`)

**Test Apps:**
- `app/` module contains demo activities for testing
- Integration examples for all SDK features
- UI testing for engagement flows

---

## Coding Guidelines

### Language Choice
- **New Code:** Write in Kotlin (strongly preferred)
- **Existing Java:** Migrate incrementally to Kotlin when refactoring
- **Interop:** Ensure Java-Kotlin interoperability with `@JvmStatic`, `@JvmOverloads`, `@JvmField`

### Kotlin Best Practices
- **Null Safety:** Use nullable types (`?`) and safe calls (`?.`)
- **Data Classes:** For immutable data structures
- **Extension Functions:** For utility functions
- **Coroutines:** Use for new async code (when not using RxJava)
- **Sealed Classes:** For state management
- **Companion Objects:** For factory methods and constants
- **Scope Functions:** Use `let`, `apply`, `run`, `also`, `with` appropriately

### Code Comments Philosophy
- **Prefer Self-Documenting Code:** Use descriptive method, class, and variable names
- **Avoid Obvious Comments:** Don't add comments where the logic is self-evident
- **Comment Only When Necessary:**
  - Complex UI layout calculations or constraints
  - Non-obvious RxJava chains or reactive flows
  - Workarounds for Android SDK bugs or API limitations
  - Thread safety considerations
  - Performance optimizations that aren't self-evident
  - Rationale for architectural decisions that may seem unusual
- **KDoc/JavaDoc for Public APIs:** Every `public` symbol MUST have documentation
  - Document parameters and return values
  - Provide code examples for complex APIs
  - Use `@Deprecated` with migration guidance
  - Include usage examples for engagement flows

**Good Example:**
```kotlin
// ❌ BAD: Unnecessary comment
// Set button background color
button.setBackgroundColor(theme.primaryColor)

// ✅ GOOD: Self-documenting
button.setBackgroundColor(theme.primaryColor)

// ❌ BAD: Comment explains what code does
// Create RxJava scheduler for main thread
val mainThread = AndroidSchedulers.mainThread()

// ✅ GOOD: Descriptive variable name
val mainThreadScheduler = AndroidSchedulers.mainThread()

// ✅ GOOD: Comment explains WHY (non-obvious behavior)
// RxJava error handler must be set before any Observable subscriptions
// to avoid UndeliverableException crashes (WIDGETS-1234)
setupRxErrorHandler()
```

### Error Handling
- **Custom Exceptions:** Use `GliaWidgetsException` with `Cause` enum for SDK errors
- **Core SDK Errors:** Propagate Core SDK errors with proper wrapping
- **User-Facing Errors:** Provide localized error messages for UI display
- **RxJava Error Handling:** Always provide error handlers in `.subscribe()`

### RxJava Best Practices
- **Dispose Subscriptions:** Always dispose in lifecycle methods
- **CompositeDisposable:** Use for managing multiple subscriptions
- **Schedulers:** Specify `.subscribeOn()` and `.observeOn()` explicitly
- **Error Handling:** Never leave `.subscribe()` without error handler
- **Memory Leaks:** Avoid holding Activity/Fragment references in RxJava chains

### Android Best Practices
- **Lifecycle Awareness:** Use AndroidX Lifecycle components
- **View Binding:** Use View Binding for type-safe view access
- **Memory Leaks:** Avoid holding Activity/Fragment references in static fields
- **Main Thread:** Never block main thread with network/disk operations
- **Permissions:** Request at runtime for dangerous permissions (CAMERA, RECORD_AUDIO)
- **Context:** Use Application context for SDK initialization
- **Configuration Changes:** Handle screen rotation properly
- **Background Restrictions:** Handle Doze mode and App Standby

### Theme Customization
- **Prefer Remote Configuration:** Use `GliaWidgetsConfig.setUiJsonRemoteConfig()` for theme customization
- **Avoid UiTheme:** `UiTheme` is deprecated, use remote JSON config instead
- **Component Themes:** Each component has its own theme configuration
- **Fallback Values:** Provide sensible defaults for all theme properties

---

## Architecture Deep Dive

### Unified UI Theme System
Location: `widgetssdk/src/main/java/com/glia/widgets/view/unifiedui/`

**Pattern:**
```kotlin
data class UnifiedTheme(
    val chatTheme: ChatTheme,
    val callTheme: CallTheme,
    val alertTheme: AlertTheme,
    val entryWidgetTheme: EntryWidgetTheme,
    // ... other component themes
)

data class ChatTheme(
    val background: ColorTheme,
    val visitorMessage: MessageBalloonTheme,
    val operatorMessage: MessageBalloonTheme,
    val input: InputTheme,
    // ... other chat properties
)
```

**Features:**
- Centralized styling for entire SDK
- Remote configuration via JSON
- Type-safe color and font definitions
- Component-level theme configuration
- Mergeable themes for composition

**Usage:**
- Configure via `GliaWidgetsConfig.setUiJsonRemoteConfig()` during SDK init
- Theme applied automatically to all components
- Customizable per-component or globally

### Dependency Injection
Location: `widgetssdk/src/main/java/com/glia/widgets/di/Dependencies.kt`

**Pattern:**
```kotlin
internal object Dependencies {
    val gliaCore: GliaCore by lazy { /* ... */ }
    val gliaThemeManager: GliaThemeManager by lazy { /* ... */ }
    val repositoryFactory: RepositoryFactory by lazy { /* ... */ }
    val useCaseFactory: UseCaseFactory by lazy { /* ... */ }
    // ... other dependencies
}
```

**Benefits:**
- Centralized dependency management
- Lazy initialization
- Testable via dependency replacement
- Singleton pattern with controlled initialization

### MVP Architecture
**Components:**
- **View:** Activity/Fragment/Custom View
- **Controller:** Presenter logic
- **Contract:** Interface defining View and Controller interactions

**Example:**
```kotlin
interface ChatContract {
    interface View {
        fun showMessage(message: ChatMessage)
        fun showError(error: String)
    }

    interface Controller {
        fun sendMessage(text: String)
        fun loadHistory()
    }
}

class ChatController(
    private val view: ChatContract.View
) : ChatContract.Controller {
    override fun sendMessage(text: String) {
        // Business logic
        view.showMessage(/* ... */)
    }
}
```

**Responsibilities:**
- View: Display UI and handle user interactions
- Controller: Business logic and state management
- Contract: Interface for testability and separation of concerns

---

## "NEVER" Rules

### Code Prohibitions
1. **NEVER modify `public` APIs without coordination** - Breaking changes require major version bump
2. **NEVER use Android Log directly** - Use `GliaLogger`/`GliaTracer` from telemetry SDK
3. **NEVER hardcode colors or dimensions** - Use theme system and resources
4. **NEVER bypass Core SDK** - All data access goes through Glia Core SDK
5. **NEVER skip snapshot tests for UI changes** - Visual regressions must be caught
6. **NEVER use force unwrap in Kotlin (`!!`) without justification** - Use safe calls
7. **NEVER commit without running lint** - `./gradlew lint` is part of CI/CD
8. **NEVER implement custom networking** - Use Core SDK's network layer
9. **NEVER store sensitive data** - Delegate all persistence to Core SDK
10. **NEVER block main thread** - Use RxJava schedulers or background threads
11. **NEVER add obvious comments** - Use self-documenting code with clear names instead

### UI/UX Violations
1. **NEVER ignore Unified Theme system** - All styling must go through theme configuration
2. **NEVER hardcode strings** - Use string resources for all user-facing text
3. **NEVER skip accessibility** - Content descriptions and accessibility support mandatory
4. **NEVER ignore configuration changes** - Handle screen rotation properly
5. **NEVER create inaccessible UI** - TalkBack support is mandatory
6. **NEVER use absolute pixels (px)** - Use dp for dimensions, sp for text sizes
7. **NEVER ignore different screen sizes** - Support phones and tablets

### Testing Anti-Patterns
1. **NEVER skip snapshot tests for UI changes** - Visual regressions must be caught
2. **NEVER commit without updating snapshots** - Outdated snapshots block CI
3. **NEVER use `Thread.sleep()` in tests** - Use Robolectric's shadow scheduler or RxJava TestScheduler
4. **NEVER test private implementation details** - Test public behavior only
5. **NEVER ignore flaky tests** - Fix or file a bug, don't disable
6. **NEVER mock value types** - Only mock interfaces and protocols
7. **NEVER commit snapshot diffs without review** - Visual changes need approval

### Architecture Violations
1. **NEVER create singletons** - Use dependency injection via `Dependencies` object
2. **NEVER bypass dependency injection** - All dependencies through `Dependencies`
3. **NEVER tightly couple to Core SDK implementation** - Use interface abstractions
4. **NEVER hold Activity/Fragment references in static fields** - Causes memory leaks
5. **NEVER leak view controllers** - Always dispose RxJava subscriptions in lifecycle methods
6. **NEVER use `synchronized` without reason** - Prefer RxJava concurrency

---

## Snapshot Testing Workflow

### Initial Setup
```bash
# No special setup required - Paparazzi integrated in build.gradle
./gradlew widgetssdk:tasks --group paparazzi
```

### Daily Workflow
```bash
# Make UI changes in Android Studio

# Generate new snapshots (first time or when UI intentionally changed)
./gradlew widgetssdk:recordPaparazziSnapshot

# Verify UI against existing snapshots
./gradlew widgetssdk:verifyPaparazziSnapshot

# Review snapshot diffs in build/reports/paparazzi/
open widgetssdk/build/reports/paparazzi/snapshot/index.html

# Commit snapshot changes to repository (PNG files)
git add widgetssdk/src/testSnapshot/
git commit -m "Update snapshots for button styling"

# Commit code changes to main repo
git add widgetssdk/src/main/
git commit -m "Update button styling"
git push
```

### Troubleshooting
- **Snapshots out of sync:** Run `./gradlew widgetssdk:recordPaparazziSnapshot` to regenerate
- **Test failures:** Check HTML report for image diffs
- **Missing snapshots:** Run record mode to generate initial snapshots
- **Flaky tests:** Ensure views are fully rendered before snapshot capture

---

## Additional Resources

- **GitHub:** [https://github.com/salemove/android-sdk-widgets](https://github.com/salemove/android-sdk-widgets)
- **Maven Central:** [https://central.sonatype.com/artifact/com.glia/android-widgets](https://central.sonatype.com/artifact/com.glia/android-widgets)
- **Documentation:** [https://developer.glia.com/api-usage-refs/android-api](https://developer.glia.com/api-usage-refs/android-api)
- **Core SDK GitHub:** [https://github.com/salemove/android-sdk](https://github.com/salemove/android-sdk)
- **iOS Counterpart:** [https://github.com/salemove/ios-sdk-widgets](https://github.com/salemove/ios-sdk-widgets)
- **Kotlin Style Guide:** [https://developer.android.com/kotlin/style-guide](https://developer.android.com/kotlin/style-guide)
- **RxJava Documentation:** [https://github.com/ReactiveX/RxJava](https://github.com/ReactiveX/RxJava)
- **Paparazzi:** [https://github.com/cashapp/paparazzi](https://github.com/cashapp/paparazzi)

---
