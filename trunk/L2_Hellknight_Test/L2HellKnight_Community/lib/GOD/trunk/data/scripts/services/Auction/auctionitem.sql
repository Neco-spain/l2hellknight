/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2open
Target Host: localhost
Target Database: l2open
Date: 29.11.2011 20:19:59
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for auctionitem
-- ----------------------------
CREATE TABLE `auctionitem` (
  `char_id` int(11) NOT NULL DEFAULT '0',
  `Item_Id` int(11) NOT NULL DEFAULT '0',
  `TypeItem` varchar(20) DEFAULT NULL,
  `client` varchar(1000) DEFAULT NULL,
  `NameItem` varchar(45) DEFAULT NULL,
  `IdPrice` int(6) NOT NULL DEFAULT '0',
  `startPrice` int(11) NOT NULL DEFAULT '0',
  `price` int(11) NOT NULL DEFAULT '0',
  `IdItem` int(6) NOT NULL DEFAULT '0',
  `time` bigint(16) NOT NULL DEFAULT '0',
  `elemValue` int(4) NOT NULL DEFAULT '0',
  `elemType` int(4) NOT NULL DEFAULT '0',
  `att0` int(4) NOT NULL DEFAULT '0',
  `att1` int(4) NOT NULL DEFAULT '0',
  `att2` int(4) NOT NULL DEFAULT '0',
  `att3` int(4) NOT NULL DEFAULT '0',
  `att4` int(4) NOT NULL DEFAULT '0',
  `att5` int(4) NOT NULL DEFAULT '0',
  `Enchant` int(6) NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `auctionitem` VALUES ('268485111', '268488967', 'оружие', '', 'Elven Sword', '57', '1231', '1231', '130', '1322591516', '0', '-2', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `auctionitem` VALUES ('268485111', '268488966', 'оружие', '', 'Sword of Mystic', '57', '123123', '123123', '143', '1322591504', '0', '-2', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `auctionitem` VALUES ('268485111', '268488969', 'оружие', '', 'Knight\'s Sword', '57', '1231231', '1231231', '128', '1322591494', '0', '-2', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `auctionitem` VALUES ('268485111', '268488968', 'оружие', '', 'Sword of Revolution', '57', '1231231', '1231231', '129', '1322591499', '0', '-2', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `auctionitem` VALUES ('268485111', '268488965', 'оружие', '', 'Elven Long Sword', '57', '12312311', '12312311', '2499', '1322591490', '0', '-2', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `auctionitem` VALUES ('268485111', '268494262', 'оружие', '', 'Periwing Sword', '57', '177777', '177777', '15829', '1322591481', '20', '2', '0', '0', '0', '0', '0', '0', '2');
