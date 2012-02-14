package l2rt.gameserver.model.instances;

import javolution.util.FastMap;
import l2rt.config.ConfigSystem;
import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.CursedWeaponsManager;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.L2ObjectTasks.SoulConsumeTask;
import l2rt.gameserver.model.base.Experience;
import l2rt.gameserver.model.base.ItemToDrop;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.items.L2ItemInstance.ItemLocation;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestEventType;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.SocialAction;
import l2rt.gameserver.network.serverpackets.SpawnEmitter;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class manages all Monsters.
 *
 * L2MonsterInstance :<BR><BR>
 * <li>L2MinionInstance</li>
 * <li>L2RaidBossInstance </li>
 */
public class L2MonsterInstance extends L2NpcInstance
{
	protected static final class RewardInfo
	{
		protected L2Character _attacker;
		protected int _dmg = 0;

		public RewardInfo(final L2Character attacker, final int dmg)
		{
			_attacker = attacker;
			_dmg = dmg;
		}

		public void addDamage(int dmg)
		{
			if(dmg < 0)
				dmg = 0;

			_dmg += dmg;
		}

		@Override
		public int hashCode()
		{
			return _attacker.getObjectId();
		}
	}

	// дроп мешочков для прем акка
	private static final int[] D_GRADE = { 32782, 32790, 32228 };
	private static final int[] C_GRADE = { 32783, 32791, 32242, 32229, 32230, 32231 };
	private static final int[] B_GRADE = { 32784, 32792, 32232,32233 };
	private static final int[] A_GRADE = { 32785, 32793, 32234, 32235, 32236 };
	private static final int[] S_GRADE = { 32786, 32794, 32237, 32238, 32239, 32240 };
	private static final int[] R_GRADE = { 32787, 32795, 32779 };
	private static final int[] R2_GRADE = { 33466, 33467 };
	private static final int[] R95_GRADE = { 32788, 32796, 32780 };
	private static final int[] R99_GRADE = { 32789, 32797, 32781 };
	
	private boolean _dead = false, _dying = false;
	private final ReentrantLock dieLock = new ReentrantLock(), dyingLock = new ReentrantLock(),
			sweepLock = new ReentrantLock(), harvestLock = new ReentrantLock();

	/** Stores the extra (over-hit) damage done to the L2NpcInstance when the attacker uses an over-hit enabled skill */
	private double _overhitDamage;

	/** Stores the attacker who used the over-hit enabled skill on the L2NpcInstance */

	protected MinionList _minionList;
	private ScheduledFuture<?> minionMaintainTask;

	private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;

	/** Максимальный уровень мобов */
	private static final int MONSTER_MAX_LEVEL = 200;

	private GArray<L2ItemInstance> _inventory;

	/** crops */
	private L2ItemInstance _harvestItem;
	private L2Item _seeded;
	private long seederStoreId, spoilerStoreId, overhitAttackerStoreId;

	/** The table containing all players objectID that successfully absorbed the soul of this L2NpcInstance */
	private GArray<Integer> _absorbersList;

	/** Table containing all Items that a Dwarf can Sweep on this L2NpcInstance */
	private L2ItemInstance[] _sweepItems;

	/** Static tables containing all mobIDs that can have their souls absorbed, divided by max soul crystal level */
	private static final int[] _absorbingMOBS_level4 = { 20583, 20584, 20585, 20586, 20587, 20588, 20636, 20637, 20638,
			20639, 20640, 20641, 20642, 20643, 20644, 20645 };
	private static final int[] _absorbingMOBS_level8 = { 20646, 20647, 20648, 20649, 20650, 21006, 21007, 21008 };
	private static final int[] _absorbingMOBS_level10 = { 20627, 20628, 20629, 20674, 20761, 20762, 20821, 20823, 20826,
			20827, 20828, 20829, 20830, 20831, 20858, 20859, 20860, 21009, 21010, 21062, 21063, 21068, 21070 };

