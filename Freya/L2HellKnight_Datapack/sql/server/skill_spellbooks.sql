DROP TABLE IF EXISTS `skill_spellbooks`;
CREATE TABLE `skill_spellbooks` (
  `skill_id` smallint(5) unsigned NOT NULL DEFAULT '0',
  `item_id` smallint(5) unsigned NOT NULL DEFAULT '0',
  KEY `skill_id` (`skill_id`,`item_id`)
) ENGINE = MYISAM DEFAULT CHARSET=utf8;

-- NOTES:
-- (0,1393),
-- Spellbook: Quickness unable to find a matching skill_id possibly never used

INSERT INTO `skill_spellbooks` VALUES
(2,1512), -- Spellbook: Confusion
(10,3039), -- Spellbook: Summon Storm Cubic
(13,3940), -- Blueprint: Summon Siege Golem
(15,1513), -- Spellbook: Charm
(21,1377), -- Spellbook: Poison Recovery
(22,3040), -- Spellbook: Summon Vampiric Cubic
(25,3038), -- Blueprint: Summon Mechanic Golem
(33,3041), -- Spellbook: Summon Phantom Cubic
(44,3432), -- Spellbook: Remedy
(45,1378), -- Spellbook: Divine Heal
(46,3042), -- Spellbook: Life Scavenge
(49,3043), -- Spellbook: Holy Strike
(58,1096), -- Spellbook: Elemental Heal
(61,1379), -- Spellbook: Cure Bleeding
(65,3044), -- Spellbook: Horror
(67,3045), -- Spellbook: Summon Life Cubic
(69,3046), -- Spellbook: Sacrifice
(70,1097), -- Spellbook: Drain Health
(72,3047), -- Spellbook: Iron Will
(77,1095), -- Spellbook: Attack Aura
(86,3048), -- Spellbook: Reflect Damage
(91,1294), -- Spellbook: Defense Aura
(102,1380), -- Spellbook: Entangle
(103,3049), -- Spellbook: Corpse Plague
(105,1381), -- Spellbook: Freezing Strike
(115,1382), -- Spellbook: Power Break
(122,3050), -- Spellbook: Hex
(123,3051), -- Spellbook: Spirit Barrier
(127,3052), -- Spellbook: Hamstring
(129,1383), -- Spellbook: Poison
(230,1384), -- Spellbook: Sprint
(262,3053), -- Spellbook: Holy Blessing
(278,3054), -- Spellbook: Summon Viper Cubic
(279,3055), -- Spellbook: Lightening Strike
(283,3056), -- Spellbook: Summon Dark Panther
(289,4203), -- Spellbook: Life Leech
(301,4921), -- Blueprint: Summon Big Boom
(1002,1518), -- Amulet: Flame Chant
(1003,1519), -- Amulet: Pa'agrio's Gift
(1004,3103), -- Amulet: Pa'agrio's Wisdom
(1005,1520), -- Amulet: Pa'agrio's Blessing
(1006,1521), -- Amulet: Chant of Fire
(1007,1522), -- Amulet: Chant of Battle
(1008,3104), -- Amulet: Pa'agrio's Glory
(1009,1523), -- Amulet: Chant of Shielding
(1010,1524), -- Amulet: Soul Shield
(1011,1152), -- Spellbook: Heal
(1012,1053), -- Spellbook: Cure Poison
(1013,1385), -- Spellbook: Recharge
(1015,1050), -- Spellbook: Battle Heal
(1016,1514), -- Spellbook: Resurrection
(1018,3072), -- Spellbook: Purify
(1020,3068), -- Spellbook: Vitalize
(1027,1054), -- Spellbook: Group Heal
(1028,3073), -- Spellbook: Might of Heaven
(1031,1386), -- Spellbook: Disrupt Undead
(1032,3094), -- Spellbook: Invigor
(1033,1387), -- Spellbook: Resist Poison
(1034,3069), -- Spellbook: Repose
(1035,1388), -- Spellbook: Mental Shield
(1036,3095), -- Spellbook: Magic Barrier
(1040,1058), -- Spellbook: Shield
(1042,3070), -- Spellbook: Hold Undead
(1043,1389), -- Spellbook: Holy Weapon
(1044,1390), -- Spellbook: Regeneration
(1045,3096), -- Spellbook: Blessed Body
(1047,4207), -- Spellbook: Mana Regeneration
(1048,3097), -- Spellbook: Blessed Soul
(1049,3071), -- Spellbook: Requiem
(1050,3098), -- Spellbook: Return
(1056,3079), -- Spellbook: Cancel
(1059,1391), -- Spellbook: Empower
(1062,1392), -- Spellbook: Berserker Spirit
(1064,3064), -- Spellbook: Silence
(1068,1048), -- Spellbook: Might
(1069,1394), -- Spellbook: Sleep
(1071,3082), -- Spellbook: Surrender to Water
(1072,3080), -- Spellbook: Sleeping Cloud
(1073,1515), -- Spellbook: Kiss of Eva
(1074,3074), -- Spellbook: Surrender to Wind
(1075,1397), -- Spellbook: Peace
(1077,1398), -- Spellbook: Focus
(1078,1399), -- Spellbook: Concentration
(1085,1401), -- Spellbook: Acumen
(1086,3099), -- Spellbook: Haste
(1087,1402), -- Spellbook: Agility
(1090,1525), -- Amulet: Life Drain
(1092,1526), -- Amulet: Fear
(1095,1527), -- Amulet: Venom
(1096,1528), -- Amulet: Seal of Chaos
(1097,1529), -- Amulet: Dreaming Spirit
(1099,1530), -- Amulet: Seal of Slow
(1100,1531), -- Amulet: Chill Flame
(1101,1532), -- Amulet: Blaze Quake
(1102,1533), -- Amulet: Aura Sink
(1104,3105), -- Amulet: Seal of Winter
(1105,1534), -- Amulet: Madness
(1107,1535), -- Amulet: Frost Flame
(1108,3106), -- Amulet: Seal of Flame
(1111,1403), -- Spellbook: Summon Kat the Cat
(1126,1404), -- Spellbook: Servitor Recharge
(1127,1405), -- Spellbook: Servitor Heal
(1128,1667), -- Spellbook: Summon Shadow
(1129,3057), -- Spellbook: Summon Reanimated Man
(1139,3091), -- Spellbook: Servitor Magic Shield
(1140,3092), -- Spellbook: Servitor Physical Shield
(1141,3093), -- Spellbook: Servitor Haste
(1144,1406), -- Spellbook: Servitor Wind Walk
(1145,1407), -- Spellbook: Servitor Magic Boost
(1146,1408), -- Spellbook: Mighty Servitor
(1147,1051), -- Spellbook: Vampiric Touch
(1148,3065), -- Spellbook: Death Spike
(1151,1516), -- Spellbook: Corpse Life Drain
(1154,3058), -- Spellbook: Summon Corrupted Man
(1155,3059), -- Spellbook: Corpse Burst
(1156,3060), -- Spellbook: Forget
(1157,1517), -- Spellbook: Body To Mind
(1159,3066), -- Spellbook: Curse Death Link
(1160,1409), -- Spellbook: Slow
(1163,3061), -- Spellbook: Curse Discord
(1164,1056), -- Spellbook: Curse: Weakness
(1167,1410), -- Spellbook: Poisonous Cloud
(1168,1055), -- Spellbook: Curse: Poison
(1169,3062), -- Spellbook: Curse Fear
(1170,3063), -- Spellbook: Anchor
(1171,3075), -- Spellbook: Blazing Circle
(1172,1411), -- Spellbook: Aura Burn
(1174,3083), -- Spellbook: Frost Wall
(1175,1370), -- Spellbook: Aqua Swirl
(1176,3089), -- Spellbook: Tempest
(1178,1371), -- Spellbook: Twister
(1181,1052), -- Spellbook: Flame Strike
(1182,1412), -- Spellbook: Resist Aqua
(1183,3084), -- Spellbook: Freezing Shackle
(1184,1049), -- Spellbook: Ice Bolt
(1189,1413), -- Spellbook: Resist Wind
(1191,1414), -- Spellbook: Resist Fire
(1201,1415), -- Spellbook: Dryad Root
(1204,1098), -- Spellbook: Wind Walk
(1206,1099), -- Spellbook: Wind Shackle
(1208,1536), -- Amulet: Seal of Binding
(1209,1537), -- Amulet: Seal of Poison
(1210,3107), -- Amulet: Seal of Gloom
(1213,3108), -- Amulet: Seal of Mirage
(1217,3429), -- Spellbook: Greater Heal
(1218,3430), -- Spellbook: Greater Battle Heal
(1219,3431), -- Spellbook: Greater Group Heal
(1220,1372), -- Spellbook: Blaze
(1222,1416), -- Spellbook: Curse of Chaos
(1223,1417), -- Spellbook: Surrender To Earth
(1224,1418), -- Spellbook: Surrender To Poison
(1225,1668), -- Spellbook: Summon Mew the Cat
(1226,1669), -- Spellbook: Summon Boxer the Unicorn
(1227,1670), -- Spellbook: Summon Mirage the Unicorn
(1228,1671), -- Spellbook: Summon Silhouette
(1229,1856), -- Amulet: Chant of Life
(1230,3076), -- Spellbook: Prominence
(1231,3081), -- Spellbook: Aura Flare
(1232,3077), -- Spellbook: Blazing Skin
(1233,3078), -- Spellbook: Decay
(1234,3067), -- Spellbook: Vampiric Claw
(1235,3085), -- Spellbook: Hydro Blast
(1236,3086), -- Spellbook: Frost Bolt
(1237,3087), -- Spellbook: Ice Dagger
(1238,3088), -- Spellbook: Freezing Skin
(1239,3090), -- Spellbook: Hurricane
(1240,3100), -- Spellbook: Guidance
(1242,3101), -- Spellbook: Death Whisper
(1243,3102), -- Spellbook: Bless Shield
(1244,3115), -- Amulet: Freezing Flame
(1245,3114), -- Amulet: Steal Essence
(1246,3109), -- Amulet: Seal of Silence
(1247,3110), -- Amulet: Seal of Scourge
(1248,3111), -- Amulet: Seal of Suspension
(1249,3112), -- Amulet: Pa'agrio's Eye
(1250,3113), -- Amulet: Pa'agrio's Protection
(1251,3116), -- Amulet: Chant of Fury
(1252,3117), -- Amulet: Chant of Evasion
(1253,3118), -- Amulet: Chant of Rage
(1254,3941), -- Spellbook: Mass Resurrection
(1256,3943), -- Amulet: Pa'agrio's Heart
(1257,3944), -- Spellbook: Decrease Weight
(1258,4200), -- Spellbook: Restore Life
(1259,4201), -- Spellbook: Resist Shock
(1260,4204), -- Amulet: Pa'agrio's Tact
(1261,4205), -- Amulet: Pa'agrio's Rage
(1262,4206), -- Spellbook: Transfer Pain
(1263,4208), -- Spellbook: Curse Gloom

