@echo off

rem %~dp0 is the directory of this script
rem No need to use "java -Dapp.name=appboot-demo" since directory already has correct name 

java -jar %~dp0lib\appboot.jar app.boot.debug %*

pause