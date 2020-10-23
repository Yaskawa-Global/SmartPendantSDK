cd java
javac -cp ../../../../External/thrift/lib/java/build/libthrift-0.11.0.jar:/usr/share/java/slf4j-api.jar:../../java/yaskawa-ext-2.0.1.jar *.java
jar -cfe TestExtension.jar TestExtension TestExtension.class