-- c3 skill spellbooks (most of them anyway - thx Luno)

(1264,4906), -- Spellbook: Solar Spark
(1265,4907), -- Spellbook: Solar Flare
(1266,4908), -- Spellbook: Shadow Spark
(1267,4909), -- Spellbook: Shadow Flare
(1268,4910), -- Spellbook: Vampiric Rage
(1269,4911), -- Spellbook: Curse Disease
(1271,4912), -- Spellbook: Benediction
(1272,4913), -- Spellbook: Word of Fear
(1273,4914), -- Spellbook: Serenade of Eva
(299,4915), -- Blueprint: Summon Wild Hog Cannon
(1274,4916), -- Spellbook: Energy Bolt
(1275,4917), -- Spellbook: Aura Bolt
(1276,4918), -- Spellbook: Summon Kai the Cat
(1277,4919), -- Spellbook: Summon Merrow the Unicorn
(1278,4920), -- Spellbook: Summon Soulless
(1279,4922), -- Spellbook: Summon Binding Cubic
(1280,4923), -- Spellbook: Summon Aqua Cubic
(1281,4924), -- Spellbook: Summon Spark Cubic
(1282,4925), -- Amulet: Pa'agrio's Haste
(1283,4926), -- Amulet: Soul Guard
(1284,4927), -- Amulet: Chant of Revenge
(1285,4928), -- Spellbook: Seed of Fire
(1286,4929), -- Spellbook: Seed of Water
(1287,4930), -- Spellbook: Seed of Wind
(1288,4931), -- Spellbook: Aura Symphony
(1289,4932), -- Spellbook: Inferno
(1290,4933), -- Spellbook: Blizzard
(1291,4934), -- Spellbook: Demon Wind
(1292,5013), -- Spellbook: Elemental Assault
(1293,5014), -- Spellbook: Elemental Symphony
(1294,5015), -- Spellbook: Elemental Storm
(1295,5809), -- Spellbook: Aqua Splash
(1296,5810), -- Spellbook: Rain of Fire
(1298,5811), -- Spellbook: Mass Slow
(1299,5812), -- Spellbook: Servitor Empowerment
(1300,5813), -- Spellbook: Servitor Cure
(1301,5814), -- Spellbook: Servitor Blessing
(1303,5815), -- Spellbook: Wild Magic
(1304,5816), -- Spellbook: Advanced Block
(1305,6350), -- Amulet: Pa'agrio's Honor
(1306,6351), -- Amulet: Ritual of Life
(1307,6352), -- Spellbook: Prayer
(1308,6395), -- Amulet: Chant of Predator
(1309,6396), -- Amulet: Chant of Eagle
(1310,6397), -- Amulet: Chant of Vampire
(1311,6398), -- Spellbook: Body of Avatar

