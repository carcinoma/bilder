#!/bin/sh
echo HOSTNAME: $HOSTNAME
set
java -XX:MaxHeapFreeRatio=10 \
    -XX:MinHeapFreeRatio=10 \
    -XX:+UseG1GC -Xms16m \
    --add-modules java.sql,java.instrument,jdk.unsupported \
    -p /bilder-app.jar \
    -m bilder.app \
    --imagesPath=/images/ \
    --cacheDir=/cache/ \
    --spring.datasource.url=jdbc:h2:file:/cache/database
