@echo off
setlocal
echo [Desky] Checking for server process on port 8080...
set FOUND=0
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080" ^| findstr "LISTENING"') do (
    set PID=%%a
    if defined PID (
        echo [Desky] Stopping PID %%a on port 8080...
        taskkill /F /PID %%a >nul 2>&1
        set FOUND=1
    )
)

if "%FOUND%"=="1" (
    echo [Desky] Server stopped successfully.
) else (
    echo [Desky] No server running on port 8080.
)
endlocal
