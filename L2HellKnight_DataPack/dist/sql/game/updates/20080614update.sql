ALTER TABLE `custom_armorsets` ADD `skill_lvl` TINYINT UNSIGNED NOT NULL default 0 AFTER `skill_id`;
ALTER TABLE `custom_armorsets` MODIFY id SMALLINT UNSIGNED NOT NULL auto_increment;
ALTER TABLE `custom_armorsets` MODIFY chest SMALLINT UNSIGNED NOT NULL default 0;
ALTER TABLE `custom_armorsets` MODIFY legs SMALLINT UNSIGNED NOT NULL default 0;
ALTER TABLE `custom_armorsets` MODIFY head SMALLINT UNSIGNED NOT NULL default 0;
ALTER TABLE `custom_armorsets` MODIFY gloves SMALLINT UNSIGNED NOT NULL default 0;
ALTER TABLE `custom_armorsets` MODIFY feet SMALLINT UNSIGNED NOT NULL default 0;
ALTER TABLE `custom_armorsets` MODIFY skill_id SMALLINT UNSIGNED NOT NULL default 0;
ALTER TABLE `custom_armorsets` MODIFY skill_lvl TINYINT UNSIGNED NOT NULL default 0;
ALTER TABLE `custom_armorsets` MODIFY shield SMALLINT UNSIGNED NOT NULL default 0;
ALTER TABLE `custom_armorsets` MODIFY shield_skill_id SMALLINT UNSIGNED NOT NULL default 0;
ALTER TABLE `custom_armorsets` MODIFY enchant6skill SMALLINT UNSIGNED NOT NULL default 0;