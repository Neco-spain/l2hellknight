#!/bin/sh
java -Xms128m -Xmx128m -cp ./../libs/*:l2hellknight_community.jar l2.hellknight.communityserver.L2CommunityServer > log/stdout.log 2>&1