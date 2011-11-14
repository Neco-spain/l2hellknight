CREATE TABLE IF NOT EXISTS `character_custom_hero` (
`charId`  int(10) NOT NULL ,
`hero`  smallint(1) NULL DEFAULT '-1' ,
`hero_reg_time`  bigint(40) NULL ,
`hero_time`  bigint(40) NULL ,
PRIMARY KEY (`charId`),
INDEX `charId` (`charId`) 
);

