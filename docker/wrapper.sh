#!/bin/sh

# Fail on unset variables and command errors
set -ue -o pipefail

# Prevent commands misbehaving due to locale differences
export LC_ALL=C

# Confirm java version
java -version

exec java -XX:InitialRAMPercentage=50.0 -XX:MaxRAMPercentage=75.0 ${JAVA_OPTS} -jar /app.jar "$@"