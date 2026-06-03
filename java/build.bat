@echo off

REM Download dependent libs first, if not already present locally
if not exist lib mkdir lib
cd lib

if not exist libthrift-0.11.0.jar (
    curl -O https://s3.us-east-2.amazonaws.com/yaskawa-yii/SmartPendant/extension/libthrift-0.11.0.jar
)

if not exist slf4j-api.jar (
    curl -O https://s3.us-east-2.amazonaws.com/yaskawa-yii/SmartPendant/extension/slf4j-api.jar
)

REM not actually needed for building the client code that uses the API, but may be used by extensions:
if not exist slf4j-simple.jar (
    curl -O https://s3.us-east-2.amazonaws.com/yaskawa-yii/SmartPendant/extension/slf4j-simple.jar
)

cd ..

cd gen-java
javac -source 1.8 -target 1.8 -Xlint:deprecation -cp ..\lib\libthrift-0.11.0.jar;..\lib\slf4j-api.jar yaskawa\ext\api\*.java
cd ..

javac -source 1.8 -target 1.8 -Xlint:deprecation -Xlint:unchecked -cp lib\libthrift-0.11.0.jar;lib\slf4j-api.jar;gen-java yaskawa\ext\*.java

cd gen-java
jar cf ..\yaskawa-ext-4.4.1.jar yaskawa
cd ..

jar uf yaskawa-ext-4.4.1.jar yaskawa

