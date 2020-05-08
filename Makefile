all: build

build: clean
	./gradlew build

backend: build
	rmiregistry &
	./gradlew -PmainClass=VectorClockBackend execute

backend2:
	./gradlew -PmainClass=VectorClockBackend execute --args='JavaNode2@127.0.0.1'
	#./gradlew -PmainClass=Backend execute --args='JavaNode2@127.0.0.1 antidote2@127.0.0.1'

#test:
#	./gradlew -PmainClass=BackendWithTesting execute

frontend:
	./gradlew -PmainClass=Frontend execute --args='localhost 8087'

frontend2:
	./gradlew -PmainClass=Frontend execute --args='localhost 8287'

clean:
	./gradlew clean
