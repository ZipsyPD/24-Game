#!/bin/bash
set -e

PROJECT="$HOME/dev/24-Game"
GLASSFISH="$HOME/dev/tools/glassfish-5.0.1/glassfish5/glassfish"
MYSQL_JAR="$PROJECT/lib/mysql-connector-j.jar"
POLICY="$PROJECT/policy.policy"

cd "$PROJECT"

java \
  -Djava.security.policy="$POLICY" \
  -cp "build:$GLASSFISH/lib/gf-client.jar:$MYSQL_JAR" \
  Client
