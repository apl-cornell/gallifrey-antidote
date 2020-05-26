#!/bin/bash

if echo $1 | egrep -q "\.jar$"; then
    echo buliding $1
else
    echo "Error: required <name>.jar argument"
    exit 1
fi

rm -r fatjar || true
unzip -o 'lib/*' -d fatjar
unzip -o gallifrey/core/build/libs/core.jar -d fatjar
unzip -o gallifrey/backend/build/libs/backend.jar -d fatjar
unzip -o gallifrey/frontend/build/libs/frontend.jar -d fatjar
cd fatjar && zip -r "$1" gallifrey/* eu/* com/* && mv "$1" ..
