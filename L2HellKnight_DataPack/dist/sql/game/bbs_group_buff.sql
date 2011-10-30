DROP TABLE IF EXISTS `bbs_group_buff`;
CREATE TABLE `bbs_group_buff` (
  `id` int(2) NOT NULL default '0',
  `name` varchar(45) default 'No name',
  `price` int(11) default '0',
  PRIMARY KEY  (`id`)
) DEFAULT CHARSET=utf8;

INSERT INTO `bbs_group_buff` VALUES 
(1, 'Warrior', 5000),
(2, 'Mage', 5000),
(3, 'Improved', 10000),
(4, 'Songs', 7500),
(5, 'Dances', 7500),
(6, 'Prophecy', 10000);