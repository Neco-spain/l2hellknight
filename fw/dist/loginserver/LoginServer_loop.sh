#!/bin/bash

while :;
do
	java -server -Xbootclasspath/p:./loader.jar -Dfile.encoding=UTF-8 -Xmx64m -cp config:./* l2p.loginserver.LoginServer > log/stdout.log 2>&1

	[ $? -ne 2 ] && break
	sleep 10;
done
