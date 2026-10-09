@echo off
setlocal
set SCRIPT_DIR=%~dp0
set TARGET_BAT=%SCRIPT_DIR%start-server.bat
set SHORTCUT_PATH=%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup\Desky-Server.lnk

echo [Desky] Creating Windows Startup shortcut...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ws = New-Object -ComObject WScript.Shell; $s = $ws.CreateShortcut('%SHORTCUT_PATH%'); $s.TargetPath = '%TARGET_BAT%'; $s.WorkingDirectory = '%SCRIPT_DIR%'; $s.WindowStyle = 7; $s.Description = 'Desky Laptop Server Auto-Start'; $s.Save()"

if exist "%SHORTCUT_PATH%" (
    echo [Desky] Success! Startup shortcut created at:
    echo        "%SHORTCUT_PATH%"
) else (
    echo [Desky] Error: Failed to create shortcut.
)
endlocal
