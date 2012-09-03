CREATE TABLE IF NOT EXISTS `items` (
  `object_id` int NOT NULL default 0,
  `owner_id` int NOT NULL,
  `item_id` smallint unsigned NOT NULL default 0,
  `count` bigint unsigned NOT NULL default 0,
  `enchant_level` smallint unsigned NOT NULL default 0,
  `class` ENUM('CONSUMABLE','MISC','EQUIPMENT','MATHERIALS','OTHER','PIECES','RECIPIES','SPELLBOOKS') NOT NULL DEFAULT 'OTHER',
  `loc` ENUM('CLANWH','CWH_BACK','FREIGHT','INVENTORY','LEASE','PAPERDOLL','VOID','WAREHOUSE','MONSTER') NOT NULL,
  `loc_data` int default NULL,
  `custom_type1` smallint unsigned NOT NULL default 0,
  `custom_type2` smallint unsigned NOT NULL default 0,
  `shadow_life_time` int NOT NULL default 0,
  `flags` int NOT NULL default 0,
  PRIMARY KEY  (`object_id`),
  KEY `key_owner_id` (`owner_id`),
  KEY `key_loc` (`loc`),
  KEY `key_item_id` (`item_id`),
  KEY `key_class` (`class`)
) ENGINE=MyISAM;
