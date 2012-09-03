/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2open
Target Host: localhost
Target Database: l2open
Date: 28.11.2011 0:34:58
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
