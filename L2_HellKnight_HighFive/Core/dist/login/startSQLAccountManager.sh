#!/bin/sh
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*:l2hellknightlogin.jar l2.hellknight.tools.accountmanager.SQLAccountManager
