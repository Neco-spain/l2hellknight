package l2p.gameserver.tables;

import gnu.trove.TIntObjectHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.PetData;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Summon;
import l2p.gameserver.model.items.ItemInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PetDataTable
{
  private static final Logger _log = LoggerFactory.getLogger(PetDataTable.class);

  private static final PetDataTable _instance = new PetDataTable();
  public static final int PET_WOLF_ID = 12077;
  public static final int HATCHLING_WIND_ID = 12311;
  public static final int HATCHLING_STAR_ID = 12312;
  public static final int HATCHLING_TWILIGHT_ID = 12313;
  public static final int STRIDER_WIND_ID = 12526;
  public static final int STRIDER_STAR_ID = 12527;
  public static final int STRIDER_TWILIGHT_ID = 12528;
  public static final int RED_STRIDER_WIND_ID = 16038;
  public static final int RED_STRIDER_STAR_ID = 16039;
  public static final int RED_STRIDER_TWILIGHT_ID = 16040;
  public static final int WYVERN_ID = 12621;
  public static final int BABY_BUFFALO_ID = 12780;
  public static final int BABY_KOOKABURRA_ID = 12781;
  public static final int BABY_COUGAR_ID = 12782;
  public static final int IMPROVED_BABY_BUFFALO_ID = 16034;
  public static final int IMPROVED_BABY_KOOKABURRA_ID = 16035;
  public static final int IMPROVED_BABY_COUGAR_ID = 16036;
  public static final int SIN_EATER_ID = 12564;
  public static final int GREAT_WOLF_ID = 16025;
  public static final int WGREAT_WOLF_ID = 16037;
  public static final int FENRIR_WOLF_ID = 16041;
  public static final int WFENRIR_WOLF_ID = 16042;
  public static final int FOX_SHAMAN_ID = 16043;
  public static final int WILD_BEAST_FIGHTER_ID = 16044;
  public static final int WHITE_WEASEL_ID = 16045;
  public static final int FAIRY_PRINCESS_ID = 16046;
  public static final int OWL_MONK_ID = 16050;
  public static final int SPIRIT_SHAMAN_ID = 16051;
  public static final int TOY_KNIGHT_ID = 16052;
  public static final int TURTLE_ASCETIC_ID = 16053;
  public static final int DEINONYCHUS_ID = 16067;
  public static final int GUARDIANS_STRIDER_ID = 16068;
  private final TIntObjectHashMap<PetData> _pets = new TIntObjectHashMap();

  public static final PetDataTable getInstance()
  {
    return _instance;
  }

  private PetDataTable()
  {
    load();
  }

  public void reload()
  {
    load();
  }

  public PetData getInfo(int petNpcId, int level)
  {
    PetData result = null;
    while ((result == null) && (level < 100))
    {
      result = (PetData)_pets.get(petNpcId * 100 + level);
      level++;
    }

    return result;
  }

  private void load()
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT id, level, exp, hp, mp, patk, pdef, matk, mdef, acc, evasion, crit, speed, atk_speed, cast_speed, max_meal, battle_meal, normal_meal, loadMax, hpregen, mpregen FROM pet_data");
      rset = statement.executeQuery();
      while (rset.next())
      {
        PetData petData = new PetData();
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
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    _log.info("PetDataTable: Loaded " + _pets.size() + " pets.");
  }

  public static void deletePet(ItemInstance item, Creature owner)
  {
    int petObjectId = 0;

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT objId FROM pets WHERE item_obj_id=?");
      statement.setInt(1, item.getObjectId());
      rset = statement.executeQuery();
      while (rset.next()) {
        petObjectId = rset.getInt("objId");
      }
      DbUtils.close(statement, rset);

      Summon summon = owner.getPet();
      if ((summon != null) && (summon.getObjectId() == petObjectId)) {
        summon.unSummon();
      }
      Player player = owner.getPlayer();
      if ((player != null) && (player.isMounted()) && (player.getMountObjId() == petObjectId)) {
        player.setMount(0, 0, 0);
      }

      statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
      statement.setInt(1, item.getObjectId());
      statement.execute();
    }
    catch (Exception e)
    {
      _log.error("could not restore pet objectid:", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public static void unSummonPet(ItemInstance oldItem, Creature owner)
  {
    int petObjectId = 0;

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT objId FROM pets WHERE item_obj_id=?");
      statement.setInt(1, oldItem.getObjectId());
      rset = statement.executeQuery();

      while (rset.next()) {
        petObjectId = rset.getInt("objId");
      }
      if (owner == null)
        return;
      Summon summon = owner.getPet();
      if ((summon != null) && (summon.getObjectId() == petObjectId)) {
        summon.unSummon();
      }
      Player player = owner.getPlayer();
      if ((player != null) && (player.isMounted()) && (player.getMountObjId() == petObjectId))
        player.setMount(0, 0, 0);
    }
    catch (Exception e)
    {
      _log.error("could not restore pet objectid:", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public static int getControlItemId(int npcId)
  {
    for (L2Pet pet : L2Pet.values())
      if (pet.getNpcId() == npcId)
        return pet.getControlItemId();
    return 1;
  }

  public static int getFoodId(int npcId)
  {
    for (L2Pet pet : L2Pet.values())
      if (pet.getNpcId() == npcId)
        return pet.getFoodId();
    return 1;
  }

  public static boolean isMountable(int npcId)
  {
    for (L2Pet pet : L2Pet.values())
      if (pet.getNpcId() == npcId)
        return pet.isMountable();
    return false;
  }

  public static int getMinLevel(int npcId)
  {
    for (L2Pet pet : L2Pet.values())
      if (pet.getNpcId() == npcId)
        return pet.getMinLevel();
    return 1;
  }

  public static int getAddFed(int npcId)
  {
    for (L2Pet pet : L2Pet.values())
      if (pet.getNpcId() == npcId)
        return pet.getAddFed();
    return 1;
  }

  public static double getExpPenalty(int npcId)
  {
    for (L2Pet pet : L2Pet.values())
      if (pet.getNpcId() == npcId)
        return pet.getExpPenalty();
    return 0.0D;
  }

  public static int getSoulshots(int npcId)
  {
    for (L2Pet pet : L2Pet.values())
      if (pet.getNpcId() == npcId)
        return pet.getSoulshots();
    return 2;
  }

  public static int getSpiritshots(int npcId)
  {
    for (L2Pet pet : L2Pet.values())
      if (pet.getNpcId() == npcId)
        return pet.getSpiritshots();
    return 2;
  }

  public static int getSummonId(ItemInstance item)
  {
    for (L2Pet pet : L2Pet.values())
      if (pet.getControlItemId() == item.getItemId())
        return pet.getNpcId();
    return 0;
  }

  public static int[] getPetControlItems()
  {
    int[] items = new int[L2Pet.values().length];
    int i = 0;
    for (L2Pet pet : L2Pet.values())
      items[(i++)] = pet.getControlItemId();
    return items;
  }

  public static boolean isPetControlItem(ItemInstance item)
  {
    for (L2Pet pet : L2Pet.values())
      if (pet.getControlItemId() == item.getItemId())
        return true;
    return false;
  }

  public static boolean isBabyPet(int id)
  {
    switch (id)
    {
    case 12780:
    case 12781:
    case 12782:
      return true;
    }
    return false;
  }

  public static boolean isImprovedBabyPet(int id)
  {
    switch (id)
    {
    case 16034:
    case 16035:
    case 16036:
    case 16046:
      return true;
    }
    return false;
  }

  public static boolean isWolf(int id)
  {
    return id == 12077;
  }

  public static boolean isHatchling(int id)
  {
    switch (id)
    {
    case 12311:
    case 12312:
    case 12313:
      return true;
    }
    return false;
  }

  public static boolean isStrider(int id)
  {
    switch (id)
    {
    case 12526:
    case 12527:
    case 12528:
    case 16038:
    case 16039:
    case 16040:
    case 16068:
      return true;
    }
    return false;
  }

  public static boolean isGWolf(int id)
  {
    switch (id)
    {
    case 16025:
    case 16037:
    case 16041:
    case 16042:
      return true;
    }
    return false;
  }

  public static boolean isVitaminPet(int id)
  {
    switch (id)
    {
    case 16043:
    case 16044:
    case 16045:
    case 16046:
    case 16050:
    case 16051:
    case 16052:
    case 16053:
      return true;
    case 16047:
    case 16048:
    case 16049: } return false;
  }

  public static enum L2Pet
  {
    WOLF(12077, 2375, 2515, false, 1, 12, 0.3D, 2, 2), 

    HATCHLING_WIND(12311, 3500, 4038, false, 1, 12, 0.3D, 2, 2), 
    HATCHLING_STAR(12312, 3501, 4038, false, 1, 12, 0.3D, 2, 2), 
    HATCHLING_TWILIGHT(12313, 3502, 4038, false, 1, 100, 0.3D, 2, 2), 

    STRIDER_WIND(12526, 4422, 5168, true, 1, 12, 0.3D, 2, 2), 
    STRIDER_STAR(12527, 4423, 5168, true, 1, 12, 0.3D, 2, 2), 
    STRIDER_TWILIGHT(12528, 4424, 5168, true, 1, 100, 0.3D, 2, 2), 

    RED_STRIDER_WIND(16038, 10308, 5168, true, 1, 12, 0.3D, 2, 2), 
    RED_STRIDER_STAR(16039, 10309, 5168, true, 1, 12, 0.3D, 2, 2), 
    RED_STRIDER_TWILIGHT(16040, 10310, 5168, true, 1, 100, 0.3D, 2, 2), 

    WYVERN(12621, 5249, 6316, true, 1, 12, 0.0D, 2, 2), 

    GREAT_WOLF(16025, 9882, 9668, false, 55, 10, 0.3D, 2, 2), 
    WGREAT_WOLF(16037, 10307, 9668, true, 55, 12, 0.3D, 2, 2), 
    FENRIR_WOLF(16041, 10426, 9668, true, 70, 12, 0.3D, 2, 2), 
    WFENRIR_WOLF(16042, 10611, 9668, true, 70, 12, 0.3D, 2, 2), 

    BABY_BUFFALO(12780, 6648, 7582, false, 1, 12, 0.05D, 2, 2), 
    BABY_KOOKABURRA(12781, 6650, 7582, false, 1, 12, 0.05D, 2, 2), 
    BABY_COUGAR(12782, 6649, 7582, false, 1, 12, 0.05D, 2, 2), 

    IMPROVED_BABY_BUFFALO(16034, 10311, 10425, false, 55, 12, 0.3D, 2, 2), 
    IMPROVED_BABY_KOOKABURRA(16035, 10313, 10425, false, 55, 12, 0.3D, 2, 2), 
    IMPROVED_BABY_COUGAR(16036, 10312, 10425, false, 55, 12, 0.3D, 2, 2), 

    SIN_EATER(12564, 4425, 2515, false, 1, 12, 0.0D, 2, 2), 

    FOX_SHAMAN(16043, 13020, -1, false, 25, 12, 0.3D, 2, 2), 
    WILD_BEAST_FIGHTER(16044, 13019, -1, false, 25, 12, 0.3D, 2, 2), 
    WHITE_WEASEL(16045, 13017, -1, false, 25, 12, 0.3D, 2, 2), 
    FAIRY_PRINCESS(16046, 13018, -1, false, 25, 12, 0.3D, 2, 2), 
    OWL_MONK(16050, 14063, -1, false, 25, 12, 0.3D, 2, 2), 
    SPIRIT_SHAMAN(16051, 14062, -1, false, 25, 12, 0.3D, 2, 2), 
    TOY_KNIGHT(16052, 14061, -1, false, 25, 12, 0.3D, 2, 2), 
    TURTLE_ASCETIC(16053, 14064, -1, false, 25, 12, 0.3D, 2, 2), 

    DEINONYCHUS(16067, 14828, 2515, false, 55, 12, 0.3D, 2, 2), 
    GUARDIANS_STRIDER(16068, 14819, 5168, true, 55, 12, 0.3D, 2, 2);

    private final int _npcId;
    private final int _controlItemId;
    private final int _foodId;
    private final boolean _isMountable;
    private final int _minLevel;
    private final int _addFed;
    private final double _expPenalty;
    private final int _soulshots;
    private final int _spiritshots;

    private L2Pet(int npcId, int controlItemId, int foodId, boolean isMountabe, int minLevel, int addFed, double expPenalty, int soulshots, int spiritshots) { _npcId = npcId;
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

    public double getExpPenalty()
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
}