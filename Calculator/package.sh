#!/bin/bash

# make a temp staging folder for the .yip archive and copy only the files we need on the pendant
rm -Rf /tmp/calc-extension
mkdir -p /tmp/calc-extension
mkdir -p /tmp/calc-extension/images
#mkdir -p /tmp/calc-extension/help

cp images/*.jpg images/*.png images/*.svg /tmp/calc-extension/images/ 2>/dev/null
#cp -r help/* /tmp/calc-extension/help/ 2>/dev/null
cp *.yml Calculator.jar /tmp/calc-extension/
#cp ourlicense.html /tmp/calc-extension/
# include the jar files we need to link with
cp java/*.jar /tmp/calc-extension
cp ../java/yaskawa-ext-2.0.4.jar /tmp/calc-extension

# Finally, ask Smart Packaer to create a unprotected package using the JSONNET template & the temp folder as archive .yip content
SmartPackager --unprotected --package calculator-1_0.yip --new calculator-yip-template.jsonnet --archive /tmp/calc-extension
