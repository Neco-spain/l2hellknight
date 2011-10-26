CREATE TABLE IF NOT EXISTS `quest_global_data` (
  `quest_name` VARCHAR(40) NOT NULL DEFAULT '',
  `var`  VARCHAR(20) NOT NULL DEFAULT '',
  `value` VARCHAR(255) ,
  PRIMARY KEY (`quest_name`,`var`)
) ENGINE = MYISAM DEFAULT CHARSET=utf8;