@echo off
color 0A
title L2 RT
@java -Djava.util.logging.config.file=console.cfg -cp lib/*; l2open.accountmanager.SQLAccountManager
