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
# include the jar files we need to link with
cp java/*.jar /tmp/demo-extension

# Finally, ask Smart Packaer to create a unprotected package using the JSONNET template & the temp folder as archive .yip content
SmartPackager --unprotected --package demo-extension-2_0.yip --new demo-extension-yip-template.jsonnet --archive /tmp/demo-extension
