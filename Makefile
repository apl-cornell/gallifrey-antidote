all: build

build: clean
	./gradlew build

backend: build
	./gradlew -PmainClass=Backend execute

backend2:
	./gradlew -PmainClass=Backend execute --args='JavaNode2@127.0.0.1'
	#./gradlew -PmainClass=Backend execute --args='JavaNode2@127.0.0.1 antidote2@127.0.0.1'

send:
	./gradlew -PmainClass=Backend execute --args='0 0 True'

#test:
#	./gradlew -PmainClass=Backend execute --args='commented out and such'

frontend:
	./gradlew -PmainClass=Frontend execute --args='localhost 8087'

frontend2:
	./gradlew -PmainClass=Frontend execute --args='localhost 8287'

clean:
	./gradlew clean