	private static final int[] _absorbingMOBS_level12 = { 29001, // Ant Queen (по идее только 11 лвл по фрее качается, но в падлу переделывать, поэтому качается 11,12 лвлы)
			29006, // Core
			29014, // Orfen
			29022, // Zaken Night
			29176, // Zaken Day
			29056, // Ice Fairy Sirra
			25016, // The 3rd Underwater Guardian
			25106, // Ghost of the Well Lidia
			25179, // Guardian of the statue of Giant Karum
			25234, // Ancient Weird Drake
			25407, // Lord Ishka
			25423, // Fairy Queen Timiniel
			25226, // Roaring Lord Kastor
			25051, // Rahha
			25125, // Fierce Tiger King Angel
			25444, // Enmity Ghost Ramdal
			25255, // Gargoyle Lord Tiphon
			25478, // Shilen's Priest Hisilrome
			25322, // Demon's Agent Falston
			25263, // Kernon's Faithful Servant Kelone
			25337, // Anakazel (68)
			25073, // Bloody Priest Rudelto
			25233, // Spirit of Andras, the Betrayer
			25035, // Shilen's Messenger Cabrio
			25092, // Korim
			25163, // Roaring Skylancer
			25198, // Fafurion's Herald Lokness
			25252, // Palibati Queen Themis
			25269, // Beast Lord Behemoth
			25281, // Anakim's Nemesis Zakaron
			25325, // Flame of Splendor Barakiel
			25453, // Meanas Anor
			25328, // Eilhalder von Hellmann
			25447, // Immortal Savior Mardil
			25199, // Water Dragon Seer Sheshark
			25235, // Vanor Chief Kandra
			25248, // Doom Blade Tanatos
			27262, // Death Lord Hallate
			25523, // Plague Golem
			25109, // Antharas Priest Cloe
			25202, // Krokian Padisha Sobekk
			25296, // Icicle Emperor Bumbalump
			29020, // Baium
			25054, // Kernon
			25229, // Storm Winged Naga
			25244, // Last Lesser Giant Olkuth
			25249, // Palatanos of Horrific Power
			25266, // Bloody Empress Decarbia
			25276, // Death Lord Ipos
			25282, // Death Lord Shax
			25205, // Ocean Flame Ashakiel
			25524, // Flamestone Giant
			25143, // Fire of Wrath Shuriel
			25245, // Last Lesser Giant Glaki
			25293, // Hestia, Guardian Deity of the Hot Springs
			25126, // Longhorn Golkonda
			25450, // Cherub Galaxia
			29062, // Andreas Van Halter
			29065, // Sailren
			29095, // Gordon
			25283, // Lilith
			25286, // Anakim
			25299, // Ketra's Hero Hekaton
			25302, // Ketra's Commander Tayr
			25305, // Ketra's Chief Brakki
			25306, // Soul of Fire Nastron
			25309, // Varka's Hero Shadith
			25312, // Varka's Commander Mos
			25315, // Varka's Chief Horus
			25316, // Soul of Water Ashutar
			25319, // Ember
			25527, // Uruka
			22215, // Tyrannosaurus
			22216, // Tyrannosaurus
			22217, // Tyrannosaurus
			25536, // Hannibal
			25539, // Typhoon
			25643, // Awakened Ancient Sentry
			25644, // Awakened Ancient Severer
			25645, // Awakened Ancient Soul Extractor
			25646, // Awakened Ancient Soul Devourer
			25647, // Awakened Ancient Fighter
			25648, // Awakened Ancient Fighter
			25649, // Awakened Ancient Executor
			25650, // Awakened Ancient Executor
			25651, // Awakened Ancient Prophet
			25652, // Awakened Ancient Prophet
			25665, // Yehan Klodekus
			25666, // Yehan Klanikus
			25690, // Aenkinel (81)
			25691, // Aenkinel (81)
			25692, // Aenkinel (81)
			25693, // Aenkinel (81)
			25667, // Cannibalistic Stakato Chief
			25680, // Giant Marpanak
			25681, // Gorgolos
			25694, // Aenkinel (82)
			25540, // Demon Prince
			25542, // Ranku
			25544, // Tully (не реализован, тип - L2Npc)
			25674, // Gwindorr
			25684, // Last Titan Utenus
			25687, // Hekaton Prime
	};
	private static final int[] _absorbingMOBS_level13 = { 29181, // High Zaken
			29118, // Beleth (не реализован, тип - L2Npc)
			29119, // Beleth
			25695, // Aenkinel (84)
			25487, // Water Spirit Lian
			25603, // Darion (не реализован, тип - L2Npc)
			25671, // Queen Shyeed
			25531, // Darnel
			25532, // Kechi
			25534, // Tears
			29099, // Baylor
			29046, // Scarlet van Halisha
			29047, // Scarlet van Halisha
			29177, // Freya Normal
			29178, // Freya Extreme
			29179, // Freya Normal (ID 2 o_O)
			29180, // Freya Extreme (ID 2 o_O)
			25696, // Takrakhan
			25698, // Dopagen
			25699, // Klakies Normal
			25700, // Klakies High
			29019, // Antharas
			29066, // Antharas
			29067, // Antharas
			29068, // Antharas
			29028, // Valakas
			29020, // Baium
			25054, // Kernon
			25229, // Storm Winged Naga
			25244, // Last Lesser Giant Olkuth
			25249, // Palatanos of Horrific Power
			25266, // Bloody Empress Decarbia
			25276, // Death Lord Ipos
			25282, // Death Lord Shax
			25205, // Ocean Flame Ashakiel
			25524, // Flamestone Giant
			25143, // Fire of Wrath Shuriel
			25245, // Last Lesser Giant Glaki
			25293, // Hestia, Guardian Deity of the Hot Springs
			25338, // Anakazel (78)
			25126, // Longhorn Golkonda
			25450, // Cherub Galaxia
			29062, // Andreas Van Halter
			29065, // Sailren
			29095, // Gordon
			25283, // Lilith
			25286, // Anakim
			25299, // Ketra's Hero Hekaton
			25302, // Ketra's Commander Tayr
			25305, // Ketra's Chief Brakki
			25306, // Soul of Fire Nastron
			25309, // Varka's Hero Shadith
			25312, // Varka's Commander Mos
			25315, // Varka's Chief Horus
			25316, // Soul of Water Ashutar
			25319, // Ember
			25527, // Uruka
			25536, // Hannibal
			25539, // Typhoon
			25643, // Awakened Ancient Sentry
			25644, // Awakened Ancient Severer
			25645, // Awakened Ancient Soul Extractor
			25646, // Awakened Ancient Soul Devourer
			25647, // Awakened Ancient Fighter
			25648, // Awakened Ancient Fighter
			25649, // Awakened Ancient Executor
			25650, // Awakened Ancient Executor
			25651, // Awakened Ancient Prophet
			25652, // Awakened Ancient Prophet
			25665, // Yehan Klodekus
			25666, // Yehan Klanikus
			25690, // Aenkinel (81)
			25691, // Aenkinel (81)
			25692, // Aenkinel (81)
			25693, // Aenkinel (81)
			25667, // Cannibalistic Stakato Chief
			25680, // Giant Marpanak
			25681, // Gorgolos
			29150, // Ekimus
			29163, // Tiat
			29175, // Tiat
			32525, // Tiat
			25694, // Aenkinel (82)
			25540, // Demon Prince
			25542, // Ranku
			25544, // Tully (не реализован, тип - L2Npc)
			25674, // Gwindorr
			25684, // Last Titan Utenus
			25687, // Hekaton Prime
			29006, // Core
			29014, // Orfen
	};
	private static final int[] _absorbingMOBS_level14 = { 25338, // Anakazel (78)
			29022, // Zaken Night
			29062, // Andreas Van Halter
			29065, // Sailren
			29095, // Gordon
			25283, // Lilith
			25286, // Anakim
			25299, // Ketra's Hero Hekaton
			25302, // Ketra's Commander Tayr
			25305, // Ketra's Chief Brakki
			25306, // Soul of Fire Nastron
			25309, // Varka's Hero Shadith
			25312, // Varka's Commander Mos
			25315, // Varka's Chief Horus
			25316, // Soul of Water Ashutar
			25319, // Ember
			25527, // Uruka
			25536, // Hannibal
			25539, // Typhoon
			25643, // Awakened Ancient Sentry
			25644, // Awakened Ancient Severer
			25645, // Awakened Ancient Soul Extractor
			25646, // Awakened Ancient Soul Devourer
			25647, // Awakened Ancient Fighter
			25648, // Awakened Ancient Fighter
			25649, // Awakened Ancient Executor
			25650, // Awakened Ancient Executor
			25651, // Awakened Ancient Prophet
			25652, // Awakened Ancient Prophet
			25665, // Yehan Klodekus
			25666, // Yehan Klanikus
			25690, // Aenkinel (81)
			25691, // Aenkinel (81)
			25692, // Aenkinel (81)
			25693, // Aenkinel (81)
			25667, // Cannibalistic Stakato Chief
			25680, // Giant Marpanak
			25681, // Gorgolos
			29150, // Ekimus
			29163, // Tiat
			29175, // Tiat
			32525, // Tiat
			25694, // Aenkinel (82)
			25540, // Demon Prince
			25542, // Ranku
			25544, // Tully (не реализован, тип - L2Npc)
			25674, // Gwindorr
			25684, // Last Titan Utenus
			25687, // Hekaton Prime
			29181, // High Zaken
			29118, // Beleth (не реализован, тип - L2Npc)
			29119, // Beleth
			25695, // Aenkinel (84)
			25487, // Water Spirit Lian
			25603, // Darion (не реализован, тип - L2Npc)
			25671, // Queen Shyeed
			25531, // Darnel
			25532, // Kechi
			25534, // Tears
			29099, // Baylor
			29046, // Scarlet van Halisha
			29047, // Scarlet van Halisha
			29177, // Freya Normal
			29178, // Freya Extreme
			29179, // Freya Normal (ID 2 o_O)
			29180, // Freya Extreme (ID 2 o_O)
			25696, // Takrakhan
			25698, // Dopagen
			25699, // Klakies Normal
			25700, // Klakies High
			29019, // Antharas
			29066, // Antharas
			29067, // Antharas
			29068, // Antharas
			29028, // Valakas
	};
	private static final int[] _absorbingMOBS_level15 = { 29019, // Antharas
			29066, // Antharas
			29067, // Antharas
			29068, // Antharas
			29028, // Valakas
			29046, // Scarlet van Halisha
			29047, // Scarlet van Halisha
			29118, // Beleth
			29119, // Beleth
			29163, // Tiat
			29175, // Tiat
			32525, // Tiat
			29150, // Ekimus
			29181, // High Zaken
			25680, // Giant Marpanak
			25681, // Gorgolos
			25694, // Aenkinel (82)
			25540, // Demon Prince
			25542, // Ranku
			25544, // Tully (не реализован, тип - L2Npc)
			25674, // Gwindorr
			25684, // Last Titan Utenus
			25687, // Hekaton Prime
			25695, // Aenkinel (84)
			25487, // Water Spirit Lian
			25603, // Darion (не реализован, тип - L2Npc)
			25671, // Queen Shyeed
			25531, // Darnel
			25532, // Kechi
			25534, // Tears
			25700, // Klakies High
			29099, // Baylor
			25696, // Takrakhan
			25698, // Dopagen
			29177, // Freya Normal
			29178, // Freya Extreme
			29179, // Freya Normal (ID 2 o_O)
			29180, // Freya Extreme (ID 2 o_O)
	};
	private static final int[] _absorbingMOBS_level16 = { 29019, // Antharas
			29066, // Antharas
			29067, // Antharas
			29068, // Antharas
			29028, // Valakas
			29046, // Scarlet van Halisha
			29047, // Scarlet van Halisha
			29118, // Beleth
			29119, // Beleth
			29163, // Tiat
			29175, // Tiat
			32525, // Tiat
			29150, // Ekimus
			29181, // High Zaken
			25603, // Darion (не реализован, тип - L2Npc)
			25671, // Queen Shyeed
			25531, // Darnel
			25532, // Kechi
			25534, // Tears
			29099, // Baylor
			25696, // Takrakhan
			25698, // Dopagen
			29177, // Freya Normal
			29178, // Freya Extreme
			29179, // Freya Normal (ID 2 o_O)
			29180, // Freya Extreme (ID 2 o_O)
	};

	private static final int[] _absorbingMOBS_level17 = { 29019, // Antharas
			29066, // Antharas
			29067, // Antharas
			29068, // Antharas
			29028, // Valakas
			29046, // Scarlet van Halisha
			29047, // Scarlet van Halisha
			29118, // Beleth
			29119, // Beleth
			29163, // Tiat
			29175, // Tiat
			32525, // Tiat
			29150, // Ekimus
			29181, // High Zaken
			29099, // Baylor
			29177, // Freya Normal
			29178, // Freya Extreme
			29179, // Freya Normal (ID 2 o_O)
			29180, // Freya Extreme (ID 2 o_O)
	};

	private static final int[] _absorbingMOBS_level18 = { 29019, // Antharas
			29066, // Antharas
			29067, // Antharas
			29068, // Antharas
			29028, // Valakas
			29178, // Freya Extreme
			29180, // Freya Extreme (ID 2 o_O)
	};

