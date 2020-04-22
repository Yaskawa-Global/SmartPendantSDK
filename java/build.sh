#! /bin/bash
cd gen-java
javac -Xlint:deprecation -cp ../../../../External/thrift/lib/java/build/libthrift-0.11.0.jar:/usr/share/java/slf4j-api.jar yaskawa/ext/api/*.java
cd ..
javac -Xlint:deprecation -Xlint:unchecked -cp ../../../External/thrift/lib/java/build/libthrift-0.11.0.jar:/usr/share/java/slf4j-api.jar:gen-java yaskawa/ext/*.java
cd gen-java
jar cf ../yaskawa-ext-2.0.1.jar yaskawa
cd ..
jar uf yaskawa-ext-2.0.1.jar yaskawa
#cd gen-java/yaskawa/ext/api
#jar cf ../../../../yaskawa-ext.jar *.class
#cd ../../../../yaskawa/ext
#jar uf ../../yaskawa-ext.jar *.class


