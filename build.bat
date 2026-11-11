@echo off
rem Compileaza proiectul si creeaza out\lab5.jar (necesita JDK 11 sau mai nou).
cd /d "%~dp0"

if exist out rmdir /s /q out
mkdir out\classes

rem javac urmareste singur toate clasele referite plecand de la Main (inclusiv task-urile din registru).
javac -encoding UTF-8 -d out\classes -sourcepath src src\lab5\Main.java
if errorlevel 1 exit /b 1

> out\manifest.txt echo Main-Class: lab5.Main
jar cfm out\lab5.jar out\manifest.txt -C out\classes .
if errorlevel 1 exit /b 1

echo Gata: out\lab5.jar
echo Rulare: java -jar out\lab5.jar
