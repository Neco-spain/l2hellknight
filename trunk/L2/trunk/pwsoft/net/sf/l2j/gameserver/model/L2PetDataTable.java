package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;

public class L2PetDataTable
{
  private static Logger _log = AbstractLogger.getLogger(L2PetInstance.class.getName());
  private static L2PetDataTable _instance;
  private static Map<Integer, Map<Integer, L2PetData>> _petTable;

  public static L2PetDataTable getInstance()
  {
    if (_instance == null) {
      _instance = new L2PetDataTable();
    }
    return _instance;
  }

  private L2PetDataTable()
  {
    _petTable = new FastMap();
  }

  public void loadPetsData()
  {
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      statement = con.prepareStatement("SELECT typeID, level, expMax, hpMax, mpMax, patk, pdef, matk, mdef, acc, evasion, crit, speed, atk_speed, cast_speed, feedMax, feedbattle, feednormal, loadMax, hpregen, mpregen, owner_exp_taken FROM pets_stats");
      rset = statement.executeQuery();

      while (rset.next())
      {
        int petId = rset.getInt("typeID");
        int petLevel = rset.getInt("level");

        L2PetData petData = new L2PetData();
        petData.setPetID(petId);
        petData.setPetLevel(petLevel);
        petData.setPetMaxExp(rset.getInt("expMax"));
        petData.setPetMaxHP(rset.getInt("hpMax"));
        petData.setPetMaxMP(rset.getInt("mpMax"));
        petData.setPetPAtk(rset.getInt("patk"));
        petData.setPetPDef(rset.getInt("pdef"));
        petData.setPetMAtk(rset.getInt("matk"));
        petData.setPetMDef(rset.getInt("mdef"));
        petData.setPetAccuracy(rset.getInt("acc"));
        petData.setPetEvasion(rset.getInt("evasion"));
        petData.setPetCritical(rset.getInt("crit"));
        petData.setPetSpeed(rset.getInt("speed"));
        petData.setPetAtkSpeed(rset.getInt("atk_speed"));
        petData.setPetCastSpeed(rset.getInt("cast_speed"));
        petData.setPetMaxFeed(rset.getInt("feedMax"));
        petData.setPetFeedNormal(rset.getInt("feednormal"));
        petData.setPetFeedBattle(rset.getInt("feedbattle"));
        petData.setPetMaxLoad(rset.getInt("loadMax"));
        petData.setPetRegenHP(rset.getInt("hpregen"));
        petData.setPetRegenMP(rset.getInt("mpregen"));
        petData.setPetRegenMP(rset.getInt("mpregen"));
        petData.setOwnerExpTaken(rset.getFloat("owner_exp_taken"));

        if (!_petTable.containsKey(Integer.valueOf(petId))) {
          _petTable.put(Integer.valueOf(petId), new FastMap());
        }
        ((Map)_petTable.get(Integer.valueOf(petId))).put(Integer.valueOf(petLevel), petData);
      }

    }
    catch (Exception e)
    {
    }
    finally
    {
      Close.CSR(con, statement, rset);
    }
  }

  public void addPetData(L2PetData petData)
  {
    Map h = (Map)_petTable.get(Integer.valueOf(petData.getPetID()));

    if (h == null)
    {
      Map statTable = new FastMap();
      statTable.put(Integer.valueOf(petData.getPetLevel()), petData);
      _petTable.put(Integer.valueOf(petData.getPetID()), statTable);
      statTable.clear();
      statTable = null;
      return;
    }

    h.put(Integer.valueOf(petData.getPetLevel()), petData);
  }

  public void addPetData(L2PetData[] petLevelsList)
  {
    for (int i = 0; i < petLevelsList.length; i++)
      addPetData(petLevelsList[i]);
  }

  public L2PetData getPetData(int petID, int petLevel)
  {
    return (L2PetData)((Map)_petTable.get(Integer.valueOf(petID))).get(Integer.valueOf(petLevel));
  }

  public static boolean isWolf(int npcId)
  {
    return npcId == 12077;
  }

  public static boolean isSinEater(int npcId)
  {
    return npcId == 12564;
  }

  public static boolean isHatchling(int npcId)
  {
    return (npcId > 12310) && (npcId < 12314);
  }

  public static boolean isStrider(int npcId)
  {
    return (npcId > 12525) && (npcId < 12529);
  }

  public static boolean isWyvern(int npcId)
  {
    return npcId == 12621;
  }

  public static boolean isBaby(int npcId)
  {
    return (npcId > 12779) && (npcId < 12783);
  }

  public static boolean isPetFood(int itemId)
  {
    return (itemId == 2515) || (itemId == 4038) || (itemId == 5168) || (itemId == 6316) || (itemId == 7582);
  }

  public static boolean isWolfFood(int itemId)
  {
    return itemId == 2515;
  }

  public static boolean isSinEaterFood(int itemId)
  {
    return itemId == 2515;
  }

  public static boolean isHatchlingFood(int itemId)
  {
    return itemId == 4038;
  }

  public static boolean isStriderFood(int itemId)
  {
    return itemId == 5168;
  }

  public static boolean isWyvernFood(int itemId)
  {
    return itemId == 6316;
  }

  public static boolean isBabyFood(int itemId)
  {
    return itemId == 7582;
  }

  public static int getFoodItemId(int npcId)
  {
    if (isWolf(npcId))
      return 2515;
    if (isSinEater(npcId))
      return 2515;
    if (isHatchling(npcId))
      return 4038;
    if (isStrider(npcId))
      return 5168;
    if (isBaby(npcId)) {
      return 7582;
    }
    return 0;
  }

  public static int getPetIdByItemId(int itemId)
  {
    switch (itemId)
    {
    case 2375:
      return 12077;
    case 4425:
      return 12564;
    case 3500:
      return 12311;
    case 3501:
      return 12312;
    case 3502:
      return 12313;
    case 4422:
      return 12526;
    case 4423:
      return 12527;
    case 4424:
      return 12528;
    case 8663:
      return 12621;
    case 6648:
      return 12780;
    case 6649:
      return 12782;
    case 6650:
      return 12781;
    }

    return 0;
  }

  public static int getHatchlingWindId()
  {
    return 12311;
  }

  public static int getHatchlingStarId() {
    return 12312;
  }

  public static int getHatchlingTwilightId() {
    return 12313;
  }

  public static int getStriderWindId() {
    return 12526;
  }

  public static int getStriderStarId() {
    return 12527;
  }

  public static int getStriderTwilightId() {
    return 12528;
  }

  public static int getWyvernItemId() {
    return 8663;
  }

  public static int getStriderWindItemId() {
    return 4422;
  }

  public static int getStriderStarItemId() {
    return 4423;
  }

  public static int getStriderTwilightItemId() {
    return 4424;
  }

  public static int getSinEaterItemId()
  {
    return 4425;
  }

  public static boolean isPetItem(int itemId)
  {
    return (itemId == 2375) || (itemId == 4425) || (itemId == 3500) || (itemId == 3501) || (itemId == 3502) || (itemId == 4422) || (itemId == 4423) || (itemId == 4424) || (itemId == 8663) || (itemId == 6648) || (itemId == 6649) || (itemId == 6650);
  }

  public static int[] getPetItemsAsNpc(int npcId)
  {
    switch (npcId)
    {
    case 12077:
      return new int[] { 2375 };
    case 12564:
      return new int[] { 4425 };
    case 12311:
    case 12312:
    case 12313:
      return new int[] { 3500, 3501, 3502 };
    case 12526:
    case 12527:
    case 12528:
      return new int[] { 4422, 4423, 4424 };
    case 12621:
      return new int[] { 8663 };
    case 12780:
    case 12781:
    case 12782:
      return new int[] { 6648, 6649, 6650 };
    }

    return new int[] { 0 };
  }

  public static boolean isMountable(int npcId)
  {
    return (npcId == 12526) || (npcId == 12527) || (npcId == 12528) || (npcId == 12621);
  }
}