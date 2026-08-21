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

ADDED_GLASSFISH=0
ADDED_OPENMQ=0

cleanup() {
    if [ "$ADDED_GLASSFISH" -eq 1 ]; then
        sudo sed -i.bak '/127\.0\.0\.1 glassfish/d' /etc/hosts
    fi

    if [ "$ADDED_OPENMQ" -eq 1 ]; then
        sudo sed -i.bak '/127\.0\.0\.1 openmq/d' /etc/hosts
    fi

    rm -f /etc/hosts.bak 2>/dev/null || true
}

trap cleanup EXIT INT TERM

if ! grep -qE '^[[:space:]]*127\.0\.0\.1[[:space:]]+glassfish([[:space:]]|$)' /etc/hosts; then
    echo "Adding temporary glassfish host mapping..."
    echo "127.0.0.1 glassfish" | sudo tee -a /etc/hosts > /dev/null
    ADDED_GLASSFISH=1
fi

if ! grep -qE '^[[:space:]]*127\.0\.0\.1[[:space:]]+openmq([[:space:]]|$)' /etc/hosts; then
    echo "Adding temporary openmq host mapping..."
    echo "127.0.0.1 openmq" | sudo tee -a /etc/hosts > /dev/null
    ADDED_OPENMQ=1
fi

cd "$PROJECT"

java \
  -Djava.security.policy="$POLICY" \
  -cp "build:$GLASSFISH/lib/gf-client.jar:$MYSQL_JAR" \
  Client
