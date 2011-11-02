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
);

INSERT IGNORE INTO `custom_npc` VALUES
(50007,31324,'Andromeda',1,'L2J Wedding Manager',1,'NPC.a_casino_FDarkElf',8.00,23.00,70,'female','L2WeddingManager',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,0,333,0,0,0,28,120,0,0,0,0),
(1000003,32226,'Shiela',1,'L2J NPC Buffer',1,'LineageNPC2.K_F1_grand',11.00,22.25,70,'male','L2NpcBuffer',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,0,333,0,0,0,28,120,0,0,0,0),

-- RusDev events
(70010,31606,'Catrina',1,'RusDev TvT Event Manager',1,'Monster2.queen_of_cat',8.00,15.00,70,'female','L2TvTEventNpc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,0,333,0,0,0,28,120,0,0,0,0),
(70011,31606,'Michelle',1,'RusDev DM Event Manager',1,'Monster2.queen_of_cat',8.00,15.00,70,'female','L2DMEventNpc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,0,333,0,0,0,28,120,0,0,0,0),
(70012,31606,'Luanna',1,'RusDev LM Event Manager',1,'Monster2.queen_of_cat',8.00,15.00,70,'female','L2LMEventNpc',40,2444,2444,0.00,0.00,10,10,10,10,10,10,0,0,500,500,500,500,278,1,0,333,0,0,0,28,120,0,0,0,0),

-- npc custom
(70027,32226,'Raicini',1,'RusDev Rank Manager',1,'LineageNPC2.K_F1_grand',11.00,22.25,70,'female','L2Npc',40,2444,1225,0.00,0.00,40,43,30,21,20,20,0,0,1086,471,749,313,230,0,1,333,9644,0,0,25,109,0,1,0,0),
(70028,4306,'Amgo',1,'RusDev Buffer Manager',1,'LineageNPC2.br_archbishop_of_eva',8.00,26.50,70,'male','L2Npc',40,3862,1494,0.00,0.00,40,43,30,21,20,20,0,0,1303,471,607,382,253,0,1,333,0,0,0,60,180,0,0,0,0),
(70051,35676,'Agent Guy',1,'RusDev Hitman Manager',1,'LineageNPC2.battery_of_insurgents',13.00,17.50,66,'male','L2Hitman',1100,2245,1110,10.00,2.00,40,43,30,21,20,20,2178,0,928,429,641,285,230,0,1,333,0,0,0,45,109,0,1,0,0),

-- eventmod Elpies
(900100,20432,'Elpy',1,'',1,'LineageMonster.elpy',5.00,4.50,1,'male','L2EventMonster',40,40,36,3.16,0.91,40,43,30,21,20,20,35,2,8,40,7,25,230,1,0,333,0,0,0,50,80,0,0,0,0),
-- eventmod Rabbits
(900101,32365,'Snow',1,'Event Manager',1,'LineageNPC2.TP_game_staff',5.00,12.50,70,'male','L2Npc',40,2444,1225,0.00,0.00,40,43,30,21,20,20,0,0,1086,471,749,313,230,1,0,333,0,0,0,68,109,1,1,0,0),
(900102,13098,'Event Treasure Chest',1,'',1,'LineageMonster.mimic_even',8.50,8.50,80,'male','L2EventChest',40,2880,1524,0.00,0.00,40,43,30,21,20,20,0,0,1499,577,1035,384,230,1,0,253,0,0,0,1,1,0,0,0,0),
-- eventmod Race
(900103,32365,'Start',1,'Event Manager',1,'LineageNPC2.TP_game_staff',5.00,12.50,70,'male','L2Npc',40,2444,1225,0.00,0.00,40,43,30,21,20,20,0,0,1086,471,749,313,230,1,0,333,0,0,0,68,109,0,0,0,0),
(900104,32365,'Finish',1,'Event Manager',1,'LineageNPC2.TP_game_staff',5.00,12.50,70,'male','L2Npc',40,2444,1225,0.00,0.00,40,43,30,21,20,20,0,0,1086,471,749,313,230,1,0,333,0,0,0,68,109,0,0,0,0);
