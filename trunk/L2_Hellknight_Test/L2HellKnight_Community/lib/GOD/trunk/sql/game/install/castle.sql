CREATE TABLE IF NOT EXISTS `castle` (
  `id` tinyint unsigned NOT NULL DEFAULT 0,
  `name` varchar(25) NOT NULL,
  `taxPercent` tinyint unsigned NOT NULL default 15,
  `treasury` bigint unsigned NOT NULL default 0,
  `siegeDate` int unsigned NOT NULL default 0,
  `siegeDayOfWeek` tinyint unsigned NOT NULL DEFAULT 1,
  `siegeHourOfDay` tinyint unsigned NOT NULL DEFAULT 20,
  `townId` tinyint unsigned NOT NULL default 0,
  `skills` varchar(32) NOT NULL default '0;0',
  `flags` varchar(32) NOT NULL default '0;0',
  `ownDate` int(11) NOT NULL default '0',
  PRIMARY KEY  (`name`),
  KEY `id` (`id`)
) ENGINE=MyISAM;

INSERT IGNORE INTO `castle` VALUES
(1,'Gludio',0,0,0,1,16,6,'593;1;600;1;606;1','1','0'),
(2,'Dion',0,0,0,1,16,8,'609;1;597;1;591;1','2','0'),
(3,'Giran',0,0,0,1,20,9,'592;1;601;1;610;1','3','0'),
(4,'Oren',0,0,0,1,20,10,'590;1;598;1;605;1','4','0'),
(5,'Aden',0,0,0,1,16,11,'596;1;602;1;608;1','5','0'),
(6,'Innadril',0,0,0,1,20,13,'595;1;599;1;607;1','6','0'),
(7,'Goddard',0,0,0,1,20,15,'590;1;591;1;603;1','7','0'),
(8,'Rune',0,0,0,1,16,14,'593;1;599;1;604;1','8','0'),
(9,'Schuttgart',0,0,0,1,16,16,'610;1;600;1;592;1','9','0');