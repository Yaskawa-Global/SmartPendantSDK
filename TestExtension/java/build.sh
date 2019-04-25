#cd ../../java
#./build.sh
#cd ../TestExtension/java
javac -cp ../../../../External/thrift/lib/java/build/libthrift-0.11.0.jar:/usr/share/java/slf4j-api.jar:../../java/yaskawa-ext-0.1.3-pre.jar *.java
jar -cfe TestExtension.jar TestExtension TestExtension.class

