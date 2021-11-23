#!/bin/bash

cd java

mkdir -p lib
cd lib
if [ ! -f "libthrift-0.11.0.jar" ]; then
  echo "libthrift-0.11.0.jar wasn't found in java/lib/ downloading from Yaskawa"
  wget https://s3.us-east-2.amazonaws.com/yaskawa-yii/SmartPendant/extension/libthrift-0.11.0.jar
fi
if [ ! -f "slf4j-api.jar" ]; then
  echo "slf4j-api.jar wasn't found in java/lib/ downloading from Yaskawa"
  wget https://s3.us-east-2.amazonaws.com/yaskawa-yii/SmartPendant/extension/slf4j-api.jar
fi
if [ ! -f "slf4j-simple.jar" ]; then
  echo "slf4j-simple.jar wasn't found in java/lib/ downloading from Yaskawa"
  wget https://s3.us-east-2.amazonaws.com/yaskawa-yii/SmartPendant/extension/slf4j-simple.jar
fi
if [ ! -f "yaskawa-ext-2.2.0.jar" ]; then
  echo "yaskawa-ext-2.2.0.jar wasn't found in java/lib/ downloading from Yaskawa"
  wget https://s3.us-east-2.amazonaws.com/yaskawa-yii/SmartPendant/extension/yaskawa-ext-2.2.0.jar
fi
cd ..

javac -cp lib/libthrift-0.11.0.jar:lib/slf4j-api.jar:lib/yaskawa-ext-2.2.0.jar *.java

jar -cfe ../DemoExtension.jar DemoExtension DemoExtension.class

