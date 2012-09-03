DROP TABLE IF EXISTS `skill_trees_squad`;
CREATE TABLE `skill_trees_squad` (
  `skill_id` MEDIUMINT UNSIGNED NOT NULL,
  `level` SMALLINT(3) UNSIGNED NOT NULL,
  `name` VARCHAR(25) NOT NULL DEFAULT 'Clan Skill',
  `clan_lvl` TINYINT(2) UNSIGNED NOT NULL,
  `repCost` INT UNSIGNED NOT NULL,
  `itemId` MEDIUMINT UNSIGNED NOT NULL,
  `itemCount` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`skill_id`,`level`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

INSERT INTO skill_trees_squad VALUES
(611,1,'Fire Squad',8,12000,9910,8),
(611,2,'Fire Squad',9,17400,9910,11),
(611,3,'Fire Squad',10,24000,9911,2),
(612,1,'Water Squad',7,10400,9910,4),
(612,2,'Water Squad',8,12000,9910,8),
(612,3,'Water Squad',9,17400,9910,11),
(613,1,'Wind Squad',8,12000,9910,8),
(613,2,'Wind Squad',9,17400,9910,11),
(613,3,'Wind Squad',10,24000,9911,2),
(614,1,'Earth Squad',8,12000,9910,8),
(614,2,'Earth Squad',9,17400,9910,11),
(614,3,'Earth Squad',10,24000,9911,2),
(615,1,'Holy Squad',7,10400,9910,4),
(615,2,'Holy Squad',8,12000,9910,8),
(615,3,'Holy Squad',9,17400,9910,11),
(616,1,'Dark Squad',8,12000,9910,8),
(616,2,'Dark Squad',9,17400,9910,11),
(616,3,'Dark Squad',10,24000,9911,2);