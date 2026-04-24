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
- **Extending `UiTheme.kt`** — that class is `@Parcelize` and deprecated. New themeable properties go exclusively through the JSON remote config path.
- **Skipping snapshot test updates** — every change in this package has visual impact. Run `./gradlew widgetssdk:recordPaparazziSnapshot` and commit updated snapshots.

## For AI Agents
- Never add a new theme property to only `RemoteConfiguration` or only `DefaultTheme` — both files must be edited together or the property is null at runtime with no error.
- Never pass `UnifiedTheme` or any screen theme through a Controller or Contract interface — Views read theme directly from `Dependencies.gliaThemeManager`.
- Never add a new remote primitive type (color, size, alignment) without registering a corresponding custom deserializer in `RemoteConfigurationParser` — Gson will silently produce null or throw for unregistered inline value classes.
