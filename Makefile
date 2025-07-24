# global service name
SERVICE                 := open-termhub

INTERACTIVE := $(shell [ -t 0 ] && echo 1)
#######################################################################
#                 OVERRIDE THIS TO MATCH YOUR PROJECT                 #
#######################################################################

# Most applications have their own method of maintaining a version number.
APP_VERSION              := $(shell echo `grep "version = " build.gradle | cut -d\= -f 2`)
DOCKER_INT_REGISTRY      := wcinformatics

.PHONY: build

clean: ## Clean build artifacts. Override for your project
	./gradlew clean

build: ## Build the library without tests
	./gradlew build -x test -x spotbugsMain -x spotbugsTest -x javadoc

scan: ## scan for vulnerabilities in dependencies
	/bin/rm -rf gradle/dependency-locks
	./gradlew dependencies --write-locks --configuration runtimeClasspath
	trivy fs gradle.lockfile --format template -o report.html --template "@config/trivy/html.tpl"
	grep CRITICAL report.html
	/bin/rm -rf gradle.lockfile
	
scandocker:
	trivy image $(DOCKER_INT_REGISTRY)/$(SERVICE):$(APP_VERSION) --format template -o report.html --template "@config/trivy/html.tpl"
	grep CRITICAL report.html

fullscan: ## scan for vulnerabilities in dependencies
	/bin/rm -rf gradle/dependency-locks
	./gradlew dependencies --write-locks
	trivy fs gradle.lockfile --format template -o report.html --template "@config/trivy/html.tpl"
	grep CRITICAL report.html
	/bin/rm -rf gradle.lockfile

test: clean ## Run all tests
	./gradlew test spotbugsMain spotbugsTest

test-r4: clean ## Run R4 tests only
	./gradlew testR4

test-r5: clean ## Run R5 tests only
	./gradlew testR5

run: ## Run the server
	./gradlew bootRun

rundebug: ## Run the server in debug mode
	./gradlew bootRun --debug-jvm

docker: ## Build the docker image and tag with version and latest
	@echo APP_VERSION=$(APP_VERSION)
	@echo REGISTRY=$(DOCKER_INT_REGISTRY)
	@echo SERVICE=$(SERVICE)
	docker build --no-cache-filter=gradle-build -t $(SERVICE) .
	docker tag $(SERVICE) $(DOCKER_INT_REGISTRY)/$(SERVICE):$(APP_VERSION)
	docker tag $(SERVICE) $(DOCKER_INT_REGISTRY)/$(SERVICE):latest

version:
	@echo $(APP_VERSION)
