cd java
javac -cp ../dependencies/libthrift-0.11.0.jar;../dependencies/slf4j-api.jar;../dependencies/yaskawa-ext-2.0.4.jar *.java
jar -cfe ../DemoExtension.jar DemoExtension DemoExtension.class
cd ..
