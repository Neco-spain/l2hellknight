CREATE TABLE IF NOT EXISTS `hellbound` (
  `name` INT UNSIGNED NOT NULL DEFAULT 0,
  `hb_points` INT UNSIGNED NOT NULL DEFAULT 0,
  `hb_level` INT UNSIGNED NOT NULL DEFAULT 1,
  `unlocked` INT UNSIGNED NOT NULL DEFAULT 0,
  `dummy` INT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY  (`name`,`hb_points`,`hb_level`,`unlocked`,`dummy`)
) ENGINE=MyISAM AVG_ROW_LENGTH=26 CHARACTER SET 'utf8' COLLATE 'utf8_general_ci';
