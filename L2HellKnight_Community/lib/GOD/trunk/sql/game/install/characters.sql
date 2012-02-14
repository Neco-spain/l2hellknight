CREATE TABLE IF NOT EXISTS `characters` (
  `account_name` VARCHAR(45) NOT NULL DEFAULT '',
  `obj_Id` int NOT NULL DEFAULT 0,
  `char_name` varchar(35) character set utf8 NOT NULL default '',
  `face` tinyint unsigned DEFAULT NULL,
  `hairStyle` tinyint unsigned DEFAULT NULL,
  `hairColor` tinyint unsigned DEFAULT NULL,
  `sex` BOOLEAN DEFAULT NULL,
  `heading` mediumint DEFAULT NULL,
  `x` mediumint DEFAULT NULL,
  `y` mediumint DEFAULT NULL,
  `z` mediumint DEFAULT NULL,
  `karma` int DEFAULT NULL,
  `pvpkills` int DEFAULT NULL,
  `pkkills` int DEFAULT NULL,
  `clanid` int DEFAULT NULL,
  `createtime` int unsigned NOT NULL DEFAULT 0,
  `deletetime` int unsigned NOT NULL DEFAULT 0,
  `title` varchar(16) character set utf8 default NULL,
  `rec_have` tinyint unsigned NOT NULL DEFAULT 0,
  `rec_left` tinyint unsigned NOT NULL DEFAULT 20,
  `rec_timeleft` int(4) NOT NULL DEFAULT 3600,
  `accesslevel` tinyint DEFAULT NULL,
  `online` BOOLEAN DEFAULT NULL,
  `onlinetime` int unsigned NOT NULL DEFAULT 0,
  `lastAccess` int unsigned NOT NULL DEFAULT 0,
  `leaveclan`  int unsigned NOT NULL DEFAULT 0,
  `deleteclan` int unsigned NOT NULL DEFAULT 0,
  `nochannel` int NOT NULL DEFAULT 0, -- not UNSIGNED, negative value means 'forever'
  `pledge_type` smallint NOT NULL DEFAULT 0,
  `pledge_rank` tinyint unsigned NOT NULL DEFAULT 0,
  `lvl_joined_academy` tinyint unsigned NOT NULL DEFAULT 0,
  `apprentice` int unsigned NOT NULL DEFAULT 0,
  `key_bindings` varbinary(8192) default NULL,
  `pcBangPoints` int NOT NULL DEFAULT 0,
  `vitality` smallint unsigned NOT NULL DEFAULT 10000,
  `fame` INT NOT NULL DEFAULT 0,
  `bookmarks` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (obj_Id),
  UNIQUE KEY `char_name` (`char_name`),
  KEY `account_name` (`account_name`),
  KEY `clanid` (`clanid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
