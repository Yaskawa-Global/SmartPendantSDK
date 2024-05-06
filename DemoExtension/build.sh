JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{sub("^$", "0", $2); print $1}')
if [[ "$JAVA_VER" == "11" ]]; then
    echo "Java Version (11) is correct" 
    javac -cp ./lib/libthrift-0.11.0.jar:./lib/slf4j-api.jar:./lib/yaskawa-ext-3.1.0.jar *.java
    jar -cfe ./DemoExtension.jar DemoExtension *.class
    rm *.class
else         
    echo "Incompatible Java version" $JAVA_VER
fi
