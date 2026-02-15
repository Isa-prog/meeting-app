#!/bin/sh
# Maven Wrapper startup script

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")"; pwd)}"
MAVEN_OPTS="${MAVEN_OPTS:--Xmx512m}"

if [ -z "$JAVA_HOME" ]; then
  JAVACMD=java
else
  JAVACMD="$JAVA_HOME/bin/java"
fi

if ! command -v mvn &> /dev/null; then
    echo "Maven not found. Please install Maven or use Maven wrapper."
    echo "Download Maven from: https://maven.apache.org/download.cgi"
    exit 1
fi

exec mvn "$@"
