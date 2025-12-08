@echo off
setlocal enabledelayedexpansion

rem Build (fat JAR) first: mvn clean package
set APP_NAME=SwingBank
set APP_VERSION=1.0.0
set INPUT_DIR=target
set MAIN_JAR=swing-bank-1.0.0-shaded.jar
set MAIN_CLASS=com.bank.App
set OUTPUT_DIR=target\installer

if not exist "%INPUT_DIR%\%MAIN_JAR%" (
  echo Jar %INPUT_DIR%\%MAIN_JAR% not found. Run "mvn clean package" first.
  exit /b 1
)

jpackage ^
  --name %APP_NAME% ^
  --app-version %APP_VERSION% ^
  --input %INPUT_DIR% ^
  --main-jar %MAIN_JAR% ^
  --main-class %MAIN_CLASS% ^
  --type exe ^
  --dest %OUTPUT_DIR% ^
  --vendor "Swing Bank" ^
  --copyright "2025 Swing Bank" ^
  --add-modules java.base,java.desktop,java.sql ^
  --jlink-options "--strip-debug --no-header-files --no-man-pages --compress=2"

echo Created installer in %OUTPUT_DIR%

