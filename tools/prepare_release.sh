#!/bin/bash

set -euo pipefail

VERSION=${1-}
REQUIRED_TOOLS="sed git mvn"

if test -z "$VERSION"; then
  echo "Missing version parameter. Usage: $0 VERSION"
  exit 1
fi

for tool in $REQUIRED_TOOLS; do
  if ! hash "$tool" 2>/dev/null; then
    echo "This script requires '$tool', but it is not installed."
    exit 1
  fi
done

if git rev-parse "$VERSION" >/dev/null 2>&1; then
  echo "Cannot prepare release, a release for $VERSION already exists"
  exit 1
fi

mvn versions:set -DnewVersion=$VERSION versions:commit
sed -i '' "s/^    <tag>.*/    <tag>$VERSION<\/tag>/" pom.xml
sed -i '' "s/^  <version>.*/  <version>$VERSION<\/version>/" README.md

git commit -a -m "Release $VERSION version"
git tag -a "$VERSION" -m "$VERSION"

mvn versions:set -DnextSnapshot=true versions:commit

git commit -a -m "Update version to next snapshot version"
