@echo off
setlocal
set SHORTCUT_PATH=%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup\Desky-Server.lnk

if exist "%SHORTCUT_PATH%" (
    del /f /q "%SHORTCUT_PATH%"
    echo [Desky] Removed startup shortcut from:
    echo        "%SHORTCUT_PATH%"
) else (
    echo [Desky] No startup shortcut found.
)
endlocal
