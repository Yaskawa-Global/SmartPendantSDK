#! /bin/bash
rm -Rf gen-java
thrift -r --gen java:generated_annotations=suppress ../extension.thrift
