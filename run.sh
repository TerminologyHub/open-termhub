#!/bin/sh
java -Xmx2048M -Xms512M -XX:+UseStringDeduplication -Dspring.profiles.active=deploy -jar open-termhub-*.jar
