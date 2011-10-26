-- -----------------------------------------------------------------------------------------------------
-- AIO Normal Buff table
-- -----------------------------------------------------------------------------------------------------

DROP TABLE IF EXISTS `aio_buffs`;
CREATE TABLE IF NOT EXISTS `aio_buffs` (
  `category` varchar(45) DEFAULT NULL,
  `buff_name` varchar(45) DEFAULT NULL,
  `buff_id` int(10) DEFAULT NULL,
  `buff_lvl` int(10) DEFAULT NULL
) ENGINE = MYISAM DEFAULT CHARSET=utf8;

INSERT INTO `aio_buffs` (`category`, `buff_name`, `buff_id`, `buff_lvl`) VALUES
('Prophet','Might', 1068, 3), 
('Prophet','Shield', 1040, 3),
('Prophet','Focus', 1077, 3),
('Prophet','Haste', 1086, 2),
('Prophet','Acumen', 1085, 3),
('Prophet','Death Whisper', 1242, 3),
('Prophet','Guidance', 1240, 3),
('Prophet','Mental Shield', 1035, 4),
('Prophet','Blessed Sould', 1048, 6),
('Prophet','Blessed Body', 1045, 6),
('Prophet','Invigor', 1032, 3),
('Prophet','Regeneration', 1044, 3),
('Prophet','Bless Shield', 1243, 6),
('Prophet','Wild Magic', 1303, 2),
('Prophet','Advanced Block', 1304, 3),
('Prophet','Wind Walk', 1204, 2),
('Prophet','Clarity', 1397, 3),
('Prophet','Empower', 1059, 3),
('Prophet','Concentration', 1078, 6),
('Prophet','Agility', 1087, 3),
('Prophet','Berserker Spirit', 1062, 2),
('Prophet','Greater Shield', 1389, 3),
('Prophet','Greater Might', 1388, 3),
('Prophet','Vampiric Rage', 1268, 4),
('Prophet','Prophecy of Water', 1355, 1),
('Prophet','Prophecy of Wind', 1357, 1),
('Prophet','Prophecy of Fire', 1356, 1),
('Dances','Dance of Fire', 274, 1),
('Dances','Dance of Light', 277, 1),
('Dances','Dance of Inspiration', 272, 1),
('Dances','Dance of the Mystic', 273, 1),
('Dances','Dance of Concentration', 276, 1),
('Dances','Dance of the Warrior', 271, 1),
('Dances','Dance of Fury', 275, 1),
('Dances','Dance of Earth Guard', 309, 1),
('Dances','Dance of Protection', 311, 1),
('Dances','Dance of Aqua Guard', 307, 1),
('Dances','Dance of Vampire', 310, 1),
('Dances','Siren Dance', 365, 1),
('Dances','Dance of Alignment', 530, 1),
('Songs','Song of Warding', 267, 1),
('Songs','Song of Invocation', 270, 1),
('Songs','Song of Wind', 268, 1),
('Songs','Song of Hunter', 269, 1),
('Songs','Song of Water', 266, 1),
('Songs','Song of Flame War', 306, 1),
('Songs','Song of Vitality', 304, 1),
('Songs','Song of Storm Guard', 308, 1),
('Songs','Song of Vengeance', 305, 1),
('Songs','Song of Renewal', 349, 1),
('Songs','Song of Champion', 364, 1),
('Songs','Song of Meditation', 363, 1),
('Chants','Chant of Victory', 1363, 1),
('Chants','Magnus Chant', 1413, 1),
('Pets','Blessing of Queen', 4699, 8),
('Pets','Gift of Queen', 4700, 8),
('Pets','Blessing of Seraphim', 4702, 8),
('Pets','Gift of Seraphim', 4703, 8),
('Improved','Combat', 1499, 1),
('Improved','Condition', 1501, 1),
('Improved','Critical Attack', 1502, 1),
('Improved','Magic', 1500, 1),
('Improved','Movement', 1504, 1),
('Improved','Shield Defense', 1503, 1),
('Improved','Blood Awakening', 1519, 1);

