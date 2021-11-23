#! /bin/bash

# Download dependent libs first, if not already present locally
mkdir -p lib
cd lib
if [ ! -f "libthrift-0.11.0.jar" ]; then
  wget https://s3.us-east-2.amazonaws.com/yaskawa-yii/SmartPendant/extension/libthrift-0.11.0.jar
fi
if [ ! -f "slf4j-api.jar" ]; then
  wget https://s3.us-east-2.amazonaws.com/yaskawa-yii/SmartPendant/extension/slf4j-api.jar
fi
# not actually needed for building the client code that uses the API, but may be used by extensions:
if [ ! -f "slf4j-simple.jar" ]; then
  wget https://s3.us-east-2.amazonaws.com/yaskawa-yii/SmartPendant/extension/slf4j-simple.jar
fi
cd ..

cd gen-java
javac -Xlint:deprecation -cp ../lib/libthrift-0.11.0.jar:../lib/slf4j-api.jar yaskawa/ext/api/*.java
cd ..
javac -Xlint:deprecation -Xlint:unchecked -cp lib/libthrift-0.11.0.jar:lib/slf4j-api.jar:gen-java yaskawa/ext/*.java
cd gen-java
jar cf ../yaskawa-ext-2.2.0.jar yaskawa
cd ..
jar uf yaskawa-ext-2.2.0.jar yaskawa

