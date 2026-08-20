#!/bin/bash
set -e

PROJECT="$HOME/dev/24-Game"
GLASSFISH="$HOME/dev/tools/glassfish-5.0.1/glassfish5/glassfish"

cd "$PROJECT"

mkdir -p build

javac -d build \
  -cp "$GLASSFISH/lib/gf-client.jar" \
  src/main/java/*.java

echo "Compiled successfully into build/"
