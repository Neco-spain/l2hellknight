package net.sf.l2j.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.templates.StatsSet;

public class Hero
{
  private static Logger _log = Logger.getLogger(Hero.class.getName());
  private static Hero _instance;
  private static final String GET_HEROES = "SELECT * FROM heroes WHERE played = 1";
  private static final String GET_ALL_HEROES = "SELECT * FROM heroes";
  private static final String UPDATE_ALL = "UPDATE heroes SET played = 0";
  private static final String INSERT_HERO = "INSERT INTO heroes VALUES (?,?,?,?,?)";
  private static final String UPDATE_HERO = "UPDATE heroes SET count = ?, played = ? WHERE char_id = ?";
  private static final String GET_CLAN_ALLY = "SELECT characters.clanid AS clanid, coalesce(clan_data.ally_Id, 0) AS allyId FROM characters LEFT JOIN clan_data ON clan_data.clan_id = characters.clanid  WHERE characters.obj_Id = ?";
  private static final String GET_CLAN_NAME = "SELECT clan_name FROM clan_data WHERE clan_id = (SELECT clanid FROM characters WHERE char_name = ?)";
  private static final String DELETE_ITEMS = "DELETE FROM items WHERE item_id IN (6842, 6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621) AND owner_id NOT IN (SELECT obj_id FROM characters WHERE accesslevel > 0)";
  private static final int[] _heroItems = { 6842, 6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621 };
  private static Map<Integer, StatsSet> _heroes;
  private static Map<Integer, StatsSet> _completeHeroes;
  public static final String COUNT = "count";
  public static final String PLAYED = "played";
  public static final String CLAN_NAME = "clan_name";
  public static final String CLAN_CREST = "clan_crest";
  public static final String ALLY_NAME = "ally_name";
  public static final String ALLY_CREST = "ally_crest";

  public static Hero getInstance()
  {
    if (_instance == null)
      _instance = new Hero();
    return _instance;
  }

  public Hero()
  {
    init();
  }

  private void init()
  {
    _heroes = new FastMap();
    _completeHeroes = new FastMap();

    Connection con = null;
    Connection con2 = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      con2 = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT * FROM heroes WHERE played = 1");
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        StatsSet hero = new StatsSet();
        int charId = rset.getInt("char_id");
        hero.set("char_name", rset.getString("char_name"));
        hero.set("class_id", rset.getInt("class_id"));
        hero.set("count", rset.getInt("count"));
        hero.set("played", rset.getInt("played"));

        PreparedStatement statement2 = con2.prepareStatement("SELECT characters.clanid AS clanid, coalesce(clan_data.ally_Id, 0) AS allyId FROM characters LEFT JOIN clan_data ON clan_data.clan_id = characters.clanid  WHERE characters.obj_Id = ?");
        statement2.setInt(1, charId);
        ResultSet rset2 = statement2.executeQuery();

        if (rset2.next())
        {
          int clanId = rset2.getInt("clanid");
          int allyId = rset2.getInt("allyId");

          String clanName = "";
          String allyName = "";
          int clanCrest = 0;
          int allyCrest = 0;

          if (clanId > 0)
          {
            clanName = ClanTable.getInstance().getClan(clanId).getName();
            clanCrest = ClanTable.getInstance().getClan(clanId).getCrestId();

            if (allyId > 0)
            {
              allyName = ClanTable.getInstance().getClan(clanId).getAllyName();
              allyCrest = ClanTable.getInstance().getClan(clanId).getAllyCrestId();
            }
          }

          hero.set("clan_crest", clanCrest);
          hero.set("clan_name", clanName);
          hero.set("ally_crest", allyCrest);
          hero.set("ally_name", allyName);
        }

        rset2.close();
        statement2.close();

        _heroes.put(Integer.valueOf(charId), hero);
      }

      rset.close();
      statement.close();

      statement = con.prepareStatement("SELECT * FROM heroes");
      rset = statement.executeQuery();

      while (rset.next())
      {
        StatsSet hero = new StatsSet();
        int charId = rset.getInt("char_id");
        hero.set("char_name", rset.getString("char_name"));
        hero.set("class_id", rset.getInt("class_id"));
        hero.set("count", rset.getInt("count"));
        hero.set("played", rset.getInt("played"));

        PreparedStatement statement2 = con2.prepareStatement("SELECT characters.clanid AS clanid, coalesce(clan_data.ally_Id, 0) AS allyId FROM characters LEFT JOIN clan_data ON clan_data.clan_id = characters.clanid  WHERE characters.obj_Id = ?");
        statement2.setInt(1, charId);
        ResultSet rset2 = statement2.executeQuery();

        if (rset2.next())
        {
          int clanId = rset2.getInt("clanid");
          int allyId = rset2.getInt("allyId");

          String clanName = "";
          String allyName = "";
          int clanCrest = 0;
          int allyCrest = 0;

          if (clanId > 0)
          {
            clanName = ClanTable.getInstance().getClan(clanId).getName();
            clanCrest = ClanTable.getInstance().getClan(clanId).getCrestId();

            if (allyId > 0)
            {
              allyName = ClanTable.getInstance().getClan(clanId).getAllyName();
              allyCrest = ClanTable.getInstance().getClan(clanId).getAllyCrestId();
            }
          }

          hero.set("clan_crest", clanCrest);
          hero.set("clan_name", clanName);
          hero.set("ally_crest", allyCrest);
          hero.set("ally_name", allyName);
        }

        rset2.close();
        statement2.close();

        _completeHeroes.put(Integer.valueOf(charId), hero);
      }

