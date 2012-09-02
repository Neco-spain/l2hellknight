CREATE TABLE IF NOT EXISTS `character_hennas` (
	`char_obj_id` INT NOT NULL DEFAULT '0',
	`symbol_id` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	`slot` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	`class_index` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	INDEX (`char_obj_id`)
) ENGINE=MyISAM;