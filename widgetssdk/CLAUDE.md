<!-- deep-research: completed 2026-04-24 | v: deep-research/1.7 | scope: widgetssdk -->
# widgetssdk
> The published SDK module — produces `com.glia:android-widgets` AAR for Maven Central

See [root CLAUDE.md](../CLAUDE.md) for project-wide architecture, MVP pattern, DI rules, RxJava requirements, and language standards. See [docs/claude-reference.md](../docs/claude-reference.md) for full anti-patterns and gotchas.

## Context Loading Order
1. `build.gradle` — three build types, publishing config, Firebase POM stripping, Dokka wiring
2. `src/main/AndroidManifest.xml` — activity launch modes, InitializationProvider override, GliaFileProvider
3. `consumer-rules.pro` — the only ProGuard keep rules shipped to integrators; review before adding any
4. `../version.properties` (repo root) — single source of truth for `versionCode`, `versionName`, Core SDK version, telemetry version

## Where to Look
| Task | File |
|------|------|
| Bump SDK version | `../version.properties` + `./gradlew saveWidgetsVersion --type=patch\|minor\|major` |
| Publish to Maven Central | `build.gradle` → `mavenPublishing` block |
| Publish snapshot | `./gradlew publishSnapshotPublicationToMavenCentralRepository` |
| Add/change activity flags | `src/main/AndroidManifest.xml` |
| Add integrator-visible ProGuard keep | `consumer-rules.pro` |
| Add internal shrinking rule | `proguard-rules.pro` |
| Run/record Paparazzi snapshots | `./gradlew widgetssdk:recordPaparazziSnapshot` (snapshot build type only) |
| Regenerate Javadoc | `./gradlew dokkaGeneratePublicationJavadoc` (Dokka V2) |

## Conventions
- Version is owned entirely by `version.properties` at the repo root. Neither `gradle.properties` nor `defaultConfig.versionName` are authoritative — `build.gradle` reads `widgetsVersionName` injected via `scripts/version-updater.gradle`.
- Paparazzi plugin is applied **inside** the `snapshot` buildTypes block, not at the top level. Tests in `src/test/java` are excluded from the `snapshot` variant by design — `test.java.srcDirs = []` with re-addition only for `testDebug`/`testRelease`. Evidence: `build.gradle` sourceSets block
- Firebase Messaging is `implementation` but stripped from the published POM via `excludeOptionalDependencies()`. Integrators supply their own FCM service and call `PushNotifications.onNewMessage()`. Never promote `firebase-messaging` to `api`. Evidence: `build.gradle`
- Paparazzi PNG files are tracked via Git LFS, routed by `.gitattributes` (`widgetssdk/src/test/snapshots/**/*.png`). Never commit raw PNGs.
- Dokka task wired into Maven publishing is `dokkaGeneratePublicationJavadoc` (Dokka V2). The `dokkaHtml` task does not exist in this project.

## Anti-Patterns
- **Do not add ProGuard rules to `consumer-rules.pro` without explicit review.** Rules there ship to every integrator and affect their whole-app shrinking pass. Internal keep rules belong in `proguard-rules.pro`. Evidence: `consumer-rules.pro` currently has only `GliaFileProvider` and `WebViewViewHolder$JavaScriptInterface` entries.
- **Do not register a new Activity in the manifest without reviewing all `ActivityLifecycleCallbacks` watchers registered in `Dependencies.onAppCreate`.** Activities are an implicit UI coordination bus; a new singleTask activity can silently disrupt lifecycle sequencing across the engagement flow.
- **Do not add a new public API without tagging internal Java classes with `@hide` Javadoc.** Dokka publishes anything without `@hide`; unintended classes appear in the public Javadoc JAR shipped to Maven Central. Evidence: `InitializationProvider.kt` uses `@hide`.
- **Do not run tests against the `snapshot` build variant expecting results.** The `snapshot` variant excludes `src/test/java` — it returns zero tests. Always use `testDebug` or `testRelease`. Evidence: `build.gradle` sourceSets

## For AI Agents
- Never call `GliaWidgets.onAppCreate()` in new code — it is deprecated and redundant because `InitializationProvider.onCreate()` already runs `super.onCreate()` → `Dependencies.onAppCreate(application)` → `GliaWidgets.setupRxErrorHandler()`. Calling it again re-invokes the init path.
- Never remove the `tools:node="remove"` entry for `com.glia.androidsdk.InitializationProvider` in `AndroidManifest.xml`. It suppresses the Core SDK's own provider so the Widgets provider can extend and replace it. Removing it causes both providers to register under the same authority and crashes at install time.
- Never rename or remove `dokkaGeneratePublicationJavadoc`. The `dokkaJavadocJar` task depends on it by name and it is wired into Maven publishing in the `afterEvaluate` block. Using the wrong task name silently omits the Javadoc JAR from the published artifact.
