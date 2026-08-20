#!/bin/sh
DIR="$(cd "$(dirname "$0")" && pwd)"
exec /home/priyanshuchawda/develop/gradle/bin/gradle -p "$DIR" "$@"
