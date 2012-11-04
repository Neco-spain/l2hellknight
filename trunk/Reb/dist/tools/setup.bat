@echo off
TITLE Rebellion Developers Team.
CLS
echo.
echo.--- Slect Your Language ---
echo.
echo.(1) = Russian.
echo.
echo.(2) = Englsh.
echo.
echo.---------------------------
echo.(e) = Exit.
set button=x
echo.
set /p button=Choice :
if /i %button%==1 ru.bat
if /i %button%==2 en.bat
if /i %button%==e exit
