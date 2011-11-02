CREATE TABLE IF NOT EXISTS `character_custom_color` (
`charId`  int(10) NOT NULL ,
`color`  int(8) NULL DEFAULT '-1' ,
`color_reg_time`  bigint(40) NULL ,
`color_time`  bigint(40) NULL ,
PRIMARY KEY (`charId`),
INDEX `charId` (`charId`) 
);

