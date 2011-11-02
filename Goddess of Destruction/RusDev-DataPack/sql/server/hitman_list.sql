CREATE TABLE IF NOT EXISTS `hitman_list` (
  `targetId` int(16) NOT NULL default 0,
  `clientId` int(16) NOT NULL default 0,
  `target_name` varchar(30) NOT NULL default '',
  `itemId` int NOT NULL default 57,
  `bounty` BIGINT UNSIGNED NOT NULL default 0,
  `pending_delete` int(16) NOT NULL default 0,
  PRIMARY KEY  (`targetId`)
);