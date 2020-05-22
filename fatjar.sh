#!/bin/bash

rm -r fatjar || true
unzip -o 'lib/*' -d fatjar
unzip -o gallifrey/core/build/libs/core.jar -d fatjar
unzip -o gallifrey/backend/build/libs/backend.jar -d fatjar
unzip -o gallifrey/frontend/build/libs/frontend.jar -d fatjar
zip -r "$1" fatjar