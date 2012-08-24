/*
Navicat MySQL Data Transfer

Source Server         : 24g_server
Source Server Version : 50522
Source Host           : localhost:3306
Source Database       : 20x

Target Server Type    : MYSQL
Target Server Version : 50522
File Encoding         : 65001

Date: 2012-05-14 20:49:08
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `buffer_scheme_list`
-- ----------------------------
DROP TABLE IF EXISTS `buffer_scheme_list`;
CREATE TABLE `buffer_scheme_list` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` varchar(40) DEFAULT NULL,
  `scheme_name` varchar(36) DEFAULT NULL,
  `mod_accepted` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of buffer_scheme_list
-- ----------------------------
INSERT INTO `buffer_scheme_list` VALUES ('1', '268489222', 'thxqq', null);
INSERT INTO `buffer_scheme_list` VALUES ('2', '268482325', '232', null);
INSERT INTO `buffer_scheme_list` VALUES ('3', '268485002', 'seer', null);
INSERT INTO `buffer_scheme_list` VALUES ('4', '268510141', 'buff', null);
INSERT INTO `buffer_scheme_list` VALUES ('5', '268520785', 'buff', null);
INSERT INTO `buffer_scheme_list` VALUES ('6', '268520111', '1', null);
INSERT INTO `buffer_scheme_list` VALUES ('7', '268483973', '1', null);
INSERT INTO `buffer_scheme_list` VALUES ('9', '268482367', 'Tank', null);
INSERT INTO `buffer_scheme_list` VALUES ('11', '268686563', 'Nana', null);
INSERT INTO `buffer_scheme_list` VALUES ('12', '268482365', 'mag', null);
INSERT INTO `buffer_scheme_list` VALUES ('13', '268489222', 'ff', null);
INSERT INTO `buffer_scheme_list` VALUES ('14', '268483083', '132', null);
INSERT INTO `buffer_scheme_list` VALUES ('15', '268483083', 'as', null);
INSERT INTO `buffer_scheme_list` VALUES ('16', '268547487', 'plm', null);
INSERT INTO `buffer_scheme_list` VALUES ('17', '268482384', '1', null);
INSERT INTO `buffer_scheme_list` VALUES ('19', '268493609', 'danex', null);
INSERT INTO `buffer_scheme_list` VALUES ('20', '268492100', 'pan', null);
INSERT INTO `buffer_scheme_list` VALUES ('21', '268499915', 'qw', null);
INSERT INTO `buffer_scheme_list` VALUES ('22', '268500561', 'asd', null);
INSERT INTO `buffer_scheme_list` VALUES ('23', '268510861', 'buff', null);
