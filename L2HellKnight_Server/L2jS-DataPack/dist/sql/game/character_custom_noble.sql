CREATE TABLE IF NOT EXISTS `character_custom_noble` (
`charId`  int(10) NOT NULL ,
`noble`  smallint(1) NULL DEFAULT '-1' ,
`noble_reg_time`  bigint(40) NULL ,
`noble_time`  bigint(40) NULL ,
PRIMARY KEY (`charId`),
INDEX `charId` (`charId`) 
);

