@echo off
title Login Server Console
:start
echo Starting l2hellknight Login Server.
echo.
java -Xms128m -Xmx128m  -cp ./../libs/*;l2hellknight_login.jar l2.hellknight.loginserver.L2LoginServer
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin Restart ...
echo.
goto start
:error
echo.
echo Server terminated abnormaly
echo.
:end
echo.
echo server terminated
echo.
pause
