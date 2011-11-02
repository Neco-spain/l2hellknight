@echo off
color 17
cls
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*;RusDev-login.jar com.l2js.gsregistering.BaseGameServerRegister -c
exit