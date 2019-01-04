#! /bin/bash
rm -Rf gen-java
thrift -r --gen java ../extension.thrift
