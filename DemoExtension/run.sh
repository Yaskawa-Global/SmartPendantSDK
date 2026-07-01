#!/bin/bash

CLASS_NAME="DemoExtension" # PascalCase

echo "Attention: SmarPendant or simulator must be running first"
java -cp ./lib/yaskawa-ext-4.0.3.jar:./lib/libthrift-0.11.0.jar:./lib/slf4j-api.jar:./lib/slf4j-simple.jar:./${CLASS_NAME}.jar:. ${CLASS_NAME} $1 $2
