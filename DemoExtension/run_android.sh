#!/bin/sh

export ANDROID_DATA=$(pwd)
chmod 444 $ANDROID_DATA/DemoExtension.jar
mkdir -p $ANDROID_DATA/dalvik-cache
exec app_process -cp $ANDROID_DATA/DemoExtension.jar:. $ANDROID_DATA DemoExtension "$@"

