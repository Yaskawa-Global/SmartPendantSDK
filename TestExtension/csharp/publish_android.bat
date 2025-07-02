cd ..\..\csharp
dotnet publish --runtime linux-bionic-arm64
cd ..\TestExtension\csharp
dotnet publish -r linux-bionic-arm64 --self-contained true
