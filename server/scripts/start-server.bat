@echo off
setlocal
set SCRIPT_DIR=%~dp0
set SERVER_DIR=%SCRIPT_DIR%..\
set WORKSPACE_DIR=%SCRIPT_DIR%..\..\
set JAR_PATH=%SERVER_DIR%build\libs\server-all.jar

rem Check if server is already running on port 8080
netstat -ano | findstr ":8080" | findstr "LISTENING" >nul 2>&1
if %errorlevel% equ 0 (
    echo [Desky] Server is already running on port 8080.
    exit /b 0
)

rem Build the fat JAR if not already present
if not exist "%JAR_PATH%" (
    echo [Desky] Server fat JAR not found. Building with Gradle...
    pushd "%WORKSPACE_DIR%"
    call gradlew.bat :server:shadowJar
    popd
)

if not exist "%JAR_PATH%" (
    echo [Desky] Error: Could not find or build "%JAR_PATH%"
    exit /b 1
)

echo [Desky] Starting Desky Ktor Server in background...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{ CommandLine = 'javaw -jar \"%JAR_PATH%\"' }" >nul 2>&1

echo [Desky] Server started successfully on port 8080.
endlocal
