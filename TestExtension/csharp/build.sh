cd ../../csharp
msbuild SDK.csproj /t:build
cd ../TestExtension/csharp
msbuild TestExtension.csproj /t:build
