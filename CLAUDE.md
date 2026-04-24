<!-- deep-research: completed 2026-04-24 | v: deep-research/1.7 | generator: glia/deep-research -->
# GliaWidgets Android SDK
> Drop-in AAR (`com.glia:android-widgets`) providing chat, audio/video call, secure conversations, call visualizer, entry widget, and survey UI — layered on top of Glia Core SDK.

## Architecture

```
Integrator App
  -> GliaWidgets.init(GliaWidgetsConfig) -> Dependencies.onSdkInit()
  -> GliaWidgets.getEngagementLauncher(queueIds)
EngagementLauncher -> ActivityLauncherImpl -> ChatActivity | CallActivity | MessageCenterActivity | ...
Activities inflate Views (ChatView, CallView, ...) which self-wire Controllers via Dependencies.controllerFactory
Controllers -> UseCases (operator fun invoke()) -> EngagementRepository (BehaviorProcessor<State>)
  -> GliaCore interface -> Glia.* (Core SDK — all networking, auth, data)
```

MVP pattern: View + Controller + Contract interfaces. `ChatController` and `CallController` are retained across Activity recreation via `ControllerFactory` (not ViewModel). DI is a single `Dependencies` Kotlin object — no Dagger/Hilt.

## Context Loading Order

1. `.claude/CLAUDE.md` — authoritative rules: logging, PII, MVP cleanup, no singletons, theme mandate
2. `widgetssdk/src/main/java/com/glia/widgets/GliaWidgets.kt` — public API surface, init chain, engagement launcher
3. `widgetssdk/src/main/java/com/glia/widgets/di/Dependencies.kt` — full DI container, init order, mock seams
4. `widgetssdk/src/main/java/com/glia/widgets/InitializationProvider.kt` — ContentProvider auto-init; why integrators call nothing manually
5. `widgetssdk/src/main/java/com/glia/widgets/engagement/States.kt` — sealed state machine that is the single source of truth for all engagement state
6. `widgetssdk/src/main/java/com/glia/widgets/view/unifiedui/` — three-stage theme pipeline; required reading before any UI change
7. `gradle/libs.versions.toml` — version pins with rationale comments

## Where to Look

| Task | Location |
|------|----------|
| Public API changes | `GliaWidgets.kt`, `GliaWidgetsConfig.kt` |
| Add/change DI wiring | `di/Dependencies.kt` → see [di/CLAUDE.md](widgetssdk/src/main/java/com/glia/widgets/di/CLAUDE.md) |
| Chat or secure conversations | `chat/` → see [chat/CLAUDE.md](widgetssdk/src/main/java/com/glia/widgets/chat/CLAUDE.md) |
| Engagement state machine | `engagement/` → see [engagement/CLAUDE.md](widgetssdk/src/main/java/com/glia/widgets/engagement/CLAUDE.md) |
| Theme / styling | `view/unifiedui/` → see [unifiedui/CLAUDE.md](widgetssdk/src/main/java/com/glia/widgets/view/unifiedui/CLAUDE.md) |
| Snapshot (UI) tests | `widgetssdk/src/testSnapshot/` → see [testSnapshot/CLAUDE.md](widgetssdk/src/testSnapshot/CLAUDE.md) |
| Version, dependency pins | `gradle/libs.versions.toml`, root `build.gradle` |
| Push notifications | `fcm/`, `push/` |
| Custom lint rules | `lint_checker/` |
| Release / version scripts | `scripts/` |

## Gotchas

1. **Snapshot build type cannot use composite-build Core substitution.** Debug and release use `includeBuild` to substitute Core SDK; the `snapshot` build type cannot. Run `./gradlew widgetssdk:publishCoreSdkToLocalMaven` first, then snapshot tasks. Mixing these causes silent resolution failures.

2. **ContentProvider auto-init makes manual init redundant.** `InitializationProvider` runs `Dependencies.onAppCreate()` before `Application.onCreate()`. The deprecated `GliaWidgets.onAppCreate()` still contains real work — calling it from integrator code re-invokes initialization and risks double-init. Do not call it.

3. **`ChatController` and `CallController` are retained across Activity recreation by design.** `ControllerFactory` holds `retainedChatController` and `retainedCallController`. This is intentional — there is no ViewModel. The no-arg `onDestroy()` override throws `RuntimeException("no op")`; always call `onDestroy(retain: Boolean)` so the factory can decide whether to clear or keep the controller instance.

## For AI Agents

