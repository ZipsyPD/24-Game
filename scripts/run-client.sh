#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT="$(cd "$SCRIPT_DIR/.." && pwd)"
GLASSFISH="$PROJECT/docker/glassfish/glassfish5/glassfish"
MYSQL_JAR="$PROJECT/lib/mysql-connector-j.jar"
POLICY="$PROJECT/policy.policy"

cd "$PROJECT"

java \
  -Djava.security.policy="$POLICY" \
  -cp "build:$GLASSFISH/lib/gf-client.jar:$MYSQL_JAR" \
  Client
