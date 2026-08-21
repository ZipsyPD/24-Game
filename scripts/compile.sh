#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT="$(cd "$SCRIPT_DIR/.." && pwd)"
GLASSFISH="$PROJECT/docker/glassfish/glassfish5/glassfish"

cd "$PROJECT"

mkdir -p build

javac -d build \
  -cp "$GLASSFISH/lib/gf-client.jar" \
  src/main/java/*.java

echo "Compiled successfully into build/"
