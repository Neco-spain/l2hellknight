-- ----------------------------
-- Table structure for `siegable_clanhall`
-- ----------------------------
DROP TABLE IF EXISTS `siegable_clanhall`;
CREATE TABLE `siegable_clanhall` (
  `clanHallId` int(10) NOT NULL default '0',
  `name` varchar(45) default NULL,
  `ownerId` int(10) default NULL,
  `desc` varchar(100) default NULL,
  `location` varchar(100) default NULL,
  `nextSiege` bigint(20) default NULL,
  `siegeInterval` int(10) default NULL,
  `siegeLenght` int(10) default NULL,
  `schedule_config` varchar(20) default NULL,
  PRIMARY KEY  (`clanHallId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of siegable_clanhall
-- ----------------------------
INSERT INTO `siegable_clanhall` VALUES ('21', 'Fortress of Resistance', '0', 'Contestable Clan Hall', 'Dion', '1318240800691', '604800000', '3600000', '7;0;0;12;00');
INSERT INTO `siegable_clanhall` VALUES ('34', 'Devastated Castle', '0', 'Contestable Clan Hall', 'Aden', '1318240800703', '604800000', '3600000', '7;0;0;12;00');
INSERT INTO `siegable_clanhall` VALUES ('35', 'Bandit StrongHold', '0', 'Contestable Clan Hall', 'Oren', '1318240800705', '604800000', '3600000', '7;0;0;12;00');
INSERT INTO `siegable_clanhall` VALUES ('62', 'Rainbow Springs', '0', 'Contestable Clan Hall', 'Goddard', '1318240800707', '604800000', '3600000', '7;0;0;12;00');
INSERT INTO `siegable_clanhall` VALUES ('63', 'Beast Farm', '0', 'Contestable Clan Hall', 'Rune', '1318240800709', '604800000', '3600000', '7;0;0;12;00');
INSERT INTO `siegable_clanhall` VALUES ('64', 'Fortresss of the Dead', '0', 'Contestable Clan Hall', 'Rune', '1318240800711', '604800000', '3600000', '7;0;0;12;00');