CREATE TABLE IF NOT EXISTS `custom_npc`(
  `id` mediumint(7) unsigned NOT NULL DEFAULT '0',
  `idTemplate` smallint(5) unsigned NOT NULL DEFAULT '0',
  `name` varchar(200) NOT NULL DEFAULT '',
  `serverSideName` tinyint(1) NOT NULL DEFAULT '1',
  `title` varchar(45) NOT NULL DEFAULT '',
  `serverSideTitle` tinyint(1) NOT NULL DEFAULT '1',
  `class` varchar(200) DEFAULT NULL,
  `collision_radius` decimal(6,2) DEFAULT NULL,
  `collision_height` decimal(6,2) DEFAULT NULL,
  `level` tinyint(2) DEFAULT NULL,
  `sex` enum('etc','female','male') NOT NULL DEFAULT 'etc',
  `type` varchar(22) DEFAULT NULL,
  `attackrange` smallint(4) DEFAULT NULL,
  `hp` decimal(30,15) DEFAULT NULL,
  `mp` decimal(30,15) DEFAULT NULL,
  `hpreg` decimal(30,15) DEFAULT NULL,
  `mpreg` decimal(30,15) DEFAULT NULL,
  `str` tinyint(2) NOT NULL DEFAULT '40',
  `con` tinyint(2) NOT NULL DEFAULT '43',
  `dex` tinyint(2) NOT NULL DEFAULT '30',
  `int` tinyint(2) NOT NULL DEFAULT '21',
  `wit` tinyint(2) NOT NULL DEFAULT '20',
  `men` tinyint(2) NOT NULL DEFAULT '20',
  `exp` int(9) NOT NULL DEFAULT '0',
  `sp` int(9) NOT NULL DEFAULT '0',
  `patk` decimal(12,5) DEFAULT NULL,
  `pdef` decimal(12,5) DEFAULT NULL,
  `matk` decimal(12,5) DEFAULT NULL,
  `mdef` decimal(12,5) DEFAULT NULL,
  `atkspd` smallint(4) NOT NULL DEFAULT '230',
  `critical` tinyint(1) NOT NULL DEFAULT '1',
  `aggro` smallint(4) NOT NULL DEFAULT '0',
  `matkspd` smallint(4) NOT NULL DEFAULT '333',
  `rhand` smallint(5) unsigned NOT NULL DEFAULT '0',
  `lhand` smallint(5) unsigned NOT NULL DEFAULT '0',
  `enchant` tinyint(1) NOT NULL DEFAULT '0',
  `walkspd` decimal(10,5) NOT NULL DEFAULT '60',
  `runspd` decimal(10,5) NOT NULL DEFAULT '120',
  `targetable` tinyint(1) NOT NULL DEFAULT '1',
  `show_name` tinyint(1) NOT NULL DEFAULT '1',
  `dropHerbGroup` tinyint(1) NOT NULL DEFAULT '0',
  `basestats` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE = MYISAM DEFAULT CHARSET=utf8;

INSERT IGNORE INTO `custom_npc` VALUES
(50007,31324,'Andromeda',1,'Wedding Manager',1,'NPC.a_casino_FDarkElf',8.00,23.00,70,'female','L2WeddingManager',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,0,333,0,0,0,28,120,0,0,0,0),
(70005,31606,'Nikola',1,'DM Event',1,'Monster2.queen_of_cat',8.00,15.00,70,'female','L2DMEventNpc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,0,333,0,0,0,28,120,0,0,0,0),
(70006,31606,'Martina',1,'LM Event',1,'Monster2.queen_of_cat',8.00,15.00,70,'female','L2LMEventNpc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,0,333,0,0,0,28,120,0,0,0,0),
(70009,31606,'Libuse',1,'CTF Event',1,'Monster2.queen_of_cat',8.00,15.00,70,'female','L2Npc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,0,333,0,0,0,28,120,0,0,0,0),
(70010,31606,'Catrina',1,'TvT Event',1,'Monster2.queen_of_cat',8.00,15.00,70,'female','L2TvTEventNpc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,0,333,0,0,0,28,120,0,0,0,0),
(70011,31606,'Monika',1,'TvT Round Event',1,'Monster2.queen_of_cat',8.00,15.00,70,'female','L2TvTRoundEventNpc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,0,333,0,0,0,28,120,0,0,0,0),
('77777', '32211', 'Jueno', '1', 'Last Hero', '1', 'LineageNPC2.K_M1_master', '15.00', '25.00', '70', 'male', 'L2Npc', '40', '2444.000000000000000', '1225.000000000000000', '7.500000000000000', '2.700000000000000', '40', '43', '30', '21', '20', '20', '0', '0', '1086.00000', '471.00000', '749.00000', '313.00000', '230', '4', '0', '333', '9646', '0', '0', '30.00000', '120.00000', '1', '1', '0', '1'),
(1000003,32226,'Shiela',1,'L2J NPC Buffer',1,'LineageNPC2.K_F1_grand',11.00,22.25,70,'male','L2NpcBuffer',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,0,333,0,0,0,28,120,0,0,0,0),
-- eventmod Elpies
(900100,20432,'Elpy',1,'',1,'LineageMonster.elpy',5.00,4.50,1,'male','L2EventMonster',40,40,36,3.16,0.91,40,43,30,21,20,20,35,2,8,40,7,25,230,1,0,333,0,0,0,50,80,0,0,0,0),
-- eventmod Rabbits
(900101,32365,'Snow',1,'Event Manager',1,'LineageNPC2.TP_game_staff',5.00,12.50,70,'male','L2Npc',40,2444,1225,0.00,0.00,40,43,30,21,20,20,0,0,1086,471,749,313,230,1,0,333,0,0,0,68,109,1,1,0,0),
(900102,13098,'Event Treasure Chest',1,'',1,'LineageMonster.mimic_even',8.50,8.50,80,'male','L2EventChest',40,2880,1524,0.00,0.00,40,43,30,21,20,20,0,0,1499,577,1035,384,230,1,0,253,0,0,0,1,1,0,0,0,0),
-- eventmod Race
(900103,32365,'Start',1,'Event Manager',1,'LineageNPC2.TP_game_staff',5.00,12.50,70,'male','L2Npc',40,2444,1225,0.00,0.00,40,43,30,21,20,20,0,0,1086,471,749,313,230,1,0,333,0,0,0,68,109,0,0,0,0),
(900104,32365,'Finish',1,'Event Manager',1,'LineageNPC2.TP_game_staff',5.00,12.50,70,'male','L2Npc',40,2444,1225,0.00,0.00,40,43,30,21,20,20,0,0,1086,471,749,313,230,1,0,333,0,0,0,68,109,0,0,0,0),
-- Server Info NPC
(50026,32379,'Information',1,'Server Brick',1,'LineageNPC2.TP_flag',12,12,70,'male','L2Npc',40,2444,1225,0.00,0.00,40,43,30,21,20,20,0,0,1086,471,749,313,230,1,0,333,0,0,0,68,109,0,0,0,0);
-- event manager
INSERT INTO `custom_npc` VALUES ('2009001', '32630', 'Rose', '1', 'Bloodshed Mannager', '1', 'LineageNPC.a_fighterguild_teacher_MDarkElf', '8.00', '20.00', '70', 'female', 'L2Npc', '40', '2444.000000000000000', '1225.000000000000000', '0.000000000000000', '0.000000000000000', '40', '43', '30', '21', '20', '20', '0', '0', '1086.00000', '471.00000', '749.00000', '313.00000', '230', '127', '10', '333', '0', '0', '0', '55.00000', '55.00000', '0', '1', '0', '0');
INSERT INTO `custom_npc` VALUES ('2009002', '30647', 'Demonic Chest', '1', 'Treasure of Naglfar', '1', 'NPC.coffer_c', '12.00', '9.00', '70', 'etc', 'L2Npc', '40', '2444.000000000000000', '1225.000000000000000', '0.000000000000000', '0.000000000000000', '40', '43', '30', '21', '20', '20', '0', '0', '1086.00000', '471.00000', '749.00000', '313.00000', '230', '127', '10', '333', '0', '0', '0', '50.00000', '120.00000', '0', '1', '0', '0');
INSERT INTO `custom_npc` VALUES ('2009010', '25642', 'Naglfar', '1', 'Demonic Lord', '1', 'LineageMonster4.rahuu', '20.00', '64.00', '83', 'male', 'L2Monster', '80', '220680.000000000000000', '1680.000000000000000', '0.000000000000000', '0.000000000000000', '60', '57', '73', '76', '70', '80', '3100452', '410692', '3112.00000', '1000.00000', '1835.00000', '839.00000', '230', '127', '10', '253', '13983', '0', '0', '73.00000', '109.00000', '0', '1', '0', '0');
INSERT INTO `custom_npc` VALUES ('2009011', '22329', 'Sin', '1', 'Demonic Sentry', '1', 'LineageMonster3.Death_Blader_Raid', '15.00', '32.50', '83', 'male', 'L2Monster', '40', '65569.000000000000000', '1617.000000000000000', '56.860000000000000', '3.090000000000000', '40', '43', '30', '21', '20', '20', '331002', '35747', '9534.00000', '974.00000', '6567.00000', '972.00000', '230', '127', '10', '333', '0', '0', '0', '55.00000', '155.00000', '0', '1', '0', '0');
INSERT INTO `custom_npc` VALUES ('2009012', '22329', 'Hel', '1', 'Demonic Sentry', '1', 'LineageMonster3.Death_Blader_Raid', '15.00', '32.50', '83', 'male', 'L2Monster', '40', '65569.000000000000000', '1617.000000000000000', '56.860000000000000', '3.090000000000000', '40', '43', '30', '21', '20', '20', '331002', '35747', '9534.00000', '974.00000', '6567.00000', '972.00000', '230', '127', '10', '333', '0', '0', '0', '55.00000', '155.00000', '0', '1', '0', '0');
INSERT INTO `custom_npc` VALUES ('2009013', '29151', 'Hellhound', '1', 'Demonic Spawn', '1', 'NPC2.bereth_fake', '64.00', '48.42', '83', 'male', 'L2Monster', '40', '4234.000000000000000', '168029.000000000000000', '0.000000000000000', '0.000000000000000', '40', '43', '30', '21', '20', '20', '0', '0', '2052.00000', '630.00000', '1415.00000', '419.00000', '230', '127', '10', '333', '0', '0', '0', '55.00000', '55.00000', '0', '1', '0', '0');
