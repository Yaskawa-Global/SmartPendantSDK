@echo off
if exist gen-java rmdir /s /q gen-java
thrift -r --gen java:generated_annotations=suppress ..\extension.thrift
