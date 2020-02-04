all:
	javac -cp .:../antidote-java-client/build/classes/java/main/:lib/protobuf-java-3.11.0-rc-1.jar:lib/jinterface-1.6.1.jar src/main/*.java

build: all

counter:
	java -cp .:src main.Counter

rwset:
	java -cp .:src main.RWSet

function:
	java -cp .:src:lib/jinterface-1.6.1.jar main/GenericFunction

backend:
	java -cp .:src:lib/jinterface-1.6.1.jar main.Backend True

backend2:
	java -cp .:src:lib/jinterface-1.6.1.jar main.Backend True False False

test:
	java -cp .:src:lib/jinterface-1.6.1.jar main.Backend False

send:
	java -cp .:src:lib/jinterface-1.6.1.jar main.Backend False True

frontend:
	java -cp .:src:../antidote-java-client/build/classes/java/main/:lib/protobuf-java-3.11.0-rc-1.jar:lib/jinterface-1.6.1.jar main.Frontend True

frontend2:
	java -cp .:src:../antidote-java-client/build/classes/java/main/:lib/protobuf-java-3.11.0-rc-1.jar:lib/jinterface-1.6.1.jar main.Frontend False

clean:
	rm src/main/*.class
