@echo off
title L2RT server: Game Server Console
color 0A
:start
set user=root
set pass=
set DBname=l2rt
set DBHost=localhost
set ctime=%TIME:~0,2%
if "%ctime:~0,1%" == " " (
set ctime=0%ctime:~1,1%
)
set ctime=%ctime%'%TIME:~3,2%'%TIME:~6,2%
echo.
echo Making a full backup into %DATE%-%ctime%_backup_full.sql
echo.
..\sql\mysqldump.exe %Ignore% --add-drop-table -h %DBHost% -u %user% --password=%pass% %DBname% > backup/%DATE%-%ctime%_backup_full.sql
echo.
echo Backup complite %DATE%-%ctime%_backup_full.sql
echo.
echo %DATE% %TIME% Game server is running !!! > gameserver_is_running.tmp
echo Starting L2RT Game Server.
echo.
rem ======== Optimize memory settings =======
rem Minimal size with geodata is 1.5G, w/o geo 1G
rem Make sure -Xmn value is always 1/4 the size of -Xms and -Xmx.
rem -Xms and -Xmx should always be equal.
rem ==========================================
java -server -Dfile.encoding=UTF-8 -Xms2048m -Xmx4048m -cp lib/ccpGuard.jar;lib/*; l2rt.gameserver.GameStart > log.txt
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
del gameserver_is_running.tmp
pause
