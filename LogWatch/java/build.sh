javac -cp ../../../../External/thrift/lib/java/build/libthrift-0.11.0.jar:/usr/share/java/slf4j-api.jar:../../java/yaskawa-ext-1.0.0-pre.jar --add-modules java.xml.ws.annotation LogWatch.java
jar -cfe LogWatch.jar LogWatch LogWatch.class

