/*
Navicat MySQL Data Transfer

Source Server         : 24g_server
Source Server Version : 50522
Source Host           : localhost:3306
Source Database       : 20x

Target Server Type    : MYSQL
Target Server Version : 50522
File Encoding         : 65001

Date: 2012-05-14 20:48:49
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `buffer_consumable_id`
-- ----------------------------
DROP TABLE IF EXISTS `buffer_consumable_id`;
CREATE TABLE `buffer_consumable_id` (
  `item_id` decimal(11,0) NOT NULL DEFAULT '0',
  `name` varchar(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`item_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of buffer_consumable_id
-- ----------------------------
INSERT INTO `buffer_consumable_id` VALUES ('57', 'Adena');
