@echo off

cd /d %~dp0
set PATH=.\jre8\bin;%PATH%

java -Xmx1000m -jar halfNES.jar
pause