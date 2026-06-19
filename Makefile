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
	./gradlew bootJar -x test -x spotbugsMain -x spotbugsTest -x javadoc

scan: ## scan for vulnerabilities in dependencies
	/bin/rm -rf gradle/dependency-locks
	./gradlew dependencies --write-locks --configuration runtimeClasspath
	trivy fs gradle.lockfile --format template -o report.html --template "@config/trivy/html.tpl"
	grep CRITICAL report.html
	/bin/rm -rf gradle.lockfile

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
	./run.sh

# connect
rundebug: ## Run the server with debug logging and JVM debug port (5005)
	./gradlew bootRun --debug-jvm -DLOG_LEVEL=debug

docker:
# Remove prior docker image if it is built
ifdef DOCKER_IMG
	docker rmi -f $(DOCKER_IMG)
else
	@echo No docker image to remove
endif
	@echo SERVICE=$(DOCKER_INT_REGISTRY)/$(SERVICE):$(APP_VERSION)
	docker build --platform linux/amd64 --build-arg APP_VERSION=$(APP_VERSION) --no-cache-filter=gradle-build -t $(DOCKER_INT_REGISTRY)/$(SERVICE):$(APP_VERSION) .
	docker tag $(DOCKER_INT_REGISTRY)/$(SERVICE):$(APP_VERSION) $(DOCKER_INT_REGISTRY)/$(SERVICE):latest

scandocker:
	docker save -o scan.tar $(DOCKER_INT_REGISTRY)/$(SERVICE):$(APP_VERSION)
	trivy image --input scan.tar $(DOCKER_INT_REGISTRY)/$(SERVICE):$(APP_VERSION) --format template -o report.html --template "@config/trivy/html.tpl"
	egrep "CRITICAL|HIGH" report.html
	/bin/rm -f scan.tar
	
release: ## publish to dockerhub
	@echo $(DOCKER_INT_REGISTRY)/$(SERVICE):$(APP_VERSION)
	docker -D push --platform linux/amd64 $(DOCKER_INT_REGISTRY)/$(SERVICE):latest
	docker -D push --platform linux/amd64 $(DOCKER_INT_REGISTRY)/$(SERVICE):$(APP_VERSION)

version:
	@echo $(APP_VERSION)
