CREATE TABLE IF NOT EXISTS `clan_news` (
  `clan_id` INT NOT NULL default 0,
  `message` varchar(500) NOT NULL,
  PRIMARY KEY  (`clan_id`)
) DEFAULT CHARSET=utf8;