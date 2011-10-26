DELETE  FROM npc where  (id=9000 );
DELETE  FROM npc where  (id=9001 );
DELETE  FROM npc where  (id=9002 );
DELETE  FROM npc where  (id=9003 );
INSERT INTO npc VALUES ('9000', '1', 'Game Master', '1', 'Hide And Seek', '1', 'LineageMonster4.br_crazy_turkey', '20.00', '30.00', '84', 'male', 'L2Npc', '40', '3862.00000', '1494.00000', '8.50000', '3.00000', '40', '43', '30', '21', '20', '20', '25111', '2528', '1962.00000', '619.00000', '1353.00000', '412.00000', '253', '4', '0', '333', '0', '0', '0', '60', '150', '1', '1', '0', '0');
INSERT INTO npc VALUES ('9001', '4', 'Holiday Santa', '1', 'Hide And Seek', '1', 'LineageNPC2.br_xmas08_santa', '9.00', '16.00', '84', 'male', 'L2Npc', '40', '3862.00000', '1494.00000', '8.50000', '3.00000', '40', '43', '30', '21', '20', '20', '25111', '2528', '1962.00000', '619.00000', '1353.00000', '412.00000', '253', '4', '0', '333', '0', '0', '0', '60', '150', '1', '1', '0', '0');
INSERT INTO npc VALUES ('9002', '8', 'Spirit of Fire', '1', 'Hide And Seek', '1', 'LineageMonster4.br_fire_elemental', '16.00', '22.00', '84', 'male', 'L2Npc', '40', '3862.00000', '1494.00000', '8.50000', '3.00000', '40', '43', '30', '21', '20', '20', '25111', '2528', '1962.00000', '619.00000', '1353.00000', '412.00000', '253', '4', '0', '333', '0', '0', '0', '60', '150', '1', '1', '0', '0');
INSERT INTO npc VALUES ('9003', '10', 'Jack Sage', '1', 'Hide And Seek', '1', 'LineageNPC2.br_archbishop_of_eva', '8.00', '26.50', '84', 'male', 'L2Npc', '40', '3862.00000', '1494.00000', '8.50000', '3.00000', '40', '43', '30', '21', '20', '20', '25111', '2528', '1962.00000', '619.00000', '1353.00000', '412.00000', '253', '4', '0', '333', '0', '0', '0', '60', '150', '1', '1', '0', '0');

DROP TABLE IF EXISTS `hide_and_seek`;
CREATE TABLE IF NOT EXISTS `hide_and_seek` (
  `npc` int(10) DEFAULT NULL,
  `x` int(10) DEFAULT NULL,
  `y` int(10) DEFAULT NULL,
  `z` int(10) DEFAULT NULL,
  `first_clue` varchar(100) DEFAULT NULL,
  `second_clue` varchar(100) DEFAULT NULL,
  `third_clue` varchar(100) DEFAULT NULL,
  `rewards` varchar(100) DEFAULT NULL
) ENGINE = MYISAM DEFAULT CHARSET=utf8;

INSERT INTO `hide_and_seek` (`npc`, `x`, `y`, `z`, `first_clue`, `second_clue`, `third_clue`, `rewards`) VALUES
(9001, 169754, 16519, -3397, 'Im near Aden. Many Skeletons around...', 'Im in a rest zone...', 'Im behind a tree of cemetary!', '57,10000;5575,10'),
(9002, 83467, 150593, -3533, 'I can hear how potions are cooked...', 'Im behind a shop...', 'Im in the Giran Castle Town!', '57,10000;5575,10'),
(9003, 178289, -85594, -7217, 'Im in a undergound site...', 'I can see 4 entrances to anywhere...', 'You can go to Frintezza from here!', '57,10000;5575,10');
