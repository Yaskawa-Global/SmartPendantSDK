#!/bin/bash

# make a temp staging folder for the .yip archive and copy only the files we need on the pendant
rm -Rf /tmp/demo-extension
mkdir -p /tmp/demo-extension
mkdir -p /tmp/demo-extension/images
mkdir -p /tmp/demo-extension/help
mkdir -p /tmp/demo-extension/jobs

cp images/*.jpg images/*.png images/*.svg /tmp/demo-extension/images/ 2>/dev/null
cp -r help/* /tmp/demo-extension/help/ 2>/dev/null
cp -r jobs/* /tmp/demo-extension/jobs/ 2>/dev/null
cp *.yml DemoExtension.jar /tmp/demo-extension/
cp *.properties /tmp/demo-extension/
# include the jar files we need to link with
cp java/lib/*.jar /tmp/demo-extension/
cp ../java/yaskawa-ext-2.2.0.jar /tmp/demo-extension/


# Finally, ask Smart Packaer to create a unprotected package using the JSONNET template & the temp folder as archive .yip content
#/home/sevieje/build-SmartPackager-Desktop_Qt_5_15_2_GCC_64bit-Release/SmartPackager --unprotected --package demo-extension-2_2.yip --new demo-extension-yip-template.jsonnet --archive /tmp/demo-extension
