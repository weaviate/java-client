#!/usr/bin/env bash
set -euo pipefail

# This script generates a GPG-ecrypted tarball with our signing GPG key
# and Maven Central Repository credentials.
#
# Make sure that key.gpg, settings.xml, and passphrase.env exist in the current directory.
# Delete these files after the script has run. Only commit secrets.tar.gpg!

rm -f secrets.tar &&
  tar --no-xattrs -czf secrets.tar key.gpg settings.xml passphrase.env

rm -f secrets.tar.gpg &&
  gpg --batch --symmetric \
  --passphrase "$GPG_PASSPHRASE" \
  --output secrets.tar.gpg \
  secrets.tar

rm -f secrets.tar

echo "Tarball secrets.tar.gpg generated successfully."
echo "Remember to delete the plaintext files. Only commit secrets.tar.gpg to source control!"
echo
echo "  \$ git add secrets.tar.gpg && git commit -m 'ci: update secrets.tar.gpg'"
echo "  \$ rm key.gpg settings.xml passphrase.env"
echo

