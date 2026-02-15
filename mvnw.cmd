@REM Maven Wrapper startup script for Windows

@echo off
setlocal

set MAVEN_OPTS=-Xmx512m

if not "%JAVA_HOME%" == "" goto findJavaHome

set JAVACMD=java
goto execute

:findJavaHome
set JAVACMD=%JAVA_HOME%\bin\java.exe

:execute
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Maven not found. Please install Maven.
    echo Download Maven from: https://maven.apache.org/download.cgi
    exit /b 1
)

mvn %*
