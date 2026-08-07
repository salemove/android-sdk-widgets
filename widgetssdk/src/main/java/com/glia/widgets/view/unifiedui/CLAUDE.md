<!-- deep-research: completed 2026-04-24 | v: deep-research/1.7 | scope: widgetssdk/src/main/java/com/glia/widgets/view/unifiedui -->
# Unified UI
> Three-stage theme pipeline: JSON → RemoteConfiguration → UnifiedTheme applied by Views via extension functions

Parent: [CLAUDE.md](../../../../../../../../../CLAUDE.md) | [docs/claude-reference.md](../../../../../../../../../docs/claude-reference.md)

## Context Loading Order
1. `config/RemoteConfiguration.kt` — stage 2 hub: owns all screen `@SerializedName` mappings and orchestrates `DefaultTheme merge unifiedTheme`
2. `theme/UnifiedTheme.kt` — final data model; all screen themes and `isWhiteLabel` live here
3. `UnifiedUiExtensions.kt` — how Views consume themes: `applyColorTheme`, `applyLayerTheme`, `applyButtonTheme`, `applyTextTheme`
4. `parse/RemoteConfigurationParser.kt` — Gson setup; all custom deserializers registered here

## Where to Look
| Task | File |
|------|------|
| Add a screen theme field | `theme/UnifiedTheme.kt` + `config/RemoteConfiguration.kt` + `theme/defaulttheme/` |
| Add a JSON key mapping | DTO inside `config/<screen>/` with `@SerializedName` |
| Add a color/size deserializer | `parse/Deserializers.kt` — see `ColorDeserializer`, `DpDeserializer`, `SpDeserializer` |
| Apply theme to a View | `UnifiedUiExtensions.kt` extension functions |
| Change default fallback colors | `theme/defaulttheme/` top-level functions; `config/GlobalColorsConfig.kt` drives `ColorPallet` |
| Suppress Glia branding | `isWhiteLabel` in `RemoteConfiguration` / `UnifiedTheme`; consumed via `applyWhiteLabel()` |

## Theming a new View or widget

Two mechanisms cover every Glia surface, and they layer: the XML `glia*` attributes first, the JSON
Unified theme on top. A new view wires up both.

**1. XML attributes — the default.** Put the value in the layout as `?attr/glia*` and stop. The
attribute is already resolved by the time the layout inflates: Glia activities compose their theme in
`onApplyThemeResource`, and views the SDK inflates into a host context wrap with `wrapWithGliaTheme()`.
Nothing needs to be passed in, and nothing needs re-applying in code.

```xml
<TextView
    android:textColor="?attr/gliaOperatorMessageTextColor"
    android:background="@drawable/bg_bubble"
    android:backgroundTint="?attr/gliaOperatorMessageBackgroundColor" />
```

**2. Resolve in code — only for one of four reasons.** If none of these applies, the value belongs in
the layout. Use the `Context.gliaAttr*` helpers in `helper/GliaThemeOverlays.kt`; never
`obtainStyledAttributes` by hand.

| Reason | Looks like | Example |
|---|---|---|
| The value depends on runtime state | picked at bind time from a model | `MediaUpgradeStartedViewHolder` audio-vs-video icon; `CallView.setButtonActivated` |
| It must be merged with the JSON theme | `unifiedTheme?.x ?: context.gliaAttrColor(...)` | `MessageView.setupAttachmentIconTheme`; `AlertDialogConfigurationFactory` builds a whole `ColorPallet` |
| The target has no XML attribute | Lottie colour filters, a `<merge>` root's background, a programmatically-constructed view | `OperatorStatusView` ripple; `SingleQuestionViewHolder`'s `RadioButton` |
| It is a behaviour flag, not styling | a `Boolean` that changes layout or visibility | `whiteLabel`, `gliaAlertDialogButtonUseVerticalAlignment` |

**The typeface is the standing exception.** It can never be a layout attribute — `android:fontFamily`
on a view beats its `textAppearance` unconditionally, so XML cannot say "use the theme's font, and
otherwise leave the `textAppearance`'s alone". Every screen with text calls
`context.applyGliaThemeFont(view1, view2, …)` (`helper/ViewExtensions.kt`) once during setup.

