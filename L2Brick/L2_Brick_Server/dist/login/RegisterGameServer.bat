@echo off
color 0b
cls
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*;l2brick_login.jar l2.brick.tools.gsregistering.BaseGameServerRegister -c
exit