-- C4 Spellbooks and Amulets

(1328,7638), -- Spellbook - Mass Summon Storm Cubic
(1329,7639), -- Spellbook - Mass Summon Aqua Cubic
(1330,7640), -- Spellbook - Mass Summon Phantom Cubic
(1331,7641), -- Spellbook - Summon Feline Queen
(1332,7642), -- Spellbook - Summon Unicorn Seraphim
(1333,7643), -- Spellbook - Summon Nightshade
(1334,7644), -- Spellbook - Summon Cursed Man
(1335,7645), -- Spellbook - Balance Life
(1336,7646), -- Spellbook - Curse of Doom
(1337,7647), -- Spellbook - Curse of Abyss
(1338,7648), -- Spellbook - Arcane Chaos
(1339,7649), -- Spellbook - Fire Vortex
(1340,7650), -- Spellbook - Ice Vortex
(1341,7651), -- Spellbook - Wind Vortex
(1342,7652), -- Spellbook - Light Vortex
(1343,7653), -- Spellbook - Dark Vortex
(1344,7654), -- Spellbook - Mass Warrior Bane
(1345,7655), -- Spellbook - Mass Mage Bane
(1346,7656), -- Spellbook - Warrior Servitor
(1347,7657), -- Spellbook - Wizard Servitor
(1348,7658), -- Spellbook - Assassin Servitor
(1349,7659), -- Spellbook - Final Servitor
(1350,7660), -- Spellbook - Warrior Bane
(1351,7661), -- Spellbook - Mage Bane
(1352,7662), -- Spellbook - Elemental Protection
(1353,7663), -- Spellbook - Divine Protection
(1354,7664), -- Spellbook - Arcane Protection
(1355,7665), -- Spellbook - Prophecy of Water
(1356,7666), -- Spellbook - Prophecy of Fire
(1357,7667), -- Spellbook - Prophecy of Wind
(1358,7668), -- Spellbook - Block Shield
(1359,7669), -- Spellbook - Block Wind Walk
(1360,7670), -- Spellbook - Mass Block Shield
(1361,7671), -- Spellbook - Mass Block Wind Walk
(1362,7672), -- Amulet - Chant of Spirit
(1363,7673), -- Amulet - Chant of Victory
(1364,7674), -- Amulet - Pa'agrio's Eye
(1365,7675), -- Amulet - Pa'agrio's Soul
(1366,7676), -- Amulet - Seal of Despair
(1367,7835); -- Amulet: Seal of Disease

