@echo off
setlocal
set MVN_VERSION=3.9.11
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  mvn -f "%~dp0pom.xml" %*
  exit /b %ERRORLEVEL%
)
set MAVEN_HOME=%~dp0.mvn\wrapper\apache-maven-%MVN_VERSION%
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  set ZIP=%~dp0.mvn\wrapper\apache-maven-%MVN_VERSION%-bin.zip
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$u='https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MVN_VERSION%/apache-maven-%MVN_VERSION%-bin.zip'; Invoke-WebRequest -Uri $u -OutFile '%~dp0.mvn\wrapper\apache-maven-%MVN_VERSION%-bin.zip'; Expand-Archive -Force '%~dp0.mvn\wrapper\apache-maven-%MVN_VERSION%-bin.zip' '%~dp0.mvn\wrapper'; Remove-Item '%~dp0.mvn\wrapper\apache-maven-%MVN_VERSION%-bin.zip'"
  if errorlevel 1 exit /b 1
)
call "%MAVEN_HOME%\bin\mvn.cmd" -f "%~dp0pom.xml" %*
exit /b %ERRORLEVEL%
