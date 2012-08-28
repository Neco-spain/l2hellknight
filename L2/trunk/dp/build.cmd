@echo on
color 0F

:Step1
cls
echo.    #######################################################################
echo.    #  /  /  /  /                                              \   \   \  #
echo.    # /  /  /  /         Java Compilator           	     \   \   \ #
echo.    #/  /  /  /                                                  \   \   \#
echo.    #######################################################################
echo.
echo.      		     Core compilation.
echo. -----------------------------------------------------------------------


:CoreCompile
@cls
title Core Compiler
color 0B
echo.
echo Compilation process. Please wait...
del compile-datapack.log
ant -f build.xml -l compile-core.log
echo Compilation successful!!!
pause


:fullend
