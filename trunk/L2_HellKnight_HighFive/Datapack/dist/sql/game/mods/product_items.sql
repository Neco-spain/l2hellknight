DROP TABLE IF EXISTS `product_items`;
CREATE TABLE `product_items` (
  `product_id` int(11) NOT NULL,
  `name` text CHARACTER SET utf8 COLLATE utf8_general_ci,
  `category` int(11) NOT NULL DEFAULT '5',
  `points` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`product_id`)
);

INSERT INTO `product_items` VALUES
('1010001', 'Dynasty Leather Armor', '2', '10000'),
('1010002', 'Dynasty Leather Leggings', '2', '10000'),
('1010003', 'Dynasty Leather Helmet', '2', '10000'),
('1010004', 'Dynasty Leather Gloves - Light Armor', '2', '10000'),
('1010005', 'Dynasty Leather Boots - Light Armor', '2', '10000'),
('1010006', 'Dynasty Breast Plate', '2', '10000'),
('1010007', 'Dynasty Gaiters', '2', '10000'),
('1010008', 'Dynasty Helmet', '2', '10000'),
('1010009', 'Dynasty Gauntlet - Heavy Armor', '2', '10000'),
('1010010', 'Dynasty Boots - Heavy Armor', '2', '10000'),
('1010011', 'Dynasty Shield', '2', '5000'),
('1010012', 'Dynasty Tunic', '2', '10000'),
('1010013', 'Dynasty Stockings', '2', '10000'),
('1010014', 'Dynasty Circlet', '2', '10000'),
('1010015', 'Dynasty Gloves - Robe', '2', '10000'),
('1010016', 'Dynasty Shoes - Robe', '2', '10000');
INSERT INTO `product_items` VALUES
('1010017', 'Moirai BreastPlate', '2', '12000'),
('1010018', 'Moirai Gaiters', '2', '12000'),
('1010019', 'Moirai Helmet', '2', '12000'),
('1010020', 'Moirai Gauntlet - Heavy Armor', '2', '12000'),
('1010021', 'Moirai Boots - Heavy Armor', '2', '12000'),
('1010022', 'Moirai Shield', '2', '6000'),
('1010023', 'Moirai Leather Breastplate', '2', '12000'),
('1010024', 'Moirai Leather Leggings', '2', '12000'),
('1010025', 'Moirai Leather Helmet', '2', '12000'),
('1010026', 'Moirai Leather Gloves - Light Armor', '2', '12000'),
('1010027', 'Moirai Leather Boots - Light Armor', '2', '12000'),
('1010028', 'Moirai Tunic', '2', '12000'),
('1010029', 'Moirai Stockings', '2', '12000'),
('1010030', 'Moirai Circlet', '2', '12000'),
('1010031', 'Moirai Gloves - Robe', '2', '12000'),
('1010032', 'Moirai Shoes - Robe', '2', '12000');
INSERT INTO `product_items` VALUES
('1050021', 'Powerful Healing Potion', '2', '3'),
('1050022', 'High-grade Healing Potion', '2', '1'),
('1080001', 'Small fortuna box', '2', '200'),
('1080002', 'Middle fortuna box', '2', '270'),
('1080003', 'Large fortuna box', '2', '405'),
('1080004', 'Small fortuna cube', '2', '81'),
('1080005', 'Middle fortuna cube', '2', '216'),
('1080006', 'Large fortuna cube', '2', '324'),
('1080009', 'Secret medicine of Will - D grade', '2', '4'),
('1080010', 'Secret medicine of Will - C grade', '2', '13'),
('1080011', 'Secret medicine of Will - B grade', '2', '22'),
('1080012', 'Secret medicine of Will - A grade', '2', '34'),
('1080013', 'Secret medicine of Will - S grade', '2', '49'),
('1080014', 'Secret medicine of Life - D grade', '2', '10'),
('1080015', 'Secret medicine of Life - C grade', '2', '30'),
('1080016', 'Secret medicine of Life - B grade', '2', '54'),
('1080017', 'Secret medicine of Life - A grade', '2', '85'),
('1080018', 'Secret medicine of Life - S grade', '2', '122'),
('1080019', 'Potion of Will', '2', '4'),
('1080021', 'Wind Walk Scroll', '5', '4'),
('1080022', 'Haste Scroll', '2', '8'),
('1080023', 'Might Scroll', '2', '4'),
('1080024', 'Shield Scroll', '2', '4'),
('1080025', 'Death Whisper Scroll', '2', '8'),
('1080026', 'Guidance Scroll', '2', '8'),
('1080027', 'Empower Scroll', '2', '8'),
('1080028', 'Grater Acumen Scroll', '2', '8'),
('1080029', 'Vampiric Rage Scroll', '2', '8'),
('1080030', 'Bless the Body Scroll', '2', '8'),
('1080031', 'Berserker Spirit Scroll', '2', '8'),
('1080032', 'Magic Barrier Scroll', '2', '4'),
('1080033', 'Rune of SP - 336 Hour Expiration Period', '2', '8'),
('1080034', 'Rune of SP - 720 Hour Expiration Period', '2', '8'),
('1080035', 'Crystal form Rune - 24 Hour Expiration Period', '2', '8'),
('1080048', 'Rune of Feather - 24 Hour Expiration Period', '1', '68'),
('1080049', 'A Scroll Bundle of Fighter', '4', '52'),
('1080050', 'A Scroll Bundle of Mage', '4', '59'),
('1080051', 'Bone Quiver', '4', '21'),
('1080052', 'Steel Quiver', '4', '34'),
('1080053', 'Silver Quiver', '4', '48'),
('1080054', 'Mithril Quiver', '4', '54'),
('1080055', 'uiver of Light', '4', '68'),
('1080056', 'Bone Bolt Container', '4', '21'),
('1080057', 'Steel Bolt Container', '4', '34'),
('1080058', 'Silver Bolt Container', '4', '48'),
('1080059', 'Mithril Bolt Container', '4', '54'),
('1080060', 'Bolt Container of Light', '4', '68'),
('1080061', 'Blessed Spiritshot Pack - D grade', '4', '31'),
('1080062', 'Blessed Spiritshot Pack - C grade', '4', '61'),
('1080063', 'Blessed Spiritshot Pack - B grade', '4', '166'),
('1080064', 'Blessed Spiritshot Pack - A grade', '4', '196'),
('1080065', 'Blessed Spiritshot Pack - S grade', '4', '237'),
('1080066', 'Spiritshot Pack - D grade', '4', '12'),
('1080067', 'Spiritshot Pack - C grade', '4', '24'),
('1080068', 'Spiritshot Pack - B grade', '4', '68'),
('1080069', 'Spiritshot Pack - A grade', '4', '81'),
('1080070', 'Spiritshot Pack - S grade', '4', '102'),
('1080071', 'Soulshot Pack - D grade', '4', '8'),
('1080072', 'Soulshot Pack - C grade', '4', '10'),
('1080073', 'Soulshot Pack - B grade', '4', '34'),
('1080074', 'Soulshot Pack - A grade', '4', '54'),
('1080075', 'Soulshot Pack - S grade', '4', '68'),
('1080076', 'Blessed Spiritshot Large Pack - D grade', '4', '61'),
('1080077', 'Blessed Spiritshot Large Pack - C grade', '4', '122'),
('1080078', 'Blessed Spiritshot Large Pack - B grade', '4', '331'),
('1080079', 'Blessed Spiritshot Large Pack - A grade', '4', '392'),
('1080080', 'Blessed Spiritshot Large Pack - S grade', '4', '473'),
('1080081', 'Spiritshot Large Pack - D grade', '4', '24'),
('1080082', 'Spiritshot Large Pack - C grade', '4', '48'),
('1080083', 'Spiritshot Large Pack - B grade', '4', '135'),
('1080084', 'Spiritshot Large Pack - A grade', '4', '162'),
('1080085', 'Spiritshot Large Pack - S grade', '4', '203'),
('1080086', 'Soulshot Large Pack - D grade', '4', '14'),
('1080087', 'Soulshot Large Pack - C grade', '4', '21'),
('1080088', 'Soulshot Large Pack - B grade', '4', '68'),
('1080089', 'Soulshot Large Pack - A grade', '4', '108'),
('1080090', 'Soulshot Large Pack - S grade', '4', '135'),
('1080091', 'Wrapped daisy hairpin', '3', '338'),
('1080092', 'Wrapped forget-me-not hairpin', '3', '338'),
('1080093', 'Wrapped outlaws eyepatch', '3', '338'),
('1080094', 'Wrapped pirates eyepatch', '3', '338'),
('1080095', 'Wrapped Monocle', '3', '338'),
('1080096', 'Wrapped Red Mask of Victory', '3', '338'),
('1080097', 'Wrapped Red Horn of Victory', '3', '338'),
('1080098', 'Wrapped Party Mask', '3', '338'),
('1080099', 'Wrapped Red Party Mask', '3', '338'),
('1080100', 'Wrapped Cat Ear', '3', '338'),
('1080101', 'Wrapped Noblewomans Hairpin', '3', '338'),
('1080102', 'Wrapped Raccoon Ear', '3', '338'),
('1080103', 'Wrapped Rabbit Ear', '3', '338'),
('1080104', 'Wrapped Little Angels Wings', '3', '338'),
('1080105', 'Wrapped Fairys Tentacle', '3', '338'),
('1080106', 'Wrapped Dandys Chapeau', '3', '338'),
('1080107', 'Wrapped Artisans Goggles', '3', '338'),
('1080112', 'Rune of Experience: 30% - 5 hour limited time', '1', '33'),
('1080113', 'Rune of Exp. Points 50% - 5 Hour Expiration Period', '1', '54'),
('1080114', 'Rune of Exp. Points 30% - 10 Hour Expiration Period', '1', '52'),
('1080115', 'Rune of Exp. Points 50% - 10 Hour Expiration Period', '1', '87'),
('1080116', 'Rune of Exp. Points 30% - 7 Day Expiration Period', '1', '697'),
('1080117', 'Rune of Exp. Points 50% - 7 Day Expiration Period', '1', '1161'),
('1080118', 'Rune of SP 30% - 5 Hour Expiration Period', '1', '17'),
('1080119', 'Rune of SP 50% - 5 Hour Expiration Period', '1', '27'),
('1080120', 'Rune of SP 30% - 10 Hour Expiration Period', '1', '26'),
('1080121', 'Rune of SP 50% - 10 Hour Expiration Period', '1', '44'),
('1080122', 'Rune of SP 30% - 7 Day Expiration Period', '1', '349'),
('1080123', 'Rune of SP 50% - 7 Day Expiration Period', '1', '581'),
('1080124', 'Rune of Crystal level 3 - 5 Hour Expiration Period', '1', '33'),
('1080125', 'Rune of Crystal level 5 - 5 Hour Expiration Period', '1', '54'),
('1080126', 'Rune of Crystal level 3 - 10 Hour Expiration Period', '1', '52'),
('1080127', 'Rune of Crystal level 5 - 10 Hour Expiration Period', '1', '87'),
('1080128', 'Rune of Crystal level 3 - 7 Day Expiration Period', '1', '697'),
('1080129', 'Rune of Crystal level 5 - 7 Day Expiration Period', '1', '1161'),
('1080130', 'Weapon-Type Enhance Backup Stone (D-Grade)', '1', '21'),
('1080131', 'Weapon-Type Enhance Backup Stone (C-Grade)', '1', '45'),
('1080132', 'Weapon-Type Enhance Backup Stone (B-Grade)', '1', '203'),
('1080133', 'Weapon-Type Enhance Backup Stone (A-Grade)', '1', '729'),
('1080134', 'Weapon-Type Enhance Backup Stone (S-Grade)', '1', '2025'),
('1080135', 'Armor-Type Enhance Backup Stone (D-Grade)', '1', '4'),
('1080136', 'Armor-Type Enhance Backup Stone (C-Grade)', '1', '7'),
('1080137', 'Armor-Type Enhance Backup Stone (B-Grade)', '1', '29'),
('1080138', 'Armor-Type Enhance Backup Stone (A-Grade)', '1', '104'),
('1080139', 'Armor-Type Enhance Backup Stone (S-Grade)', '1', '290'),
('1080140', 'Beast Soulshot Pack', '4', '14'),
('1080141', 'Beast Spiritshot Pack', '4', '11'),
('1080142', 'Blessed Beast Spiritshot Pack', '4', '68'),
('1080143', 'Beast Soulshot Large Pack', '4', '27'),
('1080144', 'Beast Spiritshot Large Pack', '4', '22'),
('1080145', 'Blessed Beast Spiritshot Large Pack', '4', '135'),
('1080146', 'Omen Beast Transformation Scroll', '5', '30'),
('1080147', 'Death Blader Transformation Scroll', '5', '30'),
('1080148', 'Grail Apostle Transformation Scroll', '5', '30'),
('1080149', 'Unicorn Transformation Scroll', '5', '30'),
('1080150', 'Lilim Knight Transformation Scroll', '5', '30'),
('1080151', 'Golem Guardian Transformation Scroll', '5', '30'),
('1080152', 'Inferno Drake Transformation Scroll', '5', '30'),
('1080153', 'Dragon Bomber Transformation Scroll', '5', '30'),
('1080154', 'Escape - Talking Island Village', '5', '27'),
('1080155', 'Escape - Elven Village', '5', '27'),
('1080156', 'Escape - Dark Elven Village', '5', '27'),
('1080157', 'Escape - Orc Village', '5', '27'),
('1080158', 'Escape - Dwarven Village', '5', '27'),
('1080159', 'Escape - Gludin Village', '5', '27'),
('1080160', 'Escape - Town of Gludio', '5', '27'),
('1080161', 'Escape - Town of Dion', '5', '27'),
('1080162', 'Escape - Floran Village', '5', '27'),
('1080163', 'Escape - Giran Castle Town', '5', '27'),
('1080164', 'Escape - Hardins Academy', '5', '27'),
('1080165', 'Escape - Heine', '5', '27'),
('1080166', 'Escape - Town of Oren', '5', '27'),
('1080167', 'Escape - Ivory Tower', '5', '27'),
('1080168', 'Escape - Hunters Village', '5', '27'),
('1080169', 'Escape - Town of Aden', '5', '27'),
('1080170', 'Escape - Town of Goddard', '5', '27'),
('1080171', 'Escape - Rune Township', '5', '27'),
('1080172', 'Escape - Town of Schuttgart', '5', '27'),
('1080173', 'My Teleport Spellbook', '5', '675'),
('1080174', 'My Teleport Scroll', '5', '135'),
('1080175', 'My Teleport Scroll', '5', '270'),
('1080176', 'My Teleport Flag', '5', '338'),
('1080177', 'My Teleport Flag', '5', '675'),
('1080178', 'Extra Entrance Pass - Kamaloka (Hall of the Abyss)', '5', '338'),
('1080179', 'Extra Entrance Pass - Kamaloka (Hall of the Abyss)', '5', '675'),
('1080180', 'Extra Entrance Pass - Near Kamaloka', '5', '338'),
('1080181', 'Extra Entrance Pass - Near Kamaloka', '5', '675'),
('1080182', 'Extra Entrance Pass - Kamaloka (Labyrinth of the Abyss)', '5', '338'),
('1080183', 'Extra Entrance Pass - Kamaloka (Labyrinth of the Abyss)', '5', '675'),
('1080185', 'Color Name', '5', '268'),
('1080186', 'Greater CP Potion', '3', '14'),
('1080197', 'Potion of Energy Maintenance', '3', '142'),
('1080198', 'Potion of Vitality Replenishin', '3', '68'),
('1080199', 'Sweet Fruit Cocktail', '5', '79'),
('1080200', 'Fresh Fruit Cocktail', '5', '91'),
('1080201', 'Sudden Agathion 7 Day Pack', '3', '338'),
('1080202', 'Shiny Agathion 7 Day Pac', '3', '338'),
('1080203', 'Sobbing Agathion 7 Day Pack', '3', '338'),
('1080205', 'Pumpkin Transformation Stick 7-Day Pack (Event)', '3', '254'),
('1080206', 'Kat the Cat Hat 7-Day Pack (Event)', '3', '169'),
('1080207', 'Feline Queen Hat 7-Day Pack (Event)', '3', '169'),
('1080208', 'Monster Eye Hat 7-Day Pack (Event)', '3', '169'),
('1080209', 'Brown Bear Hat 7-Day Pack (Event)', '3', '169'),
('1080210', 'Fungus Hat 7-Day Pack (Event)', '3', '169'),
('1080211', 'Skull Hat 7-Day Pack (Event)', '3', '169'),
('1080212', 'Ornithomimus Hat 7-Day Pack (Event)', '3', '169'),
('1080213', 'Feline King Hat 7-Day Pack (Event)', '3', '169'),
('1080214', 'Kai the Cat Hat 7-Day Pack (Event)', '3', '169'),
('1080229', 'OX Stick 7-Day Pack (Event)', '3', '169'),
('1080230', 'Rock-Paper-Scissors Stick 7-Day Pack (Event)', '3', '506'),
('1080236', 'Mounting Item 3 Pack', '5', '199'),
('1080238', 'Steam Beatle Mounting Bracelet - 7-day Limited Period', '5', '89'),
('1080239', 'Light Purple-Maned Horse Mounting Bracelet - 7 day limited period', '5', '89'),
('1080240', '10 minute Energy Maintaining Potion', '5', '18'),
('1080241', 'Vitality Maintenance Potion - 30 minutes', '5', '54'),
('1080242', 'Rune of Exp. Points 30% - 3 hours limited time', '5', '24'),
('1080243', 'Rune of Exp. Points 30%', '5', '9'),
('1080244', 'Rune of SP 30%', '5', '5'),
('1080245', 'Hardins Divine Protection', '5', '15'),
('1080246', 'Hardins Blessing', '5', '15'),
('1080247', 'Silpeeds Wing', '5', '5'),
('1080248', 'Silpeeds Blessing', '5', '92'),
('1080249', 'Potion of a Hero', '5', '1');