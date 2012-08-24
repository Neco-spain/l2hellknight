@echo off
color 0b
cls
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*;l2hellknight_login.jar l2.hellknight.tools.gsregistering.BaseGameServerRegister -c
exit