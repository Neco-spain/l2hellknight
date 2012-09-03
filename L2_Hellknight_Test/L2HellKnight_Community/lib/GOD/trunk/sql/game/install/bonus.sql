CREATE TABLE IF NOT EXISTS `bonus` (
  `obj_id` int NOT NULL default 0,
  `bonus_name` varchar(30) NOT NULL default '',
  `bonus_value` int NOT NULL default 0,
  `bonus_expire_time` int NOT NULL default 0,
  PRIMARY KEY  (`obj_id`,`bonus_name`)
) ENGINE=MyISAM;