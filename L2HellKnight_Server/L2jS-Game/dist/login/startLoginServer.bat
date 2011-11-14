@echo off
title Login Server Console
color 0C
:start
echo Starting L2J Login Server.
echo.
java -Xms128m -Xmx128m  -cp ./../libs/*;l2js-login.jar com.l2js.loginserver.L2LoginServer
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
