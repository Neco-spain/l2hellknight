DROP TABLE IF EXISTS `teleport`;
CREATE TABLE `teleport` (
  `Description` varchar(75) DEFAULT NULL,
  `id` mediumint(7) unsigned NOT NULL DEFAULT '0',
  `loc_x` mediumint(6) DEFAULT NULL,
  `loc_y` mediumint(6) DEFAULT NULL,
  `loc_z` mediumint(6) DEFAULT NULL,
  `price` int(10) unsigned DEFAULT NULL,
  `fornoble` tinyint(1) NOT NULL DEFAULT '0',
  `itemId` smallint(5) unsigned NOT NULL DEFAULT '57',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

INSERT INTO `teleport` VALUES
-- Talken Island 
('Talking Island Village -> Museum', 20181, -114708, 243918, -7968, 0, 0, 57),
('Museum -> Talken Island', 20182, -114376, 259876, -1196, 0, 0, 57),
('Talking Island Village -> берег', 20183, -107881, 248658, -3224, 0, 0, 57),
('Talking Island Village -> наблюдательная вышка', 20184, -119592, 246398, -1232, 0, 0, 57),
('Talking Island Village -> грабница духа', 20185, -114986, 226633, -2864, 0, 0, 57),
('Talking Island Village -> руины эсагира', 20186, -109300, 237498, -2944, 0, 0, 57),
('руины эсагира -> войти в руины эсагира', 20187, -114675, 230171, -1648, 0, 0, 57),
('войти в руины эсагира -> переместится в 1-ю зону Исследования', 20188, -115005, 237383, -3088, 0, 0, 57),
('войти в руины эсагира -> Выйти через Восточный выход', 20189, -109294, 237397, -2928, 0, 0, 57),
('войти в руины эсагира -> Выйти через Западный выход', 20190, -122189, 241009, -2328, 0, 0, 57),
('первая зона иследований -> Грабница Духа', 20191, -114986, 226633, -2864, 0, 0, 57),
('первая зона иследований -> Подземелье Руины Харнака', 20192, -114700, 147909, -7720, 0, 0, 57),
('войти в руины эсагира -> Вернутся в город', 20193, -114566, 253506, -1541, 0, 0, 57);