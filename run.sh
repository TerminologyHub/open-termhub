#!/bin/sh
echo "Starting application..."

# Set debug logging if DEBUG environment variable is set to true
if [ "${DEBUG:-false}" = "true" ]; then
    echo "Debug logging enabled"
    export LOG_LEVEL=DEBUG
else
    echo "Debug logging disabled"
    export LEVEL=INFO
fi

echo "java ${JAVA_OPTS:=} -Xms512M -XX:+UseZGC -XX:+UseStringDeduplication -Dspring.profiles.active=deploy -jar open-termhub-*.jar"
java ${JAVA_OPTS:=} -Xms512M -XX:+UseZGC -XX:+UseStringDeduplication -Dspring.profiles.active=deploy -jar open-termhub-*.jar
