#!/bin/sh
java -Xms128m -Xmx128m -cp ./../libs/*:l2brick_community.jar l2.brick.communityserver.L2CommunityServer > log/stdout.log 2>&1
