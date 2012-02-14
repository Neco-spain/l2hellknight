SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `vitality`
-- ----------------------------
DROP TABLE IF EXISTS `vitality`;
CREATE TABLE `vitality` (
  `account_name` varchar(45) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `vitality_points` int(5) unsigned DEFAULT '0',
  PRIMARY KEY (`account_name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of vitality
-- ----------------------------