	private static final int[] _randomLeveling = { 29176, // Zaken Day
			29056, // Ice Fairy Sirra
			25016, // The 3rd Underwater Guardian
			25106, // Ghost of the Well Lidia
			25179, // Guardian of the statue of Giant Karum
			25234, // Ancient Weird Drake
			25407, // Lord Ishka
			25423, // Fairy Queen Timiniel
			25226, // Roaring Lord Kastor
			25051, // Rahha
			25125, // Fierce Tiger King Angel
			25444, // Enmity Ghost Ramdal
			25255, // Gargoyle Lord Tiphon
			25478, // Shilen's Priest Hisilrome
			25322, // Demon's Agent Falston
			25263, // Kernon's Faithful Servant Kelone
			25337, // Anakazel (68)
			25073, // Bloody Priest Rudelto
			25233, // Spirit of Andras, the Betrayer
			25035, // Shilen's Messenger Cabrio
			25092, // Korim
			25163, // Roaring Skylancer
			25198, // Fafurion's Herald Lokness
			25252, // Palibati Queen Themis
			25269, // Beast Lord Behemoth
			25281, // Anakim's Nemesis Zakaron
			25325, // Flame of Splendor Barakiel
			25453, // Meanas Anor
			25328, // Eilhalder von Hellmann
			25447, // Immortal Savior Mardil
			25199, // Water Dragon Seer Sheshark
			25235, // Vanor Chief Kandra
			25248, // Doom Blade Tanatos
			27262, // Death Lord Hallate
			25523, // Plague Golem
			25109, // Antharas Priest Cloe
			25202, // Krokian Padisha Sobekk
			25296, // Icicle Emperor Bumbalump
			25054, // Kernon
			25229, // Storm Winged Naga
			25244, // Last Lesser Giant Olkuth
			25249, // Palatanos of Horrific Power
			25266, // Bloody Empress Decarbia
			25276, // Death Lord Ipos
			25282, // Death Lord Shax
			25205, // Ocean Flame Ashakiel
			25524, // Flamestone Giant
			25143, // Fire of Wrath Shuriel
			25245, // Last Lesser Giant Glaki
			25293, // Hestia, Guardian Deity of the Hot Springs
			25126, // Longhorn Golkonda
			25450, // Cherub Galaxia
			29062, // Andreas Van Halter
			29065, // Sailren
			29095, // Gordon
			25299, // Ketra's Hero Hekaton
			25302, // Ketra's Commander Tayr
			25305, // Ketra's Chief Brakki
			25306, // Soul of Fire Nastron
			25309, // Varka's Hero Shadith
			25312, // Varka's Commander Mos
			25315, // Varka's Chief Horus
			25316, // Soul of Water Ashutar
			25527, // Uruka
			22215, // Tyrannosaurus
			22216, // Tyrannosaurus
			22217, // Tyrannosaurus
			25536, // Hannibal
			25539, // Typhoon
			25643, // Awakened Ancient Sentry
			25644, // Awakened Ancient Severer
			25645, // Awakened Ancient Soul Extractor
			25646, // Awakened Ancient Soul Devourer
			25647, // Awakened Ancient Fighter
			25648, // Awakened Ancient Fighter
			25649, // Awakened Ancient Executor
			25650, // Awakened Ancient Executor
			25651, // Awakened Ancient Prophet
			25652, // Awakened Ancient Prophet
			25665, // Yehan Klodekus
			25666, // Yehan Klanikus
			25690, // Aenkinel (81)
			25691, // Aenkinel (81)
			25692, // Aenkinel (81)
			25693, // Aenkinel (81)
			25667, // Cannibalistic Stakato Chief
			25680, // Giant Marpanak
			25681, // Gorgolos
			25694, // Aenkinel (82)
			25540, // Demon Prince
			25542, // Ranku
			25544, // Tully (не реализован, тип - L2Npc)
			25674, // Gwindorr
			25684, // Last Titan Utenus
			25687, // Hekaton Prime
			29181, // Zaken High
			25695, // Aenkinel (84)
			25487, // Water Spirit Lian
			25603, // Darion (не реализован, типа - L2Npc)
			25671, // Queen Shyeed
			25531, // Darnel
			25532, // Kechi
			25534, // Tears
			25696, // Takrakhan
			25698, // Dopagen
			29099, // Baylor
	};

	/** Soul Crystal Basic Informations */
	// First ID of each soul crystal
	private static final int[] _REDCRYSTALS = { 4629, 4630, 4631, 4632, 4633, 4634, 4635, 4636, 4637, 4638, 4639, 5577,
			5580, 5908, 9570, 10480, 13071, 15541, 15826 };
	private static final int[] _GREENCRYSTALS = { 4640, 4641, 4642, 4643, 4644, 4645, 4646, 4647, 4648, 4649, 4650, 5578,
			5581, 5911, 9572, 10482, 13073, 15543, 15828 };
	private static final int[] _BLUECRYSTALS = { 4651, 4652, 4653, 4654, 4655, 4656, 4657, 4658, 4659, 4660, 4661, 5579,
			5582, 5914, 9571, 10481, 13072, 15542, 15827 };

	private static final short _REDCURSEDCRYSTAL_LVL14 = 10160;
	private static final short _BLUECURSEDCRYSTAL_LVL14 = 10161;
	private static final short _GREENCURSEDCRYSTAL_LVL14 = 10162;

	// Max number of levels a soul crystal may reach
	private static final short _MAX_CRYSTALS_LEVEL = 18;
	/** End of Soul Crystal Basic Informations */

	// For ALT_GAME_MATHERIALSDROP
	protected static final L2DropData[] _matdrop = new L2DropData[] {
			//                                           Item              Price Chance
			new L2DropData(1864, 1, 1, 50000, 1), // Stem              100   5%
			new L2DropData(1865, 1, 1, 25000, 1), // Varnish           200   2.5%
			new L2DropData(1866, 1, 1, 16666, 1), // Suede             300   1.6666%
			new L2DropData(1867, 1, 1, 33333, 1), // Animal Skin       150   3.3333%
			new L2DropData(1868, 1, 1, 50000, 1), // Thread            100   5%
			new L2DropData(1869, 1, 1, 25000, 1), // Iron Ore          200   2.5%
			new L2DropData(1870, 1, 1, 25000, 1), // Coal              200   2.5%
			new L2DropData(1871, 1, 1, 25000, 1), // Charcoal          200   2.5%
			new L2DropData(1872, 1, 1, 50000, 1), // Animal Bone       150   5%
			new L2DropData(1873, 1, 1, 10000, 1), // Silver Nugget     500   1%
			new L2DropData(1874, 1, 1, 1666, 20), // Oriharukon Ore    3000  0.1666%
			new L2DropData(1875, 1, 1, 1666, 20), // Stone of Purity   3000  0.1666%
			new L2DropData(1876, 1, 1, 5000, 20), // Mithril Ore       1000  0.5%
			new L2DropData(1877, 1, 1, 1000, 20), // Adamantite Nugget 5000  0.1%
			new L2DropData(4039, 1, 1, 833, 40), //  Mold Glue         6000  0.0833%
			new L2DropData(4040, 1, 1, 500, 40), //  Mold Lubricant    10000 0.05%
			new L2DropData(4041, 1, 1, 217, 40), //  Mold Hardener     23000 0.0217%
			new L2DropData(4042, 1, 1, 417, 40), //  Enria             12000 0.0417%
			new L2DropData(4043, 1, 1, 833, 40), //  Asofe             6000  0.0833%
			new L2DropData(4044, 1, 1, 833, 40) //   Thons             6000  0.0833%
	};

	protected static final GArray<L2DropGroup> _herbs = new GArray<L2DropGroup>(3);

	static
	{
		L2DropGroup d = new L2DropGroup(0);
		d.addDropItem(new L2DropData(8600, 1, 1, 120000, 1)); // of Life                    15%
		d.addDropItem(new L2DropData(8603, 1, 1, 120000, 1)); // of Mana                    15%
		d.addDropItem(new L2DropData(8601, 1, 1, 40000, 1)); //  Greater of Life            5%
		d.addDropItem(new L2DropData(8604, 1, 1, 40000, 1)); //  Greater of Mana            5%
		d.addDropItem(new L2DropData(8602, 1, 1, 12000, 1)); //  Superior of Life           1.6%
		d.addDropItem(new L2DropData(8605, 1, 1, 12000, 1)); //  Superior of Mana           1.6%
		d.addDropItem(new L2DropData(8614, 1, 1, 3000, 1)); //   of Recovery                0.3%
		_herbs.add(d);
		d = new L2DropGroup(0);
		d.addDropItem(new L2DropData(8611, 1, 1, 50000, 1)); //  of Speed                   5%
		d.addDropItem(new L2DropData(8606, 1, 1, 50000, 1)); //  of Power                   5%
		d.addDropItem(new L2DropData(8608, 1, 1, 50000, 1)); //  of Atk. Spd.               5%
		d.addDropItem(new L2DropData(8610, 1, 1, 50000, 1)); //  of Critical Attack         5%
		d.addDropItem(new L2DropData(10656, 1, 1, 50000, 1)); // of Critical Attack - Power 5%
		d.addDropItem(new L2DropData(10655, 1, 1, 50000, 1)); // of Life Force Absorption   5%
		d.addDropItem(new L2DropData(8607, 1, 1, 50000, 1)); //  of Magic                   5%
		d.addDropItem(new L2DropData(8609, 1, 1, 50000, 1)); //  of Casting Speed           5%
		d.addDropItem(new L2DropData(8612, 1, 1, 10000, 1)); //  of Warrior                 1%
		d.addDropItem(new L2DropData(8613, 1, 1, 10000, 1)); //  of Mystic                  1%
		_herbs.add(d);
		d = new L2DropGroup(0);
		d.addDropItem(new L2DropData(10657, 1, 1, 3000, 1)); //  of Doubt                   0.3%
		//d.addDropItem(new L2DropData(13028, 1, 1, 2000, 1)); //  of Vitality                0.2%
		_herbs.add(d);
	}

