@echo off
setlocal

set "CLASS_NAME=DemoExtension"
set "WORKING_DIR=%CD%\tmp"

set "WORKING_DIR_CLASSES=%WORKING_DIR%\classes"

if exist "%WORKING_DIR%" rmdir /s /q "%WORKING_DIR%"

mkdir "%WORKING_DIR_CLASSES%"

javac --release 11 -cp ".\lib\libthrift-0.11.0.jar;.\lib\slf4j-api.jar;.\lib\yaskawa-ext-4.0.3.jar" ^
    -d "%WORKING_DIR_CLASSES%" .\java\*.java

jar -cfe "%CLASS_NAME%.jar" %CLASS_NAME% -C "%WORKING_DIR_CLASSES%" .

if exist "%WORKING_DIR%" rmdir /s /q "%WORKING_DIR%"

endlocal