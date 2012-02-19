@echo off
color 17
cls
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*;l2brick_login.jar l2.brick.gsregistering.BaseGameServerRegister -c
exit