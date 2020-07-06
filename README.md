# gallifrey-antidote

Gallifrey implementation over the Antidote system

## installation

```bash
## Note, I've added the gallifrey version of antidote-java-client to gallifrey-antidote as a jar file since antidote-java-client seems rather stable. If you want to make changes to the antidote-java-client, include the commented out code, create a new jar from your changes to antidote-java-client and add the jar file to gallifrey-antidote/lib as ```antidote-java-client-gallifrey.jar```
git clone https://github.com/apl-cornell/antidote.git
git clone https://github.com/apl-cornell/gallifrey-antidote
#git clone https://github.com/apl-cornell/antidote-java-client

cd antidote
mkdir _checkouts
git clone https://github.com/apl-cornell/antidote_crdt.git _checkouts/antidote_crdt
git clone https://github.com/apl-cornell/antidote_pb_codec.git _checkouts/antidote_pb_codec

make compile

cd ..

## You may need to run ```make shell``` in a separate terminal for the next step in the antidote directory to run tests.

#cd antidote-java-client
#./gradlew build ## Alternatively you can use ```gradle build -x test```
#cd ..

cd gallifrey-antidote
## You may need to run ```make shell``` and ```make backend``` in separate terminals in the antidote directory and this directory respectively to run tests.
## Alternatively you could disable tests on build in gradle
make all
```

## running

To start antidote instance: ```make shell```

To start gallifrey backend: ```make backend```

Remember to set the EXTERNAL_CLASSES environment variable before running ```make backend```

To run example frontend code: ```make frontend```

To connect to antidote shells:

antidote@127.0.0.1

```erlang
{ok, Descriptor1} = rpc:call('antidote@127.0.0.1', antidote_dc_manager, get_connection_descriptor, []),
{ok, Descriptor2} = rpc:call('antidote2@127.0.0.1', antidote_dc_manager, get_connection_descriptor, []),
Descriptors = [Descriptor1, Descriptor2],
rpc:call('antidote@127.0.0.1', antidote_dc_manager, subscribe_updates_from, [Descriptors]),
rpc:call('antidote2@127.0.0.1', antidote_dc_manager, subscribe_updates_from, [Descriptors]).
```

Around the world test

Sub in for correct ip's of choice after @

```erlang
{ok, Descriptor1} = rpc:call('antidote@10.132.9.129', antidote_dc_manager, get_connection_descriptor, []),
{ok, Descriptor2} = rpc:call('antidote@128.253.3.197', antidote_dc_manager, get_connection_descriptor, []),
Descriptors = [Descriptor1, Descriptor2],
rpc:call('antidote@10.132.9.129', antidote_dc_manager, subscribe_updates_from, [Descriptors]),
rpc:call('antidote@128.253.3.197', antidote_dc_manager, subscribe_updates_from, [Descriptors]).
```

## troubleshooting

### Nameserver exception

If you get something like:
```java.io.IOException: Nameserver not responding on 127.0.0.1 when publishing JavaNode```

You need to run ```erl -sname putwhateveryouwant``` and then exit to get erlang epmd running or just run the antidote shell you fool.

### Socket exception

If you get something like:
```eu.antidotedb.client.SocketSender$AntidoteSocketException: java.io.IOException: End of input while data expected```

Then your antidote instance you are running is not a gallifrey instance or at some point the protocol buffers have been changed such that the protobuf-java jar isn't compatible with antidote's version.

### Serialization Error(Null Pointer Exception)

If you get a null pointer exception, especially if it is from the crdt_object. Check both that the object you are trying to use is ~actually~ serializable and that the class files for that object are available to the backend.

### IllegalAccess Exception

If you get something like:
```java.lang.IllegalAccessException: Class gallifrey.core.CRDT can not access a member of class CRDTCLASS with modifiers "public final"```

Then your crdt object is private and must be explicitly made public to access the corresponding public fields.
