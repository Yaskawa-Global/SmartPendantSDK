:: Remove any old copy of temp folder
rmdir /s /q C:\Temp\demo-extension

:: make a temp staging folder for the .yip archive and copy only the files we need on the pendant
mkdir C:\Temp\demo-extension
copy /y lib\*.jar C:\Temp\demo-extension
copy /y *.jar C:\Temp\demo-extension

:: YML files
mkdir c:\Temp\demo-extension\yml
copy /y yml\*.yml C:\Temp\demo-extension\yml

:: Language files
mkdir c:\Temp\demo-extension\language
copy /y language\*.properties C:\Temp\demo-extension\language

:: Help files
mkdir C:\Temp\demo-extension\help
xcopy help C:\Temp\demo-extension\help /s /e

:: Image files
mkdir C:\Temp\demo-extension\images
copy /y images\*.png C:\Temp\demo-extension\images
copy /y images\*.jpg C:\Temp\demo-extension\images
copy /y images\*.svg C:\Temp\demo-extension\images

:: Job files
mkdir C:\Temp\demo-extension\jobs
xcopy jobs C:\Temp\demo-extension\jobs /s /e

:: Update the path below with your local installation path of the SmartPackager
C:\PackagerWin\SmartPackager.exe --unprotected --package demo-extension-3_0.yip --new demo-extension-yip-template.jsonnet --archive C:\Temp\demo-extension