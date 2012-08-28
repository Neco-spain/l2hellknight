#!/bin/bash

# exit codes of GameServer:
#  0 normal shutdown
#  2 reboot attempt

while :; do
     [ -f log/java_login0.log.0 ] && mv log/java_login0.log.0 "log/`date +%Y-%m-%d_%H-%M-%S`_java_login.log"
     [ -f log/stdout_login.log ] && mv log/stdout_login.log "log/`date +%Y-%m-%d_%H-%M-%S`_stdout_login.log"
     #java -Djava.util.logging.manager=net.sf.l2j.util.L2LogManager -Xms1024m -Xmx1024m -cp ./../libs/*:l2jserver.jar net.sf.l2j.gameserver.GameServer > log/stdout.log 2>&1
java -Xmx512m -Xms512m -cp ./libraries/javolution.jar:./libraries/c3p0-0.9.1.2.jar:./libraries/mysql-connector-java-5.1.6-bin.jar:./libraries/servercore.jar net.sf.l2j.loginserver.L2LoginServer  > log/stdout_login.log 2>&1
     [ $? -ne 2 ] && break
#     /etc/init.d/mysql restart
     sleep 10
done