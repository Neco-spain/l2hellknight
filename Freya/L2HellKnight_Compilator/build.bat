@echo off
@setlocal
COLOR 17
TITLE L2HellKnight Compiler


:: CORE & DATAPACK folders:
SET CORE_DIR=..\L2HellKnight_Server
SET DP_DIR=..\L2HellKnight_DataPack
SET CP_DIR=..\L2HellKnight_Community
SET L2J_UNV_ZIP_NAME=L2HellKnight.zip
:: EXAMPLE PATH
:: SET JAVA_PATH="C:\Program Files\Java\jdk1.7.0"
SET JAVA_PATH=""

:: ============================================================= ::
:: Options:
::   - ON   = 1
::   - OFF  = 0
:: modify on your own risk
:: PACK ON will create a pack of all archive files in one archive
:: Default: OFF (0)
SET PACK=0
:: DEBUG ON will show only logs on screen
:: Default: OFF (0)
SET DEBUG=0
:: LOGS ON will create log file in logs folder for all compilations
:: Default: OFF (0)
SET LOGS=1
:: C3P0 ON will replace boneCP connection pool to c3p0
:: Default: OFF (0)
set C3P0=0
:: ============================================================= ::
::
SET CORE=%CORE_DIR%\build.xml
SET DATAPACK=%DP_DIR%\build.xml
SET COMMUNITY=%CP_DIR%\build.xml
::
SET cb=0
SET db=0
SET cp=0

IF %C3P0% == 0 GOTO antchecker
::
IF NOT EXIST lib\c3p0\L2DatabaseFactory.java GOTO antchecker
IF NOT EXIST lib\c3p0\c3p0_LICENSE.txt GOTO antchecker
IF NOT EXIST lib\c3p0\c3p0-0.9.1.2.jar GOTO antchecker
IF NOT EXIST lib\c3p0\build.xml GOTO antchecker

COPY /Y lib\c3p0\L2DatabaseFactory.java %CORE_DIR%\java\l2\hellknight\L2DatabaseFactory.java
COPY /Y lib\c3p0\build.xml %CORE%
COPY /Y lib\c3p0\c3p0_LICENSE.txt %CORE_DIR%\lib\c3p0_LICENSE.txt
COPY /Y lib\c3p0\c3p0-0.9.1.2.jar %CORE_DIR%\lib\c3p0-0.9.1.2.jar
IF EXIST %CORE_DIR%\lib\bonecp-0.6.7.2.jar DEL %CORE_DIR%\lib\bonecp-0.6.7.2.jar
IF EXIST %CORE_DIR%\lib\guava-r06.jar DEL %CORE_DIR%\lib\guava-r06.jar
IF EXIST %CORE_DIR%\lib\junit-3.8.1.jar DEL %CORE_DIR%\lib\junit-3.8.1.jar
IF EXIST %CORE_DIR%\lib\slf4j-api-1.6.99.jar DEL %CORE_DIR%\lib\slf4j-api-1.6.99.jar
IF EXIST %CORE_DIR%\lib\slf4j-simple-1.6.99.jar DEL %CORE_DIR%\lib\slf4j-simple-1.6.99.jar
CLS
::
:antchecker
IF NOT "%ANT_HOME%" =="" GOTO next

:next
if NOT EXIST "%JAVA_HOME%"\lib\tools.jar GOTO check_path
goto gotAntHome

:check_path
if NOT EXIST %JAVA_HOME%\lib\tools.jar GOTO jne
SET JAVA_HOME="%JAVA_PATH%"
goto gotAntHome

:jne
ECHO             ^=^=^=^=^=^=^=^=^=^=^=^=
ECHO             ^= ERROR!!! ^=
ECHO             ^=^=^=^=^=^=^=^=^=^=^=^=
ECHO.
echo     JAVA JDK not exists. 
echo     Set path to JAVA JDK into JAVA_PATH in this script file or 
echo     set JAVA_HOME variable into system variables.
goto end

:gotAntHome
SET PATH=%CD%\bin;%PATH%
GOTO core_build

:core_build
ECHO             ^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=
ECHO             ^= CORE COMPILATION ^=
ECHO             ^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=
ECHO.
IF %DEBUG% == 1 PAUSE>NUL&GOTO community_build
IF %LOGS% == 1 GOTO cp1l1
GOTO cp1l0
:cp1l1
IF EXIST %CORE% ( CALL ant.bat -buildfile %CORE% -l logs\core.log&SET cb=1 ) ELSE ( ECHO ERROR !!! %CORE% NOT EXIST&PAUSE>NUL )
GOTO community_build
:cp1l0
IF EXIST %CORE% ( CALL ant.bat -buildfile %CORE%&SET cb=1 ) ELSE ( ECHO ERROR !!! %CORE% NOT EXIST&PAUSE>NUL )
GOTO community_build

