#! /bin/bash
rm -Rf gen-csharp
thrift -r --gen csharp ../extension.thrift
