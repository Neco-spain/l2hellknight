--
-- Table structure for 'clanhall_siege'
--
CREATE TABLE IF NOT EXISTS `clanhall_siege` (
  `id` int(11) NOT NULL,
  `name` varchar(40) NOT NULL,
  `siege_data` decimal(20,0) NOT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;
-- ----------------------------
-- Records 
-- ----------------------------

INSERT INTO `clanhall_siege` VALUES
(21, 'Fortress of Resistance', 0),
(34, 'Devastated Castle', 0),
(35, 'Bandit Stronghold', 0);
