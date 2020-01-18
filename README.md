# gallifrey-antidote

Gallifrey implementation over the Antidote system

## installation

```bash
set -e

git clone https://github.com/apl-cornell/antidote.git
git clone https://github.com/apl-cornell/gallifrey-antidote
git clone https://github.com/apl-cornell/antidote-java-client

cd antidote
mkdir _checkouts
git clone https://github.com/apl-cornell/antidote_crdt.git _checkouts/antidote_crdt
git clone https://github.com/apl-cornell/antidote_pb_codec.git _checkouts/antidote_pb_codec

make compile

cd ..

cd antidote-java-client
./gradlew build
cd ..

cd gallifrey-antidote
make all

...
```

## running

To start antidote instance: ```make shell```
To start gallifrey backend: ```make backend```
To run example frontend code: ```make frontend```

## troubleshooting

If you get something like:
```java.io.IOException: Nameserver not responding on 127.0.0.1 when publishing JavaNode```

You need to run ```erl -sname putwhateveryouwant``` and then exit to open erlang epmd running
