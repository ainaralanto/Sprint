@echo off

rem Directories
set TEMP_SRC=temp_src
set MY_CLASSES=classes

rem Creating temporary directory for sources
if exist "%TEMP_SRC%" (
    echo Destination directory already exists. Removing...
    rmdir /s /q "%TEMP_SRC%"
    echo Destination directory removed successfully.
)
mkdir "%TEMP_SRC%"
echo Temporary directory for source created

rem Creating temporary directory for classes
if exist "%MY_CLASSES%" (
    echo Destination directory already exists. Removing...
    rmdir /s /q "%MY_CLASSES%"
    echo Destination directory removed successfully.
)
mkdir "%MY_CLASSES%"
echo Temporary directory for .class created

rem Copying Java files to temporary directory
for /r src %%f in (*.java) do (
    copy "%%f" "%TEMP_SRC%"
)

rem Compiling Java files from source directory to classes directory
javac -d "%MY_CLASSES%" "%TEMP_SRC%\*.java"
if %ERRORLEVEL% NEQ 0 (
    echo Error occurred during compilation of Java files.
    exit /b 1
)
echo Java files compiled into classes directory

rem Creating jar file
jar cf "lib\FrontServelet.jar" -C "%MY_CLASSES%" .
if %ERRORLEVEL% NEQ 0 (
    echo Error occurred during jar file creation.
    exit /b 1
)
echo Jar file created successfully.

rem Cleaning up temporary directories
rmdir /s /q "%TEMP_SRC%"
rmdir /s /q "%MY_CLASSES%"
echo Temporary directories cleaned up
