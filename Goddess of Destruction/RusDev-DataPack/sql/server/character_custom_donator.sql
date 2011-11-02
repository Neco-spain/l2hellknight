CREATE TABLE IF NOT EXISTS `character_custom_donator` (
`charId`  int(10) NOT NULL ,
`donator`  smallint(1) NULL DEFAULT '-1' ,
`donator_reg_time`  bigint(40) NULL ,
`donator_time`  bigint(40) NULL ,
PRIMARY KEY (`charId`),
INDEX `charId` (`charId`) 
);

