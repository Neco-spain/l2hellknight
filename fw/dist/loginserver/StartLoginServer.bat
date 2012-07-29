@echo off
title Login Server
:start
echo Starting L2s LoginServer.
echo.
java -version:1.6 -server -Xbootclasspath/p:./loader.jar -Dfile.encoding=UTF-8 -Xms64m -Xmx128m -cp config;./* l2p.loginserver.LoginServer
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Server restarted ...
echo.
goto start
:error
echo.
echo Server terminated abnormaly ...
echo.
:end
echo.
echo Server terminated ...
echo.

pause
