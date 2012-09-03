#!/bin/sh
java -Djava.util.logging.config.file=config/console.cfg -cp lib/*: l2rt.accountmanager.SQLAccountManager
