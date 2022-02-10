rmdir /s /q C:\Temp\demo-extension
mkdir C:\Temp\demo-extension
mkdir C:\Temp\demo-extension\images
mkdir C:\Temp\demo-extension\help
mkdir C:\Temp\demo-extension\jobs

copy /y images\*.png C:\Temp\demo-extension\images
copy /y images\*.jpg C:\Temp\demo-extension\images
copy /y images\*.svg C:\Temp\demo-extension\images
xcopy help C:\Temp\demo-extension\help /s /e
xcopy jobs C:\Temp\demo-extension\jobs /s /e
copy /y *.yml C:\Temp\demo-extension
copy /y java\lib\*.jar C:\Temp\demo-extension
copy /y *.jar C:\Temp\demo-extension
copy /y *.properties C:\Temp\demo-extension

SmartPackager --unprotected --package demo-extension-2_1.yip --new demo-extension-yip-template.jsonnet --archive C:\Temp\demo-extension