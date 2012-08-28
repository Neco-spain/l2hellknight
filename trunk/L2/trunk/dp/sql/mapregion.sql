/*
Navicat MySQL Data Transfer

Source Server         : home
Source Server Version : 50509
Source Host           : localhost:3306
Source Database       : eon

Target Server Type    : MYSQL
Target Server Version : 50509
File Encoding         : 65001

Date: 2011-05-28 21:05:31
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `mapregion`
-- ----------------------------
DROP TABLE IF EXISTS `mapregion`;
CREATE TABLE `mapregion` (
  `region` int(11) NOT NULL DEFAULT '0',
  `sec0` int(2) NOT NULL DEFAULT '0',
  `sec1` int(2) NOT NULL DEFAULT '0',
  `sec2` int(2) NOT NULL DEFAULT '0',
  `sec3` int(2) NOT NULL DEFAULT '0',
  `sec4` int(2) NOT NULL DEFAULT '0',
  `sec5` int(2) NOT NULL DEFAULT '0',
  `sec6` int(2) NOT NULL DEFAULT '0',
  `sec7` int(2) NOT NULL DEFAULT '0',
  `sec8` int(2) NOT NULL DEFAULT '0',
  `sec9` int(2) NOT NULL DEFAULT '0',
  PRIMARY KEY (`region`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of mapregion
-- ----------------------------
INSERT INTO `mapregion` VALUES ('0', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4');
INSERT INTO `mapregion` VALUES ('1', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4');
INSERT INTO `mapregion` VALUES ('2', '3', '3', '7', '9', '11', '4', '4', '4', '4', '4');
INSERT INTO `mapregion` VALUES ('3', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4');
INSERT INTO `mapregion` VALUES ('4', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4');
INSERT INTO `mapregion` VALUES ('5', '3', '3', '3', '3', '3', '16', '16', '16', '15', '15');
INSERT INTO `mapregion` VALUES ('6', '3', '3', '3', '3', '3', '16', '16', '16', '15', '15');
INSERT INTO `mapregion` VALUES ('7', '3', '3', '3', '3', '3', '14', '14', '15', '15', '15');
INSERT INTO `mapregion` VALUES ('8', '3', '3', '3', '14', '14', '14', '14', '15', '15', '15');
INSERT INTO `mapregion` VALUES ('9', '2', '2', '2', '2', '18', '14', '9', '9', '10', '10');
INSERT INTO `mapregion` VALUES ('10', '2', '2', '2', '2', '2', '9', '9', '10', '10', '10');
INSERT INTO `mapregion` VALUES ('11', '2', '2', '2', '2', '1', '1', '9', '11', '10', '10');
INSERT INTO `mapregion` VALUES ('12', '6', '6', '2', '5', '1', '1', '9', '11', '11', '11');
INSERT INTO `mapregion` VALUES ('13', '6', '6', '5', '5', '7', '7', '8', '8', '8', '8');
INSERT INTO `mapregion` VALUES ('14', '6', '6', '6', '5', '7', '7', '8', '8', '8', '8');
INSERT INTO `mapregion` VALUES ('15', '0', '6', '6', '5', '17', '12', '13', '13', '13', '13');
INSERT INTO `mapregion` VALUES ('16', '0', '0', '6', '6', '12', '12', '13', '13', '13', '13');
INSERT INTO `mapregion` VALUES ('17', '0', '0', '0', '0', '0', '0', '13', '13', '13', '13');
