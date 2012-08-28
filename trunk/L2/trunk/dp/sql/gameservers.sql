CREATE TABLE IF NOT EXISTS `gameservers` (
  `server_id` int(11) NOT NULL default '0',
  `hexid` varchar(50) NOT NULL default '',
  `host` varchar(50) NOT NULL default '',
  `hide` int(1) NOT NULL default '0',
  PRIMARY KEY  (`server_id`)
) ;