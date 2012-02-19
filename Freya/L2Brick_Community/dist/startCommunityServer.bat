@echo off
title Community Server Console
echo Starting L2Brick Community Server.
echo.
java -Xms128m -Xmx128m -cp ./../libs/*;l2brick_community.jar l2.brick.communityserver.L2CommunityServer
pause
