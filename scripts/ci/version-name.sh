#!/usr/bin/env bash
# Prints widgets.versionName from version.properties. Read straight from the
# file so Gradle log output can't leak into the value.
set -euo pipefail

cd "$(dirname "$0")/../.."
grep '^widgets.versionName=' version.properties | cut -d= -f2
