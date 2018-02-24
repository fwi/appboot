@echo off
SETLOCAL EnableDelayedExpansion
set WORKDIR=%cd%
set SCRIPTDIR=%~dp0
cd %SCRIPTDIR%
java -Dapp.maven.test -jar ..\dependency\appboot.jar app.main.class=com.github.fwi.appboot.Demo app.boot.debug %*
cd %WORKDIR%
ENDLOCAL