@echo off
color 17
cls
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*;l2hellknight_login.jar l2.hellknight.gsregistering.BaseGameServerRegister -c
exit