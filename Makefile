# A help command that lists what make commands are available
# This black magic brought to you by: https://gist.github.com/prwhite/8168133
help: ## Show this help
	@egrep '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

.PHONY: all
all: build ## Alias of build

# buildcore is added because apparently gradle does not run the tests of subprojects it builds for the current project its building

buildcore:
	./gradlew :core:build

buildfrontend: buildcore
	./gradlew :frontend:build

buildbackend: buildcore
	./gradlew :backend:build

.PHONY: build
build: buildbackend buildfrontend ## Build the project

backend: buildbackend ## Creates a running backend instance that Antidote can connect to
	./gradlew :backend:execute -PmainClass=VectorClockBackend
	#./gradlew -PmainClass=VectorClockBackend execute

#backend2:
	#./gradlew -PmainClass=VectorClockBackend execute --args='JavaNode2@127.0.0.1'
	#./gradlew -PmainClass=Backend execute --args='JavaNode2@127.0.0.1 antidote2@127.0.0.1'

#test:
#	./gradlew -PmainClass=BackendWithTesting execute

frontend: buildfrontend
	./gradlew :frontend:execute -PmainClass=Frontend --args='localhost 8087' --stacktrace

#frontend2:
	#./gradlew -PmainClass=Frontend execute --args='localhost 8287'

.PHONY: clean
clean: ## Has gradle clean it's build files
	./gradlew clean