# GliaWidgets Android SDK — Deep Reference

## Contents

- [Module Dependencies](#module-dependencies)
- [Conventions](#conventions)
- [Anti-Patterns](#anti-patterns)
- [Gotchas](#gotchas)
- [Stack Constraints](#stack-constraints)
- [Workflows and Commands](#workflows-and-commands)
- [Git Intelligence](#git-intelligence)

---

## Module Dependencies

**`ControllerFactory.java` ↔ `UseCaseFactory.java` ↔ `RepositoryFactory.java`**

These three Java files form the manual DI registry. They must change in lockstep whenever a new screen, use case, or repository is introduced. All three remain Java and are annotated `@hide` to exclude from Dokka-generated Javadoc. Evidence: `widgetssdk/src/main/java/com/glia/widgets/di/ControllerFactory.java`, `widgetssdk/src/main/java/com/glia/widgets/di/UseCaseFactory.java`, `widgetssdk/src/main/java/com/glia/widgets/di/RepositoryFactory.java`

**`ControllerFactory` ↔ Activity lifecycle (retained slots)**

`ControllerFactory` holds `retainedChatController` and `retainedCallController` as instance fields. These survive Activity recreation (config changes, screen rotations), which is why no `ViewModel` exists in the project. Other controllers are recreated per screen. Do not introduce `ViewModel` without understanding what the retained slots already provide.

**`Dependencies.onAppCreate` ↔ ActivityLifecycleCallbacks watchers**

`CallVisualizerActivityWatcher`, `ActivityWatcherForChatHead`, `ActivityWatcherForLiveObservation`, `ActivityWatcherForPermissionsRequest`, `EngagementCompletionActivityWatcher`, `OperatorRequestActivityWatcher`, `UiComponentsActivityWatcher` all extend `BaseSingleActivityWatcher` and expose `resumedActivity: Flowable<WeakReference<Activity>>` backed by a `PublishProcessor`. This is the implicit UI coordination bus — anything needing "top activity" reads from it. Moving watcher registration between `onAppCreate` and `onSdkInit` causes watchers to miss early app lifecycle events or run without config. Evidence: `widgetssdk/src/main/java/com/glia/widgets/base/BaseActivityWatcher.kt`

**Views ↔ `Dependencies.gliaThemeManager.theme`**

Views read the active `UnifiedTheme` directly from `Dependencies.gliaThemeManager.theme?.<screenTheme>`. Theme is a View-layer concern — it never flows through MVP contracts or controllers. Evidence: `widgetssdk/src/main/java/com/glia/widgets/chat/ChatView.kt`

**`DefaultTheme` ↔ `RemoteConfiguration`**

Adding a theme property requires edits to both `DefaultTheme` (the top-level function at `widgetssdk/src/main/java/com/glia/widgets/view/unifiedui/theme/defaulttheme/Base.kt`) AND the corresponding `RemoteConfiguration` data class and its `toUnifiedTheme()` conversion. The `Mergeable<T>` merge semantics — remote wins when non-null, default is fallback — mean that omitting either side yields `null` for that property in all theme configurations. Evidence: `widgetssdk/src/main/java/com/glia/widgets/view/unifiedui/Merge.kt`

**`EngagementRepository.engagementState` ↔ every controller**

`EngagementRepositoryImpl` owns a `BehaviorProcessor<State>` fed by `Glia.on(OmnicoreEvent, …)` callbacks from Core. Every controller and use case subscribes to `engagementState: Flowable<State>`. State hierarchy or naming changes cascade to all subscribers simultaneously.

**`PushNotificationsImpl` ↔ Core SDK events**

`PushNotificationsImpl` intercepts `SECURE_MESSAGING_TYPE` push payloads before Core ever sees them. Core has zero visibility into secure messaging push. Treat this as a hidden interception layer when debugging PN behavior.

**`version.properties` ↔ Bitrise automation**

Version is read from `version.properties`, NOT `gradle.properties`. Bitrise workflows use `git stash` around version bumps to prevent bot PR merge conflicts. The timestamp field was deliberately removed from `version.properties` to eliminate a recurring source of conflicts.

**`app/build.gradle` release variant ↔ locally-published SNAPSHOT**

The demo app's release variant does NOT reference `project(':widgetssdk')`. Instead, `preReleaseBuild.dependsOn ':widgetssdk:publishSnapshotPublicationToMavenLocal'` publishes a SNAPSHOT to `~/.m2` and the release variant consumes it. Debug uses the project dependency directly. Evidence: `app/build.gradle`

**`InitializationProvider` ↔ Core SDK's `InitializationProvider`**

`widgetssdk/src/main/AndroidManifest.xml` removes Core's `InitializationProvider` via `tools:node="remove"` and registers Widgets' own provider under the same authority. Widgets' provider EXTENDS Core's (a `@hide` class). `super.onCreate()` runs Core init, then `Dependencies.onAppCreate(application)` + `GliaWidgets.setupRxErrorHandler()`. Both must execute — removing either breaks initialization silently.

---

## Conventions

### Kotlin Style

- All new classes must be written in Kotlin, not Java. Java legacy exists but is not a pattern to copy.
- When modifying an existing Java file for a small, self-contained change, convert it to Kotlin in the same PR if the scope stays manageable. Skip the migration for high-churn Java files (`ControllerFactory.java`, `UseCaseFactory.java`, `RepositoryFactory.java`) — those need coordinated rewrites because feature branches touch them constantly.
- Explicit return types on all functions. Explicit types on class/file-level variables; local variables may use inference.
- `ktlint_official` code style, `import-ordering` disabled, 150-character line max. Evidence: `.editorconfig`
- No detekt. Custom lint rules in `lint_checker/` enforce `UndocumentedApiIssue` at `Severity.ERROR` for undocumented public APIs.
- Suppressed lint rules (project-wide, in `widgetssdk/build.gradle`): `WrongLayoutName`, `LayoutFileNameMatchesClass`, `MatchingViewId`, `RawDimen`, `WrongAnnotationOrder`, `ColorCasing`, `WrongViewIdFormat`, `HardcodedText`.
- No `!!` operator — use safe calls (`?.`) and explicit null handling throughout.

### MVP Pattern

- `BaseController.onDestroy()` (no-arg) is DELIBERATELY unusable for `ChatController` and `CallController` — the override throws `RuntimeException("no op")`. Always call `onDestroy(retain: Boolean)`. `retain=true` detaches view and stops timers but preserves the main `CompositeDisposable`; `retain=false` clears everything. Evidence: `widgetssdk/src/main/java/com/glia/widgets/chat/controller/ChatController.kt`, `widgetssdk/src/main/java/com/glia/widgets/call/CallController.kt`
- Views self-register controllers in their own `setupControllers()` method — Activities do not inject controllers. Pattern: `setController(Dependencies.controllerFactory.chatController); controller.setView(this)`. Evidence: `widgetssdk/src/main/java/com/glia/widgets/chat/ChatView.kt`
- `GliaActivity<T>` is an INTERFACE (not an abstract class) with `val gliaView: T`. Activities both extend `FadeTransitionActivity()` AND implement `GliaActivity<ViewType>`. Evidence: `widgetssdk/src/main/java/com/glia/widgets/base/GliaActivity.kt`, `widgetssdk/src/main/java/com/glia/widgets/chat/ChatActivity.kt`

### Dependency Injection

- `Dependencies` is an `internal object` (Kotlin singleton) with `@JvmStatic` for Java interop. Evidence: `widgetssdk/src/main/java/com/glia/widgets/di/Dependencies.kt`
- Use `by lazy` for properties not instantiated until `onSdkInit` (e.g., `activityLauncher`, `engagementLauncher`, `pushNotifications`, `liveObservation`, `secureConversations`).
- `entryWidget` is a computed `get()` property — NOT `by lazy` — returning a fresh `EntryWidgetImpl` on every call. Caching it externally causes double-parent attachment issues.
- `@VisibleForTesting set` on `gliaCore`, `controllerFactory`, `useCaseFactory`, `repositoryFactory`, and `schedulers` is the ONLY supported test injection seam — no DI framework, no test rule, no setup utility.
- `ControllerFactory`, `UseCaseFactory`, `RepositoryFactory` remain Java with `@hide` Javadoc to exclude them from the public API surface.

### UseCase Conventions

- Either a plain Kotlin class (no interface) or an `interface` + `Impl` pair. The `Impl` suffix is used only when an interface exists for testability.
- All use cases expose `operator fun invoke(...)`, callable like functions. Evidence: `widgetssdk/src/main/java/com/glia/widgets/engagement/domain/EndEngagementUseCase.kt`

### RxJava 3

- Controllers use SEGREGATED `CompositeDisposable`s: `disposable` (main, long-lived), `mediaUpgradeDisposable` (cleared on pause), `connectionDisposable` (cleared on pause). Evidence: `widgetssdk/src/main/java/com/glia/widgets/call/CallController.kt`, `widgetssdk/src/main/java/com/glia/widgets/chat/controller/ChatController.kt`
- Subscriptions added via `disposable.add(...)` or `.also(disposable::add)` — both styles coexist, no enforced preference.
- Tests must set up `RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }` in `@Before` and reset in `@After`. No shared rule. Evidence: `widgetssdk/src/test/java/com/glia/widgets/chat/ChatManagerTest.kt`
- Always provide error handlers in `.subscribe()` — bare lambda subscriptions that swallow errors are forbidden.

### Dual Logging Channels

Both channels are required when logging on public API paths:

- `Logger` (internal, `widgetssdk/src/main/java/com/glia/widgets/helper/Logger.kt`): operational debug/info/warn/error. Debug builds route to `android.util.Log` and return early to avoid double-logging through `LoggingAdapter`. Release dispatches to Core-injected `ClientLoggerAdapter`.
- `GliaLogger` (telemetry_lib): structured product analytics using typed `LogEvents.*` constants. Exposes `logMethodUse`, `logDeprecatedApiUse`.
- TAG convention: `internal val Any.TAG: String get() = javaClass.simpleName` extension property. Java classes define `TAG` manually as a constant. Evidence: `widgetssdk/src/main/java/com/glia/widgets/helper/Logger.kt`

### Theme System

- `Mergeable<T>` with `infix fun merge`: `defaultTheme merge remoteTheme` — the `other` (remote) operand always wins when non-null. Evidence: `widgetssdk/src/main/java/com/glia/widgets/view/unifiedui/Merge.kt`
- `DefaultTheme` in `defaulttheme/` is a TOP-LEVEL FUNCTION (not a class or companion object), annotated with `@file:Suppress("FunctionName")` because its name starts with a capital letter. Evidence: `widgetssdk/src/main/java/com/glia/widgets/view/unifiedui/theme/defaulttheme/Base.kt`
- Views apply theme via extension functions in `UnifiedUiExtensions.kt` (`applyColorTheme`, `applyLayerTheme`, `applyButtonTheme`, `applyTextTheme`). Theme access goes directly through `Dependencies.gliaThemeManager` — not through contracts.
- All hardcoded colors and dimensions are forbidden — use theme system and resources.

### Locale and Strings

- All UI strings are wrapped in `LocaleString(@StringRes stringKey: Int, vararg values: StringKeyPair)`. `LocaleProvider` resolves at runtime and emits on locale change.
- View extension functions (`TextView.setLocaleText`, `setLocaleHint`, `View.setLocaleContentDescription`, `setAccessibilityHint`) register LIVE listeners — they update automatically on locale change.
- All SDK string resource names are prefixed `glia_`. `new_strings.xml` provides backward-compatible aliases mapping old public-facing names to current `glia_*` names. Evidence: `widgetssdk/src/main/res/values/new_strings.xml`

### Testing

- Mockito-Kotlin (`org.mockito.kotlin.mock`, `whenever`, `verify`, `argumentCaptor`) is used in older controller tests. MockK (`io.mockk`, `@MockK`, `every {}`) is used in newer Kotlin-first tests. Both coexist intentionally.
- Test function names use backticks.
- Assertions use `junit.framework.TestCase.assert*` — not AssertJ or Hamcrest.
- Snapshot tests extend `SnapshotTest` (open base class), implement `Snapshot*` interfaces. Paparazzi rule name uses underscore prefix: `_paparazzi`. Defaults: `PIXEL_4A`, `"ThemeOverlay_Glia_Chat_Material"`, max pixel diff `0.001`. Evidence: `widgetssdk/src/testSnapshot/java/com/glia/widgets/SnapshotTest.kt`

### Package Organization

- Feature-based top-level packages: `chat/`, `call/`, `engagement/`, `entrywidget/`, etc.
- Sub-layers within each feature: `domain/` (use cases), `data/` (repositories, sources), `model/` (data types), `controller/` (when multiple controllers exist), `adapter/` (RecyclerView adapters).
- Cross-cutting concerns live in `internal/`.
- `com.glia.widgets.internal.*` uses Kotlin `internal` visibility — invisible at the language level to integrators. No ProGuard keep rules needed.

### Thread Safety

- `@Volatile` for state fields accessed from multiple threads (e.g., `isChatViewPaused`, `chatState`, `callState`). Evidence: `widgetssdk/src/main/java/com/glia/widgets/chat/controller/ChatController.kt`
- View detach uses `synchronized(this) { view = null }` (bare `synchronized` block, NOT `@Synchronized` annotation).

### Accessibility

- Content descriptions are mandatory on all interactive and meaningful views. TalkBack support is required — not optional polish.
- Use `View.setLocaleContentDescription` and `setAccessibilityHint` (live listener variants) — not one-shot assignments. Evidence: `widgetssdk/src/main/java/com/glia/widgets/helper/ViewExtensions.kt`

---

## Anti-Patterns

1. **Never add a singleton class.** Use the `Dependencies` object. `GliaWidgets` and `Dependencies` are the only sanctioned Kotlin `object` singletons. Java `*Factory` static fields are grandfathered.

2. **Never call `GliaWidgets.onAppCreate()` from integrator code or tests.** It is deprecated and redundant because `InitializationProvider` already invoked the initialization path; calling it again re-runs setup and risks double-init side effects. Evidence: `widgetssdk/src/main/java/com/glia/widgets/GliaWidgets.kt`

3. **Never store Activity, Fragment, or View in static fields outside `ControllerFactory`'s retained slots.** Memory leaks. Use the `resumedActivity: Flowable<WeakReference<Activity>>` surface exposed by the registered `ActivityLifecycleCallbacks` watchers.

4. **Never run the `dokkaHtml` Gradle task.** Dokka V2 is used in this project; the task `dokkaGeneratePublicationJavadoc` is what Maven publishing is wired to. Evidence: `widgetssdk/build.gradle`

5. **Never add a theme property to only one of `DefaultTheme` or `RemoteConfiguration`.** The `Mergeable<T>` merge requires both sides. Missing either yields `null` for that property at runtime with no error.

6. **Never reference `project(':widgetssdk')` in the demo app's release variant.** The release variant consumes a locally-published SNAPSHOT via `preReleaseBuild.dependsOn ':widgetssdk:publishSnapshotPublicationToMavenLocal'`. Evidence: `app/build.gradle`

7. **Never add Paparazzi to the default `buildTypes` block.** It is scoped to the `snapshot` build type specifically to isolate Kotlin classpath injection. Evidence: `widgetssdk/build.gradle`

8. **Never put version numbers in `gradle.properties`.** `version.properties` is the single source of truth. Bitrise automation reads it via Gradle tasks and uses `git stash` around bot-authored version bumps.

9. **Never commit Paparazzi snapshot PNGs without Git LFS.** `.gitattributes` routes `widgetssdk/src/test/snapshots/**/*.png` through LFS. Always verify LFS is initialized (`git lfs install`) before working on snapshot tests.

10. **Never assume Firebase Messaging is transitively available to integrators.** The SDK declares `implementation` on `firebase-messaging`, but the published POM strips this dependency via `excludeOptionalDependencies`. Integrators must declare their own Firebase Messaging dependency and proxy push payloads through `PushNotifications.onNewMessage()`.

11. **Never systematically migrate all Mockito tests to MockK.** Both coexist intentionally. Opportunistic migration during feature work is fine; a dedicated migration sweep is not planned.

12. **Never introduce `ViewModel`.** Retained controller slots in `ControllerFactory` already handle config-change survival for `ChatController` and `CallController`.

13. **Never call the no-arg `onDestroy()` on `ChatController` or `CallController`.** The override throws `RuntimeException("no op")` intentionally. Call `onDestroy(retain: Boolean)`. Evidence: `widgetssdk/src/main/java/com/glia/widgets/chat/controller/ChatController.kt`

14. **Never define a TAG constant in a Kotlin companion object.** The `internal val Any.TAG` extension property is the convention. Evidence: `widgetssdk/src/main/java/com/glia/widgets/helper/Logger.kt`

15. **Never use the `snapshot` build type with `includeBuild` composite for local Core SDK.** Composite build does not support custom build types. Run `./gradlew widgetssdk:publishCoreSdkToLocalMaven` first, then run snapshot tasks.

16. **Never upgrade `jackson-core` beyond the force-pinned version.** `com.fasterxml.jackson.core:jackson-core` is forced to `2.15.3` project-wide via `resolutionStrategy { force ... }` in root `build.gradle` to work around a Dokka CVE. Upgrading to "fix" a Snyk alert breaks Dokka generation. Evidence: `build.gradle` (root)

17. **Never use `android.util.Log` directly.** Use the internal `Logger` object for operational logging. Evidence: `widgetssdk/src/main/java/com/glia/widgets/helper/Logger.kt`

18. **Never log PII.** No user input, operator names, message content, or any personally identifiable data may appear in any log channel.

---

## Gotchas

**ContentProvider auto-init ordering is rigid.** `InitializationProvider.onCreate` has already registered lifecycle watchers before the integrator calls `GliaWidgets.init()`. Moving initialization work between `Dependencies.onAppCreate` (called from the provider) and `Dependencies.onSdkInit` (called from `init()`) causes watchers to miss early app lifecycle events or to run without applied configuration.

**Retained controllers survive Activity recreation with full state.** `ChatController` and `CallController` state — timers, subscriptions, chat history position, media upgrade state — must remain valid after `onDestroy(retain=true)`. Clearing too aggressively during `retain=true` loses in-progress state across rotation; not clearing on `retain=false` leaks memory.

**`destroyControllers()` does NOT reset repositories.** `SecureConversationsRepository`, `QueueRepository`, and `FileAttachmentRepository` are process-global static singletons in `RepositoryFactory`. They persist across controller teardown. Stale repository state can resurface on the next engagement session.

**Snapshot build type cannot use `includeBuild` composite for local Core SDK.** When iterating against an unreleased local Core SDK, run `./gradlew widgetssdk:publishCoreSdkToLocalMaven` first, then run any snapshot tasks.

**`EntryWidget.getView(context)` returns a fresh instance every call.** `Dependencies.entryWidget` is a computed `get()` property, not `by lazy`. Caching the returned view externally causes double-parent attachment exceptions.

**`./gradlew testSnapshotUnitTest` runs zero unit tests by design.** The `test.java.srcDirs = []` configuration removes the default test source set, then re-adds sources only to `testDebug` and `testRelease`. Use `./gradlew widgetssdk:test` for unit tests and `./gradlew widgetssdk:verifyPaparazziSnapshot` for snapshot verification.

**Screen sharing in Widgets SDK was fully removed (MOB-4366).** It is not deprecated — it was deleted. Any residual references are dead ends. CallVisualizer retains screen sharing capability; that is a separate path.

**Secure Conversations ↔ Live engagement transitions are a known regression area.** The bidirectional transition (GVA→SC and SC→Live upgrade) has produced multiple regressions: SC banner lingering after SC→Live transition, pre-engagement hint persisting after SC upgrades to Live. Regression-test both directions on any change touching `SecureConversationsRepository`, `EngagementRepository` state transitions, or the SC↔Live upgrade path.

**Auth + engagement-end lifecycle boundary is fragile.** A fix for "engagement end during de-authentication" was deliberately reverted (commit `bdcf1895`). A flag was added separately to suppress the push notification permission dialog during authentication flow. Any change touching auth state, engagement end, or push notification permission timing in the same commit should be treated with heightened caution.

**Gradle sync auto-installs Git hooks and requires local tooling.** `scripts/setup-git-hooks.sh` runs on every Gradle sync. It requires `gitleaks`, `trufflehog`, and `git-lfs`. Set `ANDROID_WIDGETS_SDK_SKIP_GIT_HOOKS=true` to bypass. Auto-bypassed when `BITRISE_IO=true`.

**`LocaleString` view extensions register live listeners, not one-shot setters.** Calling `setLocaleText(R.string.glia_foo)` on a `TextView` registers a listener that re-resolves on every locale change. Directly assigning `text = getString(R.string.glia_foo)` does not update on locale change.

**`new_strings.xml` is a backward-compat alias layer, not a file of new strings.** The file name is misleading. It maps old public string resource names to current `glia_`-prefixed names. New strings go in `strings.xml`. Evidence: `widgetssdk/src/main/res/values/new_strings.xml`

**`Logger` routes to `android.util.Log` ONLY when `BuildConfig.DEBUG` is true.** In a release SDK loaded inside a non-debug integrator app, logs go through `LoggingAdapter` (Core-injected `ClientLoggerAdapter`).

**`GliaActivity<T>` is an interface, not an abstract class.** Activities simultaneously extend `FadeTransitionActivity` AND implement `GliaActivity<ViewType>`. Do not refactor to a base class hierarchy — conflicts with single-inheritance and `FadeTransitionActivity`'s transition logic.

**`PushNotificationsImpl` intercepts `SECURE_MESSAGING_TYPE` before Core sees it.** When debugging why Core does not process a particular push payload, check `PushNotificationsImpl` first.

**Telemetry is initialized inside Core SDK via `InitializationProvider`.** There is no `TelemetryHelper.init` call in Widgets — removed as redundant after telemetry init moved to Core.

**`SiteApiKey` is deprecated in favor of the `AuthorizationMethod` sealed interface (MOB-5010).** Code on `development` may reference the sealed interface. Do not add new callers using `SiteApiKey`.

**The `use_overlay` bubble flag is deprecated and was reverted once.** The "display inside app" bubble behavior using `use_overlay` was reverted. Current flags: `enableBubbleOutsideApp` and `enableBubbleInsideApp`. Never propose reinstating `use_overlay`.

**Rebasing a branch without re-recording Paparazzi snapshots is a recurring mistake.** After any rebase that touches layout, theme, or resource files, run `./gradlew widgetssdk:recordPaparazziSnapshot` before pushing.

---

## Stack Constraints

### Version Pins With Rationale

All of the following pins have WHY comments in `gradle/libs.versions.toml` and must not be changed without resolving the underlying issue:

- `core-ktx 1.16.0` — version 1.17.0 requires `compileSdk 36`, which the SDK module does not use (demo app uses 36, SDK uses 35).
- `coil-core` / `coil-network` `3.2.0` — versions above 3.2.0 require Kotlin 2.2.0 or higher; the project is on Kotlin 2.1.21.
- `dokka 2.0.0` — Kotlin and Dokka versions must share the same minor version.
- `paparazzi 1.3.5` — updating risks silently upgrading the Kotlin version for the entire project, not just the snapshot build type.

### Forced Resolution Rules

- `com.fasterxml.jackson.core:jackson-core:2.15.3` is forced project-wide via `resolutionStrategy` in root `build.gradle`. Reason: Dokka CVE workaround. Do not remove or change this pin to address Snyk alerts — it will break `dokkaGeneratePublicationJavadoc`.
- `lint-api` version must equal AGP version plus the 23.0.0 offset (AGP 8.13.0 → lint-api 31.13.0). The custom `lint_checker/` module breaks silently on version mismatch.

### Packaging Quirks

- MockK introduces duplicate `META-INF/LICENSE` files. An explicit `merges` rule in `packagingOptions` resolves this — do not remove it.
- Unit tests require `org.json:json:20250517` to fill gaps in Robolectric's stub coverage of Android JSON APIs.

### Build Type and Variant Behavior

- Core SDK is declared per build type: `releaseApi`, `debugApi`, `snapshotApi`. The `snapshot` build type cannot use `includeBuild` composite for local Core SDK substitution.
- Firebase BOM and `firebase-messaging` are declared as `implementation` in the SDK module but stripped from the published POM via `excludeOptionalDependencies`.
- Demo app targets `compileSdk 36`; SDK module targets `compileSdk 35`.
- Paparazzi is applied only inside the `snapshot buildTypes` block — non-standard placement, intentional.
- Gradle parallel execution is disabled. Modules are not guaranteed to be fully decoupled.
- `resolutionStrategy.cacheChangingModulesFor 0, 'seconds'` forces a remote re-fetch of every `-SNAPSHOT` dependency on each build. Do not "optimize" this away.

### JVM Configuration

- Java 17 toolchain (Gradle toolchain API).
- JVM unit test heap: `-Xmx4g`.
- `android.enableBuildConfigAsBytecode=true`.

---

## Workflows and Commands

### Common Gradle Tasks

```
./gradlew widgetssdk:test                                       # Unit tests (testDebug + testRelease)
./gradlew widgetssdk:recordPaparazziSnapshot                    # Record/update Paparazzi snapshots
./gradlew widgetssdk:verifyPaparazziSnapshot                    # Verify against stored snapshots
./gradlew widgetssdk:lintDebug                                  # Lint SDK (includes custom lint_checker rules)
./gradlew ktlintCheck                                           # KtLint check
./gradlew ktlintFormat                                          # KtLint format + auto-fix
./gradlew dokkaGeneratePublicationJavadoc                       # Dokka V2 Javadoc JAR (wired into Maven publishing)
./gradlew -q printCurrentVersionName                            # Print current version string
./gradlew saveWidgetsVersion --type=patch|minor|major           # Bump version in version.properties
./gradlew saveCoreSdkVersion --sdkVersion=X.Y.Z                 # Update Core SDK version reference
./gradlew widgetssdk:publishSnapshotPublicationToMavenLocal     # Publish SNAPSHOT to ~/.m2
./gradlew widgetssdk:publishCoreSdkToLocalMaven                 # Publish local Core SDK to ~/.m2 (required before snapshot tasks with direct Core)
./gradlew clean widgetssdk:publishMavenPublicationToMavenCentralRepository  # Release publish (CI only)
```

### Local Core SDK Substitution

When iterating against an unreleased local Core SDK:

1. Add `dependency.coreSdk.useDirect=true` to `local.properties` (overrides `CORE_SDK_USE_DIRECT` environment variable).
2. Optionally set `dependency.coreSdk.path=<absolute path>` (defaults to `../android-sdk`).
3. Debug and release build types use Gradle composite build (`includeBuild`) automatically.
4. For snapshot build type (Paparazzi tests): run `./gradlew widgetssdk:publishCoreSdkToLocalMaven` FIRST, then any snapshot tasks. The snapshot build type does not support `includeBuild`.

### Demo App Environment Variables

Set in `local.properties` or as environment variables:

- Required: `GLIA_REGION` (default: `beta`), `GLIA_API_KEY_SECRET`, `GLIA_API_KEY_ID`, `GLIA_SITE_ID`, `GLIA_QUEUE_ID`
- Optional: `GLIA_JWT`, `FIREBASE_PROJECT_ID`, `FIREBASE_API_KEY`, `FIREBASE_APP_ID`, `FIREBASE_APP_ID_DEBUG`

### Git Hooks

Hooks live in `.git-hooks/` and are installed on every Gradle sync via `scripts/setup-git-hooks.sh`:

- `pre-commit` — runs `gitleaks` and `trufflehog` for secret scanning. Delegates to `$HOME/.git-hooks/pre-commit` if present.
- `pre-push` — enforces `git-lfs`.
- Bypass: set `ANDROID_WIDGETS_SDK_SKIP_GIT_HOOKS=true`. Auto-bypassed when `BITRISE_IO=true`.
- Install required tools: `brew install gitleaks trufflehog git-lfs`

### Branch Strategy

- `master` — release branch. Tagged on each release.
- `development` — integration branch. All PRs target `development`.
- Snyk bot opens PRs against `master`; a GitHub Actions workflow silently retargets them to `development`.

### CI Pipeline

All four GitHub Actions workflows are thin `curl` triggers to Bitrise (`workflow_dispatch` only). Real CI lives in `bitrise.yml`. Key Bitrise workflows:

- `development` — runs on merge to `development`
- `pull_request` — PR validation
- `publish_to_nexus` — release publication; auto-increments patch version after successful publish
- `_increment_project_version` — version bump helper
- `upgrade_dependencies`, `upgrade_telemetry_dependency` — automated dependency PRs
- `post_release` — post-publication steps (triggers Flutter/Ionic/RN wrapper dispatch + Cortex Financial app build)
- `browserstack_upload` — demo app upload for device testing

Maven Central signing: `ORG_GRADLE_PROJECT_signAllPublications=true` is set by Bitrise — it is not in any in-repo file.

---

## Git Intelligence

### Hotspot Files

- `widgetssdk/src/main/java/com/glia/widgets/di/ControllerFactory.java` — manual DI registry for every controller in the SDK. Touched by every new screen, controller, or major use case change. Still Java and not being migrated soon. Expect merge conflicts on feature branches that add screens.

- `widgetssdk/src/main/java/com/glia/widgets/di/UseCaseFactory.java` — moves in lockstep with `ControllerFactory.java`. Every new use case registration lands here. Same Java-retention caveat.

- `widgetssdk/src/main/java/com/glia/widgets/chat/controller/ChatController.kt` — convergence point for Secure Conversations ↔ Live transitions, media upgrade logic, read tracking, and telemetry. Java→Kotlin migration history; accumulates targeted fixes for chat-related edge cases.

### Recurring Fix Patterns

- **Accessibility sweeps follow feature work.** Features ship, then a follow-up commit adds TalkBack hints, content descriptions, and accessibility roles. Most recent wave covered chat bubble, dialog buttons, survey titles, attachment popup. Treat accessibility as a mandatory second pass for every UI change.

- **Push notification layers accumulate regressions.** PN functionality was built incrementally (initial → transcript-from-PN → secure-messaging PN → visitor ID in auth → permissions → dialog simplification). Each layer introduced regressions. The auth/engagement/PN triangle is the highest-risk area.

- **Snapshot updates after rebase are a recurring commit pattern.** Before pushing any rebase, run `./gradlew widgetssdk:recordPaparazziSnapshot` and include resulting PNG changes.

### Architectural Decisions — Never Revert

- **Screen sharing removal from Widgets SDK (MOB-4366)** — deliberately deleted, not deprecated. Residual references are dead ends. CallVisualizer retains screen sharing as a separate code path. Never propose restoring Widgets-side screen sharing.

- **`use_overlay` flag deprecation** — the "display bubble inside app" behavior was reverted once. Replacement flags are `enableBubbleOutsideApp` and `enableBubbleInsideApp`. Never propose reinstating `use_overlay`.

- **"Fix engagement end during de-authentication" revert** — the auth/engagement lifecycle interaction at de-authentication is genuinely fragile. Any proposed fix requires careful isolation and regression testing of both auth and engagement-end paths simultaneously.

- **Telemetry initialization moved to Core SDK** — `TelemetryHelper.init` was removed from Widgets as a no-op after Core SDK took over telemetry initialization in `InitializationProvider`. Do not add it back.

- **`SiteApiKey` deprecation in favor of `AuthorizationMethod` sealed interface (MOB-5010)** — do not add new usages of `SiteApiKey`.

- **`version.properties` timestamp field removal** — removed specifically to prevent bot-authored Bitrise version-bump PRs from conflicting with developer PRs. Do not add any auto-generated or timestamp-based field back.

- **`resolutionStrategy.cacheChangingModulesFor 0, 'seconds'`** — forces SNAPSHOT re-fetch on every build. Do not add a caching duration to "optimize" CI build times.

- **Snapshot PNGs routed through Git LFS** — binary PNG blobs committed to the regular object store caused a repo repair commit. The `.gitattributes` LFS routing for `widgetssdk/src/test/snapshots/**/*.png` is mandatory.
