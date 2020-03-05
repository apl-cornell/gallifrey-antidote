all: build

build:
	./gradlew build

counter:
	./gradlew -PmainClass=Counter execute

rwset:
	./gradlew -PmainClass=RWSet execute

function:
	./gradlew -PmainClass=GenericFunction execute

backend:
	./gradlew -PmainClass=Backend execute

backend2:
	./gradlew -PmainClass=Backend execute --args='JavaNode2@127.0.0.1 antidote2@127.0.0.1'

send:
	./gradlew -PmainClass=Backend execute --args='0 0 True'

#test:
#	./gradlew -PmainClass=Backend execute --args='commented out and such'

frontend:
	./gradlew -PmainClass=Frontend execute --args=8087

frontend2:
	./gradlew -PmainClass=Frontend execute --args=8287

clean:
	./gradlew clean
