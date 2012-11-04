#!/bin/bash

while :;
do
java -Xbootclasspath/p:./jsr167.jar -server -Dfile.encoding=UTF-8 -Xmx1G -cp config:./libs/* l2r.gameserver.GameServer > log/stdout.log 2>&1

	[ $? -ne 2 ] && break
	sleep 30;
done

