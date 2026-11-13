---
name: integrator-requirements
description: Check that the Widgets SDK still builds in an integrator's app at the declared minimum toolchain (Gradle, AGP, Kotlin, compileSdk, JDK). Use after changing any dependency version (gradle/libs.versions.toml, the Core or Telemetry version in version.properties, AGP, Kotlin, compileSdk), before a release, or when asked about integrator minimum requirements. Run the metadata scan, offer the template builds; it runs locally, not on CI.
user-invocable: true
trigger:
  - integrator requirements
  - minimum requirements
  - min requirements
  - integrator compat
  - dependency update
  - bump dependency
  - upgrade dependency
---

# Integrator requirements check

A dependency bump can raise what integrators need without touching SDK code: a newer
transitive library may ship Kotlin metadata their compiler cannot read, or AAR metadata that
demands a higher compileSdk or AGP. The SDK's own build does not notice, because it already
runs a newer toolchain. This check builds against the integrator's side.

Declared minimums live in `integrator-compat/minimums.properties`. They are public: the
integrator docs list them.

## When to run it

After any change to `gradle/libs.versions.toml`, to `dependency.coreSdk.version` or
`dependency.telemetryLib.version` in `version.properties`, or to the SDK's AGP, Kotlin or
compileSdk:

1. Run the metadata scan yourself and report its table. It is one Gradle task.
2. Offer the template builds (run.sh). They download Gradle distributions and take minutes,
   so run them when the user agrees, or before a release.
3. In the final summary, say whether each part ran. "Template builds: not run" is a valid
   answer; leaving it out is not.

A project hook (`.claude/hooks/integrator-requirements-reminder.sh`) reminds you after an Edit
or Write to those two files. It does not see versions changed through shell commands, so the
rule above still applies then.

## How to run

1. Metadata scan (fast, a few seconds after configuration):
   ```bash
   ./gradlew :widgetssdk:checkIntegratorRequirements
   ```
   Scans the release AAR and every release runtime dependency for AAR `minCompileSdk` /
   `minAndroidGradlePluginVersion`, Kotlin module metadata, and class-file version. Fails
   with the offending artifacts; report in `widgetssdk/build/reports/integrator-requirements.txt`.

2. Template app builds, one per profile in `integrator-compat/profiles/`:
   ```bash
   INTEGRATOR_COMPAT_JDK_21="<JDK 21 home>" integrator-compat/run.sh [profile ...]
   ```
   Publishes `com.glia:android-widgets:<version>-SNAPSHOT` to mavenLocal, builds
   `integrator-compat/template` with each profile's toolchain, and writes
   `build/integrator-compat/summary.md`. Without profile names it runs all of them.
   - The capacitor profile needs JDK 21. On macOS without one installed, Android Studio's
     bundled JBR works: `/Applications/Android Studio.app/Contents/jbr/Contents/Home`.
   - With direct Core enabled (`dependency.coreSdk.useDirect=true` in `local.properties`),
     run.sh refuses to publish. Publish yourself, then skip it:
     ```bash
     ./gradlew :widgetssdk:publishMavenPublicationToMavenLocal -Psnapshot=yes
     integrator-compat/run.sh --skip-publish
     ```

Run both. The scan cannot see what an integrator's own dependencies pull in: the lifecycle
2.11 trap (below) only fails the `minimums-compose` template build.

## Checking an unreleased Core SDK

1. In the Core repo, publish the release (not a snapshot) to mavenLocal:
   `./gradlew :androidSdk:publishMavenPublicationToMavenLocal`.
2. Point `dependency.coreSdk.version` in `version.properties` at that version, locally.
3. Run the scan and run.sh as above. The template accepts `com.glia:android-sdk` from
   mavenLocal only for versions Maven Central does not have.
4. Afterwards revert `version.properties` and delete the version from
   `~/.m2/repository/com/glia/android-sdk/`, so it is not picked up by mistake.

## When a check fails

Either keep the dependency at a version that fits, with a WHY comment on its pin in
`gradle/libs.versions.toml`, or raise the minimum. Raising a value in
`integrator-compat/minimums.properties` is a public requirement change: say so to the user,
and it needs the integrator docs (`glia-docs-at-fern`,
`fern/developer-portal/mobile/widgets-sdk/android-widgets-sdk/android-installation.mdx`)
and the release notes updated.

A profile that is expected to fail carries `knownFailing=<reason>`; run.sh reports when it
starts passing so the marker can be removed.

## Known traps

- **Kotlin metadata:** a Kotlin compiler reads metadata at most one minor version newer than
  itself. A library built with Kotlin 2.3 (metadata 2.3) needs integrators on Kotlin 2.2+.
  Coil 3.4.0 did this via `kotlin-stdlib` 2.3.10.
- **Lifecycle alignment:** lifecycle artifacts align to one version. When the SDK or Core pins
  `lifecycle-*` 2.11.0, a Compose integrator's `lifecycle-runtime-compose` and
  `lifecycle-viewmodel-compose` move to 2.11.0 too, and those need compileSdk 37 and AGP
  9.1.0. `lifecycle-process` 2.11.0 itself needs only compileSdk 34.
- **AGP below 8.12.0** fails on duplicate `META-INF/**/MANIFEST.MF` files unless the app
  excludes them; the `agp-floor` profile proves AGP 8.9.1 works with that exclude.
