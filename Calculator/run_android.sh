#!/bin/env sh

export JAVA_HOME=$(pwd)/jre
export LD_LIBRARY_PATH=$JAVA_HOME/lib

chmod u+x $JAVA_HOME/bin/java

$JAVA_HOME/bin/java -cp $(printf '%s:' *.jar). Calculator
