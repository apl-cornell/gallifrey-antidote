# A help command that lists what make commands are available
# This black magic brought to you by: https://gist.github.com/prwhite/8168133
help: ## Show this help
	@egrep '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

.PHONY: all
all: build ## Alias of build

# buildcore is added because apparently gradle does not run the tests of subprojects it builds for the current project its building

buildcore:
	./gradlew :gallifrey:core:build

buildfrontend: buildcore
	./gradlew :gallifrey:frontend:build

buildbackend: buildcore
	./gradlew :gallifrey:backend:build

.PHONY: build
build: buildbackend buildfrontend ## Build the project

.PHONY: backend
backend: buildbackend  ## Creates a running backend instance that Antidote can connect to
	# The exported class below is just an example.
	# If you want to try it out, move Counter.java from core to frontend and adjust package names, imports, and tests
	export EXTERNAL_CLASSES="../frontend/build/classes/java/main" && ./gradlew :gallifrey:backend:execute -PmainClass='gallifrey.backend.VectorClockBackend'
	#./gradlew -PmainClass=VectorClockBackend execute

#backend2:
	#./gradlew -PmainClass=VectorClockBackend execute --args='JavaNode2@127.0.0.1'
	#./gradlew -PmainClass=Backend execute --args='JavaNode2@127.0.0.1 antidote2@127.0.0.1'

#test:
#	./gradlew -PmainClass=BackendWithTesting execute

.PHONY: frontend
frontend: buildfrontend
	./gradlew :gallifrey:frontend:execute -PmainClass='gallifrey.frontend.Demo'

.PHONY: counter
counter:
	./gradlew :gallifrey:frontend:execute -PmainClass='gallifrey.frontend.DemoWithSetName'
	# ./gradlew :gallifrey:frontend:execute -PmainClass='gallifrey.frontend.DemoWithTwoObjects'


#frontend2:
	#./gradlew -PmainClass=Frontend execute --args='localhost 8287'

.PHONY: clean
clean: ## Has gradle clean it's build files
	./gradlew clean
	rm full-runtime.jar || true
	rm -r fatjar || true
	rm -r gallifrey/*/bin || true
	rm gallifrey/*/file.txt || true

.PHONY: fatjar
fatjar: ## Make sure you build before you try to make the jar file
	./fatjar.sh
