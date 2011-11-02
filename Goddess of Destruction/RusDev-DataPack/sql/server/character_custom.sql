CREATE TABLE IF NOT EXISTS `character_custom` (
`type`  enum('COLORNAME','COLORTITLE','DONATOR','NOBLE','HERO') NOT NULL ,
`charId`  int(10) NOT NULL ,
`value`  int(8) NOT NULL DEFAULT 1 ,
`regTime`  bigint(40) NOT NULL DEFAULT 0 ,
`time`  bigint(40) NOT NULL DEFAULT 0 ,
PRIMARY KEY (`type`, `charId`) ,
INDEX `type` (`type`) ,
INDEX `charId` (`charId`)
);