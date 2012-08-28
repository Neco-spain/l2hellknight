#!/bin/bash

# exit codes of GameServer:
#  0 normal shutdown
#  2 reboot attempt
echo -n "Starting GameServer "

while :; do
     [ -f log/java0.log.0 ] && mv log/java0.log.0 "log/`date +%Y-%m-%d_%H-%M-%S`_java.log"
     [ -f log/stdout.log ] && mv log/stdout.log "log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
      java -server -Xmx1024m -Xms1024m -Xmn512m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts -cp ./libraries/bsh-engine.jar:./libraries/gGuard.jar:./libraries/bsh-2.0b5.jar:./libraries/javolution.jar:./libraries/c3p0-0.9.1.2.jar:./libraries/mysql-connector-java-5.1.6-bin.jar:./libraries/servercore.jar:./libraries/jython.jar:./libraries/jython-engine.jar:./libraries/java-engine.jar:./libraries/trove.jar:./libraries/commons-logging.jar:./libraries/javax.servlet.jar:./libraries/org.mortbay.jetty.jar:./libraries/rrd4j-2.0.1.jar net.sf.l2j.gameserver.GameServer > log/stdout.log 2>&1
     [ $? -ne 2 ] && break
#     /etc/init.d/mysql restart
     sleep 10
done
