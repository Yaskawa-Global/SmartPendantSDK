#!/bin/bash

java -cp java/lib/yaskawa-ext-3.0.0.jar:java/lib/libthrift-0.11.0.jar:java/lib/slf4j-api.jar:java/lib/slf4j-simple.jar:DemoExtension.jar:. DemoExtension $1 $2

