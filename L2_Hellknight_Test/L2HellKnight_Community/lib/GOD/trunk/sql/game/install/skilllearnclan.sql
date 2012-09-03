SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for skilllearnclan
-- ----------------------------
CREATE TABLE `skilllearnclan` (
  `item_id` int(10) NOT NULL DEFAULT '0',
  `skill_id` int(10) unsigned NOT NULL DEFAULT '0',
  `level` int(10) unsigned NOT NULL DEFAULT '0',
  `name` varchar(50) NOT NULL DEFAULT '',
  `countItem` int(10) unsigned NOT NULL DEFAULT '0',
  `min_level` int(10) unsigned NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `skilllearnclan` VALUES ('57', '380', '1', 'Aegis Stance', '40000', '0');
INSERT INTO `skilllearnclan` VALUES ('57', '380', '2', '', '800000', '0');