-- C5 Spellbooks and Amulets (tnx ThePhoenixBird)

INSERT INTO `skill_spellbooks` VALUES
(1380,8380), -- Spellbook: Betray
(1381,8381), -- Spellbook: Mass Curse Fear
(1382,8382), -- Spellbook: Mass Curse Gloom
(1383,8383), -- Spellbook: Mass Surrender to Fire
(1384,8384), -- Spellbook: Mass Surrender to Water
(1385,8385), -- Spellbook: Mass Surrender to Wind
(1386,8386), -- Spellbook - Arcane Disruption
(1387,8387), -- Spellbook - Summon Cursed Bones
(1388,8388), -- Spellbook: Greater Might
(1389,8389), -- Spellbook: Greater Shield
(1390,8390), -- Amulet: War Chant
(1391,8391), -- Amulet: Earth Chant
(1392,8392), -- Spellbook: Holy Resistance
(1393,8393), -- Spellbook: Unholy Resistance
(1394,8394), -- Spellbook: Trance
(1395,8395), -- Spellbook: Erase
(1396,8396), -- Spellbook: Magical Backfire
(1397,8397), -- Spellbook: Clarity
(1398,8398), -- Spellbook: Mana Burn
(1399,8399), -- Spellbook: Mana Storm
(1400,8400), -- Spellbook: Turn Undead
(1401,8401), -- Spellbook: Major Heal
(1402,8402); -- Spellbook: Major Group Heal

