@echo off
title RusDev Community Server Console
echo Starting RusDev Community Server.
echo.
java -Xms128m -Xmx128m -cp ./../libs/*;RusDev-community.jar com.l2js.communityserver.L2CommunityServer
pause
