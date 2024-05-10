@echo off
REM Set variables for directories

set working_dir=C:\Users\Aina\Desktop\ject\Mr Naina\Sprint0_2527
set local=C:\Users\Aina\Desktop\ject\Mr Naina\Sprint0_2527
set webapps=C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps
set OUT_LIB=C:\Users\Aina\Desktop\ject\Mr Naina\Sprint0_2527\lib
set name=Sprinttest
set OUT_JAR=FrontServelet.jar


REM Supprimer le dossier temporaire s'il existe déjà
if exist "%local%\temp" (
    rmdir /s /q "%local%\temp"
)

REM Créer un nouveau dossier temporaire dans le dossier local
mkdir "%local%\temp"
mkdir "%local%\temp\WEB-INF"
mkdir "%local%\temp\WEB-INF\lib"

REM Copier les fichiers et répertoires depuis working_dir vers temp
copy "%working_dir%\*.xml" "%local%\temp\WEB-INF"

REM Copier le contenu du répertoire lib vers temp/WEB-INF/lib
copy "%working_dir%\lib\*" "%local%\temp\WEB-INF\lib"

REM Copier le fichier JAR dans le répertoire temporaire
copy "%OUT_LIB%\%OUT_JAR%" "%local%\temp\WEB-INF\lib"

REM Vérifier si la copie a réussi
if %errorlevel% equ 0 (
    echo JAR file copied successfully to WEB-INF\lib directory.
) else (
    echo Failed to copy JAR file. Please check errors.
    exit /b 1
)


jar cvf "%local%\%name%.war" -C "%local%\temp" .
move "%local%\%name%.war" "%webapps%"
