<!-- deep-research: completed 2026-04-24 | v: deep-research/1.7 | scope: widgetssdk/src/main/java/com/glia/widgets/engagement -->
# Engagement
> State machine backbone — single source of truth for every engagement lifecycle event in the SDK

Parent: [CLAUDE.md](../../../../../../../../CLAUDE.md) | [docs/claude-reference.md](../../../../../../../../docs/claude-reference.md)

## Context Loading Order
1. `States.kt` — sealed `State` and `EndAction` hierarchies; read before touching any engagement logic
2. `EngagementRepository.kt` — public contract surface subscribed to by every downstream controller
3. `EngagementRepositoryImpl.kt` — Omnicore event bridge; where `BehaviorProcessor<State>` is driven
4. `completion/EngagementCompletionController.kt` — consumes `State.EngagementEnded` and maps to UI commands

## Where to Look
| Task | File |
|------|------|
| Add or rename a State variant | `States.kt` |
| Change how Omnicore events map to State | `EngagementRepositoryImpl.kt` |
| Change what happens at engagement end | `completion/EngagementCompletionController.kt` |
| End-of-engagement UI (dialogs, survey, finish) | `completion/EngagementCompletionActivityWatcher.kt` |
| New use case over engagement state | `domain/` |
| EndAction-to-UI mapping | `completion/EngagementCompletionController.kt` |

## Conventions
- All use cases in `domain/` expose `operator fun invoke(...)` as the single entry point; add a named method only if a second distinct operation is required (see `EndEngagementUseCase.silently()` as the exception pattern). Evidence: `domain/EndEngagementUseCase.kt`
- `EngagementRepositoryImpl` sets `currentEngagement = engagement` before emitting any new `State` — downstream code that reads `currentEngagement` inside state handlers depends on this ordering. Evidence: `EngagementRepositoryImpl.kt`
- `endEngagement()` is for visitor-initiated ends (log: "ended by:visitor") and checks `state.actionOnEnd.isSurvey`; `terminateEngagement()` is for integrator-initiated ends (log: "ended by:integrator") and does not check survey. Evidence: `EngagementRepositoryImpl.kt`
- `BehaviorProcessor<State>` exposes `engagementState` via `.onBackpressureBuffer().distinctUntilChanged()` — consecutive identical states are swallowed. When a transient state must be observed (e.g., `QueueUnstaffed` → `NoEngagement`), both must be emitted as separate `onNext` calls.

## Anti-Patterns
- **Do NOT emit engagement state from anywhere except `EngagementRepositoryImpl`** — side channels break all downstream subscribers and violate the single-source-of-truth contract.
- **Do NOT read `currentEngagement` before `EngagementRepositoryImpl` has updated it** — several handlers depend on the ordering; reordering causes stale-engagement bugs that are hard to reproduce.
- **Do NOT remove the `ensureNotScTransferredEngagement` guard in `handleEngagementEnd`** — it protects against the reverted "fix engagement end during de-authentication" failure mode (commit `bdcf1895`). Removing it re-introduces that regression.
- **Do NOT call `releaseResourcesUseCase()` when `EndAction` is `Retain`** — `EngagementCompletionController` explicitly returns early for `Retain`; releasing resources breaks the retained-engagement flow.

## For AI Agents
- Never add a new `State` or `EndAction` variant without grepping for all `when (state)` and `when (state.endAction)` branches across the codebase — unhandled variants silently fall to `else` and drop the event.
- Never route engagement lifecycle events through a new repository, manager, or subject. All engagement state flows exclusively through `EngagementRepository.engagementState`.
- Never call `reset()` on `EngagementRepository` during an active live engagement without verifying the auth/de-auth path — the engagement-end-during-de-authentication fix was reverted (`bdcf1895`); any change in this area must be paired with push-notification and authentication regression testing.
- Never instantiate `EngagementCompletionController` outside the single wiring path (`ControllerFactory.getEndEngagementController()` consumed once in `Dependencies.onAppCreate`) — a second instance produces duplicate UI events.
