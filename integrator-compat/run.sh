#!/usr/bin/env bash
# Builds the integrator test app in integrator-compat/template once per profile, against the
# Widgets SDK snapshot published to mavenLocal, and writes build/integrator-compat/summary.md.
#
# Usage: integrator-compat/run.sh [--skip-publish] [profile ...]
#   Without profiles, runs every integrator-compat/profiles/*.properties.
#
# Profile keys: gradle, agp, kotlin (omit for a Java-only app), compileSdk, minSdk, jdk, javaTarget,
# compose, composeBom, activityCompose (Compose app with those versions),
# gradleProperties (';'-separated lines for the app's gradle.properties), knownFailing (reason the
# profile is expected to fail), description, source. base=minimums starts from minimums.properties.
#
# The JDK for a profile comes from $INTEGRATOR_COMPAT_JDK_<jdk>, or /usr/libexec/java_home on macOS.
# Exits 1 when a profile without knownFailing fails, or when a profile could not be checked at all.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPAT="$ROOT/integrator-compat"
OUT="$ROOT/build/integrator-compat"

publish=true
profiles=()
for arg in "$@"; do
  case "$arg" in
    --skip-publish) publish=false ;;
    *) profiles+=("$arg") ;;
  esac
done
if [ ${#profiles[@]} -eq 0 ]; then
  for file in "$COMPAT"/profiles/*.properties; do
    profiles+=("$(basename "$file" .properties)")
  done
fi

widgets_version="$(grep '^widgets.versionName=' "$ROOT/version.properties" | cut -d= -f2)-SNAPSHOT"

# Last value wins, so a profile overrides the minimums it is based on
prop() {
  grep -E "^$2=" "$1" | tail -1 | cut -d= -f2- || true
}

sdk_dir() {
  local dir="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
  if [ -z "$dir" ] && [ -f "$ROOT/local.properties" ]; then
    dir="$(prop "$ROOT/local.properties" sdk.dir)"
  fi
  echo "$dir"
}

jdk_home() {
  local var="INTEGRATOR_COMPAT_JDK_$1"
  if [ -n "${!var:-}" ]; then
    echo "${!var}"
  elif [ -x /usr/libexec/java_home ]; then
    /usr/libexec/java_home -F -v "$1" 2>/dev/null || true
  fi
}

java_major() {
  "$1/bin/java" -version 2>&1 | head -1 | sed -E 's/.*version "(1\.)?([0-9]+).*/\2/'
}

resolved() {
  grep -oE "$1:[^ ]+( -> [^ ]+)?" "$2" | head -1 | sed -E 's/.* -> //; s/.*://' || true
}

# The most specific cause first; a generic compiler error is only a fallback
evidence() {
  local pattern line
  for pattern in "Minimum supported Gradle version" "Dependency '[^']+' requires" \
    "metadata is [0-9.]+, expected version" "2 files found with path" "^e: "; do
    # First match, joined with the indented lines it wraps onto
    line="$(awk -v p="$pattern" '
      found { if ($0 ~ /^[[:space:]]+[^[:space:]]/ && prev !~ /\.$/) { prev = $0; sub(/^[[:space:]]+/, ""); out = out " " $0; next } exit }
      $0 ~ p { out = $0; prev = $0; found = 1 }
      END { if (found) print out }' "$1")"
    if [ -n "$line" ]; then
      echo "$line" | sed -E 's#^e: file://[^!]*/([^/!]+)!/[^ ]+ #\1: #; s/^[[:space:]>]*([0-9]+\.[[:space:]]+)?//' | cut -c1-200
      return
    fi
  done
  grep -A1 -m1 "What went wrong" "$1" | tail -1 | sed -E 's/^[[:space:]>]*//' | cut -c1-200 || true
}

mkdir -p "$OUT"
if [ "$publish" = true ]; then
  echo "==> Publishing com.glia:android-widgets:$widgets_version to mavenLocal"
  (cd "$ROOT" && ./gradlew :widgetssdk:publishSnapshotPublicationToMavenLocal --console=plain) > "$OUT/publish.log" 2>&1 ||
    { echo "Publishing failed, see $OUT/publish.log"; exit 1; }
fi

rows=()
notes=()
failed=false

for name in "${profiles[@]}"; do
  file="$COMPAT/profiles/$name.properties"
  [ -f "$file" ] || { echo "Unknown profile: $name"; exit 1; }
  echo "==> $name"

  merged="$OUT/$name.properties"
  : > "$merged"
  [ "$(prop "$file" base)" = minimums ] && cat "$COMPAT/minimums.properties" >> "$merged"
  cat "$file" >> "$merged"

  gradle="$(prop "$merged" gradle)"
  agp="$(prop "$merged" agp)"
  kotlin="$(prop "$merged" kotlin)"
  compile_sdk="$(prop "$merged" compileSdk)"
  jdk="$(prop "$merged" jdk)"
  compose="$(prop "$merged" compose)"
  compose_bom="$(prop "$merged" composeBom)"
  known_failing="$(prop "$merged" knownFailing)"
  toolchain="Gradle $gradle, AGP $agp, $([ -n "$kotlin" ] && echo "Kotlin $kotlin" || echo "no Kotlin"), compileSdk $compile_sdk, JDK $jdk"
  [ "$compose" = true ] && toolchain="$toolchain, Compose BOM $compose_bom"

  dir="$OUT/$name"
  log="$OUT/$name.log"
  rm -rf "$dir" "$log"
  cp -R "$COMPAT/template" "$dir"
  mkdir -p "$dir/gradle/wrapper"
  cp "$ROOT/gradlew" "$dir/"
  cp "$ROOT/gradle/wrapper/gradle-wrapper.jar" "$dir/gradle/wrapper/"

  {
    echo "org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8"
    echo "android.useAndroidX=true"
    echo "compat.widgetsVersion=$widgets_version"
    echo "compat.agp=$agp"
    [ -n "$kotlin" ] && echo "compat.kotlin=$kotlin"
    echo "compat.compileSdk=$compile_sdk"
    echo "compat.minSdk=$(prop "$merged" minSdk)"
    echo "compat.javaTarget=$(prop "$merged" javaTarget)"
    if [ "$compose" = true ]; then
      echo "compat.compose=true"
      echo "compat.composeBom=$compose_bom"
      echo "compat.activityCompose=$(prop "$merged" activityCompose)"
    fi
    prop "$merged" gradleProperties | tr ';' '\n'
  } | grep . > "$dir/gradle.properties"

  sdk="$(sdk_dir)"
  [ -n "$sdk" ] && echo "sdk.dir=$sdk" > "$dir/local.properties"

  result="" detail=""
  java_home="$(jdk_home "$jdk")"
  # Keep the wrapper's checksum verification, for this profile's distribution
  sha="$(curl -fsSL "https://services.gradle.org/distributions/gradle-$gradle-bin.zip.sha256" || true)"
  if [ -z "$java_home" ] || [ "$(java_major "$java_home")" != "$jdk" ]; then
    result=INVALID detail="no JDK $jdk: set INTEGRATOR_COMPAT_JDK_$jdk"
  elif [ -z "$sha" ]; then
    result=INVALID detail="no checksum for Gradle $gradle"
  fi

  start=$(date +%s)
  if [ -z "$result" ]; then
    cat > "$dir/gradle/wrapper/gradle-wrapper.properties" <<EOF
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-$gradle-bin.zip
distributionSha256Sum=$sha
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF
    # One build without a daemon: the dependency tree proves which SDK was used, and no daemon of
    # this profile's Gradle version is left behind
    status=0
    (cd "$dir" && JAVA_HOME="$java_home" ./gradlew --no-daemon --continue --console=plain --no-build-cache --stacktrace \
      :app:dependencies --configuration debugRuntimeClasspath :app:assembleDebug) > "$log" 2>&1 || status=$?

    if grep -qE "Verification of Gradle distribution failed|Could not find com.glia:android-widgets" "$log"; then
      result=INVALID detail="$(grep -m1 -E "Verification of Gradle distribution failed|Could not find com.glia:android-widgets" "$log" | sed -E 's/^[[:space:]>]*//')"
    elif [ $status -eq 0 ] && ! grep -qE "com.glia:android-widgets:$widgets_version( |$)" "$log"; then
      result=INVALID detail="build passed without resolving com.glia:android-widgets:$widgets_version"
    elif [ $status -eq 0 ]; then
      result=PASS
      detail="android-widgets $(resolved com.glia:android-widgets "$log"), kotlin-stdlib $(resolved org.jetbrains.kotlin:kotlin-stdlib "$log"), lifecycle-process $(resolved androidx.lifecycle:lifecycle-process "$log")"
    else
      result=FAIL detail="$(evidence "$log")"
    fi
  fi
  duration=$(( $(date +%s) - start ))

  shown="$result"
  if [ -n "$known_failing" ] && [ "$result" = FAIL ]; then
    shown="KNOWN FAILING"
    notes+=("- **$name** is known failing: $known_failing")
  elif [ -n "$known_failing" ] && [ "$result" = PASS ]; then
    shown="PASS (remove knownFailing)"
    notes+=("- **$name** passes but is marked knownFailing — remove it from profiles/$name.properties")
  elif [ "$result" != PASS ]; then
    failed=true
  fi
  echo "    $shown: $detail"
  rows+=("| $name | $toolchain | $shown | ${detail//|/\\|} | $((duration / 60))m $((duration % 60))s |")
done

{
  echo "# Integrator compatibility of com.glia:android-widgets:$widgets_version"
  echo
  echo "Each profile builds integrator-compat/template with an integrator's toolchain. Logs and project copies: \`build/integrator-compat/\`."
  echo
  echo "| Profile | Toolchain | Result | Evidence | Duration |"
  echo "|---|---|---|---|---|"
  printf '%s\n' "${rows[@]}"
  if [ ${#notes[@]} -gt 0 ]; then
    echo
    printf '%s\n' "${notes[@]}"
  fi
} > "$OUT/summary.md"

echo
cat "$OUT/summary.md"
[ "$failed" = false ]