	protected static final L2DropData[] _lifestones = new L2DropData[] {
			//
			new L2DropData(8723, 1, 1, 200, 44, 46), // Life Stone: level 46
			new L2DropData(8724, 1, 1, 200, 47, 49), // Life Stone: level 49
			new L2DropData(8725, 1, 1, 200, 50, 52), // Life Stone: level 52
			new L2DropData(8726, 1, 1, 200, 53, 55), // Life Stone: level 55
			new L2DropData(8727, 1, 1, 200, 56, 58), // Life Stone: level 58
			new L2DropData(8728, 1, 1, 200, 59, 61), // Life Stone: level 61
			new L2DropData(8729, 1, 1, 200, 62, 66), // Life Stone: level 64
			new L2DropData(8730, 1, 1, 200, 67, 72), // Life Stone: level 67
			new L2DropData(8731, 1, 1, 200, 73, 75), // Life Stone: level 70
			new L2DropData(8732, 1, 1, 200, 76, 79), // Life Stone: level 76
			new L2DropData(9573, 1, 1, 150, 80, 81), // Life Stone: level 80
			new L2DropData(10483, 1, 1, 120, 82, 83), // Life Stone: level 82
			new L2DropData(14166, 1, 1, 100, 84, MONSTER_MAX_LEVEL), // Life Stone: level 84
			new L2DropData(8733, 1, 1, 100, 44, 46), // Mid-Grade Life Stone: level 46
			new L2DropData(8734, 1, 1, 100, 47, 49), // Mid-Grade Life Stone: level 49
			new L2DropData(8735, 1, 1, 100, 50, 52), // Mid-Grade Life Stone: level 52
			new L2DropData(8736, 1, 1, 100, 53, 55), // Mid-Grade Life Stone: level 55
			new L2DropData(8737, 1, 1, 100, 56, 58), // Mid-Grade Life Stone: level 58
			new L2DropData(8738, 1, 1, 100, 59, 61), // Mid-Grade Life Stone: level 61
			new L2DropData(8739, 1, 1, 100, 62, 66), // Mid-Grade Life Stone: level 64
			new L2DropData(8740, 1, 1, 100, 67, 72), // Mid-Grade Life Stone: level 67
			new L2DropData(8741, 1, 1, 100, 73, 75), // Mid-Grade Life Stone: level 70
			new L2DropData(8742, 1, 1, 100, 76, 79), // Mid-Grade Life Stone: level 76
			new L2DropData(9574, 1, 1, 80, 80, 81), // Mid-Grade Life Stone: level 80
			new L2DropData(10484, 1, 1, 60, 82, 83), // Mid-Grade Life Stone: level 82
			new L2DropData(14167, 1, 1, 40, 84, MONSTER_MAX_LEVEL), // Mid-Grade Life Stone: level 84
			new L2DropData(8743, 1, 1, 30, 44, 46), // High-Grade Life Stone: level 46
			new L2DropData(8744, 1, 1, 30, 47, 49), // High-Grade Life Stone: level 49
			new L2DropData(8745, 1, 1, 30, 50, 52), // High-Grade Life Stone: level 52
			new L2DropData(8746, 1, 1, 30, 53, 55), // High-Grade Life Stone: level 55
			new L2DropData(8747, 1, 1, 30, 56, 58), // High-Grade Life Stone: level 58
			new L2DropData(8748, 1, 1, 30, 59, 61), // High-Grade Life Stone: level 61
			new L2DropData(8749, 1, 1, 30, 62, 66), // High-Grade Life Stone: level 64
			new L2DropData(8750, 1, 1, 30, 67, 72), // High-Grade Life Stone: level 67
			new L2DropData(8751, 1, 1, 30, 73, 75), // High-Grade Life Stone: level 70
			new L2DropData(8752, 1, 1, 30, 76, 79), // High-Grade Life Stone: level 76
			new L2DropData(9575, 1, 1, 25, 80, 81), // High-Grade Life Stone: level 80
			new L2DropData(10485, 1, 1, 20, 82, 83), // High-Grade Life Stone: level 82
			new L2DropData(14168, 1, 1, 30, 84, MONSTER_MAX_LEVEL), // High-Grade Life Stone: level 84
	};

	protected static final L2DropData[] _toplifestones = new L2DropData[] {
			//
			new L2DropData(8753, 1, 1, 100000, 44, 46), // Top-Grade Life Stone: level 46
			new L2DropData(8754, 1, 1, 100000, 47, 49), // Top-Grade Life Stone: level 49
			new L2DropData(8755, 1, 1, 100000, 50, 52), // Top-Grade Life Stone: level 52
			new L2DropData(8756, 1, 1, 100000, 53, 55), // Top-Grade Life Stone: level 55
			new L2DropData(8757, 1, 1, 100000, 56, 58), // Top-Grade Life Stone: level 58
			new L2DropData(8758, 1, 1, 100000, 59, 61), // Top-Grade Life Stone: level 61
			new L2DropData(8759, 1, 1, 100000, 62, 66), // Top-Grade Life Stone: level 64
			new L2DropData(8760, 1, 1, 100000, 67, 72), // Top-Grade Life Stone: level 67
			new L2DropData(8761, 1, 1, 100000, 73, 75), // Top-Grade Life Stone: level 70
			new L2DropData(8762, 1, 1, 100000, 76, 79), // Top-Grade Life Stone: level 76
			new L2DropData(9576, 1, 1, 85000, 80, 81), // Top-Grade Life Stone: level 80
			new L2DropData(10486, 1, 1, 65000, 82, 83), // Top-Grade Life Stone: level 82
			new L2DropData(14169, 1, 1, 50000, 84, MONSTER_MAX_LEVEL), // Top-Grade Life Stone: level 84
	};

	protected static final L2DropData[] _raiditems = new L2DropData[] {
			//
			new L2DropData(9814, 1, 2, 300000, 40, 74), // Memento Mori
			new L2DropData(9815, 1, 2, 300000, 40, 70), // Dragon Heart
			new L2DropData(9816, 1, 2, 300000, 40, 74), // Earth Egg
			new L2DropData(9817, 1, 2, 300000, 40, 74), // Nonliving Nucleus
			new L2DropData(9818, 1, 2, 300000, 40, 70), // Angelic Essence
			new L2DropData(8176, 1, 2, 300000, 40, 74) //  Destruction Tombstone
	};

