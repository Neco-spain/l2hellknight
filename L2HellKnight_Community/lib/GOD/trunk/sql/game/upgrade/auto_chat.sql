SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `auto_chat`
-- ----------------------------
DROP TABLE IF EXISTS `auto_chat`;
CREATE TABLE `auto_chat` (
  `npcId` int(11) NOT NULL default '0',
  `chatDelay` int(11) NOT NULL default '-1',
  `npcStringId` varchar(60) NOT NULL,
  PRIMARY KEY  (`npcId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of auto_chat
-- ----------------------------
INSERT INTO `auto_chat` VALUES ('33284', '11', '811248,1811249');
INSERT INTO `auto_chat` VALUES ('33114', '12', '1032344,1032343,1032345');
INSERT INTO `auto_chat` VALUES ('32974', '10', '1811292,1032103');
INSERT INTO `auto_chat` VALUES ('32975', '10', '1811294,1032003');
INSERT INTO `auto_chat` VALUES ('33025', '10', '1032341,1032342,1032340');
INSERT INTO `auto_chat` VALUES ('33124', '10', '17178345');
INSERT INTO `auto_chat` VALUES ('33116', '11', '1811244');
INSERT INTO `auto_chat` VALUES ('33026', '10', '1032319,1032320');
INSERT INTO `auto_chat` VALUES ('33271', '15', '1811245');
INSERT INTO `auto_chat` VALUES ('33238', '14', '1811250');
INSERT INTO `auto_chat` VALUES ('33223', '15', '1811243');
INSERT INTO `auto_chat` VALUES ('33125', '10', '1032347,1032346');
INSERT INTO `auto_chat` VALUES ('32972', '10', '1811291');
