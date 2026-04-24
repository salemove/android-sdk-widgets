<!-- deep-research: completed 2026-04-24 | v: deep-research/1.7 | scope: widgetssdk/src/testSnapshot -->
# testSnapshot
> Paparazzi snapshot tests — isolated `snapshot` build type, PNG outputs tracked by Git LFS

Parent: [CLAUDE.md](../../../CLAUDE.md) | [docs/claude-reference.md](../../../docs/claude-reference.md)

## Context Loading Order
1. `java/com/glia/widgets/SnapshotTest.kt` — base class; all defaults live here (device, theme, pixel diff, `_paparazzi` rule)
2. `../../build.gradle` — `snapshot` buildTypes block where Paparazzi plugin is applied; also shows `test.java.srcDirs = []` exclusion
3. `../../../.gitattributes` (repo root) — LFS filter for `widgetssdk/src/test/snapshots/**/*.png`

## Where to Look
| Task | File/Location |
|------|---------------|
| Add a new snapshot test | New class extending `SnapshotTest`; implement relevant `Snapshot*` interface |
| Change default device/theme/diff | `SnapshotTest.kt` constructor defaults |
| Paparazzi plugin config | `widgetssdk/build.gradle` — `snapshot { apply plugin: 'app.cash.paparazzi' }` |
| Theme helpers | `java/com/glia/widgets/snapshotutils/SnapshotTheme.kt`, `SnapshotThemeConfiguration.kt` |
| Locale-flake fix | `_paparazzi` rule constructor parameters in `SnapshotTest.kt` |
| PNG outputs | `widgetssdk/src/test/snapshots/` (Git LFS) |

## Conventions
- Extend `SnapshotTest` and implement one or more `Snapshot*` interfaces (`SnapshotTheme`, `SnapshotContent`, `SnapshotStrings`, `SnapshotProviders`, etc.) — never duplicate what the base class provides.
- The Paparazzi JUnit rule is named `_paparazzi` (leading underscore) — this is intentional; do not rename it.
- Default device `DeviceConfig.PIXEL_4A`, theme `"ThemeOverlay_Glia_Chat_Material"`, max pixel diff `0.001` — override only via the `SnapshotTest` constructor, never by patching globals.
- Full-width views use the `SnapshotTest.fullWidthRenderMode` companion val (itself a Mockito mock of `RenderingMode`) — do not roll your own per-test `RenderingMode` substitute.
- After rebasing onto UI changes, always re-run `./gradlew widgetssdk:recordPaparazziSnapshot` — stale PNGs cause false failures in CI.

## Anti-Patterns
- **Do NOT add Paparazzi to `debug` or `release` buildTypes** — the plugin injects Kotlin classpaths; moving it out of `snapshot {}` breaks those build types.
- **Do NOT use `dependency.coreSdk.useDirect=true` (includeBuild) for snapshot runs** — composite substitution does not resolve for custom build types. Run `./gradlew widgetssdk:publishCoreSdkToLocalMaven` first, then execute snapshot tasks.
- **Do NOT commit PNG files without confirming LFS pointers** — without `git lfs install` the files land as binary blobs; verify with `git lfs status` before pushing.
- **Do NOT place unit-test scaffolding here** — `test.java.srcDirs = []` excludes the default test source set from the `snapshot` build type entirely; unit-test helpers will not compile.

## For AI Agents
- Never add `apply plugin: 'app.cash.paparazzi'` anywhere except inside the `snapshot {}` buildTypes block in `widgetssdk/build.gradle`.
- Never write a snapshot test class that does not extend `SnapshotTest` — the `_paparazzi` rule and lifecycle wiring are only present in that base class.
- Never rename the `_paparazzi` rule — the leading underscore is part of the base-class contract.
- When a locale-sensitive screen causes flaky diffs, adjust `_paparazzi` constructor parameters (locale, device config) — do not bump `maxPercentDifference` as a workaround.
