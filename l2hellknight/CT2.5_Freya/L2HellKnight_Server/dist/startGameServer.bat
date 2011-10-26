@echo off
title Game Server Console
:start
echo Starting l2hellknight Game Server.
echo.
REM -------------------------------------
REM Default parameters for a basic server.
java -Djava.util.logging.manager=l2.hellknight.util.L2LogManager -Xms1024m -Xmx1024m -cp ./../libs/*;l2hellknight_server.jar l2.hellknight.gameserver.GameServer
REM
REM If you have a big server and lots of memory, you could experiment for example with
REM java -server -Xmx1536m -Xms1024m -Xmn512m -XX:PermSize=256m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts
REM If you are having troubles on server shutdown (saving data),
REM add this to startup paramethers: -Djava.util.logging.manager=l2.hellknight.L2LogManager. Example:
REM java -Djava.util.logging.manager=l2.hellknight.util.L2LogManager -Xmx1024m -cp ./../libs/*;l2hellknight_server.jar l2.hellknight.gameserver.GameServer
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
