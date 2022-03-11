#!/bin/bash

# make a temp staging folder for the .yip archive and copy only the files we need on the pendant
rm -Rf /tmp/test-extension
rm test-extension-2_0.yip
mkdir -p /tmp/test-extension

cp *.yml bin/Debug/netcoreapp2.2/linux-arm/publish/* /tmp/test-extension/

# Finally, ask Smart Packaer to create a unprotected package using the JSONNET template & the temp folder as archive .yip content
SmartPackager --unprotected --package test-extension-2_0.yip --new demo-extension-yip-template.jsonnet --archive /tmp/test-extension

if cp test-extension-2_0.yip /media/jurge/New\ Volume/; then
   echo "Copy Code: $? - Successful"
else
   echo "Copy Code: $? - Unsuccessful"
fi