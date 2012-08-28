SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `ban_hwid`
-- ----------------------------
DROP TABLE IF EXISTS `ban_hwid`;
CREATE TABLE `ban_hwid` (
  `hwid` varchar(8) NOT NULL default '',
  `charId` int(11) default NULL,
  PRIMARY KEY  (`hwid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

