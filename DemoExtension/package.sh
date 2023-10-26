#!/bin/bash

# make a temp staging folder for the .yip archive and copy only the files we need on the pendant
rm -Rf /tmp/demo-extension
mkdir -p /tmp/demo-extension
cp ./DemoExtension.jar /tmp/demo-extension/

# include the jar files we need to link with
cp ./lib/*.jar /tmp/demo-extension

# yml files
mkdir -p /tmp/demo-extension/yml
cp yml/*.yml /tmp/demo-extension/yml/ 2>/dev/null

# language files
mkdir -p /tmp/demo-extension/language
cp ./language/*.properties /tmp/demo-extension/language/ 2>/dev/null

# help files
mkdir -p /tmp/demo-extension/help
cp -r help/* /tmp/demo-extension/help/ 2>/dev/null

# image files
mkdir -p /tmp/demo-extension/images
cp images/*.jpg images/*.png images/*.svg /tmp/demo-extension/images/ 2>/dev/null

# job files
mkdir -p /tmp/demo-extension/jobs
cp jobs/* /tmp/demo-extension/jobs/ 2>/dev/null

# Finally, ask Smart Packager to create a unprotected package using the JSONNET template & the temp folder as archive .yip content
# Update the path below with your local installation path of the SmartPackager
~/SmartPackager/SmartPackager --unprotected --package demo-extension-3_0.yip --new demo-extension-yip-template.jsonnet --archive /tmp/demo-extension
