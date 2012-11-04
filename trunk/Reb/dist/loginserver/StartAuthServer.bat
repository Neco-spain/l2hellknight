@echo off
title Rebellion: Game Server Console
:start
echo Starting Rebellion GameServer.
echo.

REM ������ ������
set JAVA_OPTS=%JAVA_OPTS% -XX:PermSize=128m
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxPermSize=256m

REM ������� � �������� ���������� ������
REM ����������� �������� ��� ������� � ��������: 1.5G
REM ����������� �������� ��� ������� ��� �������: 1G
REM -Xms � -Xmx ������ ���� ������ ����� ���� �����.
set JAVA_OPTS=%JAVA_OPTS% -Xmn16m
set JAVA_OPTS=%JAVA_OPTS% -Xms64m
set JAVA_OPTS=%JAVA_OPTS% -Xmx64m

REM ��������� �������� ������ � �����������
set JAVA_OPTS=%JAVA_OPTS% -Xnoclassgc
set JAVA_OPTS=%JAVA_OPTS% -XX:+AggressiveOpts
set JAVA_OPTS=%JAVA_OPTS% -XX:TargetSurvivorRatio=90
set JAVA_OPTS=%JAVA_OPTS% -XX:SurvivorRatio=16
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxTenuringThreshold=12
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseParNewGC
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseConcMarkSweepGC
set JAVA_OPTS=%JAVA_OPTS% -XX:+CMSIncrementalMode
set JAVA_OPTS=%JAVA_OPTS% -XX:+CMSIncrementalPacing
set JAVA_OPTS=%JAVA_OPTS% -XX:+CMSParallelRemarkEnabled
REM ��� 64-������ ������ ����� -XX:+UseCompressedOops ��������� ������ ������ � ����� ������������������
REM set JAVA_OPTS=%JAVA_OPTS% -XX:+UseCompressedOops
set JAVA_OPTS=%JAVA_OPTS% -XX:UseSSE=3
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseFastAccessorMethods

java -Xbootclasspath/p:./jsr167.jar -server -Dfile.encoding=UTF-8 %JAVA_OPTS% -cp config;./libs/* l2r.loginserver.AuthServer

REM Debug ...
REM java -Dfile.encoding=UTF-8 -cp config;./libs/* -Xmx1G -Xnoclassgc -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=7456 l2r.gameserver.GameServer

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