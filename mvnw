#!/usr/bin/env sh
# Minimal Maven bootstrap: uses installed Maven, or downloads a pinned Maven distribution.
set -eu
MVN_VERSION="3.9.11"
ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
if command -v mvn >/dev/null 2>&1; then exec mvn -f "$ROOT_DIR/pom.xml" "$@"; fi
MAVEN_HOME="$ROOT_DIR/.mvn/wrapper/apache-maven-$MVN_VERSION"
if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  mkdir -p "$ROOT_DIR/.mvn/wrapper"
  ZIP="$ROOT_DIR/.mvn/wrapper/apache-maven-$MVN_VERSION-bin.zip"
  URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MVN_VERSION/apache-maven-$MVN_VERSION-bin.zip"
  if command -v curl >/dev/null 2>&1; then curl -fL "$URL" -o "$ZIP"; elif command -v wget >/dev/null 2>&1; then wget "$URL" -O "$ZIP"; else echo 'Instala Maven 3.9.11 o curl/wget para descargarlo.' >&2; exit 1; fi
  unzip -q "$ZIP" -d "$ROOT_DIR/.mvn/wrapper"
  rm -f "$ZIP"
fi
exec "$MAVEN_HOME/bin/mvn" -f "$ROOT_DIR/pom.xml" "$@"
