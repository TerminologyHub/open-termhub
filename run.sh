#!/bin/sh
echo "Starting application..."
echo "java ${JAVA_OPTS:=} -Xms512M -XX:+UseZGC -XX:+UseStringDeduplication -Dspring.profiles.active=deploy -jar open-termhub-*.jar"
java ${JAVA_OPTS:=} -Xms512M -XX:+UseZGC -XX:+UseStringDeduplication -Dspring.profiles.active=deploy -jar open-termhub-*.jar
