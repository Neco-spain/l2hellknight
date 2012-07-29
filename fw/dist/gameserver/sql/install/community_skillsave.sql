CREATE TABLE `community_skillsave` (
  `charId` int(10) NOT NULL,
  `name` varchar(20) NOT NULL,
  `skills` varchar(255) NOT NULL,
  PRIMARY KEY (`charId`,`name`,`skills`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;