-- Late C5 Spellbooks and Amulets

INSERT INTO `skill_spellbooks` VALUES 
(1403,8616), -- Spellbook: Summon Friend
(1404,8617); -- Spellbook: Word of Invitation

-- Interlude Spellbooks and Amulets

INSERT INTO `skill_spellbooks` VALUES 
(438,8877), -- Spellbook: Soul of the Phoenix
(1406,8878), -- Spellbook: Summon Feline King
(1407,8879), -- Spellbook: Summon Magnus the Unicorn
(1408,8880), -- Spellbook: Summon Spectral Knight
(1409,8881), -- Spellbook: Cleanse
(1410,8882), -- Spellbook: Salvation
(1411,8883), -- Spellbook: Mystic Immunity
(1412,8884), -- Spellbook: Spell Turning
(1413,8885), -- Amulet: Magnus' Chant
(1414,8886), -- Amulet: Victories of Pa'agrio
(1415,8887), -- Amulet: Pa'agrio's Emblem
(1416,8888), -- Amulet: Pa'agrio's Fist
(1429,8889), -- Amulet: Gate Chant
(449,8890), -- Spellbook: Summon Attractive Cubic
(1417,8891), -- Spellbook: Aura Flash
(454,8892), -- Ancient Tactical Manual: Symbol of Defense
(455,8893), -- Ancient Tactical Manual: Symbol of Noise
(456,8894), -- Ancient Tactical Manual: Symbol of Resistance
(457,8895), -- Ancient Tactical Manual: Symbol of Honor
(458,8896), -- Ancient Tactical Manual: Symbol of Energy
(459,8897), -- Ancient Tactical Manual: Symbol of the Sniper
(460,8898), -- Ancient Tactical Manual: Symbol of the Assassin
(1419,8899), -- Ancient Spellbook: Volcano
(1420,8900), -- Ancient Spellbook: Cyclone
(1421,8901), -- Ancient Spellbook: Raging Waves
(1422,8902), -- Ancient Spellbook: Day of Doom
(1423,8903), -- Ancient Spellbook: Gehenna
(1424,8904), -- Ancient Spellbook: Anti-Summoning Field
(1425,8905), -- Ancient Spellbook: Purification Field
(1426,8906), -- Ancient Spellbook: Miracle
(1427,8907), -- Ancient Spellbook: Flames of Invincibility
(1428,8908), -- Ancient Spellbook: Mass Recharge
(448,8909), -- Blueprint: Summon Swoop Cannon
(1418,8945), -- Spellbook: Celestial Shield
(1430,8946); -- Spellbook: Invocation

