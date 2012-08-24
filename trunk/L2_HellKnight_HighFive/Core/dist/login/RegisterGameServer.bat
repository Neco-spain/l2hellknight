@echo off
title L2HellKnight - Register Game Server
color a
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*;l2hellknight_loghin.jar l2.hellknight.tools.gsregistering.BaseGameServerRegister -c
pause