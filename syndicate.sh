#!/bin/sh
echo "Starting Syndication Data Loader..."

# Check if PROJECT_API_KEY is set
if [ -z "${PROJECT_API_KEY}" ]; then
    echo "ERROR: PROJECT_API_KEY environment variable is not set"
    echo "Syndication requires a valid API key to authenticate with the syndication service"
    echo "Please set PROJECT_API_KEY and try again"
    echo "Example: docker run -e PROJECT_API_KEY=your-key-here ..."
    exit 2
fi

# Set debug logging if DEBUG environment variable is set to true
if [ "${DEBUG:-false}" = "true" ]; then
    echo "Debug logging enabled"
    export LOG_LEVEL=DEBUG
else
    echo "Debug logging disabled"
    export LOG_LEVEL=INFO
fi

echo "java ${JAVA_OPTS:=} -Xms512M -XX:+UseZGC -XX:+UseStringDeduplication -Dspring.profiles.active=deploy -cp open-termhub-*.jar com.wci.termhub.syndication.SyndicationDataLoader 2>&1"
java ${JAVA_OPTS:=} -Xms512M -XX:+UseZGC -XX:+UseStringDeduplication -Dspring.profiles.active=deploy -cp open-termhub-*.jar com.wci.termhub.syndication.SyndicationDataLoader 2>&1

# Capture the exit code from the Java process
EXIT_CODE=$?

echo "Syndication Data Loader exited with code: $EXIT_CODE"
exit $EXIT_CODE
