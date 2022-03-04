# setup of the Yaskawa build environment for csharp

## Download dotnet core 2.2:

[direct link to download](https://download.visualstudio.microsoft.com/download/pr/022d9abf-35f0-4fd5-8d1c-86056df76e89/477f1ebb70f314054129a9f51e9ec8ec/dotnet-sdk-2.2.207-linux-x64.tar.gz)

extract and install the dotnet binary:

`mkdir -p $HOME/dotnet && tar zxf dotnet-sdk-2.2.207-linux-x64.tar.gz -C $HOME/dotnet`

Set default path for dotnet so it is found using the dotnet command:

<code>
export DOTNET_ROOT=$HOME/dotnet

export PATH=$PATH:$HOME/dotnet</code>

append `export DOTNET_ROOT=$HOME/dotnet` and `export PATH=$PATH:$HOME/.dotnet` to ~/.bashrc

## install required libraries for Apache Trift 0.12.0:

<code>sudo apt install flex bison libtool libtool-bin libboost-all-dev ant libssl-dev make g++ gnupg ca-certificates git -y</code>

add the mono repo to apt:

<code>
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 3FA7E0328081BFF6A14DA29AA6A19B38D3D831EF
echo "deb <https://download.mono-project.com/repo/ubuntu> stable-focal main" | sudo tee /etc/apt/sources.list.d/mono-official-stable.list</code>

load the repository changes:

sudo apt update

install mono-devel:

sudo apt install mono-devel -y

## get thrift-0.12.0:

`cd ~/Downloads`

`wget -C http://archive.apache.org/dist/thrift/0.12.0/thrift-0.12.0.tar.gz -O - | tar -xz`

### compile the Thrift IDL:
<code>
`cd thrift-0.12.0`

`./bootstrap.sh`

`./configure --without-java --without-python`

`make -j` # minimum requires 16 gigs of ram, omit -j if not having that much.

`make check`

`sudo make install`</code>

## start setup of code for project:

<code>
cd ~/Documents

git clone <https://github.com/JurgenKuyper/SmartPendantSDK>

cd SmartPendantSDK/csharp

thrift -r --gen csharp ../extension.thrift

cp ~/Downloads/thrift-0.12.0/lib/csharp/Thrift.dll ~/Documents/SmartPendantSDK/csharp
</code>

`msbuild SDK.csproj /t:build`

now if all went well you succesfully created your Yaskawa SDK.

you may create a new project using `dotnet new console`, then add the reference in the project to the YaskawaExtension.dll and Thrift.dll in SmartPendantSDK/csharp. you can use the TestExtension as a starting point to create your own projects.
