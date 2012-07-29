#!/bin/bash

while :;
do
	java -server -Xbootclasspath/p:./loader.jar -Dfile.encoding=UTF-8 -Xmx1G -cp config:./* l2p.gameserver.GameServer > log/stdout.log 2>&1

	[ $? -ne 2 ] && break
	sleep 30;
done

