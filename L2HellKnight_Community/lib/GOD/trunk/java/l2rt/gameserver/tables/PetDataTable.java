package l2rt.gameserver.tables;

import gnu.trove.map.hash.TIntObjectHashMap;

import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2PetData;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Summon;
import l2rt.gameserver.model.items.L2ItemInstance;

import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PetDataTable
{
	private static final Logger _log = Logger.getLogger(PetDataTable.class.getName());

	private static PetDataTable _instance = new PetDataTable();
	private static TIntObjectHashMap<L2PetData> _pets;

	public final static int PET_WOLF_ID = 12077;

	public final static int HATCHLING_WIND_ID = 12311;
	public final static int HATCHLING_STAR_ID = 12312;
	public final static int HATCHLING_TWILIGHT_ID = 12313;

	public final static int STRIDER_WIND_ID = 12526;
	public final static int STRIDER_STAR_ID = 12527;
	public final static int STRIDER_TWILIGHT_ID = 12528;

	public final static int RED_STRIDER_WIND_ID = 16038;
	public final static int RED_STRIDER_STAR_ID = 16039;
	public final static int RED_STRIDER_TWILIGHT_ID = 16040;

	public final static int WYVERN_ID = 12621;

	public final static int BABY_BUFFALO_ID = 12780;
	public final static int BABY_KOOKABURRA_ID = 12781;
	public final static int BABY_COUGAR_ID = 12782;

	public final static int IMPROVED_BABY_BUFFALO_ID = 16034;
	public final static int IMPROVED_BABY_KOOKABURRA_ID = 16035;
	public final static int IMPROVED_BABY_COUGAR_ID = 16036;

	public final static int SIN_EATER_ID = 12564;

	public final static int GREAT_WOLF_ID = 16025;
	public final static int WGREAT_WOLF_ID = 16037;
	public final static int FENRIR_WOLF_ID = 16041;
	public final static int WFENRIR_WOLF_ID = 16042;

	public final static int LIGHT_PURPLE_MANED_HORSE_ID = 13130;

        public final static int KNIGHT_HORSE_ID = 13311;
        public final static int WARRIOR_HORSE_ID = 13312;
        public final static int RUSTY_STEEL_HORSE_ID = 13313;
        public final static int ARCHER_HORSE_ID = 13314;
        public final static int PHANTOM_HORSE_ID = 13315;
        public final static int COBALT_HORSE_ID = 13316;
        public final static int ENCHANTER_HORSE_ID = 13317;
        public final static int HEALER_HORSE_ID = 13318;

        public final static int ANT_PRINCESS_ID = 159;
        public final static int HALLOWEEM_FLYING_BROOM_ID = 161;

	public final static int TAWNY_MANED_LION_ID = 13146;

	public final static int STEAM_BEATLE_ID = 13147;

	public final static int AURA_BIRD_FALCON_ID = 13144;
	public final static int AURA_BIRD_OWL_ID = 13145;

	public final static int FOX_SHAMAN_ID = 16043;
	public final static int WILD_BEAST_FIGHTER_ID = 16044;
	public final static int WHITE_WEASEL_ID = 16045;
	public final static int FAIRY_PRINCESS_ID = 16046;
	public final static int OWL_MONK_ID = 16050;
	public final static int SPIRIT_SHAMAN_ID = 16051;
	public final static int TOY_KNIGHT_ID = 16052;
	public final static int TURTLE_ASCETIC_ID = 16053;
	public final static int DEINONYCHUS_ID = 16067;
	public final static int GUARDIANS_STRIDER_ID = 16068;

	public static PetDataTable getInstance()
	{
		return _instance;
	}

	public static void reload()
	{
		_instance = new PetDataTable();
	}

	private PetDataTable()
	{
		_pets = new TIntObjectHashMap<L2PetData>();
		FillPetDataTable();
	}

	public L2PetData getInfo(int petNpcId, int level)
	{
		L2PetData result = null;
		while(result == null && level < 100)
		{
			result = _pets.get(petNpcId * 100 + level);
			level++;
		}

		return result;
	}

	private void FillPetDataTable()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		L2PetData petData;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id, level, exp, hp, mp, patk, pdef, matk, mdef, acc, evasion, crit, speed, atk_speed, cast_speed, max_meal, battle_meal, normal_meal, loadMax, hpregen, mpregen FROM pet_data");
			rset = statement.executeQuery();
			while(rset.next())
			{
				petData = new L2PetData();
				petData.setID(rset.getInt("id"));
				petData.setLevel(rset.getInt("level"));
				petData.setExp(rset.getLong("exp"));
				petData.setHP(rset.getInt("hp"));
				petData.setMP(rset.getInt("mp"));
				petData.setPAtk(rset.getInt("patk"));
				petData.setPDef(rset.getInt("pdef"));
				petData.setMAtk(rset.getInt("matk"));
				petData.setMDef(rset.getInt("mdef"));
				petData.setAccuracy(rset.getInt("acc"));
				petData.setEvasion(rset.getInt("evasion"));
				petData.setCritical(rset.getInt("crit"));
				petData.setSpeed(rset.getInt("speed"));
				petData.setAtkSpeed(rset.getInt("atk_speed"));
				petData.setCastSpeed(rset.getInt("cast_speed"));
				petData.setFeedMax(rset.getInt("max_meal"));
				petData.setFeedBattle(rset.getInt("battle_meal"));
				petData.setFeedNormal(rset.getInt("normal_meal"));
				petData.setMaxLoad(rset.getInt("loadMax"));
				petData.setHpRegen(rset.getInt("hpregen"));
				petData.setMpRegen(rset.getInt("mpregen"));

				petData.setControlItemId(getControlItemId(petData.getID()));
				petData.setFoodId(getFoodId(petData.getID()));
				petData.setMountable(isMountable(petData.getID()));
				petData.setMinLevel(getMinLevel(petData.getID()));
				petData.setAddFed(getAddFed(petData.getID()));

				_pets.put(petData.getID() * 100 + petData.getLevel(), petData);
			}
		}
		catch(Exception e)
		{
			_log.warning("Cannot fill up PetDataTable: " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		_log.config("PetDataTable: Loaded " + _pets.size() + " pets.");
	}

	public static void deletePet(L2ItemInstance item, L2Character owner)
	{
		int petObjectId = 0;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT objId FROM pets WHERE item_obj_id=?");
			statement.setInt(1, item.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
				petObjectId = rset.getInt("objId");
			DatabaseUtils.closeDatabaseSR(statement, rset);

			L2Summon summon = owner.getPet();
			if(summon != null && summon.getObjectId() == petObjectId)
				summon.unSummon();

			L2Player player = owner.getPlayer();
			if(player != null && player.isMounted() && player.getMountObjId() == petObjectId)
				player.setMount(0, 0, 0);

			// if it's a pet control item, delete the pet
			statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
			statement.setInt(1, item.getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "could not restore pet objectid:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public static void unSummonPet(L2ItemInstance oldItem, L2Character owner)
	{
		int petObjectId = 0;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT objId FROM pets WHERE item_obj_id=?");
			statement.setInt(1, oldItem.getObjectId());
			rset = statement.executeQuery();

			while(rset.next())
				petObjectId = rset.getInt("objId");

			if(owner == null)
				return;

			L2Summon summon = owner.getPet();
			if(summon != null && summon.getObjectId() == petObjectId)
				summon.unSummon();

			L2Player player = owner.getPlayer();
			if(player != null && player.isMounted() && player.getMountObjId() == petObjectId)
				player.setMount(0, 0, 0);
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "could not restore pet objectid:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public static enum L2Pet
	{
		WOLF(PET_WOLF_ID, 2375, 2515, false, 1, 12, .30f, 2, 2),

		HATCHLING_WIND(HATCHLING_WIND_ID, 3500, 4038, false, 1, 12, .30f, 2, 2),
		HATCHLING_STAR(HATCHLING_STAR_ID, 3501, 4038, false, 1, 12, .30f, 2, 2),
		HATCHLING_TWILIGHT(HATCHLING_TWILIGHT_ID, 3502, 4038, false, 1, 100, .30f, 2, 2),

		STRIDER_WIND(STRIDER_WIND_ID, 4422, 5168, true, 1, 12, .30f, 2, 2),
		STRIDER_STAR(STRIDER_STAR_ID, 4423, 5168, true, 1, 12, .30f, 2, 2),
		STRIDER_TWILIGHT(STRIDER_TWILIGHT_ID, 4424, 5168, true, 1, 100, .30f, 2, 2),

		RED_STRIDER_WIND(RED_STRIDER_WIND_ID, 10308, 5168, true, 1, 12, .30f, 2, 2),
		RED_STRIDER_STAR(RED_STRIDER_STAR_ID, 10309, 5168, true, 1, 12, .30f, 2, 2),
		RED_STRIDER_TWILIGHT(RED_STRIDER_TWILIGHT_ID, 10310, 5168, true, 1, 100, .30f, 2, 2),

		WYVERN(WYVERN_ID, 5249, 6316, true, 1, 12, 0f, 2, 2),

		GREAT_WOLF(GREAT_WOLF_ID, 9882, 9668, false, 55, 10, .30f, 2, 2),
		WGREAT_WOLF(WGREAT_WOLF_ID, 10307, 9668, true, 55, 12, .30f, 2, 2),
		FENRIR_WOLF(FENRIR_WOLF_ID, 10426, 9668, true, 70, 12, .30f, 2, 2),
		WFENRIR_WOLF(WFENRIR_WOLF_ID, 10611, 9668, true, 70, 12, .30f, 2, 2),

		BABY_BUFFALO(BABY_BUFFALO_ID, 6648, 7582, false, 1, 12, .05f, 2, 2),
		BABY_KOOKABURRA(BABY_KOOKABURRA_ID, 6650, 7582, false, 1, 12, .05f, 2, 2),
		BABY_COUGAR(BABY_COUGAR_ID, 6649, 7582, false, 1, 12, .05f, 2, 2),

		IMPROVED_BABY_BUFFALO(IMPROVED_BABY_BUFFALO_ID, 10311, 10425, false, 55, 12, .3f, 2, 2),
		IMPROVED_BABY_KOOKABURRA(IMPROVED_BABY_KOOKABURRA_ID, 10313, 10425, false, 55, 12, .3f, 2, 2),
		IMPROVED_BABY_COUGAR(IMPROVED_BABY_COUGAR_ID, 10312, 10425, false, 55, 12, .3f, 2, 2),

		SIN_EATER(SIN_EATER_ID, 4425, 2515, false, 1, 12, 0f, 2, 2),

		LIGHT_PURPLE_MANED_HORSE(LIGHT_PURPLE_MANED_HORSE_ID, -1, -1, true, 1, 0, 0f, 2, 2),

                KNIGHT_HORSE(KNIGHT_HORSE_ID, -1, -1, true, 1, 0, 0f, 2, 2),
                WARRIOR_HORSE(WARRIOR_HORSE_ID, -1, -1, true, 1, 0, 0f, 2, 2),
                RUSTY_STEEL_HORSE(RUSTY_STEEL_HORSE_ID, -1, -1, true, 1, 0, 0f, 2, 2),
                ARCHER_HORSE(ARCHER_HORSE_ID, -1, -1, true, 1, 0, 0f, 2, 2),
                PHANTOM_HORSE(PHANTOM_HORSE_ID, -1, -1, true, 1, 0, 0f, 2, 2),
                COBALT_HORSE(COBALT_HORSE_ID, -1, -1, true, 1, 0, 0f, 2, 2),
                ENCHANTER_HORSE(ENCHANTER_HORSE_ID, -1, -1, true, 1, 0, 0f, 2, 2),
                HEALER_HORSE(HEALER_HORSE_ID, -1, -1, true, 1, 0, 0f, 2, 2),

		TAWNY_MANED_LION(TAWNY_MANED_LION_ID, -1, -1, true, 1, 0, 0f, 2, 2),

                ANT_PRINCESS(ANT_PRINCESS_ID, -1, -1, true, 1, 0, 0f, 2, 2),
                HALLOWEEM_FLYING_BROOM(HALLOWEEM_FLYING_BROOM_ID, -1, -1, true, 1, 0, 0f, 2, 2),

		STEAM_BEATLE(STEAM_BEATLE_ID, -1, -1, true, 1, 0, 0f, 2, 2),

		AURA_BIRD_FALCON(AURA_BIRD_FALCON_ID, -1, -1, true, 1, 0, 0f, 2, 2),
		AURA_BIRD_OWL(AURA_BIRD_OWL_ID, -1, -1, true, 1, 0, 0f, 2, 2),

		FOX_SHAMAN(FOX_SHAMAN_ID, 13020, -1, false, 25, 12, .3f, 2, 2),
		WILD_BEAST_FIGHTER(WILD_BEAST_FIGHTER_ID, 13019, -1, false, 25, 12, .3f, 2, 2),
		WHITE_WEASEL(WHITE_WEASEL_ID, 13017, -1, false, 25, 12, .3f, 2, 2),
		FAIRY_PRINCESS(FAIRY_PRINCESS_ID, 13018, -1, false, 25, 12, .3f, 2, 2),
		OWL_MONK(OWL_MONK_ID, 14063, -1, false, 25, 12, .3f, 2, 2),
		SPIRIT_SHAMAN(SPIRIT_SHAMAN_ID, 14062, -1, false, 25, 12, .3f, 2, 2),
		TOY_KNIGHT(TOY_KNIGHT_ID, 14061, -1, false, 25, 12, .3f, 2, 2),
		TURTLE_ASCETIC(TURTLE_ASCETIC_ID, 14064, -1, false, 25, 12, .3f, 2, 2),
		DEINONYCHUS(DEINONYCHUS_ID, 14828, -1, false, 25, 12, .3f, 2, 2),
		GUARDIANS_STRIDER(GUARDIANS_STRIDER_ID, 14819, 5168, true, 1, 12, .3f, 2, 2);

		private final int _npcId;
		private final int _controlItemId;
		private final int _foodId;
		private final boolean _isMountable;
		private final int _minLevel; // Уровень, ниже которого не может опускаться пет
		private final int _addFed; // На сколько процентов увеличивается полоска еды, при кормлении
		private final float _expPenalty;
		private final int _soulshots;
		private final int _spiritshots;

		private L2Pet(int npcId, int controlItemId, int foodId, boolean isMountabe, int minLevel, int addFed, float expPenalty, int soulshots, int spiritshots)
		{
			_npcId = npcId;
			_controlItemId = controlItemId;
			_foodId = foodId;
			_isMountable = isMountabe;
			_minLevel = minLevel;
			_addFed = addFed;
			_expPenalty = expPenalty;
			_soulshots = soulshots;
			_spiritshots = spiritshots;
		}

		public int getNpcId()
		{
			return _npcId;
		}

		public int getControlItemId()
		{
			return _controlItemId;
		}

		public int getFoodId()
		{
			return _foodId;
		}

		public boolean isMountable()
		{
			return _isMountable;
		}

		public int getMinLevel()
		{
			return _minLevel;
		}

		public int getAddFed()
		{
			return _addFed;
		}

		public float getExpPenalty()
		{
			return _expPenalty;
		}

		public int getSoulshots()
		{
			return _soulshots;
		}

		public int getSpiritshots()
		{
			return _spiritshots;
		}
	}

	public static int getControlItemId(int npcId)
	{
		for(L2Pet pet : L2Pet.values())
			if(pet.getNpcId() == npcId)
				return pet.getControlItemId();
		return 1;
	}

	public static int getFoodId(int npcId)
	{
		for(L2Pet pet : L2Pet.values())
			if(pet.getNpcId() == npcId)
				return pet.getFoodId();
		return 1;
	}

	public static boolean isMountable(int npcId)
	{
		for(L2Pet pet : L2Pet.values())
			if(pet.getNpcId() == npcId)
				return pet.isMountable();
		return false;
	}

	public static int getMinLevel(int npcId)
	{
		for(L2Pet pet : L2Pet.values())
			if(pet.getNpcId() == npcId)
				return pet.getMinLevel();
		return 1;
	}

	public static int getAddFed(int npcId)
	{
		for(L2Pet pet : L2Pet.values())
			if(pet.getNpcId() == npcId)
				return pet.getAddFed();
		return 1;
	}

	public static float getExpPenalty(int npcId)
	{
		for(L2Pet pet : L2Pet.values())
			if(pet.getNpcId() == npcId)
				return pet.getExpPenalty();
		return 0f;
	}

	public static int getSoulshots(int npcId)
	{
		for(L2Pet pet : L2Pet.values())
			if(pet.getNpcId() == npcId)
				return pet.getSoulshots();
		return 2;
	}

	public static int getSpiritshots(int npcId)
	{
		for(L2Pet pet : L2Pet.values())
			if(pet.getNpcId() == npcId)
				return pet.getSpiritshots();
		return 2;
	}

	public static int getSummonId(L2ItemInstance item)
	{
		for(L2Pet pet : L2Pet.values())
			if(pet.getControlItemId() == item.getItemId())
				return pet.getNpcId();
		return 0;
	}

	public static int[] getPetControlItems()
	{
		int[] items = new int[L2Pet.values().length];
		int i = 0;
		for(L2Pet pet : L2Pet.values())
			items[i++] = pet.getControlItemId();
		return items;
	}

	public static boolean isPetControlItem(L2ItemInstance item)
	{
		for(L2Pet pet : L2Pet.values())
			if(pet.getControlItemId() == item.getItemId())
				return true;
		return false;
	}

	public static boolean isBabyPet(int id)
	{
		switch(id)
		{
			case BABY_BUFFALO_ID:
			case BABY_KOOKABURRA_ID:
			case BABY_COUGAR_ID:
				return true;
			default:
				return false;
		}
	}

	public static boolean isImprovedBabyPet(int id)
	{
		switch(id)
		{
			case IMPROVED_BABY_BUFFALO_ID:
			case IMPROVED_BABY_KOOKABURRA_ID:
			case IMPROVED_BABY_COUGAR_ID:
			case FAIRY_PRINCESS_ID:
				return true;
			default:
				return false;
		}
	}

	public static boolean isWolf(int id)
	{
		return id == PET_WOLF_ID;
	}

	public static boolean isHatchling(int id)
	{
		switch(id)
		{
			case HATCHLING_WIND_ID:
			case HATCHLING_STAR_ID:
			case HATCHLING_TWILIGHT_ID:
				return true;
			default:
				return false;
		}
	}

	public static boolean isStrider(int id)
	{
		switch(id)
		{
			case STRIDER_WIND_ID:
			case STRIDER_STAR_ID:
			case STRIDER_TWILIGHT_ID:
			case RED_STRIDER_WIND_ID:
			case RED_STRIDER_STAR_ID:
			case RED_STRIDER_TWILIGHT_ID:
				return true;
			default:
				return false;
		}
	}

	public static boolean isHorse(int id)
	{
		switch(id)
		{
			case KNIGHT_HORSE_ID:
			case WARRIOR_HORSE_ID:
			case RUSTY_STEEL_HORSE_ID:
			case ARCHER_HORSE_ID:
			case PHANTOM_HORSE_ID:
			case COBALT_HORSE_ID:
			case ENCHANTER_HORSE_ID:
			case HEALER_HORSE_ID:
				return true;
			default:
				return false;
		}
	}

	public static boolean isGWolf(int id)
	{
		switch(id)
		{
			case GREAT_WOLF_ID:
			case WGREAT_WOLF_ID:
			case FENRIR_WOLF_ID:
			case WFENRIR_WOLF_ID:
				return true;
			default:
				return false;
		}
	}

	public static void unload()
	{
		if(_instance != null)
			_instance = null;
	}
}