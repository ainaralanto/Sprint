@echo off
rem Set variables for directories
set SRC_DIR=src
set LIB_DIR=lib
set OUT_JAR=FrontServlet.jar

rem Create lib directory if it doesn't exist
if not exist "%LIB_DIR%" (
    mkdir "%LIB_DIR%"
)

rem Compile Java classes
echo Compiling Java classes...
for /r "%SRC_DIR%" %%F in (*.java) do (
    javac -d "%LIB_DIR%" "%%F"
)

rem Check if compilation was successful
if %ERRORLEVEL% EQU 0 (
    echo Compilation successful.

    rem Create JAR file
    echo Creating JAR file...
    pushd "%LIB_DIR%"
    jar cf "%OUT_JAR%" .
    popd

    echo JAR file created: "%LIB_DIR%\%OUT_JAR%"
) else (
    echo Compilation failed. Please check errors.
)

rem Clean up
del sources.txt 2>NUL
