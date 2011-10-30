DROP TABLE IF EXISTS `bbs_buff`;
CREATE TABLE `bbs_buff` (
  `id_skill` int(5) NOT NULL default '0',
  `lvl_skill` int(2) NOT NULL default '1',
  `id_group` int(2) default '1',
  PRIMARY KEY  (`id_skill`,`lvl_skill`,`id_group`)
) DEFAULT CHARSET=utf8;

INSERT INTO `bbs_buff` VALUES 
(1068, 3, 1), -- Might
(1388, 3, 1), -- Greater Might
(1040, 3, 1), -- Shield
(1389, 3, 1), -- Greater Shield
(1204, 2, 1), -- Wind Walk
(1035, 4, 1), -- Mental Shield
(1077, 3, 1), -- Focus
(1242, 3, 1), -- Death Whisper
(1062, 2, 1), -- Berserker Spirit
(1045, 6, 1), -- Blessed Body
(1048, 6, 1), -- Blessed Soul
(1086, 2, 1), -- Haste
(1036, 2, 1), -- Magic Barrier
(1257, 3, 1), -- Decrease Weight
(1268, 3, 1), -- Vampiric Rage
(1259, 4, 1), -- Resist Shock
(1243, 6, 1), -- Bless Shield
(1304, 4, 1), -- Advanced Block
(1397, 3, 1), -- Clarity
(1047, 4, 1), -- Mana Regeneration
(1044, 3, 1), -- Regeneration
(1240, 3, 1), -- Guidance
(1044, 3, 2), -- Regeneration
(1047, 4, 2), -- Mana Regeneration
(1243, 6, 2), -- Bless Shield
(1304, 4, 2), -- Advanced Block
(1397, 3, 2), -- Clarity
(1259, 4, 2), -- Resist Shock
(1257, 3, 2), -- Decrease Weight
(1059, 3, 2), -- Empower
(1036, 2, 2), -- Magic Barrier
(1303, 2, 2), -- Wild Magic
(1048, 6, 2), -- Blessed Soul
(1045, 6, 2), -- Blessed Body
(1062, 2, 2), -- Berserker Spirit
(1035, 4, 2), -- Mental Shield
(1204, 2, 2), -- Wind Walk
(1040, 3, 2), -- Shield
(1389, 3, 2), -- Greater Shield
(1085, 3, 2), -- Acumen
(1078, 6, 2), -- Concentration
(1499, 1, 3), -- Improved Combat
(1501, 1, 3), -- Improved Condition
(1502, 1, 3), -- Improved Critical Attack
(1500, 1, 3), -- Improved Magic
(1504, 1, 3), -- Improved Movement
(1503, 1, 3), -- Improved Shield Defense
(1519, 1, 3), -- Chant of Blood Awakening
(267, 1, 4), -- Song of Warding
(268, 1, 4), -- Song of Wind
(264, 1, 4), -- Song of Earth
(269, 1, 4), -- Song of Hunter
(265, 1, 4), -- Song of Life
(266, 1, 4), -- Song of Water
(304, 1, 4), -- Song of Vitality 
(363, 1, 4), -- Song of Meditation
(349, 1, 4), -- Song of Renewal
(364, 1, 4), -- Song of Champion
(274, 1, 5), -- Dance of Fire
(275, 1, 5), -- Dance of Fury
(271, 1, 5), -- Dance of the Warrior
(272, 1, 5), -- Dance of Inspiration
(310, 1, 5), -- Dance of the Vampire
(277, 1, 5), -- Dance of Light
(276, 1, 5), -- Dance of Concentration
(273, 1, 5), -- Dance of the Mystic
(365, 1, 5), -- Siren's Dance
(1356, 1, 6), -- Prophecy of Fire
(1355, 1, 6), -- Prophecy of Water
(1357, 1, 6), -- Prophecy of Wind
(1363, 1, 6), -- Chant of Victory
(1413, 1, 6), -- Magnus' Chant
(1414, 1, 6), -- Victory of Pa'agrio <!
-- x 50
(1087, 3, 2), -- Agility 
(1362, 1, 6), -- Chant of Spirit
(1461, 1, 6), -- Chant of Protection
(4699, 13, 6), -- Blessing of Queen
(4700, 13, 6), -- Gift of Queen
(4702, 13, 6), -- Blessing of Seraphim
(4703, 13, 6), -- Gift of Seraphim
(306, 1, 4), -- Song of Flame Guard
(270, 1, 4), -- Song of Invocation
(308, 1, 4), -- Song of Storm Guard
(307, 1, 5), -- Dance of Aqua Guard
(309, 1, 5), -- Dance of Earth Guard
(311, 1, 5), -- Dance of Protection
(1073, 1, 6); -- Kiss of Eva
-- x 2k
-- (529, 1, 4), -- Song of Elemental
-- (305, 1, 6), -- Song of Vengeance
-- (530, 1, 5), -- Dance of Alignment
-- (915, 1, 5), -- Dance of Berserker
-- (366, 1, 5), -- Dance of Shadows
-- (1307, 3, 6), -- Prayer
-- (1032, 3, 6), -- Invigor 
-- (1191, 3, 6), -- Resist Fire
-- (1182, 3, 6), -- Resist Aqua
-- (1189, 3, 6), -- Resist Wind
-- (1033, 3, 6), -- Resist Poison
-- (1392, 3, 6), -- Holy Resistance
-- (1383, 3, 6), -- Unholy Resistance
-- (1352, 1, 6), -- Elemental Protection
-- (1353, 1, 6), -- Divine Protection
-- (1542, 1, 6), -- Counter Critical ?
-- (1284, 3, 6), -- Chant of Revenge
-- (828, 1, 6), -- Case Harden
-- (830, 1, 6), -- Embroider
-- (829, 1, 6), -- Hard Tanning
-- (827, 1, 6), -- Restring
-- (825, 1, 6), -- Sharp Edge
-- (826, 1, 6), -- Spike
-- (1460, 1, 6), -- Mana Gain
-- (1416, 1, 6); -- Pa'agrio's Fist-->