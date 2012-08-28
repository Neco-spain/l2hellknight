@echo off
title L2jsoftware Interlude Project - Game Server
:start
echo L2jsoftware Interlude Project Game Server
echo.
java -Xmx1024m -Xms1024m -cp ./libraries/bsh-engine.jar;./libraries/bsh-2.0b5.jar;./libraries/gGuard.jar;./libraries/javolution.jar;./libraries/c3p0-0.9.1.2.jar;./libraries/mysql-connector-java-5.1.6-bin.jar;./libraries/servercore.jar;./libraries/jython.jar;./libraries/jython-engine.jar;./libraries/java-engine.jar;./libraries/commons-logging.jar;./libraries/trove.jar;./libraries/javax.servlet.jar;./libraries/org.mortbay.jetty.jar;./libraries/rrd4j-2.0.1.jar net.sf.l2j.gameserver.GameServer
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Перезагрузка...
echo.
goto start
:error
echo.
echo Работа сервера завершена некорректно
echo.
:end
echo.
echo сервер отключен
echo.
pause
