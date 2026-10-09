@echo off
setlocal
set SCRIPT_DIR=%~dp0
set WORKSPACE_DIR=%SCRIPT_DIR%..\..\

echo [Desky] 1. Stopping running server...
call "%SCRIPT_DIR%stop-server.bat"

echo [Desky] 2. Packaging updated fat JAR with Gradle...
pushd "%WORKSPACE_DIR%"
call gradlew.bat :server:shadowJar
set BUILD_STATUS=%errorlevel%
popd

if %BUILD_STATUS% neq 0 (
    echo [Desky] Build failed! Server was not started.
    exit /b %BUILD_STATUS%
)

echo [Desky] 3. Starting updated server in background...
call "%SCRIPT_DIR%start-server.bat"

echo [Desky] Server rebuilt and restarted successfully!
endlocal
