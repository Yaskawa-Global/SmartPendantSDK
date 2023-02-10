# Setup of the Yaskawa build environment for csharp on Windows

## Generate Thrift source code

[Download the Thrift compiler (0.17.0  used here)](https://dlcdn.apache.org/thrift/0.17.0/thrift-0.17.0.exe)

Open Command Prompt

`cd SmartPendantSDK/csharp`

`path\to\thrift\thrift-0.17.0.exe -r -gen netstd ..\extension.thrift`

## Build the library

Open SDK.sln with Visual Studio

Verify ApacheThrift NuGet package is installed

Build

## Creating a new extension

In Visual Studio, create a new Console App project targeting .NET 6.0

Add the YaskawaExtension.dll that was built in the previous section as a dependancy

Install ApacheThrift NuGet package


## Adding the extension to a YIP

[Download .NET 6.0 Runtime for Arm32](https://dotnet.microsoft.com/en-us/download/dotnet/thank-you/runtime-6.0.13-linux-arm32-binaries)

Move the downloaded file to your extensions build folder

Create a new bash script in your extensions build folder *e.g. run.sh*

Add the following to the script (replacing 'MyExtension.dll' with your extension's name)

`#!/bin/bash`
`DOTNET_FILE=dotnet-runtime-6.0.13-linux-arm.tar.gz
`export DOTNET_ROOT=$(pwd)/.dotnet`
`mkdir -p "$DOTNET_ROOT" && tar zxf "$DOTNET_FILE" -C "$DOTNET_ROOT"`
`export PATH=$PATH:$DOTNET_ROOT:$DOTNET_ROOT/tools`
`chmod 777 ./MyExtension.dll`
`dotnet MyExtension.dll`

Be careful with the line endings if. If you're on Windows, you will need to convert to UNIX line endings. You can do this with Notepad++ by doing: **Edit > EOL Conversion > Unix (LF)**


List the *run.sh* script as the 'Executable File' when adding your extension to a YIP