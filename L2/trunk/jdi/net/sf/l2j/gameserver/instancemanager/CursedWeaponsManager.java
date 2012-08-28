package net.sf.l2j.gameserver.instancemanager;

import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.CursedWeapon;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2FeedableBeastInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FestivalMonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RiftInvaderInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeGuardInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CursedWeaponsManager
{
  private static final Logger _log = Logger.getLogger(CursedWeaponsManager.class.getName());
  private static CursedWeaponsManager _instance;
  private Map<Integer, CursedWeapon> _cursedWeapons;

  public static final CursedWeaponsManager getInstance()
  {
    if (_instance == null)
    {
      _instance = new CursedWeaponsManager();
    }
    return _instance;
  }

  public CursedWeaponsManager()
  {
    _log.info("Initializing CursedWeaponsManager");
    _cursedWeapons = new FastMap();

    if (!Config.ALLOW_CURSED_WEAPONS) return;

    load();
    restore();
    controlPlayers();
    _log.info("Loaded : " + _cursedWeapons.size() + " cursed weapon(s).");
  }

  public final void reload()
  {
    _instance = new CursedWeaponsManager();
  }

  private final void load() {
    if (Config.DEBUG)
      System.out.print("  Parsing ... ");
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);

      File file = new File(Config.DATAPACK_ROOT + "/data/xml/cursedWeapons.xml");
      if (!file.exists())
      {
        if (Config.DEBUG)
          System.out.println("NO FILE");
        return;
      }

      Document doc = factory.newDocumentBuilder().parse(file);

      for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
      {
        if (!"list".equalsIgnoreCase(n.getNodeName()))
          continue;
        for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
        {
          if (!"item".equalsIgnoreCase(d.getNodeName()))
            continue;
          NamedNodeMap attrs = d.getAttributes();
          int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
          int skillId = Integer.parseInt(attrs.getNamedItem("skillId").getNodeValue());
          String name = attrs.getNamedItem("name").getNodeValue();

          CursedWeapon cw = new CursedWeapon(id, skillId, name);

          for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
          {
            if ("dropRate".equalsIgnoreCase(cd.getNodeName()))
            {
              attrs = cd.getAttributes();
              int val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
              cw.setDropRate(val);
            } else if ("duration".equalsIgnoreCase(cd.getNodeName()))
            {
              attrs = cd.getAttributes();
              int val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
              cw.setDuration(val);
            } else if ("durationLost".equalsIgnoreCase(cd.getNodeName()))
            {
              attrs = cd.getAttributes();
              int val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
              cw.setDurationLost(val);
            } else if ("disapearChance".equalsIgnoreCase(cd.getNodeName()))
            {
              attrs = cd.getAttributes();
              int val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
              cw.setDisapearChance(val); } else {
              if (!"stageKills".equalsIgnoreCase(cd.getNodeName()))
                continue;
              attrs = cd.getAttributes();
              int val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
              cw.setStageKills(val);
            }

          }

          _cursedWeapons.put(Integer.valueOf(id), cw);
        }

      }

      if (Config.DEBUG)
        System.out.println("OK");
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Error parsing cursed weapons file.", e);

      if (Config.DEBUG)
        System.out.println("ERROR");
      return;
    }
  }

  private final void restore() {
    if (Config.DEBUG)
      System.out.print("  Restoring ... ");
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("SELECT itemId, playerId, playerKarma, playerPkKills, nbKills, endTime FROM cursed_weapons");
      ResultSet rset = statement.executeQuery();

      if (rset.next())
      {
        int itemId = rset.getInt("itemId");
        int playerId = rset.getInt("playerId");
        int playerKarma = rset.getInt("playerKarma");
        int playerPkKills = rset.getInt("playerPkKills");
        int nbKills = rset.getInt("nbKills");
        long endTime = rset.getLong("endTime");

        CursedWeapon cw = (CursedWeapon)_cursedWeapons.get(Integer.valueOf(itemId));
        cw.setPlayerId(playerId);
        cw.setPlayerKarma(playerKarma);
        cw.setPlayerPkKills(playerPkKills);
        cw.setNbKills(nbKills);
        cw.setEndTime(endTime);
        cw.reActivate();
      }

      rset.close();
      statement.close();

      if (Config.DEBUG)
        System.out.println("OK");
    }
    catch (Exception e)
    {
      _log.warning("Could not restore CursedWeapons data: " + e);

      if (Config.DEBUG)
        System.out.println("ERROR");
      return;
    } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private final void controlPlayers() {
    if (Config.DEBUG) {
      System.out.print("  Checking players ... ");
    }
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = null;
      rset = null;

      for (CursedWeapon cw : _cursedWeapons.values())
      {
        if (cw.isActivated()) {
          continue;
        }
        int itemId = cw.getItemId();
        try
        {
          statement = con.prepareStatement("SELECT owner_id FROM items WHERE item_id=?");
          statement.setInt(1, itemId);
          rset = statement.executeQuery();

          if (rset.next())
          {
            int playerId = rset.getInt("owner_id");
            _log.info("PROBLEM : Player " + playerId + " owns the cursed weapon " + itemId + " but he shouldn't.");

            statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
            statement.setInt(1, playerId);
            statement.setInt(2, itemId);
            if (statement.executeUpdate() != 1)
            {
              _log.warning("Error while deleting cursed weapon " + itemId + " from userId " + playerId);
            }
            statement.close();

            statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE obj_id=?");
            statement.setInt(1, cw.getPlayerKarma());
            statement.setInt(2, cw.getPlayerPkKills());
            statement.setInt(3, playerId);
            if (statement.executeUpdate() != 1)
            {
              _log.warning("Error while updating karma & pkkills for userId " + cw.getPlayerId());
            }

            removeFromDb(itemId);
          }
          rset.close();
          statement.close();
        } catch (SQLException sqlE) {
        }
        try {
          con.close();
        }
        catch (Exception e)
        {
        }
      }
    }
    catch (Exception e)
    {
      PreparedStatement statement;
      ResultSet rset;
      _log.warning("Could not check CursedWeapons data: " + e);

      if (Config.DEBUG)
        System.out.println("ERROR");
      return;
    } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    if (Config.DEBUG)
      System.out.println("DONE");
  }

  public synchronized void checkDrop(L2Attackable attackable, L2PcInstance player)
  {
    if (((attackable instanceof L2SiegeGuardInstance)) || ((attackable instanceof L2RiftInvaderInstance)) || ((attackable instanceof L2FestivalMonsterInstance)) || ((attackable instanceof L2GuardInstance)) || ((attackable instanceof L2RaidBossInstance)) || ((attackable instanceof L2GrandBossInstance)) || ((attackable instanceof L2FeedableBeastInstance)))
    {
      return;
    }
    if (player.isCursedWeaponEquiped()) {
      return;
    }
    for (CursedWeapon cw : _cursedWeapons.values())
    {
      if (cw.isActive())
        continue;
      if (cw.checkDrop(attackable, player))
        break;
    }
  }

  public void activate(L2PcInstance player, L2ItemInstance item) {
    CursedWeapon cw = (CursedWeapon)_cursedWeapons.get(Integer.valueOf(item.getItemId()));
    if (player.isCursedWeaponEquiped())
    {
      CursedWeapon cw2 = (CursedWeapon)_cursedWeapons.get(Integer.valueOf(player.getCursedWeaponEquipedId()));

      cw2.setNbKills(cw2.getStageKills() - 1);
      cw2.increaseKills();

      cw.setPlayer(player);
      cw.endOfLife();
    } else {
      cw.activate(player, item);
    }
  }

  public void drop(int itemId, L2Character killer) {
    CursedWeapon cw = (CursedWeapon)_cursedWeapons.get(Integer.valueOf(itemId));

    cw.dropIt(killer);
  }

  public void increaseKills(int itemId)
  {
    CursedWeapon cw = (CursedWeapon)_cursedWeapons.get(Integer.valueOf(itemId));

    cw.increaseKills();
  }

  public int getLevel(int itemId)
  {
    CursedWeapon cw = (CursedWeapon)_cursedWeapons.get(Integer.valueOf(itemId));

    return cw.getLevel();
  }

  public static void announce(SystemMessage sm)
  {
    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
    {
      if (player == null)
        continue;
      player.sendPacket(sm);
    }
    if (Config.DEBUG)
      _log.info("MessageID: " + sm.getMessageID());
  }

  public void checkPlayer(L2PcInstance player)
  {
    if (player == null) {
      return;
    }
    for (CursedWeapon cw : _cursedWeapons.values())
    {
      if ((cw.isActivated()) && (player.getObjectId() == cw.getPlayerId()))
      {
        cw.setPlayer(player);
        cw.setItem(player.getInventory().getItemByItemId(cw.getItemId()));
        cw.giveSkill();
        player.setCursedWeaponEquipedId(cw.getItemId());

        SystemMessage sm = new SystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
        sm.addString(cw.getName());

        sm.addNumber((int)((cw.getEndTime() - System.currentTimeMillis()) / 60000L));
        player.sendPacket(sm);
      }
    }
  }

  public static void removeFromDb(int itemId)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");
      statement.setInt(1, itemId);
      statement.executeUpdate();

      statement.close();
      con.close();
    }
    catch (SQLException e)
    {
      _log.severe("CursedWeaponsManager: Failed to remove data: " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public void saveData() {
    for (CursedWeapon cw : _cursedWeapons.values())
    {
      cw.saveData();
    }
  }

  public boolean isCursed(int itemId)
  {
    return _cursedWeapons.containsKey(Integer.valueOf(itemId));
  }

  public Collection<CursedWeapon> getCursedWeapons()
  {
    return _cursedWeapons.values();
  }

  public Set<Integer> getCursedWeaponsIds()
  {
    return _cursedWeapons.keySet();
  }

  public CursedWeapon getCursedWeapon(int itemId)
  {
    return (CursedWeapon)_cursedWeapons.get(Integer.valueOf(itemId));
  }

  public void givePassive(int itemId) {
    try {
      ((CursedWeapon)_cursedWeapons.get(Integer.valueOf(itemId))).giveSkill();
    }
    catch (Exception e)
    {
    }
  }
}