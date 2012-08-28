DROP TABLE IF EXISTS `announce_records`;
CREATE TABLE `announce_records` (
  `online` varchar(255) NOT NULL default '1',
  `date` varchar(255) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;