/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50522
Source Host           : localhost:3306
Source Database       : 20x

Target Server Type    : MYSQL
Target Server Version : 50522
File Encoding         : 65001

Date: 2012-06-08 23:30:38
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `bbs_teleport`
-- ----------------------------
DROP TABLE IF EXISTS `bbs_teleport`;
CREATE TABLE `bbs_teleport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` tinyint(1) NOT NULL,
  `sub_id` int(11) NOT NULL,
  `desription` varchar(60) CHARACTER SET utf8 NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `z` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=411 DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

-- ----------------------------
-- Records of bbs_teleport
-- ----------------------------
INSERT INTO `bbs_teleport` VALUES ('1', '0', '0', 'Aden', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('2', '0', '0', 'Giran', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('3', '0', '0', 'Godard', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('4', '0', '0', 'Rune', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('5', '0', '0', 'Heine', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('6', '0', '0', 'Dion', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('7', '0', '0', 'Gludio', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('8', '0', '0', 'Gludin', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('9', '0', '0', 'Hanters vilage', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('10', '0', '0', 'Oren', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('11', '0', '0', 'Schuttgart', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('12', '0', '0', 'Dark Elven Village', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('13', '0', '0', 'Dwarven Village', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('14', '0', '0', 'Elven Village', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('15', '0', '0', 'Kamael Village', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('16', '0', '0', 'Orc Village', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('17', '0', '0', 'Talking Island Village', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('18', '0', '0', 'Seven Signs', '0', '0', '0');
INSERT INTO `bbs_teleport` VALUES ('254', '1', '1', 'Aden', '147450', '27064', '-2208');
INSERT INTO `bbs_teleport` VALUES ('255', '1', '1', 'Anghel Waterfall', '165584', '85997', '-2338');
INSERT INTO `bbs_teleport` VALUES ('256', '1', '1', 'Ancient Battleground', '106561', '-2900', '1670');
INSERT INTO `bbs_teleport` VALUES ('257', '1', '1', 'Blazing Swamp', '146828', '-12859', '-4455');
INSERT INTO `bbs_teleport` VALUES ('258', '1', '1', 'Cemetary', '172136', '20325', '-3326');
INSERT INTO `bbs_teleport` VALUES ('259', '1', '1', 'Coliseum', '150086', '46733', '-3412');
INSERT INTO `bbs_teleport` VALUES ('260', '1', '1', 'Forbidden Gateway', '185395', '20359', '-3270');
INSERT INTO `bbs_teleport` VALUES ('261', '1', '1', 'Forest Of Mirrors', '150477', '85907', '-2753');
INSERT INTO `bbs_teleport` VALUES ('262', '1', '1', 'Forsaken Plains', '168207', '37959', '-4067');
INSERT INTO `bbs_teleport` VALUES ('263', '1', '1', 'Silent Valley', '170852', '55808', '-5275');
INSERT INTO `bbs_teleport` VALUES ('264', '1', '1', 'Fields of Massacre', '183505', '-14932', '-2771');
INSERT INTO `bbs_teleport` VALUES ('265', '1', '1', 'Giants Cave', '174528', '52683', '-4369');
INSERT INTO `bbs_teleport` VALUES ('266', '1', '1', 'Tower of Insolence', '121685', '15749', '-3852');
INSERT INTO `bbs_teleport` VALUES ('267', '1', '2', 'Giran', '82698', '148638', '-3473');
INSERT INTO `bbs_teleport` VALUES ('268', '1', '2', 'Death Pass', '70000', '126636', '-3804');
INSERT INTO `bbs_teleport` VALUES ('269', '1', '2', 'Giran Harbor', '47114', '187152', '-3485');
INSERT INTO `bbs_teleport` VALUES ('270', '1', '2', 'Devils Isle', '42006', '208234', '-3756');
INSERT INTO `bbs_teleport` VALUES ('271', '1', '2', 'Dragon Valley', '122881', '110792', '-3727');
INSERT INTO `bbs_teleport` VALUES ('272', '1', '2', 'Antharas Lair', '131355', '114451', '-3718');
INSERT INTO `bbs_teleport` VALUES ('273', '1', '2', 'Hardins Academy', '105925', '109712', '-3187');
INSERT INTO `bbs_teleport` VALUES ('274', '1', '2', 'Brekas Stronhold', '8551', '131318', '-3667');
INSERT INTO `bbs_teleport` VALUES ('275', '1', '2', 'Giran Arena', '73890', '142656', '-3778');
INSERT INTO `bbs_teleport` VALUES ('276', '1', '3', 'Godard', '147725', '-56517', '-2780');
INSERT INTO `bbs_teleport` VALUES ('277', '1', '3', 'Monastery of Silence', '107944', '-87728', '-2917');
INSERT INTO `bbs_teleport` VALUES ('278', '1', '3', 'Hot Springs', '149616', '-112428', '-2065');
INSERT INTO `bbs_teleport` VALUES ('279', '1', '3', 'Varka Silenos Outpost', '108275', '-53785', '-2524');
INSERT INTO `bbs_teleport` VALUES ('280', '1', '3', 'Forge of the Gods', '170723', '-116207', '-2067');
INSERT INTO `bbs_teleport` VALUES ('281', '1', '3', 'Imperial Tomb', '188191', '-74959', '-2738');
INSERT INTO `bbs_teleport` VALUES ('282', '1', '3', 'Ketra Orc OutPost', '149774', '-81243', '-5624');
INSERT INTO `bbs_teleport` VALUES ('283', '1', '3', 'Wall of Agros', '176886', '-50812', '-3394');
INSERT INTO `bbs_teleport` VALUES ('284', '1', '3', 'Temple of Pilgrims', '168505', '-86606', '-2992');
INSERT INTO `bbs_teleport` VALUES ('285', '1', '4', 'Rune', '44070', '-50243', '-796');
INSERT INTO `bbs_teleport` VALUES ('286', '1', '4', 'Rune Harbor', '38015', '-38305', '-3609');
INSERT INTO `bbs_teleport` VALUES ('287', '1', '4', 'Swamp of Screams', '93078', '-58289', '-2854');
INSERT INTO `bbs_teleport` VALUES ('288', '1', '4', 'Stakato Nest', '89685', '-44666', '-2147');
INSERT INTO `bbs_teleport` VALUES ('289', '1', '4', 'Forest of the Dead', '52063', '-54448', '-3166');
INSERT INTO `bbs_teleport` VALUES ('290', '1', '4', 'Cursed Village', '59425', '-47753', '-2562');
INSERT INTO `bbs_teleport` VALUES ('291', '1', '4', 'Beast Farm', '52270', '-81456', '-2767');
INSERT INTO `bbs_teleport` VALUES ('292', '1', '4', 'Vally of Saints', '67992', '-72012', '-3748');
INSERT INTO `bbs_teleport` VALUES ('293', '1', '4', 'Windtail Waterfall', '40723', '-92245', '-3747');
INSERT INTO `bbs_teleport` VALUES ('294', '1', '4', 'Primeval Isle', '10468', '-24569', '-3645');
INSERT INTO `bbs_teleport` VALUES ('295', '1', '5', 'Heine', '111115', '219017', '-3547');
INSERT INTO `bbs_teleport` VALUES ('296', '1', '5', 'Alligator Island', '101712', '174198', '-2457');
INSERT INTO `bbs_teleport` VALUES ('297', '1', '5', 'Field Of Silence', '84904', '182410', '-3670');
INSERT INTO `bbs_teleport` VALUES ('298', '1', '5', 'Field Of Whispers', '86519', '211911', '-3764');
INSERT INTO `bbs_teleport` VALUES ('299', '1', '5', 'Parnassus', '149361', '172327', '-945');
INSERT INTO `bbs_teleport` VALUES ('300', '1', '5', 'Isle of Prayer', '159111', '183721', '-3720');
INSERT INTO `bbs_teleport` VALUES ('301', '1', '5', 'Chromatic Highlands', '152857', '149040', '-3280');
INSERT INTO `bbs_teleport` VALUES ('302', '1', '5', 'Garden of Eva Entrance', '85170', '241576', '-6848');
INSERT INTO `bbs_teleport` VALUES ('303', '1', '6', 'Dion', '18748', '145437', '-3132');
INSERT INTO `bbs_teleport` VALUES ('304', '1', '6', 'Floran', '17144', '170156', '-3502');
INSERT INTO `bbs_teleport` VALUES ('305', '1', '6', 'Execution Grounds', '51055', '141959', '-2869');
INSERT INTO `bbs_teleport` VALUES ('306', '1', '6', 'Cruma Tower', '17192', '114178', '-3439');
INSERT INTO `bbs_teleport` VALUES ('307', '1', '6', 'Fortress of Resistance', '47406', '111281', '-2099');
INSERT INTO `bbs_teleport` VALUES ('308', '1', '6', 'Plains of Dion', '650', '179215', '-3715');
INSERT INTO `bbs_teleport` VALUES ('309', '1', '6', 'Bee Hive', '34515', '188053', '-2971');
INSERT INTO `bbs_teleport` VALUES ('310', '1', '6', 'Tanor Canyon', '60393', '164315', '-2851');
INSERT INTO `bbs_teleport` VALUES ('311', '1', '7', 'Gludio', '-14225', '123540', '-3121');
INSERT INTO `bbs_teleport` VALUES ('312', '1', '7', 'Ruins of Agony', '-56235', '106668', '-3773');
INSERT INTO `bbs_teleport` VALUES ('313', '1', '7', 'Ruins of Despair', '-20043', '137688', '-3896');
INSERT INTO `bbs_teleport` VALUES ('314', '1', '7', 'Ants Nest', '-26111', '173692', '-4152');
INSERT INTO `bbs_teleport` VALUES ('315', '1', '7', 'South of Wastelands', '-16730', '209417', '-3664');
INSERT INTO `bbs_teleport` VALUES ('316', '1', '7', 'Wastelands', '-23403', '186599', '-4317');
INSERT INTO `bbs_teleport` VALUES ('317', '1', '8', 'Gludin', '-83063', '150791', '-3133');
INSERT INTO `bbs_teleport` VALUES ('318', '1', '8', 'Abandoned Camp', '-56742', '140569', '-2625');
INSERT INTO `bbs_teleport` VALUES ('319', '1', '8', 'Fellmere Lake', '-66931', '120296', '-3651');
INSERT INTO `bbs_teleport` VALUES ('320', '1', '8', 'Forgotten Temple', '-53838', '179285', '-4640');
INSERT INTO `bbs_teleport` VALUES ('321', '1', '8', 'Gludin Harbor', '-89199', '149962', '-3586');
INSERT INTO `bbs_teleport` VALUES ('322', '1', '8', 'Orc Barracks', '-90562', '108182', '-3546');
INSERT INTO `bbs_teleport` VALUES ('323', '1', '8', 'Langk Lizardman Dwelling', '-44772', '203530', '-3587');
INSERT INTO `bbs_teleport` VALUES ('324', '1', '8', 'Red Rock Ridge', '-42288', '198324', '-2795');
INSERT INTO `bbs_teleport` VALUES ('325', '1', '8', 'Windmill Hill', '-75435', '168842', '-3627');
INSERT INTO `bbs_teleport` VALUES ('326', '1', '8', 'Windy Hill', '-88558', '83421', '-2859');
INSERT INTO `bbs_teleport` VALUES ('327', '1', '8', 'Gludin Arena', '-86979', '142402', '-3643');
INSERT INTO `bbs_teleport` VALUES ('328', '1', '9', 'Hanters vilage', '116589', '76268', '-2734');
INSERT INTO `bbs_teleport` VALUES ('329', '1', '9', 'Hardins Academy', '105925', '109712', '-3187');
INSERT INTO `bbs_teleport` VALUES ('330', '1', '9', 'Forest Of Mirrors', '150477', '85907', '-2753');
INSERT INTO `bbs_teleport` VALUES ('331', '1', '9', 'Enchanted Valley (North)', '104413', '33734', '-3795');
INSERT INTO `bbs_teleport` VALUES ('332', '1', '9', 'Enchanted Valley (South)', '124899', '61995', '-3915');
INSERT INTO `bbs_teleport` VALUES ('333', '1', '10', 'Oren', '82321', '55139', '-1529');
INSERT INTO `bbs_teleport` VALUES ('334', '1', '10', 'Sea Of Spores', '62425', '30856', '-3779');
INSERT INTO `bbs_teleport` VALUES ('335', '1', '10', 'Ivory Tower', '85332', '16186', '-3673');
INSERT INTO `bbs_teleport` VALUES ('336', '1', '10', 'Hardins Academy', '105925', '109712', '-3187');
INSERT INTO `bbs_teleport` VALUES ('337', '1', '10', 'Northern Waterfall', '70833', '6426', '-3639');
INSERT INTO `bbs_teleport` VALUES ('338', '1', '10', 'Plains of Lizardmen', '87252', '85514', '-3056');
INSERT INTO `bbs_teleport` VALUES ('339', '1', '10', 'Sel Mahum Training Grounds', '87448', '61460', '-3664');
INSERT INTO `bbs_teleport` VALUES ('340', '1', '10', 'Outlaw Forest', '91585', '-12232', '-2435');
INSERT INTO `bbs_teleport` VALUES ('341', '1', '11', 'Schuttgart', '87358', '-141982', '-1341');
INSERT INTO `bbs_teleport` VALUES ('342', '1', '11', 'Crypts of Disgrace', '56095', '-118952', '-3290');
INSERT INTO `bbs_teleport` VALUES ('343', '1', '11', 'Den of Evil', '76860', '-125169', '-3414');
INSERT INTO `bbs_teleport` VALUES ('344', '1', '11', 'Ice Merchant Cabin', '113487', '-109888', '-865');
INSERT INTO `bbs_teleport` VALUES ('345', '1', '11', 'Valley of The Lords', '23006', '-126115', '-870');
INSERT INTO `bbs_teleport` VALUES ('346', '1', '11', 'Pavel Ruins', '88275', '-125690', '-3815');
INSERT INTO `bbs_teleport` VALUES ('347', '1', '11', 'Plunderous Plains', '113900', '-154175', '-1488');
INSERT INTO `bbs_teleport` VALUES ('348', '1', '11', 'Carons Dungeon', '69762', '-111260', '-1807');
INSERT INTO `bbs_teleport` VALUES ('349', '1', '11', 'Windtail Waterfall', '40825', '-90317', '-3095');
INSERT INTO `bbs_teleport` VALUES ('350', '1', '11', 'Archaic Laboratory', '87475', '-109835', '-3330');
INSERT INTO `bbs_teleport` VALUES ('351', '1', '11', 'Sky Wagon Relic', '117715', '-141750', '-2700');
INSERT INTO `bbs_teleport` VALUES ('352', '1', '11', 'Brigand Stronghold', '124585', '-160240', '-1180');
INSERT INTO `bbs_teleport` VALUES ('353', '1', '12', 'Dark Elven Village', '12428', '16551', '-4588');
INSERT INTO `bbs_teleport` VALUES ('354', '1', '12', 'Newbie Start Area', '28303', '11036', '-4234');
INSERT INTO `bbs_teleport` VALUES ('355', '1', '12', 'Altar Of Rites', '-45563', '73216', '-3575');
INSERT INTO `bbs_teleport` VALUES ('356', '1', '12', 'Central Waterfall', '-5162', '55702', '-3483');
INSERT INTO `bbs_teleport` VALUES ('357', '1', '12', 'South Border', '-60947', '99748', '-3719');
INSERT INTO `bbs_teleport` VALUES ('358', '1', '12', 'School Of Dark Arts', '-47129', '59678', '-3336');
INSERT INTO `bbs_teleport` VALUES ('359', '1', '12', 'Southern Part of Dark Forest', '-61095', '75104', '-3356');
INSERT INTO `bbs_teleport` VALUES ('360', '1', '12', 'Swamp', '-14162', '44879', '-3592');
INSERT INTO `bbs_teleport` VALUES ('361', '1', '12', 'Temple of Shilen', '23965', '10989', '-3723');
INSERT INTO `bbs_teleport` VALUES ('362', '1', '12', 'Neutral Zone', '-10604', '75858', '-3587');
INSERT INTO `bbs_teleport` VALUES ('363', '1', '13', 'Dwarven Village', '116551', '-182493', '-1525');
INSERT INTO `bbs_teleport` VALUES ('364', '1', '13', 'Newbie Start Area', '108460', '-174084', '-410');
INSERT INTO `bbs_teleport` VALUES ('365', '1', '13', 'Abandoned Coal Mines', '139783', '-177260', '-1539');
INSERT INTO `bbs_teleport` VALUES ('366', '1', '13', 'Mithril Mines Western', '171906', '-173316', '3445');
INSERT INTO `bbs_teleport` VALUES ('367', '1', '13', 'Mithril Mines Eastern', '178559', '-184656', '-355');
INSERT INTO `bbs_teleport` VALUES ('368', '1', '13', 'The Northeast Coast', '169008', '-208272', '-3504');
INSERT INTO `bbs_teleport` VALUES ('369', '1', '14', 'Elven Village', '45873', '49288', '-3064');
INSERT INTO `bbs_teleport` VALUES ('370', '1', '14', 'Newbie Start Area', '46093', '41409', '-3509');
INSERT INTO `bbs_teleport` VALUES ('371', '1', '14', 'Elven Fortress', '29205', '74948', '-3775');
INSERT INTO `bbs_teleport` VALUES ('372', '1', '14', 'Elven Forest', '21383', '51103', '-3683');
INSERT INTO `bbs_teleport` VALUES ('373', '1', '14', 'Neutral Zone', '-10634', '75908', '-3587');
INSERT INTO `bbs_teleport` VALUES ('374', '1', '14', 'Lake Iris', '51746', '71559', '-3427');
INSERT INTO `bbs_teleport` VALUES ('375', '1', '15', 'Kamael Village', '-116934', '46616', '368');
INSERT INTO `bbs_teleport` VALUES ('376', '1', '15', 'Newbie Start Area', '-125760', '38115', '1235');
INSERT INTO `bbs_teleport` VALUES ('377', '1', '15', 'Stronghold I', '-122201', '73090', '-2871');
INSERT INTO `bbs_teleport` VALUES ('378', '1', '15', 'Stronghold II', '-95267', '52168', '-2029');
INSERT INTO `bbs_teleport` VALUES ('379', '1', '15', 'Stronghold III', '-86077', '37332', '-1998');
INSERT INTO `bbs_teleport` VALUES ('380', '1', '15', 'Nornils Cave', '-86976', '43251', '-2684');
INSERT INTO `bbs_teleport` VALUES ('381', '1', '15', 'Nornils Garden', '-84757', '60009', '-2581');
INSERT INTO `bbs_teleport` VALUES ('382', '1', '15', 'Soul Harbor', '-73696', '53507', '-3680');
INSERT INTO `bbs_teleport` VALUES ('383', '1', '16', 'Orc Village', '-44133', '-113911', '-244');
INSERT INTO `bbs_teleport` VALUES ('384', '1', '16', 'Newbie Start Area', '-56631', '-113602', '-677');
INSERT INTO `bbs_teleport` VALUES ('385', '1', '16', 'Cave of Trials', '9954', '-112487', '-2470');
INSERT INTO `bbs_teleport` VALUES ('386', '1', '16', 'Frozen Waterfalls', '9621', '-139945', '-1353');
INSERT INTO `bbs_teleport` VALUES ('387', '1', '16', 'South Coast', '-37955', '-100767', '-3774');
INSERT INTO `bbs_teleport` VALUES ('388', '1', '16', 'Immortal Plateau', '-4160', '-80067', '-2691');
INSERT INTO `bbs_teleport` VALUES ('389', '1', '16', 'The Immortal Plateau', '-10937', '-117499', '-2459');
INSERT INTO `bbs_teleport` VALUES ('390', '1', '17', 'Talking Island Village', '-82687', '243157', '-3734');
INSERT INTO `bbs_teleport` VALUES ('391', '1', '17', 'Newbie Start Area - Fighter', '-71471', '258229', '-3126');
INSERT INTO `bbs_teleport` VALUES ('392', '1', '17', 'Newbie Start Area - Mage', '-90414', '248424', '-3570');
INSERT INTO `bbs_teleport` VALUES ('393', '1', '17', 'Elven Ruins', '48736', '248463', '-6162');
INSERT INTO `bbs_teleport` VALUES ('394', '1', '17', 'Northern Coast', '-101294', '212553', '-3093');
INSERT INTO `bbs_teleport` VALUES ('395', '1', '17', 'Obelisk Of Victory', '-99746', '237538', '-3572');
INSERT INTO `bbs_teleport` VALUES ('396', '1', '17', 'Talking Island Harbor', '-96041', '261133', '-3483');
INSERT INTO `bbs_teleport` VALUES ('397', '1', '18', 'Cat Heretics Entrance', '-53187', '-250271', '-7906');
INSERT INTO `bbs_teleport` VALUES ('398', '1', '18', 'Cat Branded Entrance', '46529', '170248', '-4979');
INSERT INTO `bbs_teleport` VALUES ('399', '1', '18', 'Cat Apostate Entrance', '-20248', '-250791', '-8163');
INSERT INTO `bbs_teleport` VALUES ('400', '1', '18', 'Cat Witch Entrance', '140404', '79678', '-5431');
INSERT INTO `bbs_teleport` VALUES ('401', '1', '18', 'Cat DarkOmen Entrance', '-19500', '13508', '-4905');
INSERT INTO `bbs_teleport` VALUES ('402', '1', '18', 'Cat Forbidden Path Entrance', '12519', '-248498', '-9580');
INSERT INTO `bbs_teleport` VALUES ('403', '1', '18', 'Necro Saints Entrance', '-41570', '209785', '-5089');
INSERT INTO `bbs_teleport` VALUES ('404', '1', '18', 'Necro Pilgrims Entrance', '45251', '123890', '-5415');
INSERT INTO `bbs_teleport` VALUES ('405', '1', '18', 'Necro Worshippers Entrance', '111273', '174015', '-5417');
INSERT INTO `bbs_teleport` VALUES ('406', '1', '18', 'Necro Patriots Entrance', '-21726', '77385', '-5177');
INSERT INTO `bbs_teleport` VALUES ('407', '1', '18', 'Necro Ascetics Entrance', '-52254', '79103', '-4743');
INSERT INTO `bbs_teleport` VALUES ('408', '1', '18', 'Necro Martyrs Entrance', '118308', '132800', '-4833');
INSERT INTO `bbs_teleport` VALUES ('409', '1', '18', 'Necro Sacrifice Entrance', '83000', '209213', '-5443');
INSERT INTO `bbs_teleport` VALUES ('410', '1', '18', 'Necro Disciples Entrance', '172251', '-17605', '-4903');
