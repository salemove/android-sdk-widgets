<!-- deep-research: completed 2026-04-24 | v: deep-research/1.7 | scope: widgetssdk/src/main/java/com/glia/widgets/di -->
# DI Layer
> The wiring spine of the SDK — factories, lifecycle watchers, and the GliaCore testability seam

Parent: [CLAUDE.md](../../../../../../../../CLAUDE.md) | [docs/claude-reference.md](../../../../../../../../docs/claude-reference.md)

## Context Loading Order
1. `Dependencies.kt` — Kotlin `object`; orchestration entry point, initialization order, lifecycle watcher registration
2. `ControllerFactory.java` — retained controller semantics; central DI registry for every controller
3. `UseCaseFactory.java` — use case registration; moves in lockstep with `ControllerFactory`

## Where to Look
| Task | File |
|------|------|
| Wire a new Controller | `ControllerFactory.java` |
| Wire a new UseCase | `UseCaseFactory.java` |
| Wire a new Repository | `RepositoryFactory.java` |
| Substitute Core SDK in tests | `Dependencies.kt` — `gliaCore` has `@VisibleForTesting set` |
| Substitute factories in tests | `Dependencies.kt` — `controllerFactory`, `useCaseFactory`, `repositoryFactory`, `schedulers` all expose `@VisibleForTesting set` |
| Understand Activity-top access | `Dependencies.kt` `onAppCreate` — `registerActivityLifecycleCallbacks` calls |
| Add/change the Core SDK wrapper | `GliaCore.kt` (interface), `GliaCoreImpl.kt` (delegates to `Glia.*`) |

## Conventions

**`by lazy` vs `get()` in `Dependencies.kt`**:
- `by lazy` — initialized once on first access, cached for the process lifetime. Used for `activityLauncher`, `engagementLauncher`, `pushNotifications`, `liveObservation`, `secureConversations`. Safe only when the object is stateless or its dependencies are stable after `onAppCreate`.
- `get()` computed property — returns a **new instance on every access**. Used for `entryWidget` (`EntryWidgetImpl`). Caching it externally causes double-parent attachment issues.

**`@VisibleForTesting set`** is the sanctioned test injection seam. `gliaCore`, `controllerFactory`, `useCaseFactory`, `repositoryFactory`, `schedulers` all expose public setters guarded by `@VisibleForTesting`. Tests substitute via direct assignment — there is no DI framework, no `@Rule`, no helper.

**Retained controllers** — `retainedChatController` and `retainedCallController` in `ControllerFactory.java` are instance fields, null-checked and lazily constructed. They survive Activity recreation; no `ViewModel` exists because of them. `destroyChatController()` / `destroyCallController()` call `onDestroy(false)` and null the reference. `destroyControllers()` runs both plus service/application chat-head controllers and `messagesNotSeenHandler`.

**Java factories are intentional** — `ControllerFactory.java`, `UseCaseFactory.java`, `RepositoryFactory.java` are Java pseudo-singletons with static fields for selected cached instances (see the `private static` declarations near the top of `UseCaseFactory.java` — a small set of `get*` methods cache via those fields; most `get*` and all `create*` methods return fresh instances). Do not rewrite them to Kotlin objects without coordinating across active feature branches; these files are touched by most feature work.

**Activity watchers share a coordination bus** — all watchers registered in `Dependencies.onAppCreate` (`CallVisualizerActivityWatcher`, `ActivityWatcherForChatHead`, `ActivityWatcherForLiveObservation`, `ActivityWatcherForPermissionsRequest`, `EngagementCompletionActivityWatcher`, `OperatorRequestActivityWatcher`, `UiComponentsActivityWatcher`) extend `BaseSingleActivityWatcher` and expose `resumedActivity: Flowable<WeakReference<Activity>>` (backed by a `PublishProcessor`). Route "top activity" access through one of these — never a static field.

**Initialization is two-phase** — `onAppCreate(application)` must run first (registers watchers, creates factories). `onSdkInit(config)` runs later via `GliaWidgets.init(...)` and calls `gliaCore.init`, `controllerFactory.init()`, `repositoryFactory.initialize()`, `configurationManager.applyConfiguration(config)`, `gliaThemeManager.applyJsonConfig(...)`. Properties guarded by `lateinit` (e.g., `controllerFactory`, `useCaseFactory`) throw `UninitializedPropertyAccessException` if accessed before `onAppCreate`.

**`RepositoryFactory` static fields** — `secureConversationsRepository`, `queueRepository`, `fileAttachmentRepository` etc. are `static` in `RepositoryFactory.java`. They survive `destroyControllers()` — repositories are process-global state, controllers are not.

## Anti-Patterns

- **Adding a `by lazy` property to `Dependencies.kt` that depends on `useCaseFactory` or `controllerFactory`** before confirming both `lateinit` fields are initialized — `by lazy` executes on first access, which may be before `onAppCreate`. Move such initialization inside `onAppCreate` or guard access.
- **Adding a retained controller without a matching `destroy*Controller()` method and a call from `destroyControllers()`** — the retention pattern leaks memory without explicit cleanup.
- **Mixing `create*` and `get*` semantics in `UseCaseFactory.java`** — the convention is that a handful of `get*` methods cache via the `private static` fields declared at the top of the class; other `get*` and all `create*` return fresh instances. Caching a stateful new use case accidentally silently shares mutable state across controllers.

## For AI Agents
- Never call `Dependencies.controllerFactory`, `Dependencies.useCaseFactory`, or `Dependencies.repositoryFactory` before `onAppCreate` has been called — they are `lateinit` and will throw `UninitializedPropertyAccessException`.
- Never call `Glia.*` statics directly outside `GliaCoreImpl.kt` — they bypass the `GliaCore` testability seam and break tests that substitute `Dependencies.gliaCore`.
- Never access the current Activity via a static field — route through a registered `ActivityLifecycleCallbacks` watcher.
- When adding to `UseCaseFactory.java`, name new methods with `create*` if they return a fresh instance and `get*` only if you intentionally cache via a new `private static` field — mixing the convention hides shared state.
