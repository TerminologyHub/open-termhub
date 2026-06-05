#!/bin/sh
echo "Starting application..."

# Set debug logging if DEBUG environment variable is set to true
if [ "${DEBUG:-false}" = "true" ]; then
    echo "  Debug logging enabled"
    export LOG_LEVEL=DEBUG
else
    echo "  Debug logging disabled"
    export LOG_LEVEL=INFO
fi


# echo syndication startup setting
echo "  SYNDICATION_CHECK_ON_STARTUP=${SYNDICATION_CHECK_ON_STARTUP:-true}"

if [ -z "${PROJECT_API_KEY+x}" ]; then
    echo "  PROJECT_API_KEY is NOT set"
else
    echo "  PROJECT_API_KEY is set"
fi


# Verify index exists and is writeable
indexDir="${INDEX_DIR:-/index}"
if [ -d $indexDir ]; then
    echo "  Index directory $indexDir exists"
else
    echo "  Index directory $indexDir does not exist - make it"
    mkdir $indexDir
fi
if [ -w "$indexDir"  ]; then
    echo "  Index directory $indexDir is writeable"
else
    echo "  Index directory $indexDir is not writeable"
    exit 1
fi

if [[ `ls build/libs/open-termhub-*.war 2> /dev/null | wc -l` -eq 1 ]]; then
    # First form exists
    war_file=$(ls build/libs/open-termhub-*.war)
    echo "  Found war file = $war_file"
elif [[ `ls open-termhub-*.war 2> /dev/null | wc -l` -eq 1 ]]; then
    # Second form exists
    war_file=$(ls open-termhub-*.war)
    log_file=log4j2-deploy.xml
    echo "  Found war file = $war_file"
else
    # Neither exists
    echo "'build/libs/open-termhub-*.war' nor 'open-termhub-*.war' could be found."
    exit 1
fi

echo "java ${JAVA_OPTS:=} -Xms512M -XX:+UseZGC -XX:+UseStringDeduplication -Dspring.profiles.active=deploy -jar $war_file"
java ${JAVA_OPTS:=} -Xms512M -XX:+UseZGC -XX:+UseStringDeduplication -Dspring.profiles.active=deploy -jar $war_file

