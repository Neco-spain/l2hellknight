DROP TABLE IF EXISTS `epic_boss_spawn`;

CREATE TABLE `epic_boss_spawn` (
  `bossId` smallint unsigned NOT NULL,
  `respawnDate` int NOT NULL,
  `state` int NOT NULL,
  PRIMARY KEY  (`bossId`)
) ENGINE=MyISAM;

INSERT INTO `epic_boss_spawn` (`bossId`, `respawnDate`, `state`) VALUES
(29019,'0',0),
(29020,'0',0),
(29028,'0',0),
(29062,'0',0),
(29065,'0',0),
(29099,'0',0);