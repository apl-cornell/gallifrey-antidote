compile:
	javac -cp .:lib/jinterface-1.6.1.jar src/main/*.java

counter:
	java -cp .:src:lib/jinterface-1.6.1.jar main.Counter

function:
	java -cp .:src:lib/jinterface-1.6.1.jar main/GenericFunction

backend:
	java -cp .:src:lib/jinterface-1.6.1.jar main.Backend

frontend:
	java -cp .:src:lib/jinterface-1.6.1.jar main.Frontend

clean:
	rm src/main/*.class
