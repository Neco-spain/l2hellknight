CREATE TABLE IF NOT EXISTS `clan_skills` (
  `clan_id` int NOT NULL default 0,
  `skill_id` smallint unsigned NOT NULL default 0,
  `skill_level` tinyint unsigned NOT NULL default 0,
  `skill_name` varchar(26) default NULL,
  `squad_index` smallint(6) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`clan_id`,`skill_id`,`squad_index`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;