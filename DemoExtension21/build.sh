cd java
javac -cp ../../../../External/thrift/lib/java/build/libthrift-0.11.0.jar:/usr/share/java/slf4j-api.jar:../../java/yaskawa-ext-2.1.0.jar *.java
jar -cfe ../DemoExtension.jar DemoExtension DemoExtension.class

