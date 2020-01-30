# gallifrey-antidote

Gallifrey implementation over the Antidote system

## installation

```bash
git clone https://github.com/apl-cornell/antidote.git
git clone https://github.com/apl-cornell/gallifrey-antidote
git clone https://github.com/apl-cornell/antidote-java-client

cd antidote
mkdir _checkouts
git clone https://github.com/apl-cornell/antidote_crdt.git _checkouts/antidote_crdt
git clone https://github.com/apl-cornell/antidote_pb_codec.git _checkouts/antidote_pb_codec

sudo make compile

cd ..

## you may need to run ```sudo make shell``` in a seperate terminal for the next step in the antidote directory to run tests.

cd antidote-java-client
./gradlew build
cd ..

cd gallifrey-antidote
make all
```

## running

To start antidote instance: ```sudo make shell```

To start gallifrey backend: ```make backend```

To run example frontend code: ```make frontend```

To connect to antidote shells:

```erlang
{ok, Descriptor1} = rpc:call('antidote@127.0.0.1', antidote_dc_manager, get_connection_descriptor, []),
{ok, Descriptor2} = rpc:call('antidote2@127.0.0.1', antidote_dc_manager, get_connection_descriptor, []),
Descriptors = [Descriptor1, Descriptor2],
rpc:call('antidote@127.0.0.1', antidote_dc_manager, subscribe_updates_from, [Descriptors]),
rpc:call('antidote@127.0.0.1', antidote_dc_manager, subscribe_updates_from, [Descriptors]).
```

## troubleshooting

If you get something like:
```java.io.IOException: Nameserver not responding on 127.0.0.1 when publishing JavaNode```

You need to run ```erl -sname putwhateveryouwant``` and then exit to get erlang epmd running
