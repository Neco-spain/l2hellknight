/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50522
Source Host           : localhost:3306
Source Database       : 20x

Target Server Type    : MYSQL
Target Server Version : 50522
File Encoding         : 65001

Date: 2012-06-08 23:30:21
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `bbs_buff`
-- ----------------------------
DROP TABLE IF EXISTS `bbs_buff`;
CREATE TABLE `bbs_buff` (
  `id_skill` int(5) NOT NULL DEFAULT '0',
  `lvl_skill` int(2) NOT NULL DEFAULT '1',
  `id_group` int(2) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_skill`,`lvl_skill`,`id_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of bbs_buff
-- ----------------------------
INSERT INTO `bbs_buff` VALUES ('264', '1', '4');
INSERT INTO `bbs_buff` VALUES ('265', '1', '4');
INSERT INTO `bbs_buff` VALUES ('266', '1', '4');
INSERT INTO `bbs_buff` VALUES ('267', '1', '4');
INSERT INTO `bbs_buff` VALUES ('268', '1', '4');
INSERT INTO `bbs_buff` VALUES ('269', '1', '4');
INSERT INTO `bbs_buff` VALUES ('270', '1', '4');
INSERT INTO `bbs_buff` VALUES ('271', '1', '5');
INSERT INTO `bbs_buff` VALUES ('272', '1', '5');
INSERT INTO `bbs_buff` VALUES ('273', '1', '5');
INSERT INTO `bbs_buff` VALUES ('274', '1', '5');
INSERT INTO `bbs_buff` VALUES ('275', '1', '5');
INSERT INTO `bbs_buff` VALUES ('276', '1', '5');
INSERT INTO `bbs_buff` VALUES ('277', '1', '5');
INSERT INTO `bbs_buff` VALUES ('304', '1', '4');
INSERT INTO `bbs_buff` VALUES ('306', '1', '4');
INSERT INTO `bbs_buff` VALUES ('307', '1', '5');
INSERT INTO `bbs_buff` VALUES ('308', '1', '4');
INSERT INTO `bbs_buff` VALUES ('309', '1', '5');
INSERT INTO `bbs_buff` VALUES ('310', '1', '5');
INSERT INTO `bbs_buff` VALUES ('311', '1', '5');
INSERT INTO `bbs_buff` VALUES ('349', '1', '4');
INSERT INTO `bbs_buff` VALUES ('363', '1', '4');
INSERT INTO `bbs_buff` VALUES ('364', '1', '4');
INSERT INTO `bbs_buff` VALUES ('365', '1', '5');
INSERT INTO `bbs_buff` VALUES ('1035', '4', '1');
INSERT INTO `bbs_buff` VALUES ('1035', '4', '2');
INSERT INTO `bbs_buff` VALUES ('1036', '2', '1');
INSERT INTO `bbs_buff` VALUES ('1036', '2', '2');
INSERT INTO `bbs_buff` VALUES ('1040', '3', '1');
INSERT INTO `bbs_buff` VALUES ('1040', '3', '2');
INSERT INTO `bbs_buff` VALUES ('1044', '3', '1');
INSERT INTO `bbs_buff` VALUES ('1044', '3', '2');
INSERT INTO `bbs_buff` VALUES ('1045', '6', '1');
INSERT INTO `bbs_buff` VALUES ('1045', '6', '2');
INSERT INTO `bbs_buff` VALUES ('1047', '4', '1');
INSERT INTO `bbs_buff` VALUES ('1047', '4', '2');
INSERT INTO `bbs_buff` VALUES ('1048', '6', '1');
INSERT INTO `bbs_buff` VALUES ('1048', '6', '2');
INSERT INTO `bbs_buff` VALUES ('1059', '3', '2');
INSERT INTO `bbs_buff` VALUES ('1062', '2', '1');
INSERT INTO `bbs_buff` VALUES ('1062', '2', '2');
INSERT INTO `bbs_buff` VALUES ('1068', '3', '1');
INSERT INTO `bbs_buff` VALUES ('1073', '1', '6');
INSERT INTO `bbs_buff` VALUES ('1077', '3', '1');
INSERT INTO `bbs_buff` VALUES ('1078', '6', '2');
INSERT INTO `bbs_buff` VALUES ('1085', '3', '2');
INSERT INTO `bbs_buff` VALUES ('1086', '2', '1');
INSERT INTO `bbs_buff` VALUES ('1087', '3', '2');
INSERT INTO `bbs_buff` VALUES ('1204', '2', '1');
INSERT INTO `bbs_buff` VALUES ('1204', '2', '2');
INSERT INTO `bbs_buff` VALUES ('1240', '3', '1');
INSERT INTO `bbs_buff` VALUES ('1242', '3', '1');
INSERT INTO `bbs_buff` VALUES ('1243', '6', '1');
INSERT INTO `bbs_buff` VALUES ('1243', '6', '2');
INSERT INTO `bbs_buff` VALUES ('1257', '3', '1');
INSERT INTO `bbs_buff` VALUES ('1257', '3', '2');
INSERT INTO `bbs_buff` VALUES ('1259', '4', '1');
INSERT INTO `bbs_buff` VALUES ('1259', '4', '2');
INSERT INTO `bbs_buff` VALUES ('1268', '3', '1');
INSERT INTO `bbs_buff` VALUES ('1303', '2', '2');
INSERT INTO `bbs_buff` VALUES ('1304', '4', '1');
INSERT INTO `bbs_buff` VALUES ('1304', '4', '2');
INSERT INTO `bbs_buff` VALUES ('1355', '1', '6');
INSERT INTO `bbs_buff` VALUES ('1356', '1', '6');
INSERT INTO `bbs_buff` VALUES ('1357', '1', '6');
INSERT INTO `bbs_buff` VALUES ('1362', '1', '6');
INSERT INTO `bbs_buff` VALUES ('1363', '1', '6');
INSERT INTO `bbs_buff` VALUES ('1388', '3', '1');
INSERT INTO `bbs_buff` VALUES ('1389', '3', '1');
INSERT INTO `bbs_buff` VALUES ('1389', '3', '2');
INSERT INTO `bbs_buff` VALUES ('1397', '3', '1');
INSERT INTO `bbs_buff` VALUES ('1397', '3', '2');
INSERT INTO `bbs_buff` VALUES ('1413', '1', '6');
INSERT INTO `bbs_buff` VALUES ('1414', '1', '6');
INSERT INTO `bbs_buff` VALUES ('1461', '1', '6');
INSERT INTO `bbs_buff` VALUES ('1499', '1', '3');
INSERT INTO `bbs_buff` VALUES ('1500', '1', '3');
INSERT INTO `bbs_buff` VALUES ('1501', '1', '3');
INSERT INTO `bbs_buff` VALUES ('1502', '1', '3');
INSERT INTO `bbs_buff` VALUES ('1503', '1', '3');
INSERT INTO `bbs_buff` VALUES ('1504', '1', '3');
INSERT INTO `bbs_buff` VALUES ('1519', '1', '3');
INSERT INTO `bbs_buff` VALUES ('4699', '13', '6');
INSERT INTO `bbs_buff` VALUES ('4700', '13', '6');
INSERT INTO `bbs_buff` VALUES ('4702', '13', '6');
INSERT INTO `bbs_buff` VALUES ('4703', '13', '6');