**3. JSON on top, always last.** Apply the screen's `UnifiedTheme` after everything above, via the
`UnifiedUiExtensions.kt` helpers, so remote configuration keeps winning. See the Conventions below.

**4. Snapshot it.** Add a golden under `src/testSnapshot`, and if the widget has themeable colour, a
variant against `Test.Glia.Customized` — that fixture gives the coarse and precise attributes
different colours, so it catches a layout that reads the wrong one.

## Conventions
- **Three-stage pipeline**: JSON string → `RemoteConfigurationParser` (Gson + custom deserializers) → `RemoteConfiguration.toUnifiedTheme()` → `UnifiedThemeManager.theme`. Entry point: `UnifiedThemeManager.applyJsonConfig()`.
- **Merge direction**: `defaultTheme merge unifiedTheme` — the remote overlay wins when non-null. Any field absent from JSON stays as `DefaultTheme` fallback; explicit `null` in remote also means "use default" under current semantics. Evidence: `Merge.kt`
- **`DefaultTheme` is a top-level function**, not a class. Lives in `theme/defaulttheme/` under `@file:Suppress("FunctionName")` with a capital name. Treat it as a builder, not a singleton. Evidence: `theme/defaulttheme/Base.kt`
- **Views read theme directly**: `Dependencies.gliaThemeManager.theme?.<screenTheme>` in View `onAttach`/`init`. Theme is a View-layer concern — never pass it through MVP Contracts or Controllers.
- **Custom Gson deserializers** handle type coercion: `ColorRemoteConfig` (`@JvmInline internal value class` wrapping `@ColorInt Int`), `SizeDpRemoteConfig`, `SizeSpRemoteConfig`. New primitive remote types always need a matching deserializer registered in `RemoteConfigurationParser`. Evidence: `parse/Deserializers.kt`, `config/base/ColorRemoteConfig.kt`, `config/base/Size.kt`
- `isWhiteLabel` propagates from JSON → `RemoteConfiguration` → `UnifiedTheme`; Views call `applyWhiteLabel(isWhiteLabel)` to hide Glia branding elements.

## Anti-Patterns
- **Adding a theme property to only one side of the pipeline** — adding to `RemoteConfiguration` without updating the `DefaultTheme` function (or vice versa) silently yields `null` everywhere. Both sides are required.
- **New screen theme not wired into `UnifiedTheme`** — a `FooTheme` data class that isn't added to `UnifiedTheme` and `RemoteConfiguration.toUnifiedTheme()` is unreachable at runtime.
- **Hardcoding colors or dimensions in Views** — all values must flow from `GlobalColorsConfig` / theme overlay. Hardcoded values break white-label and operator-configured theming silently.
- **Adding a new `glia*` XML theme attribute for a new themeable property** — the XML mechanism (`Theme.Glia.Internal` defaults + the `GliaTheme` override hook, see `helper/GliaThemeOverlays.kt` and [docs/theming-migration.md](../../../../../../../../../docs/theming-migration.md)) is frozen at its current attribute set. New themeable properties go exclusively through the JSON remote config path.
- **Skipping snapshot test updates** — every change in this package has visual impact. Run `./gradlew widgetssdk:recordSnapshots` and commit updated snapshots.

## For AI Agents
- When adding a View or widget, read "Theming a new View or widget" above first. Default to `?attr/glia*` in the layout; resolving a `glia*` value in code needs one of the four reasons listed there, and re-applying in code a value the layout already resolves is the single most common mistake in this codebase's history.
- Never add a new theme property to only `RemoteConfiguration` or only `DefaultTheme` — both files must be edited together or the property is null at runtime with no error.
- Never pass `UnifiedTheme` or any screen theme through a Controller or Contract interface — Views read theme directly from `Dependencies.gliaThemeManager`.
- Never add a new remote primitive type (color, size, alignment) without registering a corresponding custom deserializer in `RemoteConfigurationParser` — Gson will silently produce null or throw for unregistered inline value classes.