-- -----------------------------------------------------------------------------------------------------
-- AIO Scheme system buffs
-- -----------------------------------------------------------------------------------------------------
DROP TABLE IF EXISTS `aio_scheme_buffs`;
CREATE TABLE IF NOT EXISTS `aio_scheme_buffs` (
  `buff_name` varchar(45) CHARACTER SET latin1 COLLATE latin1_spanish_ci DEFAULT NULL,
  `buff_id` int(10) DEFAULT NULL,
  `buff_lvl` int(10) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

INSERT INTO `aio_scheme_buffs` (`buff_name`, `buff_id`, `buff_lvl`) VALUES
('Might', 1028, 3),
('Shield', 1040, 3),
('Wind Walk', 1204, 2),
('Focus', 1077, 3),
('Haste', 1086, 2),
('Acumen', 1085, 3),
('Death Whisper', 1242, 3),
('Guidance', 1240, 3),
('Mental Shield', 1035, 4),
('Blessed Sould', 1048, 6),
('Blessed Body', 1045, 6),
('Invigor', 1032, 3),
('Regeneration', 1044, 3),
('Bless Shield', 1243, 6),
('Wild Magic', 1303, 2),
('Advanced Block', 1304, 3),
('Clarity', 1397, 3),
('Empower', 1059, 3),
('Concentration', 1078, 6),
('Agility', 1087, 3),
('Berserker Spirit', 1062, 2),
('Greater Shield', 1389, 3),
('Greater Might', 1388, 3),
('Vampiric Rage', 1268, 4),
('Prophecy of Water', 1355, 1),
('Prophecy of Wind', 1357, 1),
('Prophecy of Fire', 1356, 1),
('Dance of Fire', 274, 1),
('Dance of Light', 277, 1),
('Dance of Inspiration', 272, 1),
('Dance of the Mystic', 273, 1),
('Dance of Concentration', 276, 1),
('Dance of the Warrior', 271, 1),
('Dance of Fury', 275, 1),
('Dance of Earth Guard', 309, 1),
('Dance of Protection', 311, 1),
('Dance of Aqua Guard', 307, 1),
('Dance of Vampire', 310, 1),
('Siren Dance', 365, 1),
('Dance of Alignment', 530, 1),
('Song of Warding', 267, 1),
('Song of Invocation', 270, 1),
('Song of Wind', 268, 1),
('Song of Hunter', 269, 1),
('Song of Water', 266, 1),
('Song of Flame War', 306, 1),
('Song of Vitality', 304, 1),
('Song of Storm Guard', 308, 1),
('Song of Vengeance', 305, 1),
('Song of Renewal', 349, 1),
('Song of Champion', 364, 1),
('Song of Meditation', 363, 1),
('Chant of Victory', 1363, 1),
('Magnus Chant', 1413, 1),
('Blessing of Queen', 4699, 8),
('Gift of Queen', 4700, 8),
('Blessing of Seraphim', 4702, 8),
('Gift of Seraphim', 4703, 8),
('Improved Combat', 1499, 1),
('Improved Condition', 1501, 1),
('Improved Critical Attack', 1502, 1),
('Improved Magic', 1500, 1),
('Improved Movement', 1504, 1),
('Improved Shield Defense', 1503, 1),
('Chant of Blood Awakening', 1519, 1);


-- -----------------------------------------------------------------------------------------------------
-- AIO Scheme system profiles table
-- -----------------------------------------------------------------------------------------------------
DROP TABLE IF EXISTS `aio_scheme_profiles`;
CREATE TABLE IF NOT EXISTS `aio_scheme_profiles` (
  `charId` int(10) unsigned NOT NULL,
  `profile` varchar(45) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


-- -----------------------------------------------------------------------------------------------------
-- AIO Scheme system profiles buff table
-- -----------------------------------------------------------------------------------------------------
DROP TABLE IF EXISTS `aio_scheme_profiles_buffs`;
CREATE TABLE IF NOT EXISTS `aio_scheme_profiles_buffs` (
  `charId` int(10) unsigned NOT NULL,
  `profile` varchar(45) DEFAULT NULL,
  `buff_id` varchar(45) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


-- -----------------------------------------------------------------------------------------------------
-- AIO Gatekeeper table
-- -----------------------------------------------------------------------------------------------------
DROP TABLE IF EXISTS `aio_teleports_categories`;
CREATE TABLE IF NOT EXISTS `aio_teleports_categories` (
  `category_id` int(10) DEFAULT NULL,
  `category` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`category_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

INSERT INTO aio_teleports_categories VALUES
(1, 'Towns'),
(2, 'Starting Village');


DROP TABLE IF EXISTS `aio_teleports`;
CREATE TABLE IF NOT EXISTS `aio_teleports` (
  `id` int(10) DEFAULT NULL,
  `category` varchar(45) DEFAULT NULL,
  `tpname` varchar(45) DEFAULT NULL,
  `x` int(10) DEFAULT NULL,
  `y` int(10) DEFAULT NULL,
  `z` int(10) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

INSERT INTO aio_teleports VALUES 
	(1, 'Towns', 'Giran', 83400, 147943, -3404),
	(2, 'Towns', 'Aden', 146331, 25762, -2018),
	(3, 'Towns', 'Oren', 82956,53162,-1495),
	(4, 'Towns', 'Dion', 15670,142983,-2705),
	(5, 'Towns', 'Floran', 17430,170103,-3496),
	(6, 'Towns', 'Hunters', 117110,76883,-2695),
	(7, 'Towns', 'Goddard', 147978,-55408,-2728),
	(8, 'Towns', 'Rune', 43799,-47727,-798),
	(9, 'Towns', 'Schuttgart', 87386,-143246,-1293),
	(1, 'Starting Vilalge', 'Talking Island', -84318,244579,-3730),
	(2, 'Starting Village', 'Dark Elf Village', 9745,15606,-4574),
	(3, 'Starting Village', 'Elven Village', 46934,51467,-2977),
	(4, 'Starting Village', 'Dwarven Village', 115113,-178212,-901),
	(5, 'Starting Village', 'Orc Village', -44836,-112524,-235),
	(6, 'Starting Village', 'Kamael Vilalge', -117251,46771,360);

