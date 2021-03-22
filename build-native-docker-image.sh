#!/bin/bash
./gradlew clean buildLayers
docker build -t vocab-mate .
docker run --rm -it --entrypoint bash vocab-mate