#!/bin/bash

# Bump version number in every build.gradle file in the repository.

set -euf -o pipefail

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

main() {
  find "$DIR/.." -name "build.gradle" -print0 |
    xargs -0 perl -pi -e "s/$1/$2/g"
}

main "$@"
