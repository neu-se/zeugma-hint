#!/bin/bash
readonly LIB=$(pwd)/lib/
readonly VERSION="1.0.0-SNAPSHOT"

for d in "$LIB"/*/; do
  name=$(basename "$d")
  jar="$d/$VERSION/$name-$VERSION.jar"
  sources="$d/$VERSION/$name-$VERSION-sources.jar"
  javadoc="$d/$VERSION/$name-$VERSION-javadoc.jar"
  pom="$d/$VERSION/$name-$VERSION.pom"
  if [ -f "$jar" ]; then
    mvn install:install-file \
      -Dpackaging=jar \
      -Dfile="$jar" \
      -Djavadoc="$javadoc" \
      -Dsources="$sources" \
      -DpomFile="$pom"
  else
    mvn install:install-file \
      -Dpackaging=pom \
      -Dfile="$pom" \
      -DpomFile="$pom"
  fi
done
