#!/bin/bash

# 1. Initialize output directory
echo "Cleaning old generated files..."
rm -rf gen-csharp
mkdir -p gen-csharp/Guis/V1

# 2. Locate tools in NuGet packages
echo "Locating gRPC tools in NuGet cache..."
PROTOC_PATH=$(find ~/.nuget/packages/grpc.tools -name protoc | grep linux_x64 | head -n 1)
GRPC_PLUGIN=$(find ~/.nuget/packages/grpc.tools -name grpc_csharp_plugin | grep linux_x64 | head -n 1)

if [ -z "$PROTOC_PATH" ] || [ -z "$GRPC_PLUGIN" ]; then
    echo "ERROR: gRPC tools not found. Please run 'dotnet restore' first."
    exit 1
fi

echo "Using Protoc: $PROTOC_PATH"
echo "Using Plugin: $GRPC_PLUGIN"

# 3. Generate C# and gRPC code
echo "Generating C# and gRPC source files..."
$PROTOC_PATH --proto_path=.. \
             --csharp_out=./gen-csharp/Guis/V1 \
             --grpc_out=./gen-csharp/Guis/V1 \
             --plugin=protoc-gen-grpc=$GRPC_PLUGIN \
             ../Extension.proto

# 4. Verify file generation
echo "Verifying generated files in gen-csharp/Guis/V1:"
ls -R gen-csharp/Guis/V1

# 5. Build the project
echo "Starting dotnet build for Grpc.dll..."
dotnet build SDK.csproj --configuration Release
