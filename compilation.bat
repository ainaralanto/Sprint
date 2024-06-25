@echo off
REM Set variables
set SRC_DIR=src
set OUT_DIR=out
set LIB_DIR=lib
set WEB_DIR=web
set WEB_INF_DIR=WEB-INF
set BUILD_DIR=build
set CLASSES_DIR=classes
set JAR_NAME=FrontServlet.jar
set WAR_NAME=Sprintko.war

REM Create necessary directories
mkdir %OUT_DIR%
mkdir %BUILD_DIR%
mkdir %BUILD_DIR%\%WEB_INF_DIR%
mkdir %BUILD_DIR%\%CLASSES_DIR%

REM Compile .java files
echo Compiling Java files...
javac -d %OUT_DIR% -cp "%LIB_DIR%/*" %SRC_DIR%\**\*.java

REM Create JAR from the mg package
echo Creating JAR from the mg package...
jar cvf %LIB_DIR%\%JAR_NAME% -C %OUT_DIR% mg

REM Copy compiled classes to the classes directory
echo Copying other packages to classes directory...
xcopy %OUT_DIR% %BUILD_DIR%\%CLASSES_DIR% /s /i /exclude:%OUT_DIR%\mg

REM Copy web files to the build directory
echo Copying web files...
xcopy %WEB_DIR%\* %BUILD_DIR%\ /s /i

REM Move web.xml to WEB-INF
echo Moving web.xml to WEB-INF...
move web.xml %BUILD_DIR%\%WEB_INF_DIR%

REM Copy compiled classes to WEB-INF/classes
echo Copying classes to WEB-INF/classes...
xcopy %BUILD_DIR%\%CLASSES_DIR% %BUILD_DIR%\%WEB_INF_DIR%\classes /s /i

REM Create the WAR file
echo Creating WAR file...
cd %BUILD_DIR%
jar cvf %WAR_NAME% *
cd ..

REM Move the WAR file to Tomcat webapps directory
echo Moving WAR file to Tomcat webapps directory...
move %BUILD_DIR%\%WAR_NAME% C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps\

REM Clean up
echo Cleaning up...
rd /s /q %BUILD_DIR%
echo Done.
pause
