CREATE TABLE IF NOT EXISTS `ctf` (
  `eventName` varchar(255) NOT NULL DEFAULT '',
  `eventDesc` varchar(255) NOT NULL DEFAULT '',
  `joiningLocation` varchar(255) NOT NULL DEFAULT '',
  `minlvl` int(4) NOT NULL DEFAULT '0',
  `maxlvl` int(4) NOT NULL DEFAULT '0',
  `npcId` int(11) NOT NULL DEFAULT '0',
  `npcX` int(11) NOT NULL DEFAULT '0',
  `npcY` int(11) NOT NULL DEFAULT '0',
  `npcZ` int(11) NOT NULL DEFAULT '0',
  `npcHeading` int(11) NOT NULL DEFAULT '0',
  `rewardId` int(11) NOT NULL DEFAULT '0',
  `rewardAmount` int(11) NOT NULL DEFAULT '0',
  `teamsCount` int(4) NOT NULL DEFAULT '0',
  `joinTime` int(11) NOT NULL DEFAULT '0',
  `eventTime` int(11) NOT NULL DEFAULT '0',
  `minPlayers` int(4) NOT NULL DEFAULT '0',
  `maxPlayers` int(4) NOT NULL DEFAULT '0',
  `flagHoldTime` int(11) NOT NULL
) ENGINE = MYISAM DEFAULT CHARSET=utf8;

INSERT INTO `ctf` (`eventName`, `eventDesc`, `joiningLocation`, `minlvl`, `maxlvl`, `npcId`, `npcX`, `npcY`, `npcZ`, `npcHeading`, `rewardId`, `rewardAmount`, `teamsCount`, `joinTime`, `eventTime`, `minPlayers`, `maxPlayers`, `flagHoldTime`) VALUES
('Capture The Flag', 'Capture Flag', 'Giran', 85, 85, 70009, 83425, 148585, -3406, 33068, 3470, 10, 2, 15, 15, 2, 100, 1200);
