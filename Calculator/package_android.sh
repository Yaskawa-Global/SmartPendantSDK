#!/bin/bash

# download self contained openjre17
if [ ! -d "openjre17" ]
then
    mkdir "openjre17"
    curl -sL https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre17-ec28559/jre17-arm-20210914-release.tar.xz | tar -Jx -C openjre17
fi

# make a temp staging folder for the .yip archive and copy only the files we need on the pendant
rm -Rf /tmp/calc-extension
mkdir -p /tmp/calc-extension
mkdir -p /tmp/calc-extension/images
mkdir -p /tmp/calc-extension/jre
#mkdir -p /tmp/calc-extension/help

cp images/*.jpg images/*.png images/*.svg /tmp/calc-extension/images/ 2>/dev/null
#cp -r help/* /tmp/calc-extension/help/ 2>/dev/null
cp *.yml Calculator.jar /tmp/calc-extension/
#cp ourlicense.html /tmp/calc-extension/
cp -Lr openjre17/* /tmp/calc-extension/jre/
# include the jar files we need to link with
cp Calculator.jar /tmp/calc-extension
cp ../java/lib/*.jar ../java/yaskawa-ext-*.jar /tmp/calc-extension
# include the run script
cp run_android.sh /tmp/calc-extension

# Finally, ask Smart Packaer to create a unprotected package using the JSONNET template & the temp folder as archive .yip content
SmartPackager --unprotected --package calculatorWJRE-1_0.yip --new calculator-yip-template-android.jsonnet --archive /tmp/calc-extension
