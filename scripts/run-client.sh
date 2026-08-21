#!/bin/bash
set -e

JAVA_VERSION=$(java -version 2>&1 | head -n 1)

if [[ "$JAVA_VERSION" != *"1.8."* ]]; then
    echo "Error: Java 8 is required to run the client."
    echo
    echo "Current Java version:"
    java -version
    echo
    echo "Please switch to Java 8 and try again."
    exit 1
fi

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
