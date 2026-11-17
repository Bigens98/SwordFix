@echo off
echo Building SwordFix for Minecraft 1.12.2...
if not exist "bin" mkdir "bin"

javac --release 8 -proc:none -d bin src\main\java\bigmineworld\swordfix\*.java
copy src\main\resources\mcmod.info bin\ >nul
jar cfm SwordFix-1.12.2-1.0.0.jar src\main\resources\META-INF\MANIFEST.MF -C bin .

echo Build completed: SwordFix-1.12.2-1.0.0.jar
pause
