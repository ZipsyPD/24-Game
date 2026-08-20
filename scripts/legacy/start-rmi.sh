#!/bin/bash
set -e

PROJECT="$HOME/dev/24-Game"

cd "$PROJECT/build"

echo "Starting rmiregistry from $(pwd)"
rmiregistry