      rset.close();
      statement.close();
    }
    catch (SQLException e)
    {
      _log.warning("Hero System: Couldnt load Heroes");
      if (Config.DEBUG) e.printStackTrace(); 
    }
    finally
    {
      try {
        con.close(); con2.close(); } catch (SQLException e) { e.printStackTrace();
      }
    }
    _log.info("Hero System: Loaded " + _heroes.size() + " Heroes.");
    _log.info("Hero System: Loaded " + _completeHeroes.size() + " all time Heroes.");
  }

  public Map<Integer, StatsSet> getHeroes()
  {
    return _heroes;
  }

  public synchronized void computeNewHeroes(List<StatsSet> newHeroes)
  {
    updateHeroes(true);

    List heroItems = Arrays.asList(new int[][] { _heroItems });

    if (_heroes.size() != 0)
    {
      for (StatsSet hero : _heroes.values())
      {
        String name = hero.getString("char_name");

        L2PcInstance player = L2World.getInstance().getPlayer(name);

        if (player == null)
          continue;
        try {
          player.setHero(false);

          L2ItemInstance[] items = player.getInventory().unEquipItemInBodySlotAndRecord(16384);
          InventoryUpdate iu = new InventoryUpdate();
          for (L2ItemInstance item : items)
          {
            iu.addModifiedItem(item);
          }
          player.sendPacket(iu);

          items = player.getInventory().unEquipItemInBodySlotAndRecord(128);
          iu = new InventoryUpdate();
          for (L2ItemInstance item : items)
          {
            iu.addModifiedItem(item);
          }
          player.sendPacket(iu);

          items = player.getInventory().unEquipItemInBodySlotAndRecord(65536);
          iu = new InventoryUpdate();
          for (L2ItemInstance item : items)
          {
            iu.addModifiedItem(item);
          }
          player.sendPacket(iu);

          items = player.getInventory().unEquipItemInBodySlotAndRecord(262144);
          iu = new InventoryUpdate();
          for (L2ItemInstance item : items)
          {
            iu.addModifiedItem(item);
          }
          player.sendPacket(iu);

          items = player.getInventory().unEquipItemInBodySlotAndRecord(524288);
          iu = new InventoryUpdate();
          for (L2ItemInstance item : items)
          {
            iu.addModifiedItem(item);
          }
          player.sendPacket(iu);

          for (L2ItemInstance item : player.getInventory().getAvailableItems(false))
          {
            if ((item == null) || 
              (!heroItems.contains(Integer.valueOf(item.getItemId()))))
              continue;
            player.destroyItem("Hero", item, null, true);
            iu = new InventoryUpdate();
            iu.addRemovedItem(item);
            player.sendPacket(iu);
          }

          player.sendPacket(new UserInfo(player));
          player.broadcastUserInfo();
        } catch (NullPointerException e) {
        }
      }
    }
    if (newHeroes.size() == 0)
    {
      _heroes.clear();
      return;
    }

    Map heroes = new FastMap();

    for (StatsSet hero : newHeroes)
    {
      int charId = hero.getInteger("char_id");

      if ((_completeHeroes != null) && (_completeHeroes.containsKey(Integer.valueOf(charId))))
      {
        StatsSet oldHero = (StatsSet)_completeHeroes.get(Integer.valueOf(charId));
        int count = oldHero.getInteger("count");
        oldHero.set("count", count + 1);
        oldHero.set("played", 1);

        heroes.put(Integer.valueOf(charId), oldHero);
      }
      else
      {
        StatsSet newHero = new StatsSet();
        newHero.set("char_name", hero.getString("char_name"));
        newHero.set("class_id", hero.getInteger("class_id"));
        newHero.set("count", 1);
        newHero.set("played", 1);

        heroes.put(Integer.valueOf(charId), newHero);
      }
    }

    deleteItemsInDb();

    _heroes.clear();
    _heroes.putAll(heroes);
    heroes.clear();

    updateHeroes(false);

    for (StatsSet hero : _heroes.values())
    {
      String name = hero.getString("char_name");

      L2PcInstance player = L2World.getInstance().getPlayer(name);

      if (player != null)
      {
        player.setHero(true);
        L2Clan clan = player.getClan();
        if (clan != null)
        {
          clan.setReputationScore(clan.getReputationScore() + 1000, true);
          clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
          SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS);
          sm.addString(name);
          sm.addNumber(1000);
          clan.broadcastToOnlineMembers(sm);
        }
        player.sendPacket(new UserInfo(player));
        player.broadcastUserInfo();
      }
      else
      {
        Connection con = null;
        try
        {
          con = L2DatabaseFactory.getInstance().getConnection();
          PreparedStatement statement = con.prepareStatement("SELECT clan_name FROM clan_data WHERE clan_id = (SELECT clanid FROM characters WHERE char_name = ?)");
          statement.setString(1, name);
          ResultSet rset = statement.executeQuery();
          if (rset.next())
          {
            String clanName = rset.getString("clan_name");
            if (clanName != null)
            {
              L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
              if (clan != null)
              {
                clan.setReputationScore(clan.getReputationScore() + 1000, true);
                clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
                SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS);
                sm.addString(name);
                sm.addNumber(1000);
                clan.broadcastToOnlineMembers(sm);
              }
            }
          }

          rset.close();
          statement.close();
        }
        catch (Exception e)
        {
          _log.warning("could not get clan name of " + name + ": " + e);
        }
        finally {
          try {
            con.close(); } catch (Exception e) {
          }
        }
      }
    }
  }

  public void updateHeroes(boolean setDefault) {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      if (setDefault)
      {
        PreparedStatement statement = con.prepareStatement("UPDATE heroes SET played = 0");
        statement.execute();
        statement.close();
      }
      else
      {
        for (Integer heroId : _heroes.keySet())
        {
          StatsSet hero = (StatsSet)_heroes.get(heroId);
          PreparedStatement statement;
          if ((_completeHeroes == null) || (!_completeHeroes.containsKey(heroId)))
          {
            PreparedStatement statement = con.prepareStatement("INSERT INTO heroes VALUES (?,?,?,?,?)");
            statement.setInt(1, heroId.intValue());
            statement.setString(2, hero.getString("char_name"));
            statement.setInt(3, hero.getInteger("class_id"));
            statement.setInt(4, hero.getInteger("count"));
            statement.setInt(5, hero.getInteger("played"));
            statement.execute();

            Connection con2 = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement2 = con2.prepareStatement("SELECT characters.clanid AS clanid, coalesce(clan_data.ally_Id, 0) AS allyId FROM characters LEFT JOIN clan_data ON clan_data.clan_id = characters.clanid  WHERE characters.obj_Id = ?");
            statement2.setInt(1, heroId.intValue());
            ResultSet rset2 = statement2.executeQuery();

            if (rset2.next())
            {
              int clanId = rset2.getInt("clanid");
              int allyId = rset2.getInt("allyId");

              String clanName = "";
              String allyName = "";
              int clanCrest = 0;
              int allyCrest = 0;

              if (clanId > 0)
              {
                clanName = ClanTable.getInstance().getClan(clanId).getName();
                clanCrest = ClanTable.getInstance().getClan(clanId).getCrestId();

                if (allyId > 0)
                {
                  allyName = ClanTable.getInstance().getClan(clanId).getAllyName();
                  allyCrest = ClanTable.getInstance().getClan(clanId).getAllyCrestId();
                }
              }

              hero.set("clan_crest", clanCrest);
              hero.set("clan_name", clanName);
              hero.set("ally_crest", allyCrest);
              hero.set("ally_name", allyName);
            }

            rset2.close();
            statement2.close();
            con2.close();

            _heroes.remove(hero);
            _heroes.put(heroId, hero);

            _completeHeroes.put(heroId, hero);
          }
          else
          {
            statement = con.prepareStatement("UPDATE heroes SET count = ?, played = ? WHERE char_id = ?");
            statement.setInt(1, hero.getInteger("count"));
            statement.setInt(2, hero.getInteger("played"));
            statement.setInt(3, heroId.intValue());
            statement.execute();
          }

          statement.close();
        }
      }
    }
    catch (SQLException e)
    {
      _log.warning("Hero System: Couldnt update Heroes");
      if (Config.DEBUG) e.printStackTrace(); 
    }
    finally
    {
      try {
        con.close(); } catch (Exception e) { e.printStackTrace(); }
    }
  }

  public int[] getHeroItems()
  {
    return _heroItems;
  }

  private void deleteItemsInDb()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE item_id IN (6842, 6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621) AND owner_id NOT IN (SELECT obj_id FROM characters WHERE accesslevel > 0)");
      statement.execute();
      statement.close();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    finally {
      try {
        con.close(); } catch (SQLException e) { e.printStackTrace();
      }
    }
  }
}