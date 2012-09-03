DROP TABLE IF EXISTS `community_skillsave`;
CREATE TABLE `community_skillsave` (
  `charId` int(10) DEFAULT NULL,
  `skills` varchar(250) DEFAULT '',
  `pet` varchar(250) DEFAULT ''
) ENGINE = MYISAM;