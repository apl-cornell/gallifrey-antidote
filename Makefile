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
	./gradlew -PmainClass=Backend execute --args=True

backend2:
	./gradlew -PmainClass=Backend execute --args='True False False'

test:
	./gradlew -PmainClass=Backend execute --args=False

send:
	./gradlew -PmainClass=Backend execute --args='False True'

frontend:
	./gradlew -PmainClass=Frontend execute --args=True

frontend2:
	./gradlew -PmainClass=Frontend execute --args=False

clean:
	./gradlew clean
