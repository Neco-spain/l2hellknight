@echo off
title Community Server Console
echo Starting L2HellKnight Community Server.
echo.
java -Xms128m -Xmx128m -cp ./../libs/*;l2hellknight_community.jar l2.hellknight.communityserver.L2CommunityServer
pause
