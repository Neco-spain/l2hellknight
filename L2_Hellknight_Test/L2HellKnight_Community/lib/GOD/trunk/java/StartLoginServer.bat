@echo off
title L2RT: Login Server Console
color 0A
:start
echo %DATE% %TIME% Login server is running !!! > login_is_running.tmp
echo Starting L2RT Login Server.
echo.
java -server -Xms128m -Xmx256m -cp lib/*; ccpGuard.login.Antibrute l2rt.loginserver.L2LoginStart
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
del login_is_running.tmp
pause