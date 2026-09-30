@echo off
setlocal enabledelayedexpansion
set "JAVA_HOME=C:\Program Files\Java\jdk-26"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo [1/3] Compilando y empaquetando Fat JAR...
call .\mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 exit /b %errorlevel%

echo [2/3] Generando icono ICO...
powershell -Command "Add-Type -AssemblyName System.Drawing; $img = [System.Drawing.Image]::FromFile('src/main/resources/com/ferreteria/ferreteriapro/icon.png'); $bmp = New-Object System.Drawing.Bitmap($img, 256, 256); $hIcon = $bmp.GetHicon(); $icon = [System.Drawing.Icon]::FromHandle($hIcon); $fs = New-Object System.IO.FileStream('src/main/resources/com/ferreteria/ferreteriapro/icon.ico', [System.IO.FileMode]::Create); $icon.Save($fs); $fs.Close()"

echo [3/3] Creando Instalador EXE con jpackage...
jpackage --type exe ^
  --name "FerreteriaPro" ^
  --app-version "1.0.0" ^
  --vendor "Ferreteria Pro Enterprise" ^
  --dest "target/installer" ^
  --input "target" ^
  --main-jar "ferreteria-pro-1.0-SNAPSHOT.jar" ^
  --main-class "com.ferreteria.ferreteriapro.Launcher" ^
  --win-shortcut --win-menu --win-dir-chooser ^
  --icon "src/main/resources/com/ferreteria/ferreteriapro/icon.ico"

echo Proceso completado con exito.