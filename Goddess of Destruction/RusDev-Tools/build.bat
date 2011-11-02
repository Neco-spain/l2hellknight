@echo off
@setlocal
COLOR 0C
TITLE RusDev - http://rusdev.pp.ua

SET CORE_DIR=..\RusDev-Game
SET DP_DIR=..\RusDev-DataPack
SET CP_DIR=..\RusDev-Community
SET TO_DIR=..\RusDev-Tools
SET RusDev_ZIP=RusDev.zip
SET JAVA_PATH=""

:: ============================================================= ::
SET PACK=1

SET DEBUG=0

SET LOGS=1

set ANT=0
:: ============================================================= ::
::
SET CORE=%CORE_DIR%\build.xml
SET DATAPACK=%DP_DIR%\build.xml
SET COMMUNITY=%CP_DIR%\build.xml
SET TOOLS=%TO_DIR%\build.xml
::
SET cb=0
SET db=0
SET cp=0
SET to=0

IF %ANT% == 0 GOTO antchecker
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
ECHO ===============================================================================
ECHO ------------------------------------ ERROR ------------------------------------
ECHO ===============================================================================
ECHO.
echo     JAVA JDK not exists. 
echo     Set path to JAVA JDK into JAVA_PATH in this script file or 
echo     set JAVA_HOME variable into system variables.
goto end

:gotAntHome
SET PATH=%CD%\bin;%PATH%
GOTO core_build

:core_build
ECHO ===============================================================================
ECHO --------------------------------- RusDev GAME ---------------------------------
ECHO ===============================================================================
ECHO.
IF %DEBUG% == 1 PAUSE>NUL&GOTO community_build
IF %LOGS% == 1 GOTO cp1l1
GOTO cp1l0
:cp1l1
IF EXIST %CORE% ( CALL ant.bat -buildfile %CORE% -l logs\RusDev-Game.log&SET cb=1 ) ELSE ( ECHO ERROR !!! %CORE% NOT EXIST&PAUSE>NUL )
GOTO community_build
:cp1l0
IF EXIST %CORE% ( CALL ant.bat -buildfile %CORE%&SET cb=1 ) ELSE ( ECHO ERROR !!! %CORE% NOT EXIST&PAUSE>NUL )
GOTO community_build

:community_build
CLS
ECHO ===============================================================================
ECHO ------------------------------- RusDev COMMUNITY --------------------------------
ECHO ===============================================================================
ECHO.
IF %DEBUG% == 1 PAUSE>NUL&GOTO datapack_build
IF %LOGS% == 1 GOTO cp2l1
GOTO cp2l0
:cp2l1
IF EXIST %COMMUNITY% ( CALL ant.bat -buildfile %COMMUNITY% -l logs\RusDev-Community.log&SET cp=1 ) ELSE ( ECHO ERROR !!! %COMMUNITY% NOT EXIST&PAUSE>NUL )
GOTO datapack_build
:cp2l0
IF EXIST %COMMUNITY% ( CALL ant.bat -buildfile %COMMUNITY%&SET cp=1 ) ELSE ( ECHO ERROR !!! %COMMUNITY% NOT EXIST&PAUSE>NUL )
GOTO datapack_build

:datapack_build
CLS
ECHO ===============================================================================
ECHO -------------------------------- RusDev DATAPACK --------------------------------
ECHO ===============================================================================
ECHO.
IF %DEBUG% == 1 PAUSE>NUL&GOTO tools_build
IF %LOGS% == 1 GOTO cp3l1
GOTO cp3l0
:cp3l1
IF EXIST %DATAPACK% ( CALL ant.bat -buildfile %DATAPACK% -l logs\RusDev-DataPack.log&SET db=1 ) ELSE ( ECHO ERROR !!! %DATAPACK% NOT EXIST&PAUSE>NUL )
GOTO tools_build
:cp3l0
IF EXIST %DATAPACK% ( CALL ant.bat -buildfile %DATAPACK%&SET db=1 ) ELSE ( ECHO ERROR !!! %DATAPACK% NOT EXIST&PAUSE>NUL )
GOTO tools_build

:tools_build
CLS
ECHO ===============================================================================
ECHO ---------------------------------- RusDev TOOLS ---------------------------------
ECHO ===============================================================================
ECHO.
IF %DEBUG% == 1 PAUSE>NUL&GOTO log_info
IF %LOGS% == 1 GOTO cp4l1
GOTO cp4l0
:cp4l1
IF EXIST %TOOLS% ( CALL ant.bat -buildfile %TOOLS% -l logs\RusDev-Tools.log&SET to=1 ) ELSE ( ECHO ERROR !!! %TOOLS% NOT EXIST&PAUSE>NUL )
GOTO log_info
:cp4l0
IF EXIST %TOOLS% ( CALL ant.bat -buildfile %TOOLS%&SET to=1 ) ELSE ( ECHO ERROR !!! %TOOLS% NOT EXIST&PAUSE>NUL )
GOTO log_info

:log_info
CLS
ECHO ===============================================================================
ECHO ------------------------------------- RusDev ------------------------------------
ECHO ===============================================================================
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

:check_b4
IF %DEBUG% == 1 GOTO build_succ_4
IF %to% == 0 GOTO build_unsucc_4
GOTO build_succ_4

:check_pack
IF %PACK% == 0 GOTO end
GOTO full_pack

:full_pack
IF %DEBUG% == 1 GOTO fp1
IF EXIST %RusDev_ZIP% DEL %RusDev_ZIP%

IF NOT EXIST 7za.exe GOTO c1
7za.exe a %RusDev_ZIP% .\build\dist\RusDev-Server.zip .\build\dist\RusDev-Community.zip .\build\dist\RusDev-DataPack.zip>NUL
GOTO fp1

:c1
IF NOT EXIST dist\7za.exe GOTO end
dist\7za.exe a %RusDev_ZIP% .\build\dist\RusDev-Server.zip .\build\dist\RusDev-Community.zip .\build\dist\RusDev-DataPack.zip>NUL
GOTO fp1

:fp1
ECHO.
ECHO ===============================================================================
ECHO ------------------------------------ RusDev -----------------------------------
ECHO ===============================================================================
ECHO     CREATED ZIP FILE: 
ECHO        [^>^>   %RusDev_ZIP%   ^<^<] 
ECHO     IN: 
ECHO        [ %CD% ]
ECHO ===============================================================================
ECHO ----------------------------- PRESS ANY KEY TO EXIT ---------------------------
ECHO ===============================================================================
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
GOTO check_b4

:build_unsucc_3
ECHO     [ - ] WAS TROUBLES WITH COMPILE DATAPACK SOURCES
GOTO check_b4

:build_succ_4
ECHO     [ + ] TOOLS COMPILED SUCCESSFULLY
GOTO check_pack

:build_unsucc_4
ECHO     [ - ] WAS TROUBLES WITH COMPILE TOOLS SOURCES
GOTO check_pack

:full_pack_err
ECHO     [ - ] WAS TROUBLES WITH FULL PACK CREATE
GOTO end

:end
PAUSE>NUL
