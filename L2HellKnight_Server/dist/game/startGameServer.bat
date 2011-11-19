@echo off
title Game Server Console
color 0b
:start
echo Starting L2 HellKnight Game Server.
echo.
REM -------------------------------------
REM Default parameters for a basic server.
java -Djava.util.logging.manager=l2.hellknight.util.L2LogManager -Xms1024m -Xmx1024m -cp ./../libs/*;l2hellknight_server.jar l2.brick.gameserver.GameServer
REM
REM If you have a big server and lots of memory, you could experiment for example with
REM java -server -Xmx1536m -Xms1024m -Xmn512m -XX:PermSize=256m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts
REM -------------------------------------
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
