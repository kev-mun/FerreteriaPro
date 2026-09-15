@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-26"
set "PATH=%JAVA_HOME%\bin;%PATH%"
call mvnw.cmd clean javafx:run
