@echo off

set PATH=%PATH%;%ProgramFiles%\MySQL\MySQL Server 5.1\bin

set USER=root
set PASS=1q2w3e
set DBNAME=l2sdb
set DBHOST=localhost

mysql -h %DBHOST% -u %USER% --password=%PASS% -Bse "use %DBNAME%" > nul 2>&1

if errorlevel 9009 goto notfound
if errorlevel 1 goto error
goto end

:error
echo Can't use %DBNAME%!
exit /b 1

:notfound
echo Can't find mysql binary!
exit /b 1

:END