- Never use `android.util.Log` directly — route all logging through `helper/Logger.kt` and `GliaLogger` from the telemetry SDK. Public API entry points must log to both.
- Never log PII: no chat content, operator names, visitor names, or API keys at any severity level.
- Never bypass the `GliaCore` interface — all networking, auth, and data storage must flow through `GliaCoreImpl`, never through direct Glia SDK calls from feature code.
- Never use `!!` — use `?.`, `?:`, `requireNotNull("message")`, or early returns.
- Never block the main thread — use RxJava 3 schedulers (`io.reactivex.rxjava3.*`); always provide an error handler in `.subscribe()`.
- Never hardcode colors, dimensions, or strings — use the Unified Theme system and string resources (prefix `glia_`, wrap in `LocaleString`); legacy `UiTheme.kt` is deprecated.
- Never introduce a new singleton class — add dependencies to the `Dependencies` object.
- Never use Jetpack Compose — traditional View system only.
- Never write new classes in Java — all new code must be Kotlin. When touching an existing Java file for a small, self-contained change, migrate it to Kotlin in the same PR if doing so fits the task's context; leave large/high-churn Java files (e.g., `ControllerFactory.java`, `UseCaseFactory.java`, `RepositoryFactory.java`) for coordinated migration.
- Always dispose `CompositeDisposable` in `onDestroy(retain = false)`, not in `onPause` or unconditional `onDestroy`.
- Always declare explicit return types on Kotlin functions and class/file-level variables; local variables may use inference.
- Always add or update unit tests for every changed layer; always run `recordPaparazziSnapshot` + `verifyPaparazziSnapshot` for any UI change.
- MockK and Mockito-Kotlin coexist in the test suite intentionally — do not migrate one to the other.

## Directory Map

```
widgetssdk/             # Published AAR → see widgetssdk/CLAUDE.md
├── src/main/java/com/glia/widgets/
│   ├── di/             # DI container + ControllerFactory → see di/CLAUDE.md
│   ├── chat/           # Chat + secure conversations → see chat/CLAUDE.md
│   ├── engagement/     # State machine, single source of truth → see engagement/CLAUDE.md
│   ├── view/unifiedui/ # Three-stage theme pipeline → see view/unifiedui/CLAUDE.md
│   ├── call/, callvisualizer/, survey/, messagecenter/, entrywidget/, webbrowser/
│   ├── fcm/, push/     # Firebase push (integrator-owned FCM proxy pattern)
│   ├── internal/       # Kotlin-internal implementation detail
│   └── helper/, view/  # Shared helpers, legacy UI base classes
├── src/test/           # Unit tests (MockK + Mockito-Kotlin, JUnit 4, Robolectric)
└── src/testSnapshot/   # Paparazzi snapshot tests (custom build type) → see testSnapshot/CLAUDE.md
app/                    # Demo app (compileSdk 36; release consumes locally-published SNAPSHOT)
lint_checker/           # Custom lint: UndocumentedApiIssue at ERROR severity
scripts/                # version-updater.gradle, direct-core.gradle, changelog, git-hooks setup
.git-hooks/             # pre-commit (gitleaks + trufflehog), pre-push (git-lfs)
```

## References

### [docs/claude-reference.md](docs/claude-reference.md)
- [Module Dependencies](docs/claude-reference.md#module-dependencies)
- [Conventions](docs/claude-reference.md#conventions)
- [Anti-Patterns](docs/claude-reference.md#anti-patterns)
- [Gotchas](docs/claude-reference.md#gotchas)
- [Stack Constraints](docs/claude-reference.md#stack-constraints)
- [Workflows and Commands](docs/claude-reference.md#workflows-and-commands)
- [Git Intelligence](docs/claude-reference.md#git-intelligence)

### Subdirectory Guides
| File | Description |
|------|-------------|
| [widgetssdk/CLAUDE.md](widgetssdk/CLAUDE.md) | SDK module build config, publishing, AAR internals |
| [di/CLAUDE.md](widgetssdk/src/main/java/com/glia/widgets/di/CLAUDE.md) | DI wiring, ControllerFactory retention, mock seams |
| [chat/CLAUDE.md](widgetssdk/src/main/java/com/glia/widgets/chat/CLAUDE.md) | Chat + secure conversations patterns |
| [engagement/CLAUDE.md](widgetssdk/src/main/java/com/glia/widgets/engagement/CLAUDE.md) | State machine, EndAction, EngagementRepository |
| [view/unifiedui/CLAUDE.md](widgetssdk/src/main/java/com/glia/widgets/view/unifiedui/CLAUDE.md) | Theme pipeline stages, LocaleString, remote JSON config |
| [testSnapshot/CLAUDE.md](widgetssdk/src/testSnapshot/CLAUDE.md) | Paparazzi setup, snapshot build type, record/verify workflow |

### External
- [Developer Guide](.claude/DEVELOPER_GUIDE.md) — local setup, composite build, git hooks
- [Developer Docs](https://developer.glia.com/api-usage-refs/android-api) — public API reference
- [iOS counterpart](https://github.com/salemove/ios-sdk-widgets) — cross-platform parity reference
