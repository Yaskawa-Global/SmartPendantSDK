#!/bin/bash

if [ ! -f "../SmartPackager/SmartPackager" ]; then
  echo "Error: SmartPackager not found"
  exit 1
fi

cd ../SmartPackager/
QT_PLUGIN_PATH=./plugins LD_LIBRARY_PATH=./lib ./SmartPackager 2> /dev/null
