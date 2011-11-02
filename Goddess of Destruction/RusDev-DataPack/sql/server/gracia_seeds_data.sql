CREATE TABLE IF NOT EXISTS `gracia_seeds_data` (
  `var`  VARCHAR(25) NOT NULL DEFAULT '',
  `value` VARCHAR(255) ,
  PRIMARY KEY (`var`)
);

INSERT IGNORE INTO `gracia_seeds_data` VALUES
('SoDTiatKilled','0'),
('SoDState','1'),
('SoDLastStateChangeDate','0');
