@echo off
cls
title L2 HellKnight - SQL Account Manager
color 0b
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*;l2hellknight_login.jar l2.hellknight.tools.accountmanager.SQLAccountManager 2> NUL
if %errorlevel% == 0 (
echo.
echo Execution succesful
echo.
) else (
echo.
echo An error has ocurred while running the L2 HellKnight Account Manager!
echo.
echo Possible reasons for this to happen:
echo.
echo - Missing .jar files or ../libs directory.
echo - MySQL server not running or incorrect MySQL settings:
echo    check ./config/loginserver.properties
echo - Wrong data types or values out of range were provided:
echo    specify correct values for each required field
echo.
)
pause
