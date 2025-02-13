dotnet publish --runtime linux-bionic-arm64 --self-contained true

:: Remove any old copy of temp folder
rmdir /s /q C:\Temp\test-extension-android

:: make a temp staging folder for the .yip archive and copy only the files we need on the pendant
mkdir C:\Temp\test-extension-android
copy /y bin\Release\netcoreapp8.0\linux-bionic-arm64\publish\* C:\Temp\test-extension-android

:: YML files
copy /y *.yml C:\Temp\test-extension-android

:: Update the path below with your local installation path of the SmartPackager
SmartPackager.exe --unprotected --package test-extension-2_0.yip --new demo-extension-yip-template-android.jsonnet --archive C:\Temp\test-extension-android
