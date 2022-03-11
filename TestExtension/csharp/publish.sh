cd ../../csharp
msbuild SDK.csproj /t:build
cd ../TestExtension/csharp
dotnet publish -r linux-arm --self-contained true
