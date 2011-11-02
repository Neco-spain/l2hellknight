#!/bin/sh
java -Xms128m -Xmx128m -cp ./../libs/*:RusDev-community.jar com.l2js.communityserver.L2CommunityServer > log/stdout.log 2>&1