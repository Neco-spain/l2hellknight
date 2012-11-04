@Echo off
cls
echo ��� �ਢ������ �ਯ� ��⠭���� �ࢥ� �� ������� Rebellion-Team.
echo ��� �ਯ� ��� ������� ��⠭����� ���� ������ �ࢥ�.
echo ��� �த������� ������ �஡��, ��� �����襭�� Ctrl+C
pause > nul
echo ======================================================================
echo �믮������ �஢�ઠ ���㦥���...
mysql --help >nul 2>nul
if errorlevel 1 goto nomysql
echo  - MySQL...       ok
echo ======================================================================
echo ��ࢥ� �� Rebellion-Team ��⮢ � ��⠭����. 
echo ��������, �ந������ ��砫��� ���䨣����...
echo ======================================================================
set DO_INSTALL=Y
set /P DO_INSTALL=��⠭����� �����-�ࢥ�[Y/n]
if "%DO_INSTALL%"=="N" goto installgame
if "%DO_INSTALL%"=="n" goto installgame
set INSTALL_MODE=login
:prepare
set DB_HOST=localhost
set DB_USER=root
set DB_PASSWORD=password
set DB_NAME=l2r
:step2

set /P DB_HOST=��ࢥ� �� [%DB_HOST%]:

set /P DB_USER=���짮��⥫� �� [%DB_USER%]:

set /P DB_PASSWORD=��஫� ���짮��⥫� %DB_USER%:

set /P DB_NAME=��� �� [%DB_NAME%]:
SET MYSQL_PARAM=-u %DB_USER% -h %DB_HOST%
if NOT "%DB_PASSWORD%"=="" SET MYSQL_PARAM=%MYSQL_PARAM% --password=%DB_PASSWORD%
echo exit | mysql %MYSQL_PARAM% >nul 2>nul
if errorlevel 1 goto dberror
echo exit | mysql %MYSQL_PARAM% %DB_NAME% >nul 2>nul
if errorlevel 1 goto dbnotexists
goto install
:dbnotexists
echo  ! ���� ������ %DB_NAME% �� �������
set ANSWER=Y
set /P ANSWER=������� �� [Y/n]?
if "%ANSWER%"=="y" goto createdb
if "%ANSWER%"=="Y" goto createdb
goto step2
:createdb
echo create database %DB_NAME% charset=utf8; | mysql %MYSQL_PARAM%
if errorlevel 1 goto dberror
goto install
:dberror
echo  ! �� ���� ���������� � ��. �஢���� �ࠢ��쭮��� ��ࠬ��஢
goto step2

:install
cls
echo ======================================================================
echo �஢���� �ࠢ��쭮��� ��������� ��ࠬ��஢
echo   - ��ࢥ� �㤥� ��⠭����� � %INSTALL_DIR%
echo   - ��ࢥ� ���� ������ %DB_HOST%
echo   - ��� ���� ������ %DB_NAME%
set ANSWER=Y
set /P ANSWER=�� ��ࠬ���� 㪠���� ��୮ [Y/n]?
if "%ANSWER%"=="n" goto step1
if "%ANSWER%"=="N" goto step1
echo - ��⠭�������� ��, ��������...
for %%i in (sql\%INSTALL_MODE%\*.sql) do mysql %MYSQL_PARAM% %DB_NAME% < %%i
if "%INSTALL_MODE%"=="login" goto installgame
goto end
:installgame
cls
set DO_INSTALL=Y
set /P DO_INSTALL=��⠭����� ����-�ࢥ�[Y/n]
if "%DO_INSTALL%"=="N" goto end
if "%DO_INSTALL%"=="n" goto end
set INSTALL_MODE=server
goto prepare 
:nomysql
cls
echo  ! �⨫�� mysql ������㯭�
echo  ��������, �� mysql.exe ��室���� � ��६����� ���㦥��� PATH
echo  ��� � ⥪�饬 ��⠫��� � �ਯ⮬ ��⠭����
goto end
:end
cls
echo ======================================================================
echo ======================================================================
echo ��⠭���� �����襭�, ᯠᨡ� �� �롮� ��襣� �த�� ...
echo ======================================================================
echo ======================================================================
pause > nul