-- Kamael Spellbooks

INSERT INTO `skill_spellbooks` VALUES 
(1433,10032), -- Battle Manual: Abyssal Blaze
(1438,10047), -- Battle Manual: Annihilation Circle
(518,10062), -- Battle Manual: Binding Trap
(495,10098), -- Battle Manual: Blade Rush
(496,10037), -- Battle Manual: Blade Slash
(509,10086), -- Battle Manual: Bleeding Shot
(1448,10050), -- Battle Manual: Blink
(1473,10070), -- Battle Manual: Change Weapon
(506,10044), -- Battle Manual: Checkmate
(498,10039), -- Battle Manual: Contagion
(1452,10099), -- Spellbook: Count of Fire
(499,10079), -- Battle Manual: Courage
(513,10057), -- Battle Manual: Create Dark Seed
(621,10183), -- Battle Manual: Create Special Bolt
(497,10038), -- Battle Manual: Crushing Pain
(524,10065), -- Battle Manual: Cure Dark Seed
(1439,10048), -- Battle Manual: Curse of Divinity
(1434,10033), -- Battle Manual: Dark Explosion
(1437,10046), -- Battle Manual: Dark Flame
(477,10030), -- Battle Manual: Dark Smash
(476,10029), -- Battle Manual: Dark Strike
(1443,10083), -- Battle Manual: Dark Weapon
(1442,10082), -- Battle Manual: Darkness Protection
(510,10054), -- Battle Manual: Deadly Roulette
(1435,10078), -- Battle Manual: Death Mark
(525,10066), -- Battle Manual: Decoy
(471,10028), -- Battle Manual: Defuse Trap
(1476,10185), -- Battle Manual: Destroy Instinct
(470,10027), -- Battle Manual: Detect Trap
(1454,10100), -- Spellbook: Diamond Dust
(485,10076), -- Battle Manual: Disarm
(1459,10103), -- Spellbook: Divine Power
(478,10031), -- Battle Manual: Double Thrust
(1484,10192), -- Battle Manual: Enervation
(526,10068), -- Battle Manual: Enuma Elish
(1475,10181), -- Battle Manual: Erase Mark
(1431,10025), -- Battle Manual: Fallen Arrow
(468,10026), -- Battle Manual: Fallen Attack
(490,10077), -- Battle Manual: Fast Shot
(538,10091), -- Battle Manual: Final Form
(514,10058), -- Battle Manual: Fire Trap
(1451,10107), -- Spellbook: Fire Vortex Buster
(517,10061), -- Battle Manual: Flash Trap
(482,10074), -- Battle Manual: Furious Soul
(1453,10108), -- Spellbook: Ice Vortex Crusher
(523,10089), -- Battle Manual: Imbue Dark Seed
(1432,10072), -- Battle Manual: Increase Power
(1469,10069), -- Battle Manual: Leopold
(502,10040), -- Battle Manual: Life to Soul
(1479,10191), -- Battle Manual: Magic Impulse
(1486,10195), -- Battle Manual: Mental Impoverish
(1460,10104), -- Spellbook: Mana Gain
(1481,10187), -- Battle Manual: Oblivion
(487,10180), -- Battle Manual: Penetrate Shot
(515,10059), -- Battle Manual: Poison Trap
(1470,10090), -- Battle Manual: Prahnah
(1444,10084), -- Battle Manual: Pride of Kamael
(1478,10189), -- Battle Manual: Protection Instinct
(519,10063), -- Battle Manual: Quiver of Bolts - Grade A
(620,10071), -- Battle Manual: Quiver of Bolts - Grade B
(520,10064), -- Battle Manual: Quiver of Bolts - Grade S
(469,10073), -- Battle Manual: Rapid Attack
(522,10088), -- Battle Manual: Real Target
(1487,10186), -- Battle Manual: Restoration
(1488,10184), -- Battle Manual: Restoration Impact
(508,10053), -- Battle Manual: Rising Shot
(484,10094), -- Battle Manual: Rush
(503,10041), -- Battle Manual: Scorn
(1446,10092), -- Battle Manual: Shadow Bind
(521,10087), -- Battle Manual: Sharpshooting
(489,10034), -- Battle Manual: Shift Target
(505,10043), -- Battle Manual: Shining Edge
(494,10097), -- Battle Manual: Shoulder Charge
(496,10037), -- Battle Manual: Slashing Blade
(516,10060), -- Battle Manual: Slow Trap
(625,10204), -- Battle Manual: Soul Gathering
(1480,10196), -- Battle Manual: Soul Harmony
(1436,10045), -- Battle Manual: Soul of Pain
(627,10276), -- Battle Manual: Soul Shock
(1441,10081), -- Battle Manual: Soul to Empower
(1485,10194), -- Battle Manual: Spite
(492,10036), -- Battle Manual: Spread Wing
(1440,10049), -- Battle Manual: Steal Divinity
(493,10095), -- Battle Manual: Storm Assault
(1445,10085), -- Battle Manual: Surrender To Unholy
(483,10075), -- Battle Manual: Sword Shield
(511,10055), -- Battle Manual: Temptation
(1483,10188), -- Battle Manual: Thin Skin
(1455,10101), -- Spellbook: Throne of Ice
(1458,10102), -- Spellbook: Throne of Wind
(504,10042), -- Battle Manual: Triple Thrust
(507,10052), -- Battle Manual: Twin Shot
(622,10182), -- Battle Manual: Ultimate Escape
(1477,10190), -- Battle Manual: Vampiric Impulse
(501,10080), -- Battle Manual: Violent Temper
(1447,10093), -- Battle Manual: Voice Bind
(628,10275), -- Battle Manual: Warp
(1482,10193), -- Battle Manual: Weak Constitution
(1457,10203), -- Spellbook - Empowering Echo
(1510,10596), -- Battle Manual - Soul Cleanse
(1511,10597), -- Battle Manual - Curse of Life Flow
(1512,10598), -- Battle Manual - Soul Vortex
(1513,10599), -- Battle Manual - Soul Vortex Extinction
(1514,10600), -- Battle Manual - Soul Barrier
(1515,10601), -- Battle Manual - Lightning Barrier
(1516,10602), -- Battle Manual - Soul Strike
(792,10603), -- Battle Manual - Betrayal Mark
(793,10604), -- Battle Manual - Rush Impact
(794,10605), -- Battle Manual - Mass Disarm
(1517,10608), -- Amulet - Chant of Combat
(1518,10609), -- Amulet - Chant of Critical Attack
(1519,10610), -- Amulet - Chant of Blood Awakening
(818,12768), -- Spellbook - Evasion Counter
(819,12769), -- Spellbook - Evasion Chance
(820,12770), -- Spellbook - Evasion Haste
(821,12771), -- Spellbook - Shadow Step
(1520,12772), -- Spellbook - Inquisitor
(1521,12772), -- Spellbook - Inquisitor
(1522,12772), -- Spellbook - Inquisitor
(831,12820), -- Blueprint - Summon Merchant Golem
(833,12773), -- Battle Manual - Body Reconstruction
(834,12774), -- Battle Manual - Blood Construct
(835,12775), -- Battle Manual - Imbue Seed of Destruction
(836,12776), -- Battle Manual - Oblivion Trap
(837,12777), -- Battle Manual - Painkiller
(838,12778), -- Battle Manual - Soul Web
(1456,10109); -- Spellbook: Wind Vortex Slug