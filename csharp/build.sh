if [ ! -d gen-netstd ]; then
  thrift -r --gen netstd ../extension.thrift
fi
dotnet build SDK.csproj --configuration Release
