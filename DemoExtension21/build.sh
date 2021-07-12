#!/bin/bash

cd java

javac -cp ../lib/libthrift-0.11.0.jar:../lib/slf4j-api.jar:../lib/yaskawa-ext-2.1.0.jar *.java

jar -cfe ../DemoExtension.jar DemoExtension DemoExtension.class

