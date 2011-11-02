DROP TABLE IF EXISTS `class_list`;
CREATE TABLE `class_list` (
  `class_name` varchar(20) NOT NULL DEFAULT '',
  `id` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `parent_id` smallint(3) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
);

INSERT INTO `class_list` VALUES
('H_Fighter', 0, -1),
('H_Warrior', 1, 0), 
('H_Gladiator', 2, 1), 
('H_Duelist', 88, 2), 
('H_Warlord', 3, 1), 
('H_Dreadnought', 89, 3), 
('H_Knight', 4, 0), 
('H_Paladin', 5, 4), 
('H_PhoenixKnight', 90, 5), 
('H_DarkAvenger', 6, 4), 
('H_HellKnight', 91, 6), 
('H_Rogue', 7, 0), 
('H_TreasureHunter', 8, 7), 
('H_Adventurer', 93, 8), 
('H_Hawkeye', 9, 7), 
('H_Sagittarius', 92, 9), 
('H_Mage', 10, -1), 
('H_Wizard', 11, 10), 
('H_Sorceror', 12, 11), 
('H_Archmage', 94, 12), 
('H_Necromancer', 13, 11), 
('H_Soultaker', 95, 13), 
('H_Warlock', 14, 11), 
('H_ArcanaLord', 96, 14), 
('H_Cleric', 15, 10), 
('H_Bishop', 16, 15), 
('H_Cardinal', 97, 16), 
('H_Prophet', 17, 15), 
('H_Hierophant', 98, 17), 
('E_Fighter', 18, -1), 
('E_Knight', 19, 18), 
('E_TempleKnight', 20, 19), 
('E_EvaTemplar', 99, 20), 
('E_SwordSinger', 21, 19), 
('E_SwordMuse', 100, 21), 
('E_Scout', 22, 18), 
('E_PlainsWalker', 23, 22), 
('E_WindRider', 101, 23), 
('E_SilverRanger', 24, 22), 
('E_MoonlightSentinel', 102, 24), 
('E_Mage', 25, -1), 
('E_Wizard', 26, 25), 
('E_SpellSinger', 27, 26), 
('E_MysticMuse', 103, 27), 
('E_ElementalSummoner', 28, 26), 
('E_ElementalMaster', 104, 28), 
('E_Oracle', 29, 25), 
('E_Elder', 30, 29), 
('E_EvaSaint', 105, 30), 
('DE_Fighter', 31, -1), 
('DE_PaulusKnight', 32, 31), 
('DE_ShillienKnight', 33, 32), 
('DE_ShillienTemplar', 106, 33), 
('DE_BladeDancer', 34, 32), 
('DE_SpectralDancer', 107, 34), 
('DE_Assassin', 35, 31), 
('DE_AbyssWalker', 36, 35), 
('DE_GhostHunter', 108, 36), 
('DE_PhantomRanger', 37, 35), 
('DE_GhostSentinel', 109, 37), 
('DE_Mage', 38, -1), 
('DE_DarkWizard', 39, 38), 
('DE_Spellhowler', 40, 39), 
('DE_StormScreamer', 110, 40), 
('DE_PhantomSummoner', 41, 39), 
('DE_SpectralMaster', 111, 41), 
('DE_ShillienOracle', 42, 38), 
('DE_ShillienElder', 43, 42), 
('DE_ShillienSaint', 112, 43), 
('O_Fighter', 44, -1), 
('O_Raider', 45, 44), 
('O_Destroyer', 46, 45), 
('O_Titan', 113, 46), 
('O_Monk', 47, 44), 
('O_Tyrant', 48, 47), 
('O_GrandKhauatari', 114, 48), 
('O_Mage', 49, -1), 
('O_Shaman', 50, 49), 
('O_Overlord', 51, 50), 
('O_Dominator', 115, 51), 
('O_Warcryer', 52, 50), 
('O_Doomcryer', 116, 52), 
('D_Fighter', 53, -1), 
('D_Scavenger', 54, 53), 
('D_BountyHunter', 55, 54), 
('D_FortuneSeeker', 117, 55), 
('D_Artisan', 56, 53), 
('D_Warsmith', 57, 56), 
('D_Maestro', 118, 57),
('K_Male_Soldier', 123, -1),
('K_Male_Trooper', 125, 123),
('K_Male_Berserker', 127, 125),
('K_Male_Doombringer', 131, 127),
('K_Male_Soulbreaker', 128, 125),
('K_Male_Soulhound', 132, 128),
('K_Female_Soldier', 124, -1),
('K_Female_Warder', 126, 124),
('K_Female_Soulbreaker', 129, 126),
('K_Female_Soulhound', 133, 129),
('K_Female_Arbalester', 130, 126),
('K_Female_Trickster', 134, 130),
('K_Inspector', 135, 126),
('K_Judicator', 136, 135);
