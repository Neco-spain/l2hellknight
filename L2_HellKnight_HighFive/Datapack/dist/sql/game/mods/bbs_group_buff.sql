/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50522
Source Host           : localhost:3306
Source Database       : 20x

Target Server Type    : MYSQL
Target Server Version : 50522
File Encoding         : 65001

Date: 2012-06-08 23:30:29
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `bbs_group_buff`
-- ----------------------------
DROP TABLE IF EXISTS `bbs_group_buff`;
CREATE TABLE `bbs_group_buff` (
  `id` int(2) NOT NULL DEFAULT '0',
  `name` varchar(45) DEFAULT 'No name',
  `price` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of bbs_group_buff
-- ----------------------------
INSERT INTO `bbs_group_buff` VALUES ('1', 'Warrior', '5000');
INSERT INTO `bbs_group_buff` VALUES ('2', 'Mage', '5000');
INSERT INTO `bbs_group_buff` VALUES ('3', 'Improved', '10000');
INSERT INTO `bbs_group_buff` VALUES ('4', 'Songs', '7500');
INSERT INTO `bbs_group_buff` VALUES ('5', 'Dances', '7500');
INSERT INTO `bbs_group_buff` VALUES ('6', 'Prophecy', '10000');