:community_build
CLS
ECHO           ^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=
ECHO           ^= COMMUNITY COMPILATION ^=
ECHO           ^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=
ECHO.
IF %DEBUG% == 1 PAUSE>NUL&GOTO datapack_build
IF %LOGS% == 1 GOTO cp2l1
GOTO cp2l0
:cp2l1
IF EXIST %COMMUNITY% ( CALL ant.bat -buildfile %COMMUNITY% -l logs\community.log&SET cp=1 ) ELSE ( ECHO ERROR !!! %COMMUNITY% NOT EXIST&PAUSE>NUL )
GOTO datapack_build
:cp2l0
IF EXIST %COMMUNITY% ( CALL ant.bat -buildfile %COMMUNITY%&SET cp=1 ) ELSE ( ECHO ERROR !!! %COMMUNITY% NOT EXIST&PAUSE>NUL )
GOTO datapack_build

:datapack_build
CLS
ECHO           ^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=
ECHO           ^= DATAPACK COMPILATION ^=
ECHO           ^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=
ECHO.
IF %DEBUG% == 1 PAUSE>NUL&GOTO log_info
IF %LOGS% == 1 GOTO cp3l1
GOTO cp3l0
:cp3l1
IF EXIST %DATAPACK% ( CALL ant.bat -buildfile %DATAPACK% -l logs\datapack.log&SET db=1 ) ELSE ( ECHO ERROR !!! %DATAPACK% NOT EXIST&PAUSE>NUL )
GOTO log_info
:cp3l0
IF EXIST %DATAPACK% ( CALL ant.bat -buildfile %DATAPACK%&SET db=1 ) ELSE ( ECHO ERROR !!! %DATAPACK% NOT EXIST&PAUSE>NUL )
GOTO log_info

:log_info
CLS
ECHO           ^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=
ECHO           ^=                 L2HellKnight ^=
ECHO           ^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=
ECHO.
GOTO check_b1

:check_b1
IF %DEBUG% == 1 GOTO build_succ_1
IF %cb% == 0 GOTO build_unsucc_1
GOTO build_succ_1

:check_b2
IF %DEBUG% == 1 GOTO build_succ_2
IF %cp% == 0 GOTO build_unsucc_2
GOTO build_succ_2

:check_b3
IF %DEBUG% == 1 GOTO build_succ_3
IF %db% == 0 GOTO build_unsucc_3
GOTO build_succ_3

:check_pack
IF %PACK% == 0 GOTO end
GOTO full_pack

:full_pack
IF %DEBUG% == 1 GOTO fp1
IF EXIST %L2J_HELLKNIGHT_ZIP_NAME% DEL %L2J_HELLKNIGHT_ZIP_NAME%

IF NOT EXIST 7za.exe GOTO c1
7za.exe a %L2J_HELLKNIGHT_ZIP_NAME% ..\builds\core\Core.zip ..\builds\community\L2HellKnight-community.zip ..\builds\datapack\DataPack.zip>NUL
GOTO fp1

:c1
IF NOT EXIST bin\7za.exe GOTO end
bin\7za.exe a %L2J_HELLKNIGHT_ZIP_NAME% ..\builds\core\Core.zip ..\builds\community\L2HellKnight-community.zip ..\builds\datapack\DataPack.zip>NUL
GOTO fp1



:fp1
ECHO.
ECHO     ^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=^=
ECHO     CREATED ZIP FILE: 
ECHO        [^>^>   %L2J_HELLKNIGHT_ZIP_NAME%   ^<^<] 
ECHO     IN: 
ECHO        [ %CD% ]
GOTO end

:build_succ_1
ECHO     [ + ] CORE COMPILED SUCCESSFULLY
GOTO check_b2

:build_unsucc_1
ECHO     [ - ] WAS TROUBLES WITH COMPILE CORE SOURCES
GOTO check_b2

:build_succ_2
ECHO     [ + ] COMMUNITY COMPILED SUCCESSFULLY
GOTO check_b3

:build_unsucc_2
ECHO     [ - ] WAS TROUBLES WITH COMPILE COMMUNITY SOURCES
GOTO check_b3

:build_succ_3
ECHO     [ + ] DATAPACK COMPILED SUCCESSFULLY
GOTO check_pack

:build_unsucc_3
ECHO     [ - ] WAS TROUBLES WITH COMPILE DATAPACK SOURCES
GOTO check_pack

:full_pack_err
ECHO     [ - ] WAS TROUBLES WITH FULL PACK CREATE
GOTO end

:end
PAUSE>NUL
