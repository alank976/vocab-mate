#!/bin/bash
./gradlew clean buildLayers
docker build -t vocab-mate .