SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for requestplayer
-- ----------------------------
DROP TABLE IF EXISTS `requestplayer`;
CREATE TABLE `requestplayer` (
  `char_id` int(11) unsigned NOT NULL,
  `Goal_name` varchar(35) CHARACTER SET utf8 DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `count_kill` int(11) DEFAULT NULL,
  `time` text,
  KEY `key_mobId` (`char_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

