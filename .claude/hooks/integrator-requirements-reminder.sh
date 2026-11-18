#!/usr/bin/env bash
# PostToolUse hook (Edit|Write|MultiEdit): after a dependency version file changes, remind Claude
# to run the integrator requirements check. Prints nothing for any other file.
# Reads the hook input JSON on stdin; matches the file_path with grep so it needs no jq.
input="$(cat)"
file="$(printf '%s' "$input" | grep -oE '"file_path"[[:space:]]*:[[:space:]]*"[^"]*"' | head -1 | sed -E 's/.*"([^"]*)"$/\1/')"

case "$file" in
  */gradle/libs.versions.toml | gradle/libs.versions.toml | */version.properties | version.properties) ;;
  *) exit 0 ;;
esac
# Only the root version.properties holds dependency versions
case "$file" in
  */version.properties | version.properties)
    [ "$(dirname "$file")" = "${CLAUDE_PROJECT_DIR:-$(pwd)}" ] || [ "$file" = version.properties ] || exit 0 ;;
esac

cat <<EOF
{"hookSpecificOutput":{"hookEventName":"PostToolUse","additionalContext":"Dependency versions changed in $(basename "$file"). Before finishing: run ./gradlew :widgetssdk:checkIntegratorRequirements and report its result; offer integrator-compat/run.sh (template builds, slow) and say in the final summary whether it ran. See the integrator-requirements skill."}}
EOF
