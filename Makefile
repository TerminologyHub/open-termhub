# global service name
SERVICE                 := open-termhub

INTERACTIVE := $(shell [ -t 0 ] && echo 1)
#######################################################################
#                 OVERRIDE THIS TO MATCH YOUR PROJECT                 #
#######################################################################

# Most applications have their own method of maintaining a version number.
APP_VERSION              := $(shell echo `grep "version = " build.gradle | cut -d\= -f 2`)
GIT_VERSION              ?= $(shell echo `git describe --match=NeVeRmAtCh --always --dirty`)
GIT_COMMIT               ?= $(shell echo `git log | grep -m1 -oE '[^ ]+$'`)
GIT_COMMITTED_AT         ?= $(shell echo `git log -1 --format=%ct`)
GIT_BRANCH               ?= $(shell echo `git branch --show-current`)

.PHONY: build

# Clean build artifacts. Override for your project
clean:
	./gradlew clean

# Build the library without tests
build:
	./gradlew build -x test -x javadoc

scan:
	/bin/rm -rf gradle/dependency-locks
	./gradlew dependencies --write-locks
	cp gradle/dependency-locks/compileClasspath.lockfile gradle/dependency-locks/gradle.lockfile
	trivy fs gradle/dependency-locks/gradle.lockfile --format template -o report.html --template "@config/trivy/html.tpl"
	grep CRITICAL report.html
	/bin/rm -rf gradle/dependency-locks

test:
	./gradlew test spotbugsMain spotbugsTest

install:
	./gradlew install -x test -x javadoc

# Publish artifacts to nexus (requires a local .gradle/gradle.properties propery configured)
release:
	./gradlew uploadArchives

run:
	./gradlew bootRun

version:
	@echo $(APP_VERSION)

