CREATE TABLE IF NOT EXISTS `ctf_teams` (
  `teamId` int(4) NOT NULL DEFAULT '0',
  `teamName` varchar(255) NOT NULL DEFAULT '',
  `teamX` int(11) NOT NULL DEFAULT '0',
  `teamY` int(11) NOT NULL DEFAULT '0',
  `teamZ` int(11) NOT NULL DEFAULT '0',
  `teamColor` int(11) NOT NULL DEFAULT '0',
  `flagX` int(11) NOT NULL DEFAULT '0',
  `flagY` int(11) NOT NULL DEFAULT '0',
  `flagZ` int(11) NOT NULL DEFAULT '0',
  `teamBaseX` int(11) NOT NULL DEFAULT '0',
  `teamBaseY` int(11) NOT NULL DEFAULT '0',
  `teamBaseZ` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`teamId`)
) ENGINE = MYISAM DEFAULT CHARSET=utf8;

INSERT INTO `ctf_teams` (`teamId`, `teamName`, `teamX`, `teamY`, `teamZ`, `teamColor`, `flagX`, `flagY`, `flagZ`, `teamBaseX`, `teamBaseY`, `teamBaseZ`) VALUES
(1, 'Blue', -6238, 246661, -1901, 255, -3986, 246678, -1916, 0, 0, 0),
(0, 'Red', -7443, 241418, -1900, 16711680, -8696, 242454, -1875, 0, 0, 0);
