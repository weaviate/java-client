#!/usr/bin/env bash
set -euo pipefail

# Options:
# --batch to prevent interactive command
# --yes to assume "yes" for questions
gpg --quiet --batch --yes --decrypt --passphrase="$GPG_PASSPHRASE" --output secrets.tar secrets.tar.gpg
tar xvf secrets.tar
gpg --batch --import key.gpg
gpg --list-secret-keys
echo "Success"
