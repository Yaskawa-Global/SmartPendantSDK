if [ ! -d gen-csharp ]; then
  thrift -r --gen csharp ../extension.thrift
fi
msbuild SDK.csproj /t:build
