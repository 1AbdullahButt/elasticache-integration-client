#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
    echo "usage: $0 <repository-url> <version>" >&2
    exit 2
fi

repository_url=$1
version=$2
script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
java_dir=$(cd "$script_dir/.." && pwd)

"$java_dir/gradlew" \
    --no-daemon \
    -p "$java_dir/smoke/gradle" \
    -PcacheRepository="$repository_url" \
    -PcacheVersion="$version" \
    clean smoke

mvn \
    --batch-mode \
    --no-transfer-progress \
    -f "$java_dir/smoke/maven/pom.xml" \
    -Dcache.repository="$repository_url" \
    -Dcache.version="$version" \
    clean verify
