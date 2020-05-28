#!/bin/bash

if echo $1 | egrep -q "\.jar$"; then
    echo building $1
    jarname=$1
else
    echo "Warning: assuming full-runtime.jar is desired name"
    jarname=full-runtime.jar
fi

rm -r fatjar || true
unzip -o 'lib/*' -d fatjar
unzip -o gallifrey/core/build/libs/core.jar -d fatjar
unzip -o gallifrey/backend/build/libs/backend.jar -d fatjar
unzip -o gallifrey/frontend/build/libs/frontend.jar -d fatjar
cd fatjar && zip -r "$jarname" gallifrey/* eu/* com/* && mv "$jarname" ..
