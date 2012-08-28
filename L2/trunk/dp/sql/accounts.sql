-- ---------------------------
-- Table structure for accounts
-- ---------------------------
CREATE TABLE accounts (
  `login` VARCHAR(45) NOT NULL default '',
  `password` VARCHAR(45) ,
  `lastactive` DECIMAL(20),
  `access_level` INT,
  `lastIP` VARCHAR(20),
  `lastServer` int(4) default 1,
  `IPBlock` BOOL DEFAULT '0',
  `HWIDBlock` VARCHAR(45),
  `HWIDBlockON` int(4) default 0,
  PRIMARY KEY (`login`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;