CREATE TABLE IF NOT EXISTS `access_levels` (
  `accessLevel` MEDIUMINT(9) NOT NULL,
  `name` VARCHAR(255) NOT NULL DEFAULT '',
  `nameColor` CHAR(6) NOT NULL DEFAULT 'FFFFFF',
  `titleColor` CHAR(6) NOT NULL DEFAULT 'FFFFFF',
  `childAccess` VARCHAR(255) NOT NULL DEFAULT '',
  `isGm` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0,
  `allowPeaceAttack` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0,
  `allowFixedRes` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0,
  `allowTransaction` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0,
  `allowAltg` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0,
  `giveDamage` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0,
  `takeAggro` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0,
  `gainExp` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY  (`accessLevel`)
) ENGINE = MYISAM DEFAULT CHARSET=utf8;

INSERT IGNORE INTO `access_levels` VALUES 
(1,'Admin','0FF000','0FF000','2;3;4;5;6;7',1,1,1,1,1,1,1,1),
(2,'Head GM','0C0000','0C0000','5;6;7',0,0,1,1,1,1,1,1),    
(3,'Event GM','00C000','00C000','5;6;7',0,0,1,0,1,0,0,0),   
(4,'Support GM','000C00','000C00','5;6;7',0,0,1,0,1,0,0,0), 
(5,'General GM','0000C0','0000C0','6;7',0,0,1,0,1,0,0,0),   
(6,'Test GM','FFFFFF','FFFFFF','','0',0,1,0,1,0,0,0),
(7,'Chat Moderator','-1','-1','','0',0,0,1,0,1,1,1);