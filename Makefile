all: build

# buildcore is added because apparently gradle does not run the tests of subprojects it builds for the current project its building

buildcore:
	./gradlew :core:build

buildfrontend: buildcore
	./gradlew :frontend:build

buildbackend: buildcore
	./gradlew :backend:build

build: clean buildbackend buildfrontend

backend: buildbackend
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

clean:
	./gradlew clean
