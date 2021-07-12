#!/bin/bash

java -cp lib/yaskawa-ext-2.0.4.jar:lib/libthrift-0.11.0.jar:lib/slf4j-api.jar:lib/slf4j-simple.jar:DemoExtension.jar:. DemoExtension $1 $2

