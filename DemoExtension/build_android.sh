javac -source 1.8 -target 1.8 -cp ./lib/libthrift-0.11.0.jar:./lib/slf4j-api.jar:./lib/yaskawa-ext-4.0.3.jar *.java
$ANDROID_HOME/build-tools/30.0.3/d8 --lib $ANDROID_HOME/platforms/android-30/android.jar ./lib/libthrift-0.11.0.jar ./lib/slf4j-api.jar ./lib/slf4j-simple.jar ./lib/yaskawa-ext-4.0.3.jar --output DemoExtension.jar *.class
rm *.class

