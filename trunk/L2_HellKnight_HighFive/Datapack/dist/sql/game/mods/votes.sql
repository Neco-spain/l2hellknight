DROP TABLE IF EXISTS `votes`;
CREATE TABLE `votes` (
  `id` int(6) NOT NULL,
  `vote` int(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


INSERT INTO `votes` VALUES ('1', '0');
