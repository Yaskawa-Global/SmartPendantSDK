cd java
javac -cp ../../../../External/thrift/lib/java/build/libthrift-0.11.0.jar:/usr/share/java/slf4j-api.jar:../../java/yaskawa-ext-2.0.3.jar *.java
jar -cfe ../Calculator.jar Calculator Calculator.class

