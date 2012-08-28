@echo off
title L2jsoftware Login Server
:start
echo L2jsoftwareLogin Server
echo.
java -Xmx512m -Xms128m -cp ./libraries/javolution.jar;./libraries/c3p0-0.9.1.2.jar;./libraries/mysql-connector-java-5.1.6-bin.jar;./libraries/servercore.jar net.sf.l2j.loginserver.L2LoginServer
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
