/*
Navicat MySQL Data Transfer

Source Server         : home
Source Server Version : 50509
Source Host           : localhost:3306
Source Database       : eon

Target Server Type    : MYSQL
Target Server Version : 50509
File Encoding         : 65001

Date: 2011-06-21 17:05:32
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `raidboss_spawnlist`
-- ----------------------------
DROP TABLE IF EXISTS `raidboss_spawnlist`;
CREATE TABLE `raidboss_spawnlist` (
  `boss_id` int(11) NOT NULL DEFAULT '0',
  `amount` int(11) NOT NULL DEFAULT '0',
  `loc_x` int(11) NOT NULL DEFAULT '0',
  `loc_y` int(11) NOT NULL DEFAULT '0',
  `loc_z` int(11) NOT NULL DEFAULT '0',
  `heading` int(11) NOT NULL DEFAULT '0',
  `respawn_min_delay` int(11) NOT NULL DEFAULT '43200',
  `respawn_max_delay` int(11) NOT NULL DEFAULT '129600',
  `respawn_time` bigint(20) NOT NULL DEFAULT '0',
  `currentHp` decimal(8,0) DEFAULT NULL,
  `currentMp` decimal(8,0) DEFAULT NULL,
  PRIMARY KEY (`boss_id`,`loc_x`,`loc_y`,`loc_z`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of raidboss_spawnlist
-- ----------------------------
INSERT INTO `raidboss_spawnlist` VALUES ('25001', '1', '-54416', '146480', '-2887', '0', '43200', '129600', '0', '95986', '545');
INSERT INTO `raidboss_spawnlist` VALUES ('25004', '1', '-94208', '100240', '-3520', '0', '43200', '129600', '0', '168366', '763');
INSERT INTO `raidboss_spawnlist` VALUES ('25007', '1', '124240', '75376', '-2800', '0', '43200', '129600', '0', '331522', '1062');
INSERT INTO `raidboss_spawnlist` VALUES ('25010', '1', '113920', '52960', '-3735', '0', '43200', '129600', '0', '624464', '2039');
INSERT INTO `raidboss_spawnlist` VALUES ('25013', '1', '169744', '11920', '-2732', '0', '43200', '129600', '0', '507285', '1722');
INSERT INTO `raidboss_spawnlist` VALUES ('25016', '1', '76787', '245775', '-10376', '0', '43200', '129600', '0', '188376', '2368');
INSERT INTO `raidboss_spawnlist` VALUES ('25019', '1', '7376', '169376', '-3600', '0', '43200', '129600', '0', '206185', '606');
INSERT INTO `raidboss_spawnlist` VALUES ('25020', '1', '90384', '125568', '-2128', '0', '43200', '129600', '0', '156584', '893');
INSERT INTO `raidboss_spawnlist` VALUES ('25023', '1', '27280', '101744', '-3696', '0', '43200', '129600', '0', '208019', '1415');
INSERT INTO `raidboss_spawnlist` VALUES ('25026', '1', '92976', '7920', '-3914', '0', '43200', '129600', '0', '352421', '1660');
INSERT INTO `raidboss_spawnlist` VALUES ('25029', '1', '54941', '206705', '-3728', '0', '43200', '129600', '0', '156190', '1911');
INSERT INTO `raidboss_spawnlist` VALUES ('25032', '1', '88532', '245798', '-10376', '0', '43200', '129600', '0', '229722', '2707');
INSERT INTO `raidboss_spawnlist` VALUES ('25035', '1', '180968', '12035', '-2720', '0', '43200', '129600', '0', '888658', '3058');
INSERT INTO `raidboss_spawnlist` VALUES ('25038', '1', '-57360', '186272', '-4967', '0', '43200', '129600', '0', '116581', '699');
INSERT INTO `raidboss_spawnlist` VALUES ('25041', '1', '10416', '126880', '-3676', '0', '43200', '129600', '0', '165289', '927');
INSERT INTO `raidboss_spawnlist` VALUES ('25044', '1', '107792', '27728', '-3488', '0', '43200', '129600', '0', '319791', '1296');
INSERT INTO `raidboss_spawnlist` VALUES ('25047', '1', '116352', '27648', '-3319', '0', '43200', '129600', '0', '352421', '1660');
INSERT INTO `raidboss_spawnlist` VALUES ('25050', '1', '125520', '27216', '-3632', '0', '43200', '129600', '0', '771340', '2039');
INSERT INTO `raidboss_spawnlist` VALUES ('25051', '1', '117760', '-9072', '-3264', '0', '43200', '129600', '0', '818959', '2707');
INSERT INTO `raidboss_spawnlist` VALUES ('25054', '1', '113432', '16403', '3960', '0', '43200', '129600', '0', '945900', '3420');
INSERT INTO `raidboss_spawnlist` VALUES ('25057', '1', '107056', '168176', '-3456', '0', '43200', '129600', '0', '288415', '2235');
INSERT INTO `raidboss_spawnlist` VALUES ('25060', '1', '-60428', '188264', '-4512', '0', '43200', '129600', '0', '99367', '575');
INSERT INTO `raidboss_spawnlist` VALUES ('25063', '1', '-91024', '116304', '-3466', '0', '43200', '129600', '0', '330579', '927');
INSERT INTO `raidboss_spawnlist` VALUES ('25064', '1', '92528', '84752', '-3703', '0', '43200', '129600', '0', '218810', '1120');
INSERT INTO `raidboss_spawnlist` VALUES ('25067', '1', '94992', '-23168', '-2176', '0', '43200', '129600', '0', '554640', '1598');
INSERT INTO `raidboss_spawnlist` VALUES ('25070', '1', '125600', '50100', '-3600', '0', '43200', '129600', '0', '451391', '2039');
INSERT INTO `raidboss_spawnlist` VALUES ('25073', '1', '143265', '110044', '-3944', '0', '43200', '129600', '0', '875948', '2987');
INSERT INTO `raidboss_spawnlist` VALUES ('25076', '1', '-60976', '127552', '-2960', '0', '43200', '129600', '0', '103092', '606');
INSERT INTO `raidboss_spawnlist` VALUES ('25079', '1', '53712', '102656', '-1072', '0', '43200', '129600', '0', '168366', '763');
INSERT INTO `raidboss_spawnlist` VALUES ('25082', '1', '88512', '140576', '-3483', '0', '43200', '129600', '0', '206753', '1062');
INSERT INTO `raidboss_spawnlist` VALUES ('25085', '1', '66944', '67504', '-3704', '0', '43200', '129600', '0', '371721', '1355');
INSERT INTO `raidboss_spawnlist` VALUES ('25088', '1', '90848', '16368', '-5296', '0', '43200', '129600', '0', '702418', '2039');
INSERT INTO `raidboss_spawnlist` VALUES ('25089', '1', '165424', '93776', '-2992', '0', '43200', '129600', '0', '512194', '2301');
INSERT INTO `raidboss_spawnlist` VALUES ('25092', '1', '116151', '16227', '1944', '0', '43200', '129600', '0', '888658', '3058');
INSERT INTO `raidboss_spawnlist` VALUES ('25095', '1', '-37856', '198128', '-2672', '0', '43200', '129600', '0', '121941', '731');
INSERT INTO `raidboss_spawnlist` VALUES ('25098', '1', '-5937', '175004', '-2940', '59144', '43200', '129600', '0', '330579', '927');
INSERT INTO `raidboss_spawnlist` VALUES ('25098', '1', '123536', '133504', '-3584', '0', '43200', '129600', '0', '330579', '927');
INSERT INTO `raidboss_spawnlist` VALUES ('25099', '1', '64048', '16048', '-3536', '0', '43200', '129600', '0', '273375', '1355');
INSERT INTO `raidboss_spawnlist` VALUES ('25102', '1', '113840', '84256', '-2480', '0', '43200', '129600', '0', '576831', '1722');
INSERT INTO `raidboss_spawnlist` VALUES ('25103', '1', '135872', '94592', '-3735', '0', '43200', '129600', '0', '451391', '2039');
INSERT INTO `raidboss_spawnlist` VALUES ('25106', '1', '173880', '-11412', '-2880', '0', '43200', '129600', '0', '526218', '2570');
INSERT INTO `raidboss_spawnlist` VALUES ('25109', '1', '152660', '110387', '-5520', '0', '43200', '129600', '0', '935092', '3347');
INSERT INTO `raidboss_spawnlist` VALUES ('25112', '1', '116128', '139392', '-3640', '0', '43200', '129600', '0', '127782', '763');
INSERT INTO `raidboss_spawnlist` VALUES ('25115', '1', '94000', '197500', '-3300', '0', '43200', '129600', '0', '294846', '1120');
INSERT INTO `raidboss_spawnlist` VALUES ('25118', '1', '50896', '146576', '-3645', '0', '43200', '129600', '0', '330579', '1415');
INSERT INTO `raidboss_spawnlist` VALUES ('25119', '1', '121872', '64032', '-3536', '0', '43200', '129600', '0', '507285', '1722');
INSERT INTO `raidboss_spawnlist` VALUES ('25122', '1', '86300', '-8200', '-3000', '0', '43200', '129600', '0', '467209', '2235');
INSERT INTO `raidboss_spawnlist` VALUES ('25125', '1', '170656', '85184', '-2000', '0', '43200', '129600', '0', '1637918', '2707');
INSERT INTO `raidboss_spawnlist` VALUES ('25126', '1', '116263', '15916', '6992', '0', '43200', '129600', '0', '1974940', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25127', '1', '-47552', '219232', '-2413', '0', '43200', '129600', '0', '198734', '763');
INSERT INTO `raidboss_spawnlist` VALUES ('25128', '1', '17696', '179056', '-3520', '0', '43200', '129600', '0', '148507', '860');
INSERT INTO `raidboss_spawnlist` VALUES ('25131', '1', '75488', '-9360', '-2720', '0', '43200', '129600', '0', '369009', '1415');
INSERT INTO `raidboss_spawnlist` VALUES ('25134', '1', '87536', '75872', '-3591', '0', '43200', '129600', '0', '218810', '1722');
INSERT INTO `raidboss_spawnlist` VALUES ('25137', '1', '125280', '102576', '-3305', '0', '43200', '129600', '0', '451391', '2039');
INSERT INTO `raidboss_spawnlist` VALUES ('25140', '1', '191975', '56959', '-7616', '0', '43200', '129600', '0', '818959', '2707');
INSERT INTO `raidboss_spawnlist` VALUES ('25143', '1', '113102', '16002', '6992', '0', '43200', '129600', '0', '977229', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25146', '1', '-13056', '215680', '-3760', '0', '43200', '129600', '0', '90169', '485');
INSERT INTO `raidboss_spawnlist` VALUES ('25149', '1', '-12656', '138176', '-3584', '0', '43200', '129600', '0', '103092', '606');
INSERT INTO `raidboss_spawnlist` VALUES ('25152', '1', '43872', '123968', '-2928', '0', '43200', '129600', '0', '165289', '927');
INSERT INTO `raidboss_spawnlist` VALUES ('25155', '1', '73520', '66912', '-3728', '0', '43200', '129600', '0', '294846', '1120');
INSERT INTO `raidboss_spawnlist` VALUES ('25158', '1', '77104', '5408', '-3088', '0', '43200', '129600', '0', '920790', '1722');
INSERT INTO `raidboss_spawnlist` VALUES ('25159', '1', '124984', '43200', '-3625', '0', '43200', '129600', '0', '435256', '1975');
INSERT INTO `raidboss_spawnlist` VALUES ('25162', '1', '194107', '53884', '-4368', '0', '43200', '129600', '0', '1461912', '2368');
INSERT INTO `raidboss_spawnlist` VALUES ('25163', '1', '130500', '59098', '3584', '0', '43200', '129600', '0', '888658', '3058');
INSERT INTO `raidboss_spawnlist` VALUES ('25166', '1', '-21800', '152000', '-2900', '0', '43200', '129600', '0', '134813', '606');
INSERT INTO `raidboss_spawnlist` VALUES ('25169', '1', '-54464', '170288', '-3136', '0', '43200', '129600', '0', '336732', '763');
INSERT INTO `raidboss_spawnlist` VALUES ('25170', '1', '26064', '121808', '-3738', '0', '43200', '129600', '0', '195371', '1028');
INSERT INTO `raidboss_spawnlist` VALUES ('25173', '1', '75968', '110784', '-2512', '0', '43200', '129600', '0', '288415', '1415');
INSERT INTO `raidboss_spawnlist` VALUES ('25176', '1', '92544', '115232', '-3200', '0', '43200', '129600', '0', '451391', '2039');
INSERT INTO `raidboss_spawnlist` VALUES ('25179', '1', '181814', '52379', '-4344', '0', '43200', '129600', '0', '526218', '2368');
INSERT INTO `raidboss_spawnlist` VALUES ('25182', '1', '41966', '215417', '-3728', '0', '43200', '129600', '0', '512194', '2707');
INSERT INTO `raidboss_spawnlist` VALUES ('25185', '1', '88123', '166312', '-3412', '0', '43200', '129600', '0', '165289', '927');
INSERT INTO `raidboss_spawnlist` VALUES ('25188', '1', '88256', '176208', '-3488', '0', '43200', '129600', '0', '255564', '763');
INSERT INTO `raidboss_spawnlist` VALUES ('25189', '1', '68832', '203024', '-3547', '0', '43200', '129600', '0', '156584', '893');
INSERT INTO `raidboss_spawnlist` VALUES ('25192', '1', '125920', '190208', '-3291', '0', '43200', '129600', '0', '258849', '1296');
INSERT INTO `raidboss_spawnlist` VALUES ('25198', '1', '102656', '157424', '-3735', '0', '43200', '129600', '0', '1777317', '2639');
INSERT INTO `raidboss_spawnlist` VALUES ('25199', '1', '108096', '157408', '-3688', '0', '43200', '129600', '0', '912634', '2707');
INSERT INTO `raidboss_spawnlist` VALUES ('25202', '1', '119760', '157392', '-3744', '0', '43200', '129600', '0', '935092', '2777');
INSERT INTO `raidboss_spawnlist` VALUES ('25205', '1', '123808', '153408', '-3671', '0', '43200', '129600', '0', '956490', '3274');
INSERT INTO `raidboss_spawnlist` VALUES ('25208', '1', '73776', '201552', '-3760', '0', '43200', '129600', '0', '218810', '1722');
INSERT INTO `raidboss_spawnlist` VALUES ('25211', '1', '76352', '193216', '-3648', '0', '43200', '129600', '0', '174646', '1975');
INSERT INTO `raidboss_spawnlist` VALUES ('25214', '1', '112112', '209936', '-3616', '0', '43200', '129600', '0', '218810', '2368');
INSERT INTO `raidboss_spawnlist` VALUES ('25217', '1', '89904', '105712', '-3292', '0', '43200', '129600', '0', '369009', '1722');
INSERT INTO `raidboss_spawnlist` VALUES ('25220', '1', '113551', '17083', '-2120', '0', '43200', '129600', '0', '924022', '3274');
INSERT INTO `raidboss_spawnlist` VALUES ('25223', '1', '43152', '152352', '-2848', '0', '43200', '129600', '0', '165289', '1237');
INSERT INTO `raidboss_spawnlist` VALUES ('25226', '1', '104240', '-3664', '-3392', '0', '43200', '129600', '0', '768537', '2502');
INSERT INTO `raidboss_spawnlist` VALUES ('25229', '1', '137568', '-19488', '-3552', '0', '43200', '129600', '0', '1891801', '3420');
INSERT INTO `raidboss_spawnlist` VALUES ('25230', '1', '66672', '46704', '-3920', '0', '43200', '129600', '0', '482650', '2169');
INSERT INTO `raidboss_spawnlist` VALUES ('25233', '1', '185800', '-26500', '-2000', '0', '43200', '129600', '0', '1256671', '3643');
INSERT INTO `raidboss_spawnlist` VALUES ('25234', '1', '120080', '111248', '-3047', '0', '43200', '129600', '0', '1052436', '2707');
INSERT INTO `raidboss_spawnlist` VALUES ('25235', '1', '116400', '-62528', '-3264', '0', '43200', '129600', '0', '912634', '3202');
INSERT INTO `raidboss_spawnlist` VALUES ('25238', '1', '155000', '85400', '-3200', '0', '43200', '129600', '0', '512194', '2846');
INSERT INTO `raidboss_spawnlist` VALUES ('25241', '1', '165984', '88048', '-2384', '0', '43200', '129600', '0', '624464', '2639');
INSERT INTO `raidboss_spawnlist` VALUES ('25244', '1', '187360', '45840', '-5856', '0', '43200', '129600', '0', '1891801', '3420');
INSERT INTO `raidboss_spawnlist` VALUES ('25245', '1', '172000', '55000', '-5400', '0', '43200', '129600', '0', '977229', '3643');
INSERT INTO `raidboss_spawnlist` VALUES ('25248', '1', '127903', '-13399', '-3720', '0', '43200', '129600', '0', '1825269', '3274');
INSERT INTO `raidboss_spawnlist` VALUES ('25249', '1', '147104', '-20560', '-3377', '0', '43200', '129600', '0', '945900', '3420');
INSERT INTO `raidboss_spawnlist` VALUES ('25252', '1', '192376', '22087', '-3608', '0', '43200', '129600', '0', '888658', '3058');
INSERT INTO `raidboss_spawnlist` VALUES ('25255', '1', '170048', '-24896', '-3440', '0', '43200', '129600', '0', '1637918', '2707');
INSERT INTO `raidboss_spawnlist` VALUES ('25256', '1', '170320', '42640', '-4832', '0', '43200', '129600', '0', '526218', '2368');
INSERT INTO `raidboss_spawnlist` VALUES ('25259', '1', '42050', '208107', '-3752', '0', '43200', '129600', '0', '1248928', '2039');
INSERT INTO `raidboss_spawnlist` VALUES ('25260', '1', '93120', '19440', '-3607', '0', '43200', '129600', '0', '392985', '1722');
INSERT INTO `raidboss_spawnlist` VALUES ('25263', '1', '144400', '-28192', '-1920', '0', '43200', '129600', '0', '848789', '2846');
INSERT INTO `raidboss_spawnlist` VALUES ('25266', '1', '188983', '13647', '-2672', '0', '43200', '129600', '0', '945900', '3420');
INSERT INTO `raidboss_spawnlist` VALUES ('25269', '1', '123504', '-23696', '-3481', '0', '43200', '129600', '0', '888658', '3058');
INSERT INTO `raidboss_spawnlist` VALUES ('25272', '1', '49248', '127792', '-3552', '0', '43200', '129600', '0', '233163', '1415');
INSERT INTO `raidboss_spawnlist` VALUES ('25276', '1', '154088', '-14116', '-3736', '0', '43200', '129600', '0', '1891801', '3420');
INSERT INTO `raidboss_spawnlist` VALUES ('25277', '1', '54651', '180269', '-4976', '0', '43200', '129600', '0', '507285', '1722');
INSERT INTO `raidboss_spawnlist` VALUES ('25280', '1', '85622', '88766', '-5120', '0', '43200', '129600', '0', '1248928', '2039');
INSERT INTO `raidboss_spawnlist` VALUES ('25281', '1', '151053', '88124', '-5424', '0', '43200', '129600', '0', '1777317', '3058');
INSERT INTO `raidboss_spawnlist` VALUES ('25282', '1', '179311', '-7632', '-4896', '0', '43200', '129600', '0', '1891801', '3420');
INSERT INTO `raidboss_spawnlist` VALUES ('25293', '1', '134672', '-115600', '-1216', '0', '43200', '129600', '0', '977229', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25299', '1', '148160', '-73808', '-4919', '0', '43200', '129600', '0', '714778', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25302', '1', '145504', '-81664', '-6016', '0', '43200', '129600', '0', '743801', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25305', '1', '145008', '-84992', '-6240', '0', '43200', '129600', '0', '1532678', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25309', '1', '115552', '-39200', '-2480', '0', '43200', '129600', '0', '714778', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25312', '1', '109216', '-36160', '-938', '0', '43200', '129600', '0', '743801', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25315', '1', '105584', '-43024', '-1728', '0', '43200', '129600', '0', '1532678', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25319', '1', '184542', '-106330', '-6304', '0', '43200', '129600', '0', '1048567', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25322', '1', '93296', '-75104', '-1824', '0', '43200', '129600', '0', '834231', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25325', '1', '91008', '-85904', '-2736', '0', '43200', '129600', '0', '888658', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25328', '1', '59331', '-42403', '-3003', '0', '7200', '10800', '0', '232323', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25352', '1', '-16912', '174912', '-3264', '0', '43200', '129600', '0', '127782', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25354', '1', '-16096', '184288', '-3817', '0', '43200', '129600', '0', '165289', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25357', '1', '-3456', '112864', '-3456', '0', '43200', '129600', '0', '90169', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25360', '1', '29216', '179280', '-3624', '0', '43200', '129600', '0', '107186', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25362', '1', '-55920', '186768', '-3336', '0', '43200', '129600', '0', '95986', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25365', '1', '-62000', '190256', '-3687', '0', '43200', '129600', '0', '214372', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25366', '1', '-62368', '179440', '-3594', '0', '43200', '129600', '0', '95986', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25369', '1', '-45616', '111024', '-3808', '0', '43200', '129600', '0', '103092', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25372', '1', '48000', '243376', '-6611', '0', '43200', '129600', '0', '175392', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25373', '1', '9649', '77467', '-3808', '0', '43200', '129600', '0', '90169', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25375', '1', '22500', '80300', '-2772', '0', '43200', '129600', '0', '87696', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25378', '1', '-54096', '84288', '-3512', '0', '43200', '129600', '0', '87696', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25380', '1', '-47367', '51548', '-5904', '0', '43200', '129600', '0', '90169', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25383', '1', '51632', '153920', '-3552', '0', '43200', '129600', '0', '156584', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25385', '1', '53600', '143472', '-3872', '0', '43200', '129600', '0', '174646', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25388', '1', '40128', '101920', '-1241', '0', '43200', '129600', '0', '165289', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25391', '1', '45600', '120592', '-2455', '0', '43200', '129600', '0', '297015', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25392', '1', '29928', '107160', '-3708', '0', '43200', '129600', '0', '141034', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25394', '1', '101888', '200224', '-3680', '0', '43200', '129600', '0', '390743', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25395', '1', '15000', '119000', '-11900', '0', '43200', '129600', '0', '288415', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25398', '1', '5000', '189000', '-3728', '0', '43200', '129600', '0', '165289', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25401', '1', '117808', '102880', '-3600', '0', '43200', '129600', '0', '141034', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25404', '1', '35992', '191312', '-3104', '0', '43200', '129600', '0', '148507', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25407', '1', '115072', '112272', '-3018', '0', '43200', '129600', '0', '526218', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25410', '1', '72192', '125424', '-3657', '0', '43200', '129600', '0', '218810', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25412', '1', '81920', '113136', '-3056', '0', '43200', '129600', '0', '319791', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25415', '1', '128352', '138464', '-3467', '0', '43200', '129600', '0', '218810', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25418', '1', '62416', '8096', '-3376', '0', '43200', '129600', '0', '273375', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25420', '1', '42032', '24128', '-4704', '0', '43200', '129600', '0', '335987', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25423', '1', '113600', '47120', '-4640', '0', '43200', '129600', '0', '539706', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25426', '1', '-18048', '-101264', '-2112', '0', '43200', '129600', '0', '103092', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25429', '1', '172064', '-214752', '-3565', '0', '43200', '129600', '0', '103092', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25431', '1', '79648', '18320', '-5232', '0', '43200', '129600', '0', '273375', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25434', '1', '104096', '-16896', '-1803', '0', '43200', '129600', '0', '451391', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25437', '1', '67296', '64128', '-3723', '0', '43200', '129600', '0', '576831', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25438', '1', '107000', '92000', '-2272', '0', '43200', '129600', '0', '273375', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25441', '1', '111440', '82912', '-2912', '0', '43200', '129600', '0', '288415', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25444', '1', '113232', '17456', '-4384', '0', '43200', '129600', '0', '588136', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25447', '1', '113200', '17552', '-1424', '0', '43200', '129600', '0', '645953', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25450', '1', '113600', '15104', '9559', '0', '43200', '129600', '0', '987470', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25453', '1', '156704', '-6096', '-4185', '0', '43200', '129600', '0', '888658', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25456', '1', '133632', '87072', '-3623', '0', '43200', '129600', '0', '352421', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25460', '1', '150304', '67776', '-3688', '0', '43200', '129600', '0', '385670', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25463', '1', '166288', '68096', '-3264', '0', '43200', '129600', '0', '467209', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25467', '1', '186192', '61472', '-4160', '0', '43200', '129600', '0', '576851', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25470', '1', '186896', '56276', '-4576', '0', '43200', '129600', '0', '598898', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25473', '1', '175712', '29856', '-3776', '0', '43200', '129600', '0', '402319', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25475', '1', '183568', '24560', '-3184', '0', '43200', '129600', '0', '451391', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25478', '1', '168288', '28368', '-3632', '0', '43200', '129600', '0', '588136', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25481', '1', '53517', '205413', '-3728', '0', '43200', '129600', '0', '66938', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25484', '1', '43160', '220463', '-3680', '0', '43200', '129600', '0', '369009', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25487', '1', '83056', '183232', '-3616', '0', '43200', '129600', '0', '218810', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25490', '1', '86528', '216864', '-3584', '0', '43200', '129600', '0', '218810', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25493', '1', '83174', '254428', '-10873', '0', '43200', '129600', '0', '451391', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25496', '1', '88300', '258000', '-10200', '0', '43200', '129600', '0', '402319', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25498', '1', '126624', '174448', '-3056', '0', '43200', '129600', '0', '288415', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25514', '1', '79635', '-55434', '-6135', '0', '43200', '129600', '0', '714778', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('25517', '1', '113000', '-76000', '200', '0', '43200', '129600', '0', '1069643', '9999');
INSERT INTO `raidboss_spawnlist` VALUES ('25523', '1', '168641', '-60417', '-3888', '0', '43200', '129600', '0', '1858045', '9999');
INSERT INTO `raidboss_spawnlist` VALUES ('25524', '1', '144143', '-5731', '-4722', '0', '43200', '129600', '0', '956490', '3247');
INSERT INTO `raidboss_spawnlist` VALUES ('25527', '1', '3776', '-6768', '-3288', '0', '43200', '129600', '0', '1532678', '3718');
INSERT INTO `raidboss_spawnlist` VALUES ('29095', '1', '141569', '-45908', '-2387', '0', '43200', '129600', '0', '2289038', '2746');
