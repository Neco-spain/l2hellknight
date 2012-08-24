ALTER TABLE `characters` ADD `bot_report_points` int(10) NOT NULL DEFAULT 7 AFTER `nevit_points` ;

CREATE TABLE IF NOT EXISTS `bot_report` (
  `report_id` int(10) NOT NULL auto_increment,
  `reported_name` varchar(45) DEFAULT NULL,
  `reported_objectId` int(10) DEFAULT NULL,
  `reporter_name` varchar(45) DEFAULT NULL,
  `reporter_objectId` int(10) DEFAULT NULL,
  `date` DECIMAL(20,0) NOT NULL default 0,
  `read` enum('true','false') DEFAULT 'false' NOT NULL,
  PRIMARY KEY (`report_id`)
);
CREATE TABLE IF NOT EXISTS `bot_reported_punish` (
  `charId` int(11) NOT NULL DEFAULT '0',
  `punish_type` varchar(45) DEFAULT NULL,
  `time_left` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`charId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1
