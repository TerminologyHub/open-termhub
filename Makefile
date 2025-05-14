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
DOCKER_INT_REGISTRY      := termhub

.PHONY: build

clean: ## Clean build artifacts. Override for your project
	./gradlew clean

build: ## Build the library without tests
	./gradlew build	test -x javadoc

scan: ## scan for vulnerabilities in dependencies
	/bin/rm -rf gradle/dependency-locks
	./gradlew dependencies --write-locks
	trivy fs gradle.lockfile --format template -o report.html --template "@config/trivy/html.tpl"
	grep CRITICAL report.html
	/bin/rm -rf gradle.lockfile

test: ## Run all tests
	./gradlew test

test-r4: ## Run R4 tests only
	./gradlew testR4

test-r5: ## Run R5 tests only
	./gradlew testR5

run: ## Run the server
	./gradlew bootRun

rundebug: ## Run the server in debug mode
	./gradlew bootRun --debug-jvm

docker: ## Build the docker image
	@echo APP_VERSION=$(APP_VERSION)
	@echo REGISTRY=$(DOCKER_INT_REGISTRY)
	@echo SERVICE=$(SERVICE)
	docker build --no-cache-filter=gradle-build -t $(SERVICE) .
	docker tag $(SERVICE) $(DOCKER_INT_REGISTRY)/$(SERVICE):$(APP_VERSION)

scandocker:
	trivy image $(DOCKER_INT_REGISTRY)/$(SERVICE):$(APP_VERSION) --format template -o report.html --template "@config/trivy/html.tpl"
	grep CRITICAL report.html

version:
	@echo $(APP_VERSION)
