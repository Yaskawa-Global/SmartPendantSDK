#!/bin/bash

CLASS_NAME="DemoExtension" # PascalCase
VERSION="0.0.1"


JAVA_MAJOR=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
if [ "$JAVA_MAJOR" -lt 11 ]; then
  echo "Error: Java version >= 11 is required. Installed version:" $JAVA_MAJOR
  exit 1
fi

KC_NAME=$(printf '%s' ${CLASS_NAME} | sed -E 's/([A-Z])/-\1/g' | sed 's/^-//' | tr 'A-Z' 'a-z')
WORKING_DIR="$PWD/tmp"
# WORKING_DIR="/tmp/${KC_NAME}"
WORKING_DIR_CLASSES="${WORKING_DIR}/classes"
WORKING_DIR_PACKAGER="${WORKING_DIR}/packager"
rm -rf ${WORKING_DIR}
mkdir -p ${WORKING_DIR_CLASSES}

javac --release 11 -cp ./lib/libthrift-0.11.0.jar:./lib/slf4j-api.jar:./lib/yaskawa-ext-4.0.3.jar \
    -d ${WORKING_DIR_CLASSES} ./java/*.java
jar -cfe ${CLASS_NAME}.jar ${CLASS_NAME} -C ${WORKING_DIR_CLASSES} .

if [ "$1" = "--jar-only" ] || [ "$1" = "--only-jar" ]; then
  rm -rf ${WORKING_DIR}
  exit 0
fi

if [ ! -f "../SmartPackager/SmartPackager" ]; then
  echo "Error: SmartPackager not found"
  exit 1
fi

# make a temp staging folder for the .yip archive and copy only the files we need on the pendant
mkdir -p ${WORKING_DIR_PACKAGER}
cp ./${CLASS_NAME}.jar ${WORKING_DIR_PACKAGER}/${CLASS_NAME}.jar

# include the jar files we need to link with
cp ./lib/*.jar ${WORKING_DIR_PACKAGER}

# yml files
mkdir -p ${WORKING_DIR_PACKAGER}/yml
cp yml/*.yml ${WORKING_DIR_PACKAGER}/yml/ 2>/dev/null

# language files
if [ -d "./language" ]; then
  mkdir -p ${WORKING_DIR_PACKAGER}/language
  cp ./language/*.properties ${WORKING_DIR_PACKAGER}/language/ 2>/dev/null
fi

# help files
if [ -d "./help" ]; then
  mkdir -p ${WORKING_DIR_PACKAGER}/help
  cp -r help/* ${WORKING_DIR_PACKAGER}/help/ 2>/dev/null
fi

# image files
if [ -d "./images" ]; then
  mkdir -p ${WORKING_DIR_PACKAGER}/images
  cp images/*.jpg images/*.png images/*.svg ${WORKING_DIR_PACKAGER}/images/ 2>/dev/null
fi

# job files
if [ -d "./jobs" ]; then
  mkdir -p ${WORKING_DIR_PACKAGER}/jobs
  cp jobs/* ${WORKING_DIR_PACKAGER}/jobs/ 2>/dev/null
fi

# Finally, ask Smart Packager to create a unprotected package using the JSONNET template & the WORKING_DIR_PACKAGER folder as archive .yip content
QT_PLUGIN_PATH=../SmartPackager/plugins LD_LIBRARY_PATH=../SmartPackager/lib \
    ../SmartPackager/SmartPackager --unprotected --package ${KC_NAME}-${VERSION//./_}.yip \
    --new ${KC_NAME}-yip-template.jsonnet --archive ${WORKING_DIR_PACKAGER}

rm -rf ${WORKING_DIR} ./packager.ini ./pendant.log
