#!/bin/sh
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*:hellknight_login.jar l2.hellknight.tools.accountmanager.SQLAccountManager
