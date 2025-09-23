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

build: clean ## Build the library without tests
	./gradlew build spotlessApply -x test -x spotbugsMain -x spotbugsTest -x javadoc

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

docker: ## Build the docker image and tag with version and latest leave arm64 out for now)
	docker buildx build --platform linux/amd64 --no-cache-filter=gradle-build -t $(DOCKER_INT_REGISTRY)/$(SERVICE):$(APP_VERSION) . 
	docker tag $(DOCKER_INT_REGISTRY)/$(SERVICE):$(APP_VERSION) $(DOCKER_INT_REGISTRY)/$(SERVICE):latest
	@echo APP_VERSION=$(DOCKER_INT_REGISTRY)/$(SERVICE):$(APP_VERSION)

release: ## publish to dockerhub
	@echo $(DISPLAY_BOLD)"Publishing container to $(DOCKER_INT_REGISTRY) registry"$(DISPLAY_RESET)
	@echo $(DOCKER_INT_REGISTRY)/$(SERVICE):latest
	docker -D push --platform linux/amd64 $(DOCKER_INT_REGISTRY)/$(SERVICE):latest
	# docker -D push --platform linux/arm64 $(DOCKER_INT_REGISTRY)/$(SERVICE):latest
	@echo $(DOCKER_INT_REGISTRY)/$(SERVICE):$(APP_VERSION)
	docker -D push --platform linux/amd64 $(DOCKER_INT_REGISTRY)/$(SERVICE):$(APP_VERSION)
	# docker -D push --platform linux/arm64 $(DOCKER_INT_REGISTRY)/$(SERVICE):$(APP_VERSION)

version:
	@echo $(APP_VERSION)
