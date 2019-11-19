compile:
	javac -cp .:../antidote-java-client/build/classes/java/main/:lib/protobuf-java-2.4.1.jar:lib/jinterface-1.6.1.jar src/main/*.java

counter:
	java -cp .:src:lib/jinterface-1.6.1.jar main.Counter

function:
	java -cp .:src:lib/jinterface-1.6.1.jar main/GenericFunction

backend:
	java -cp .:src:lib/jinterface-1.6.1.jar main.Backend True

test:
	java -cp .:src:lib/jinterface-1.6.1.jar main.Backend False

send:
	java -cp .:src:lib/jinterface-1.6.1.jar main.Backend False True

frontend:
	java -cp .:src:lib/jinterface-1.6.1.jar main.Frontend

clean:
	rm src/main/*.class