	/**
	 * Constructor<?> of L2MonsterInstance (use L2Character and L2NpcInstance constructor).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to set the _template of the L2MonsterInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR) </li>
	 * <li>Set the name of the L2MonsterInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template to apply to the NPC
	 */
	public L2MonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_dead = false;
        _dying = false;
	}

	@Override
	public boolean isMovementDisabled()
	{
		// Невозможность ходить для этих мобов
		return getNpcId() == 18344 || getNpcId() == 18345 || /* Pailaka 3 wall's mobs*/getNpcId() == 18636 || getNpcId() == 18642 || getNpcId() == 18646 || getNpcId() == 18654 || getNpcId() == 18649 || getNpcId() == 18650 || getNpcId() == 18655 || getNpcId() == 18657 || super.isMovementDisabled();
	}

	@Override
	public boolean isLethalImmune()
	{
		return _isChampion > 0 || getNpcId() == 22215 || getNpcId() == 22216 || getNpcId() == 22217 || super.isLethalImmune();
	}

	@Override
	public boolean isFearImmune()
	{
		return _isChampion > 0 || super.isFearImmune();
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return _isChampion > 0 || super.isParalyzeImmune();
	}

	/**
	 * Return True if the attacker is not another L2MonsterInstance.<BR><BR>
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return !attacker.isMonster();
	}

	private int _isChampion;

	public int getChampion()
	{
		return _isChampion;
	}

	public void setChampion(int level)
	{
		if(level == 0)
		{
			removeSkillById(4407);
			_isChampion = 0;
		}
		else
		{
			addSkill(SkillTable.getInstance().getInfo(4407, level));
			_isChampion = level;
			setCurrentHp(getMaxHp(), false);
		}
	}

	public boolean canChampion()
	{
		return getTemplate().revardExp > 0;
	}

	@Override
	public int getTeam()
	{
		return getChampion();
	}

	/**
	 * Очищает флаги состояний смерти. Дает или очищает статус чемпиона. Восстанавливает HP. Спавнит миньонов. Отключает аггр на 10 секунд.
	 */
	@Override
	public void onSpawn()
	{
		_dead = false;
		_dying = false;
		overhitAttackerStoreId = 0;
		setChampion(0);
		if (!isRaid() && getReflection().canChampions() && !(this instanceof L2ReflectionBossInstance) && !(this instanceof L2MinionInstance) && !(this instanceof L2ChestInstance) && getTemplate().revardExp > 0 && getLevel()>=ConfigSystem.getInt("ChampionMinLevel") && getLevel()<=ConfigSystem.getInt("ChampionMaxLevel"))
		{
			double random = Rnd.nextDouble();
			if(Config.ALT_CHAMPION_CHANCE2 / 100 >= random)
				setChampion(2);
			else if((Config.ALT_CHAMPION_CHANCE1 + Config.ALT_CHAMPION_CHANCE2) / 100 >= random)
				setChampion(1);
		}
		setCurrentHpMp(getMaxHp(), getMaxMp(), true);
		super.onSpawn();
		spawnMinions();
		getAI().setGlobalAggro(System.currentTimeMillis() + 10000);

		// Clear mob spoil, absorbs, seed
		setSpoiled(false, null);
		_sweepItems = null;
		_absorbersList = null;
		_seeded = null;
		seederStoreId = 0;
		spoilerStoreId = 0;
	}

	protected int getMaintenanceInterval()
	{
		return MONSTER_MAINTENANCE_INTERVAL;
	}

	public MinionList getMinionList()
	{
		return _minionList;
	}

	public void setNewMinionList()
	{
		_minionList = new MinionList(this);
	}

	public class MinionMaintainTask implements Runnable
	{
		public void run()
		{
			if(L2MonsterInstance.this == null || L2MonsterInstance.this.isDead())
				return;
			try
			{
				if(L2MonsterInstance.this._minionList == null)
					L2MonsterInstance.this.setNewMinionList();
				L2MonsterInstance.this._minionList.maintainMinions();
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
	}

	public void spawnMinions()
	{
		if(getTemplate().getMinionData().size() > 0)
		{
			if(minionMaintainTask != null)
			{
				minionMaintainTask.cancel(true);
				minionMaintainTask = null;
			}
			minionMaintainTask = ThreadPoolManager.getInstance().scheduleAi(new MinionMaintainTask(), getMaintenanceInterval(), false);
		}
	}

	public Location getMinionPosition()
	{
		return Location.getAroundPosition(this, this, 100, 150, 10);
	}

	@Override
	public void callMinionsToAssist(L2Character attacker)
	{
		if(_minionList != null && _minionList.hasMinions())
			for(L2MinionInstance minion : _minionList.getSpawnedMinions())
				if(minion != null && minion.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK && !minion.isDead())
					minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(1, 100));
	}

	public void setDead(boolean dead)
	{
		_dead = dead;
	}

	public void removeMinions()
	{
		if(minionMaintainTask != null)
		{
			minionMaintainTask.cancel(true);
			minionMaintainTask = null;
		}
		if(_minionList != null)
			_minionList.maintainLonelyMinions();
		_minionList = null;
	}

	public int getTotalSpawnedMinionsInstances()
	{
		return _minionList == null ? 0 : _minionList.countSpawnedMinions();
	}

	public void notifyMinionDied(L2MinionInstance minion)
	{
		if(_minionList != null)
			_minionList.removeSpawnedMinion(minion);
	}

	@Override
	public boolean hasMinions()
	{
		return _minionList != null && _minionList.hasMinions();
	}

	@Override
	public void setReflection(long i)
	{
		super.setReflection(i);

		if(hasMinions())
			for(L2MinionInstance m : _minionList.getSpawnedMinions())
				m.setReflection(i);
	}

	@Override
	public void deleteMe()
	{
		removeMinions();
		if(_inventory != null)
			synchronized (_inventory)
			{
				for(L2ItemInstance item : _inventory)
					getTemplate().giveItem(item, false);
				_inventory = null;
			}
		super.deleteMe();
	}

	@Override
	public void doDie(final L2Character killer)
	{
		if(minionMaintainTask != null)
		{
			minionMaintainTask.cancel(true);
			minionMaintainTask = null;
		}

		if(_dead)
			return;

		dieLock.lock();
		try
		{
			if(_dead)
				return;
			_dieTime = System.currentTimeMillis();
			_dead = true;

			if(this instanceof L2ChestInstance && !((L2ChestInstance) this).isFake())
			{
				super.doDie(killer);
				return;
			}

			try
			{
				dyingLock.lock();
				_dying = true;
				calculateRewards(killer);
			}
			catch(final Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				_dying = false;
				dyingLock.unlock();
			}
		}
		finally
		{
			dieLock.unlock();
		}

		super.doDie(killer);
	}

	public void calculateRewards(L2Character lastAttacker)
	{
		HashMap<L2Playable, AggroInfo> aggroList = getAggroMap();
		L2Character topDamager = getTopDamager(aggroList.values());
		if(lastAttacker == null && topDamager != null)
			lastAttacker = topDamager;
		if(lastAttacker == null || aggroList.isEmpty())
			return;
		L2Player killer = lastAttacker.getPlayer();
		if(killer == null)
			return;

		if(topDamager == null)
			topDamager = lastAttacker;

		// Notify the Quest Engine of the L2NpcInstance death if necessary
		try
		{
			if(Config.KILL_COUNTER)
				killer.incrementKillsCounter(getNpcId());
			getTemplate().killscount++;

			if(getTemplate().hasQuestEvents())
			{
				GArray<L2Player> players = null; // массив с игроками, которые могут быть заинтересованы в квестах
				if(isRaid() && Config.ALT_NO_LASTHIT) // Для альта на ластхит берем всех игроков вокруг
				{
					players = new GArray<L2Player>();
					for(L2Playable pl : aggroList.keySet())
						if(pl.isPlayer() && !pl.isDead() && pl.getReflectionId() == getReflectionId() && (pl.isInRange(this, Config.ALT_PARTY_DISTRIBUTION_RANGE) || pl.isInRange(killer, Config.ALT_PARTY_DISTRIBUTION_RANGE)) && Math.abs(pl.getZ() - getZ()) < 400)
							players.add((L2Player) pl);
				}
				else if(killer.getParty() != null) // если пати то собираем всех кто подходит
				{
					players = new GArray<L2Player>(killer.getParty().getMemberCount());
					for(L2Player pl : killer.getParty().getPartyMembers())
						if(!pl.isDead() && pl.getReflectionId() == getReflectionId() && (pl.isInRange(this, Config.ALT_PARTY_DISTRIBUTION_RANGE) || pl.isInRange(killer, Config.ALT_PARTY_DISTRIBUTION_RANGE)) && Math.abs(pl.getZ() - getZ()) < 400)
							players.add(pl);
				}

				for(Quest quest : getTemplate().getEventQuests(QuestEventType.MOBKILLED))
				{
					L2Player toReward = killer;
					if(quest.getParty() != Quest.PARTY_NONE && players != null)
						if(isRaid() || quest.getParty() == Quest.PARTY_ALL) // если цель рейд или квест для всей пати награждаем всех участников
						{
							for(L2Player pl : players)
							{
								QuestState qs = pl.getQuestState(quest.getName());
								if(qs != null && !qs.isCompleted())
									quest.notifyKill(this, qs);
							}
							toReward = null;
						}
						else
						{ // иначе выбираем одного
							GArray<L2Player> interested = new GArray<L2Player>(players.size());
							for(L2Player pl : players)
							{
								QuestState qs = pl.getQuestState(quest.getName());
								if(qs != null && !qs.isCompleted()) // из тех, у кого взят квест
									interested.add(pl);
							}

							if(interested.isEmpty())
								continue;

							toReward = interested.get(Rnd.get(interested.size()));
							if(toReward == null)
								toReward = killer;
						}

					if(toReward != null)
					{
						QuestState qs = toReward.getQuestState(quest.getName());
						if(qs != null && !qs.isCompleted())
							quest.notifyKill(this, qs);
					}
				}
			}
		}
		catch(final Exception e)
		{
			e.printStackTrace();
		}

		// Distribute Exp and SP rewards to L2Player (including Summon owner) that hit the L2NpcInstance and to their Party members
		FastMap<L2Player, RewardInfo> rewards = new FastMap<L2Player, RewardInfo>().setShared(true);
		for(AggroInfo info : aggroList.values())
		{
			if(info.damage <= 1)
				continue;
			L2Character attacker = info.attacker;
			if(attacker == null || !attacker.isPlayer())
				continue;
			L2Player player = attacker.getPlayer();
			if(player != null)
			{
				RewardInfo reward = rewards.get(player);
				if(reward == null)
					rewards.put(player, new RewardInfo(player, info.damage));
				else
					reward.addDamage(info.damage);
			}
		}

		for(FastMap.Entry<L2Player, RewardInfo> e = rewards.head(), end = rewards.tail(); e != null && (e = e.getNext()) != end && e != null;)
		{
			L2Player attacker = e.getKey();
			RewardInfo reward = e.getValue();
			if(attacker == null || attacker.isDead() || reward == null)
				continue;
			L2Party party = attacker.getParty();
			int maxHp = getMaxHp();
			if(party == null)
			{
				int damage = Math.min(reward._dmg, maxHp);
				if(damage > 0)
				{
					double[] xpsp = calculateExpAndSp(attacker, attacker.getLevel(), damage);
					double neededExp = attacker.calcStat(Stats.SOULS_CONSUME_EXP, 0, this, null); // Начисление душ камаэлянам
					int dlvl = 0;
					if(neededExp > 0 && xpsp[0] > neededExp)
					{
						broadcastPacket(new SpawnEmitter(this, attacker));
						ThreadPoolManager.getInstance().scheduleGeneral(new SoulConsumeTask(attacker), 1000);
					}
					xpsp[0] = applyOverhit(killer, xpsp[0]);
					xpsp = attacker.applyVitality(this, xpsp[0], xpsp[1], 1.0);
					if (attacker.getLevel() > this.getLevel())
						dlvl = attacker.getLevel() - this.getLevel();
					if (dlvl < 11)
						attacker.addExpAndSp((long) xpsp[0], (long) xpsp[1], false, true);
				}
				rewards.remove(attacker);
			}
			else
			{
				int partyDmg = 0;
				int partylevel = 1;
				GArray<L2Player> rewardedMembers = new GArray<L2Player>();
				for(L2Player partyMember : party.getPartyMembers())
				{
					RewardInfo ai = rewards.remove(partyMember);
					if(partyMember.isDead() || !partyMember.isInRange(lastAttacker, Config.ALT_PARTY_DISTRIBUTION_RANGE))
						continue;
					if(ai != null)
						partyDmg += ai._dmg;
					rewardedMembers.add(partyMember);
					if(partyMember.getLevel() > partylevel)
						partylevel = partyMember.getLevel();
				}
				partyDmg = Math.min(partyDmg, maxHp);
				if(partyDmg > 0)
				{
					double[] xpsp = calculateExpAndSp(attacker, partylevel, partyDmg);
					double partyMul = (double) partyDmg / maxHp;
					xpsp[0] *= partyMul;
					xpsp[1] *= partyMul;
					xpsp[0] = applyOverhit(killer, xpsp[0]);
					party.distributeXpAndSp(xpsp[0], xpsp[1], rewardedMembers, lastAttacker, this);
				}
			}
		}

		// Check the drop of a cursed weapon
		CursedWeaponsManager.getInstance().dropAttackable(this, killer);

		// Manage Base, Quests and Special Events drops of the L2NpcInstance
		doItemDrop(topDamager);

		// Manage Sweep drops of the L2NpcInstance
		if(isSpoiled())
			doSweepDrop(lastAttacker, topDamager);
		
		 if (killer.getPA() && killer.getLevel() >= 20) 
			if (Rnd.chance(1))
			{
				if (killer.getLevel() >= 20 & killer.getLevel() < 40)
					dropItem(killer, D_GRADE[Rnd.get(D_GRADE.length-1)], 1); //D
				else if (killer.getLevel() >= 40 && killer.getLevel() < 52)
					dropItem(killer, C_GRADE[Rnd.get(C_GRADE.length-1)], 1); //C
				else if (killer.getLevel() >= 52 && killer.getLevel() < 61)
					dropItem(killer, B_GRADE[Rnd.get(B_GRADE.length-1)], 1); //B
				else if (killer.getLevel() >= 61 && killer.getLevel() < 76)
					dropItem(killer, A_GRADE[Rnd.get(A_GRADE.length-1)], 1); //A
				else if (killer.getLevel() >= 76 && killer.getLevel() < 90)
					dropItem(killer, S_GRADE[Rnd.get(S_GRADE.length-1)], 1); //S
				else if (killer.getLevel() >= 90 && killer.getLevel() < 95)
					dropItem(killer, R_GRADE[Rnd.get(R_GRADE.length-1)], 1); //R
				else if (killer.getLevel() >= 95 && killer.getLevel() < 99)
					dropItem(killer, R95_GRADE[Rnd.get(R95_GRADE.length-1)], 1); //R95
				else if (killer.getLevel() == 99)
					dropItem(killer, R99_GRADE[Rnd.get(R99_GRADE.length-1)], 1); //R99
			}
			

		if(!isRaid()) // С рейдов падают только топовые лайфстоны
		{
			double chancemod = ((L2NpcTemplate) _template).rateHp * Experience.penaltyModifier(calculateLevelDiffForDrop(topDamager.getLevel()), 9);

			// Дополнительный дроп материалов
			if(Config.ALT_GAME_MATHERIALSDROP && chancemod > 0 && (!isSeeded() || _seeded.isAltSeed()))
				for(L2DropData d : _matdrop)
					if(getLevel() >= d.getMinLevel())
					{
						long count = Util.rollDrop(d.getMinDrop(), d.getMaxDrop(), d.getChance() * chancemod * Config.RATE_DROP_ITEMS * killer.getRateItems(), true);
						if(count > 0)
							dropItem(killer, d.getItemId(), count);
					}

			// Хербы
			if(((L2NpcTemplate) _template).isDropHerbs && chancemod > 0)
				for(L2DropGroup h : _herbs)
				{
					Collection<ItemToDrop> itdl = h.rollFixedQty(0, this, killer, chancemod);
					if(itdl != null)
						for(ItemToDrop itd : itdl)
							dropItem(killer, itd.itemId, 1);
				}

			// Лайфстоуны
			if(chancemod > 0)
				for(L2DropData l : _lifestones)
					if(getLevel() >= l.getMinLevel() && getLevel() <= l.getMaxLevel() && Rnd.get(1, L2Drop.MAX_CHANCE) <= l.getChance() * Config.RATE_DROP_ITEMS * killer.getRateItems() * chancemod)
					{
						dropItem(killer, l.getItemId(), 1);
						break;
					}
		}
		else if(isRaid())
		{
			// Лайфстоуны с рейдов
			for(L2DropData l : _toplifestones)
				if(getLevel() >= l.getMinLevel() && getLevel() <= l.getMaxLevel())
				{
					GArray<ItemToDrop> itd = l.roll(killer, isBoss() ? 15 : 1, true);
					for(ItemToDrop t : itd)
						for(int i = 0; i < t.count; i++)
							dropItem(killer, t.itemId, 1);
				}
			// предметы для изучения клановых скилов (не падают с эпиков)
			if(!isBoss())
				for(L2DropData l : _raiditems)
					if(getLevel() >= l.getMinLevel() && getLevel() <= l.getMaxLevel() && Rnd.get(1, L2Drop.MAX_CHANCE) <= l.getChance())
					{
						int mod = (int) (getLevel() * Config.RATE_DROP_RAIDBOSS / 20);
						dropItem(killer, l.getItemId(), Rnd.get(l.getMinDrop() * mod, l.getMaxDrop() * mod));
					}
		}

		// Enhance soul crystals of the attacker if this L2NpcInstance had its soul absorbed
		try
		{
			levelSoulCrystals(killer, aggroList);
		}
		catch(final Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Моб уже формально мертв, но его труп еще нельзя использовать поскольку не закончен подсчет наград
	 */
	public boolean isDying()
	{
		return _dying;
	}

	public void giveItem(L2ItemInstance item, boolean store)
	{
		if(_inventory == null)
			_inventory = new GArray<L2ItemInstance>();

		synchronized (_inventory)
		{
			if(item.isStackable())
				for(L2ItemInstance i : _inventory)
					if(i.getItemId() == item.getItemId())
					{
						i.setCount(item.getCount() + i.getCount());
						if(store)
							i.updateDatabase(true, false);
						return;
					}

			_inventory.add(item);

			if(store)
			{
				item.setOwnerId(getNpcId());
				item.setLocation(ItemLocation.MONSTER);
				item.updateDatabase();
			}
		}
	}

	@Override
	public void onRandomAnimation()
	{
		// Action id для живности 1-3
		broadcastPacket(new SocialAction(getObjectId(), Rnd.get(1, 3)));
	}

	@Override
	public int getKarma()
	{
		return 0;
	}

	/**
	 * Adds an attacker that successfully absorbed the soul of this L2NpcInstance into the _absorbersList.<BR><BR>
	 *
	 * params:  attacker  - a valid L2Player
	 *      condition - an integer indicating the event when mob dies. This should be:
	 *              = 0   - "the crystal scatters";
	 *              = 1   - "the crystal failed to absorb. nothing happens";
	 *              = 2   - "the crystal resonates because you got more than 1 crystal on you";
	 *              = 3   - "the crystal cannot absorb the soul because the mob level is too low";
	 *              = 4   - "the crystal successfuly absorbed the soul";
	 */
	public void addAbsorber(final L2Player attacker)
	{
		// The attacker must not be null
		if(attacker == null)
			return;

		// This L2NpcInstance must be of one type in the _absorbingMOBS_levelXX tables.
		if(getMaxLevelCrystal() == 0 || getCurrentHpPercents() > 50)
			return;

		if(_absorbersList == null)
			_absorbersList = new GArray<Integer>();

		if(!_absorbersList.contains(attacker.getObjectId()))
			_absorbersList.add(attacker.getObjectId());
	}

	/**
	 * Возвращает минимально возможный уровень прокачки кристала для этого моба.
	 */
	private int getMinLevelCrystal()
	{
		int mobId = getNpcId();
		for(int id : _absorbingMOBS_level4)
			if(id == mobId)
				return 0;
		for(int id : _absorbingMOBS_level8)
			if(id == mobId)
				return 0;
		for(int id : _absorbingMOBS_level10)
			if(id == mobId)
				return 0;
		for(int id : _absorbingMOBS_level12)
			if(id == mobId)
				return 10;
		for(int id : _absorbingMOBS_level13)
			if(id == mobId)
				return 12;
		for(int id : _absorbingMOBS_level14)
			if(id == mobId)
				return 13;
		for(int id : _absorbingMOBS_level15)
			if(id == mobId)
				return 14;
		for(int id : _absorbingMOBS_level16)
			if(id == mobId)
				return 15;
		for(int id : _absorbingMOBS_level17)
			if(id == mobId)
				return 16;
		for(int id : _absorbingMOBS_level18)
			if(id == mobId)
				return 17;
		return 0;
	}

	/**
	 * Возвращает максимально возможный уровень прокачки кристала для этого моба.
	 */
	private int getMaxLevelCrystal()
	{
		int mobId = getNpcId();
		for(int id : _absorbingMOBS_level18)
			if(id == mobId)
				return 18;
		for(int id : _absorbingMOBS_level17)
			if(id == mobId)
				return 17;
		for(int id : _absorbingMOBS_level16)
			if(id == mobId)
				return 16;
		for(int id : _absorbingMOBS_level15)
			if(id == mobId)
				return 15;
		for(int id : _absorbingMOBS_level14)
			if(id == mobId)
				return 14;
		for(int id : _absorbingMOBS_level13)
			if(id == mobId)
				return 13;
		for(int id : _absorbingMOBS_level12)
			if(id == mobId)
				return 12;
		for(int id : _absorbingMOBS_level10)
			if(id == mobId)
				return 10;
		for(int id : _absorbingMOBS_level8)
			if(id == mobId)
				return 8;
		for(int id : _absorbingMOBS_level4)
			if(id == mobId)
				return 4;
		return 0;
	}

	/**
	 * Calculate the leveling chance of Soul Crystals based on the attacker that killed this L2NpcInstance
	 * @param attacker The player that last hitted (killed) this L2NpcInstance
	 */
	private void levelSoulCrystals(final L2Character attacker, HashMap<L2Playable, AggroInfo> aggroList)
	{
		// Only player can absorb a soul
		if(attacker == null || !attacker.isPlayable())
		{
			_absorbersList = null;
			return;
		}

		// Init some useful vars
		boolean levelPartyCrystals = false;
		final L2Player killer = attacker.getPlayer();

		// Check if this L2NpcInstance isn't within any of the groups of mobs that can be absorbed
		int minCrystalLevel = getMinLevelCrystal();
		int maxCrystalLevel = getMaxLevelCrystal();

		// If this mob is a boss, then skip some checkings
		if(minCrystalLevel >= 10 && maxCrystalLevel > 10)
			levelPartyCrystals = true;
		// If this is not a valid L2NpcInstance, clears the _absorbersList and just return
		// Fail if this L2NpcInstance isn't absorbed or there's no one in its _absorbersList or killer isn't in the _absorbersList and mob is not boss
		else if(maxCrystalLevel == 0 || _absorbersList == null || !_absorbersList.contains(killer.getObjectId()))
		{
			_absorbersList = null;
			return;
		}

		_absorbersList = null;

		// Now we got four choices:
		// 1- The Monster level is too low for the crystal. Nothing happens.
		// 2- Everything is correct, but it failed. Nothing happens. (57.5%)
		// 3- Everything is correct, but it failed. The crystal scatters. A sound event is played. (10%)
		// 4- Everything is correct, the crystal level up. A sound event is played. (32.5%)

		GArray<L2Player> players = null;
		if(levelPartyCrystals)
			if(Config.ALT_NO_LASTHIT)
			{
				players = new GArray<L2Player>();
				for(L2Playable p : aggroList.keySet())
					if(p.isPlayer())
						players.add((L2Player) p);
			}
			else if(killer.isInParty())
				players = killer.getParty().getPartyMembers();

		if(players == null)
		{
			players = new GArray<L2Player>();
			players.add(killer);
		}

		// Кристаллы качаются с шансом
		for(int id : _randomLeveling)
			if(getNpcId() == id)
			{
				levelPartyCrystals = false;
				break;
			}

		for(final L2Player player : players)
		{
			// Для прокачки кристалла обязательно должен быть взят квест _350_EnhanceYourWeapon
			if(player.getQuestState("_350_EnhanceYourWeapon") == null)
				continue;

			if(!player.isInRange(this, Config.ALT_PARTY_DISTRIBUTION_RANGE) && !player.isInRange(killer, Config.ALT_PARTY_DISTRIBUTION_RANGE))
				continue;

			int oldCrystalId = 0;
			int newCrystalId = 0;
			int crystalsCount = 0;
			int crystalLevel = 0;
			boolean canIncreaseCrystal = false;
			boolean resonated = false;

			// Check how many soul crystals the player has in his inventory and which soul crystal he has
			for(final L2ItemInstance item : player.getInventory().getItems())
			{
				final int itemId = item.getItemId();
				if(!isSoulCrystal(itemId))
					continue;

				if(++crystalsCount > 1)
				{
					resonated = true;
					break;
				}

				if(crystalsCount < 1)
					continue;

				if((newCrystalId = getNextLevelCrystalId(itemId)) != 0)
				{
					crystalLevel = getCrystalLevel(itemId);
					canIncreaseCrystal = crystalLevel >= minCrystalLevel && crystalLevel < maxCrystalLevel;
					oldCrystalId = itemId;
				}
			}

			// The player has more than one soul crystal with him, the crystal resonates and we skip this player
			if(resonated)
			{
				if(!levelPartyCrystals)
					player.sendPacket(Msg.THE_SOUL_CRYSTALS_CAUSED_RESONATION_AND_FAILED_AT_ABSORBING_A_SOUL);
				continue;
			}

			// The soul crystal stage of the player is way too high, refuse to increase it
			if(!canIncreaseCrystal)
			{
				if(!levelPartyCrystals)
					player.sendPacket(Msg.THE_SOUL_CRYSTAL_IS_REFUSING_TO_ABSORB_A_SOUL);
				continue;
			}

			// If the killer succeeds or it is a boss mob, level up the crystal
			if(levelPartyCrystals || Rnd.chance(ConfigSystem.getInt("SoulCrystalRate")))
			{
				L2ItemInstance oldCrystal = player.getInventory().getItemByItemId(oldCrystalId);
				if(oldCrystal == null)
					continue;

				// Baylor с 50% шансом апает курсед кристал
				if((getNpcId() == 29099 || getNpcId() == 29103) && Rnd.chance(50))
					newCrystalId = getCursedCrystal(newCrystalId);

				player.sendPacket(SystemMessage.removeItems(oldCrystal.getItemId(), 1), Msg.THE_SOUL_CRYSTAL_SUCCEEDED_IN_ABSORBING_A_SOUL, SystemMessage.obtainItems(newCrystalId, 1, 0));
				player.getInventory().destroyItem(oldCrystal, 1, true);
				player.getInventory().addItem(ItemTemplates.getInstance().createItem(newCrystalId));

				// извещаем окружающих если получен кристал выше 10-ого уровня
				if(!(player.isGM() && player.isInvisible()))
				{
					String newCrystalColor = getCrystalColor(newCrystalId);
					if(newCrystalColor != null)
					{
						int newCrystalLvl = getCrystalLevel(newCrystalId);
						CustomMessage cm = new CustomMessage("l2rt.gameserver.model.instances.L2MonsterInstance.levelSoulCrystals", player);
						cm.addCharName(player).addString(newCrystalColor).addNumber(newCrystalLvl);
						player.broadcastPacketToOthers(new SystemMessage(cm));
					}
				}
				continue;
			}

			player.sendPacket(Msg.THE_SOUL_CRYSTAL_IS_REFUSING_TO_ABSORB_A_SOUL);
		}
	}

	private static String getCrystalColor(int crystalId)
	{
		for(int id : _REDCRYSTALS)
			if(id == crystalId)
				return "red";
		for(int id : _BLUECRYSTALS)
			if(id == crystalId)
				return "blue";
		for(int id : _GREENCRYSTALS)
			if(id == crystalId)
				return "green";

		switch(crystalId)
		{
			case _REDCURSEDCRYSTAL_LVL14:
				return "red";
			case _BLUECURSEDCRYSTAL_LVL14:
				return "blue";
			case _GREENCURSEDCRYSTAL_LVL14:
				return "green";
		}

		return null;
	}

	private static int getCrystalLevel(int crystalId)
	{
		for(int i = 0; i < _REDCRYSTALS.length; i++)
			if(_REDCRYSTALS[i] == crystalId)
				return i;
		for(int i = 0; i < _BLUECRYSTALS.length; i++)
			if(_BLUECRYSTALS[i] == crystalId)
				return i;
		for(int i = 0; i < _GREENCRYSTALS.length; i++)
			if(_GREENCRYSTALS[i] == crystalId)
				return i;

		switch(crystalId)
		{
			case _REDCURSEDCRYSTAL_LVL14:
			case _BLUECURSEDCRYSTAL_LVL14:
			case _GREENCURSEDCRYSTAL_LVL14:
				return 14;
		}

		return Integer.MAX_VALUE;
	}

	private static int getNextLevelCrystalId(int crystalId)
	{
		for(int i = 0; i < _REDCRYSTALS.length; i++)
			if(_REDCRYSTALS[i] == crystalId)
				return i >= _MAX_CRYSTALS_LEVEL ? _REDCRYSTALS[_MAX_CRYSTALS_LEVEL] : _REDCRYSTALS[i + 1];
		for(int i = 0; i < _GREENCRYSTALS.length; i++)
			if(_GREENCRYSTALS[i] == crystalId)
				return i >= _MAX_CRYSTALS_LEVEL ? _GREENCRYSTALS[_MAX_CRYSTALS_LEVEL] : _GREENCRYSTALS[i + 1];
		for(int i = 0; i < _BLUECRYSTALS.length; i++)
			if(_BLUECRYSTALS[i] == crystalId)
				return i >= _MAX_CRYSTALS_LEVEL ? _BLUECRYSTALS[_MAX_CRYSTALS_LEVEL] : _BLUECRYSTALS[i + 1];

		return 0;
	}

	private static int getCursedCrystal(int soulCrystalLvl14)
	{
		if(soulCrystalLvl14 == _REDCRYSTALS[14])
			return _REDCURSEDCRYSTAL_LVL14;
		if(soulCrystalLvl14 == _GREENCRYSTALS[14])
			return _GREENCURSEDCRYSTAL_LVL14;
		if(soulCrystalLvl14 == _BLUECRYSTALS[14])
			return _BLUECURSEDCRYSTAL_LVL14;
		return soulCrystalLvl14;
	}

	private static boolean isSoulCrystal(int crystalId)
	{
		for(int id : _REDCRYSTALS)
			if(id == crystalId)
				return true;
		for(int id : _BLUECRYSTALS)
			if(id == crystalId)
				return true;
		for(int id : _GREENCRYSTALS)
			if(id == crystalId)
				return true;

		return false;
	}

	public L2ItemInstance takeHarvest()
	{
		harvestLock.lock();
		final L2ItemInstance harvest = _harvestItem;
		_harvestItem = null;
		_seeded = null;
		seederStoreId = 0;
		harvestLock.unlock();
		return harvest;
	}

	public void setSeeded(L2Item seed, L2Player player)
	{
		if(player == null)
			return;

		harvestLock.lock();
		try
		{
			_seeded = seed;
			seederStoreId = player.getStoredId();

			_harvestItem = ItemTemplates.getInstance().createItem(L2Manor.getInstance().getCropType(seed.getItemId()));
			// Количество всходов от xHP до (xHP + xHP/2)
			if(getTemplate().rateHp <= 1)
				_harvestItem.setCount(1);
			else
				_harvestItem.setCount(Rnd.get(Math.round(getTemplate().rateHp * Config.RATE_MANOR), Math.round(1.5 * getTemplate().rateHp * Config.RATE_MANOR)));
		}
		finally
		{
			harvestLock.unlock();
		}
	}

	public boolean isSeeded(L2Player seeder)
	{
		if(seederStoreId == 0)
			return false;
		L2Player _seeder = L2ObjectsStorage.getAsPlayer(seederStoreId);
		return seeder != null && seeder == _seeder || _dieTime + 10000 < System.currentTimeMillis();
	}

	public boolean isSeeded()
	{
		return _seeded != null;
	}

	/** True if a Dwarf has used Spoil on this L2NpcInstance */
	private boolean _isSpoiled;

	/**
	 * Return True if this L2NpcInstance has drops that can be sweeped.<BR><BR>
	 */
	public boolean isSpoiled()
	{
		return _isSpoiled; //FIXME возможно нужно делать sweepLock.lock() ... но тут же только чтение одного поля...
	}

	public boolean isSpoiled(L2Player spoiler)
	{
		L2Player this_spoiler;
		sweepLock.lock();
		try
		{
			if(!_isSpoiled) // если не заспойлен то false
				return false;
			this_spoiler = L2ObjectsStorage.getAsPlayer(spoilerStoreId);
		}
		finally
		{
			sweepLock.unlock();
		}
		if(this_spoiler == null || spoiler.getObjectId() == this_spoiler.getObjectId() || _dieTime + 10000 < System.currentTimeMillis())
			return true;
		if(getDistance(this_spoiler) > Config.ALT_PARTY_DISTRIBUTION_RANGE) // если спойлер слишком далеко разрешать
			return true;
		if(spoiler.getParty() != null && spoiler.getParty().containsMember(this_spoiler)) // сопартийцам тоже можно
			return true;
		return false;
	}

	/**
	 * Set the spoil state of this L2NpcInstance.<BR><BR>
	 * @param spoiler
	 */
	public void setSpoiled(boolean isSpoiled, L2Player spoiler)
	{
		sweepLock.lock();
		try
		{
			_isSpoiled = isSpoiled;
			spoilerStoreId = spoiler != null ? spoiler.getStoredId() : 0;
		}
		finally
		{
			sweepLock.unlock();
		}
	}

	public void doItemDrop(L2Character topDamager)
	{
		L2Player player = topDamager.getPlayer();
		if(player == null)
			return;

		double mod = calcStat(Stats.DROP, 1., topDamager, null);

		if(getTemplate().getDropData() != null)
		{
			GArray<ItemToDrop> drops = getTemplate().getDropData().rollDrop(calculateLevelDiffForDrop(topDamager.getLevel()), this, player, mod);
			for(ItemToDrop drop : drops)
			{
				// Если в моба посеяно семя, причем не альтернативное - не давать никакого дропа, кроме адены.
				if(_seeded != null && !_seeded.isAltSeed() && !drop.isAdena)
					continue;
				if(getChampion() > 0 && drop.isAdena)
					dropItem(player, drop.itemId, (drop.count * ConfigSystem.getInt("ChampionAdenasRewards")));
				if(getChampion() > 0 && !drop.isAdena)
					dropItem(player, drop.itemId, (drop.count * ConfigSystem.getInt("ChampionRewards")));
				else
					dropItem(player, drop.itemId, drop.count);
			}
		}
		if (getChampion() > 0 && (ConfigSystem.getInt("ChampionRewardLowerLvlItemChance") > 0 || ConfigSystem.getInt("ChampionRewardHigherLvlItemChance") > 0))
		{
			int champqty = Rnd.get(ConfigSystem.getInt("ChampionRewardItemQty"));

			L2ItemInstance item = ItemTemplates.getInstance().createItem(ConfigSystem.getInt("ChampionRewardItemID"));
			item.setCount(++champqty);			

			if (player.getLevel() <= getLevel() && (Rnd.get(100) < ConfigSystem.getInt("ChampionRewardLowerLvlItemChance")))
			{
				if (Config.AUTO_LOOT || isFlying())
					player.getInventory().addItem(item);
				else
					dropItem(player, item);
			}
			else if (player.getLevel() > getLevel() && (Rnd.get(100) < ConfigSystem.getInt("ChampionRewardHigherLvlItemChance")))
			{
				if (Config.AUTO_LOOT || isFlying())
					player.getInventory().addItem(item);
				else
					dropItem(player, item);
			}
		}

		if(_inventory != null)
			synchronized (_inventory)
			{
				for(L2ItemInstance drop : _inventory)
					if(drop != null)
					{
						player.sendMessage(new CustomMessage("l2rt.gameserver.model.instances.L2MonsterInstance.ItemBelongedToOther", player).addString(drop.getName()));
						dropItem(player, drop);
					}
				if(_inventory != null)
					_inventory.clear();
				_inventory = null;
			}

		GArray<L2ItemInstance> templateInv = getTemplate().takeInventory();
		if(templateInv != null)
		{
			for(L2ItemInstance drop : templateInv)
				if(drop != null)
				{
					player.sendMessage(new CustomMessage("l2rt.gameserver.model.instances.L2MonsterInstance.ItemBelongedToOther", player).addString(drop.getName()));
					dropItem(player, drop);
				}
			if(_inventory != null)
				_inventory.clear();
			_inventory = null;
		}
	}

	private void doSweepDrop(final L2Character lastAttacker, L2Character topDamager)
	{
		final L2Player player = lastAttacker.getPlayer();

		if(player == null)
			return;

		final int levelDiff = calculateLevelDiffForDrop(topDamager.getLevel());

		final GArray<L2ItemInstance> spoiled = new GArray<L2ItemInstance>();

		if(getTemplate().getDropData() != null)
		{
			double mod = calcStat(Stats.DROP, 1., lastAttacker, null);
			final GArray<ItemToDrop> spoils = getTemplate().getDropData().rollSpoil(levelDiff, this, player, mod);
			for(final ItemToDrop spoil : spoils)
			{
				final L2ItemInstance dropit = ItemTemplates.getInstance().createItem(spoil.itemId);
				dropit.setCount(spoil.count);
				spoiled.add(dropit);
			}
		}

		if(spoiled.size() > 0)
			_sweepItems = spoiled.toArray(new L2ItemInstance[spoiled.size()]);
	}

	private double[] calculateExpAndSp(L2Character attacker, int level, long damage)
	{
		if(!isInRange(attacker, Config.ALT_PARTY_DISTRIBUTION_RANGE) && Math.abs(attacker.getZ() - getZ()) < 400)
			return new double[] { 0., 0. };

		int diff = level - getLevel();
		if(level > 77 && diff > 3 && diff <= 5) // kamael exp penalty
			diff += 3;

		double xp = getExpReward() * damage / getMaxHp();
		double sp = getSpReward() * damage / getMaxHp();

		if(diff > 5)
		{
			double mod = Math.pow(.83, diff - 5);
			xp *= mod;
			sp *= mod;
		}

		xp = Math.max(0, xp);
		sp = Math.max(0, sp);

		return new double[] { xp, sp };
	}

	private double applyOverhit(L2Player killer, double xp)
	{
		if(xp > 0 && getOverhitAttacker() != null && killer == getOverhitAttacker())
		{
			int overHitExp = calculateOverhitExp(xp);
			killer.sendPacket(Msg.OVER_HIT, new SystemMessage(SystemMessage.ACQUIRED_S1_BONUS_EXPERIENCE_THROUGH_OVER_HIT).addNumber(overHitExp));
			xp += overHitExp;
		}
		return xp;
	}

	public L2Character getOverhitAttacker()
	{
		return overhitAttackerStoreId == 0 ? null : L2ObjectsStorage.getAsCharacter(overhitAttackerStoreId);
	}

	@Override
	public void setOverhitAttacker(L2Character overhitAttacker)
	{
		overhitAttackerStoreId = overhitAttacker == null ? 0 : overhitAttacker.getStoredId();
	}

	public double getOverhitDamage()
	{
		return _overhitDamage;
	}

	@Override
	public void setOverhitDamage(double damage)
	{
		_overhitDamage = damage;
	}

	public int calculateOverhitExp(final double normalExp)
	{
		double overhitPercentage = getOverhitDamage() * 100 / getMaxHp();
		if(overhitPercentage > 25)
			overhitPercentage = 25;
		double overhitExp = overhitPercentage / 100 * normalExp;
		overhitAttackerStoreId = 0;
		setOverhitDamage(0);
		return (int) Math.round(overhitExp);
	}

	/**
	 * Return True if a Dwarf use Sweep on the L2NpcInstance and if item can be spoiled.<BR><BR>
	 */
	public boolean isSweepActive()
	{
		dyingLock.lock();
		try
		{
			return _sweepItems != null && _sweepItems.length > 0;
		}
		finally
		{
			dyingLock.unlock();
		}
	}

	/**
	 * Return table containing all L2ItemInstance that can be spoiled.<BR><BR>
	 */
	public L2ItemInstance[] takeSweep()
	{
		L2ItemInstance[] sweep;
		sweepLock.lock();
		try
		{
			sweep = (_sweepItems == null || _sweepItems.length == 0) ? null : _sweepItems.clone();
			_sweepItems = null;
		}
		finally
		{
			sweepLock.unlock();
		}
		return sweep;
	}

	@Override
	public boolean isInvul()
	{
		return _isInvul;
	}

	@Override
	public boolean isAggressive()
	{
		return (Config.ALT_CHAMPION_CAN_BE_AGGRO || getChampion() == 0) && super.isAggressive();
	}

	@Override
	public String getFactionId()
	{
		return Config.ALT_CHAMPION_CAN_BE_SOCIAL || getChampion() == 0 ? super.getFactionId() : "";
	}

	@Override
	public String toString()
	{
		return "Mob " + getName() + " [" + getNpcId() + "] / " + getObjectId();
	}

	// Не отображаем на монстрах значки клана. 
	@Override 
	public boolean isCrestEnable() 
	{ 
		return false; 
	}
}