<!-- deep-research: completed 2026-04-24 | v: deep-research/1.7 | scope: widgetssdk/src/main/java/com/glia/widgets/chat -->
# Chat
> Full-stack chat feature: live engagement and Secure Conversations (SC), converged in one MVP stack.

Parent: [CLAUDE.md](../../../../../../../../CLAUDE.md) | [docs/claude-reference.md](../../../../../../../../docs/claude-reference.md)

## Context Loading Order
1. `ChatContract.kt` — full MVP interface pair; read before touching Controller or View
2. `controller/ChatController.kt` — retained controller; three segregated `CompositeDisposable` fields at the top are load-bearing
3. `ChatManager.kt` — owns message list state; `ChatController` delegates all list mutations here
4. `ChatView.kt` — self-wires via `setupControllers()`; handles SC top-banner visibility and engagement banners

## Where to Look
| Task | File |
|------|------|
| Add Controller method | `ChatContract.kt` (both sides), `controller/ChatController.kt` |
| SC ↔ Live transition logic | `controller/ChatController.kt` — `initChat()`, `initSecureMessaging()`, `initLiveChat()` |
| Message list mutations | `ChatManager.kt` |
| RecyclerView item types / view holders | `adapter/ChatAdapter.kt`, `adapter/holder/` |
| Chat state shape | `model/ChatState.kt` |
| SC top-banner visibility | `controller/ChatController.kt` — `observeTopBannerUseCase()` subscription |
| Media upgrade offer | `controller/ChatController.kt` — `mediaUpgradeDisposable` block |
| Read-tracking / unseen indicator | `../view/MessagesNotSeenHandler.java` |
| Domain use cases | `domain/` |

## Conventions
- **Self-wiring**: `ChatView.setupControllers()` calls `Dependencies.controllerFactory.chatController` then `controller.setView(this)`. Activity does not inject or own the controller. Evidence: `ChatView.kt`
- **Three segregated disposables**: `disposable` (main, cleared only when `retain=false` in `onDestroy`), `mediaUpgradeDisposable` (cleared `onPause`), `connectionDisposable` (cleared `onPause`). Each has distinct lifecycle. Evidence: `controller/ChatController.kt`
- **`Intention` enum gates init path**: `initChat(intention)` dispatches to `initLiveChat()`, `initSecureMessaging()`, or leave-dialog flows. Adding a new entry point means adding an `Intention` variant. Evidence: `controller/ChatController.kt`
- **`onDestroy(retain: Boolean)` is the real cleanup hook** — `onDestroy()` (no-arg) throws `RuntimeException("no op")` by design. Evidence: `controller/ChatController.kt`
- **`emitViewState {}` is `@Synchronized`** and `view` is nulled inside a `synchronized(this)` block in `onDestroy` — keep UI mutation on the synchronized path. Evidence: `controller/ChatController.kt`
- **`ChatAdapter` uses integer view-type constants** — the `dataObserver` in `ChatView` branches on `CUSTOM_CARD_TYPE` for scroll-delay behaviour. Match the existing `ChatAdapter.*_TYPE` constants when adding a new item type. Evidence: `adapter/ChatAdapter.kt`, `ChatView.kt`

## Anti-Patterns
- **Do NOT merge the three `CompositeDisposable` instances.** `mediaUpgradeDisposable` and `connectionDisposable` must be released on pause but the controller survives pause; `disposable` must survive pauses but release only on permanent destroy. Consolidating them causes either resource leaks or premature disposal during orientation change.
- **Do NOT subscribe to `observeTopBannerUseCase()` conditionally on Activity re-attach.** The subscription lives inside `disposable`, which survives config changes. Resubscribing on re-attach doubles observers and produces duplicate banner-visibility events — the known regression pattern in SC↔Live boundary history.
- **Do NOT emit view state from a non-`@Synchronized` path.** Direct `view?.emitState(...)` calls from a background thread race with `onDestroy`'s `synchronized(this) { view = null }`.
- **Do NOT use `!!` for the view reference.** The view is nulled inside a `synchronized` block; unsafe non-null assertions crash on configuration change.

## For AI Agents
- Never consolidate `disposable`, `mediaUpgradeDisposable`, and `connectionDisposable` into a single `CompositeDisposable` — the three-way split is the memory-leak and pause-lifecycle contract.
- Never call `onDestroy()` (no-arg) on `ChatController` — it throws intentionally. Always use `onDestroy(retain: Boolean)`.
- Never inject or assign `ChatContract.Controller` from `ChatActivity` — `ChatView.setupControllers()` is the only wiring point; duplicating it creates double-subscription bugs.
- Never remove `contentDescription` or accessibility roles from chat bubble views or the message `EditText` — TalkBack support was added deliberately and must be preserved on every edit to those layouts.
- When touching SC↔Live transition code, manually verify: (1) SC top banner disappears after upgrade to Live, (2) pre-engagement hint does not reappear after upgrade, (3) banner does not linger after SC→Live hand-off. Each of these has caused a separate past regression.
