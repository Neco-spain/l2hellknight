package l2p.gameserver.instancemanager;

import gnu.trove.TIntObjectHashMap;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import l2p.commons.dbutils.DbUtils;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.data.xml.holder.ItemHolder;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.model.CursedWeapon;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.utils.Location;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CursedWeaponsManager
{
  private static final Logger _log = LoggerFactory.getLogger(CursedWeaponsManager.class);

  private static final CursedWeaponsManager _instance = new CursedWeaponsManager();
  private CursedWeapon[] _cursedWeapons;
  private TIntObjectHashMap<CursedWeapon> _cursedWeaponsMap;
  private ScheduledFuture<?> _removeTask;
  private static final int CURSEDWEAPONS_MAINTENANCE_INTERVAL = 300000;

  public static final CursedWeaponsManager getInstance()
  {
    return _instance;
  }

  public CursedWeaponsManager()
  {
    _cursedWeaponsMap = new TIntObjectHashMap();
    _cursedWeapons = new CursedWeapon[0];

    if (!Config.ALLOW_CURSED_WEAPONS) {
      return;
    }
    load();
    restore();
    checkConditions();

    cancelTask();
    _removeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new RemoveTask(null), 300000L, 300000L);

    _log.info("CursedWeaponsManager: Loaded " + _cursedWeapons.length + " cursed weapon(s).");
  }

  @Deprecated
  public final void reload()
  {
  }

  private void load()
  {
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);

      File file = new File(Config.DATAPACK_ROOT, "data/cursed_weapons.xml");
      if (!file.exists()) {
        return;
      }
      Document doc = factory.newDocumentBuilder().parse(file);

      for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
        if ("list".equalsIgnoreCase(n.getNodeName()))
          for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
            if (!"item".equalsIgnoreCase(d.getNodeName()))
              continue;
            NamedNodeMap attrs = d.getAttributes();
            int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
            int skillId = Integer.parseInt(attrs.getNamedItem("skillId").getNodeValue());
            String name = "Unknown cursed weapon";
            if (attrs.getNamedItem("name") != null)
              name = attrs.getNamedItem("name").getNodeValue();
            else if (ItemHolder.getInstance().getTemplate(id) != null) {
              name = ItemHolder.getInstance().getTemplate(id).getName();
            }
            if (id == 0) {
              continue;
            }
            CursedWeapon cw = new CursedWeapon(id, skillId, name);
            for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
              if ("dropRate".equalsIgnoreCase(cd.getNodeName())) {
                cw.setDropRate(Integer.parseInt(cd.getAttributes().getNamedItem("val").getNodeValue()));
              } else if ("duration".equalsIgnoreCase(cd.getNodeName()))
              {
                attrs = cd.getAttributes();
                cw.setDurationMin(Integer.parseInt(attrs.getNamedItem("min").getNodeValue()));
                cw.setDurationMax(Integer.parseInt(attrs.getNamedItem("max").getNodeValue()));
              }
              else if ("durationLost".equalsIgnoreCase(cd.getNodeName())) {
                cw.setDurationLost(Integer.parseInt(cd.getAttributes().getNamedItem("val").getNodeValue()));
              } else if ("disapearChance".equalsIgnoreCase(cd.getNodeName())) {
                cw.setDisapearChance(Integer.parseInt(cd.getAttributes().getNamedItem("val").getNodeValue()));
              } else if ("stageKills".equalsIgnoreCase(cd.getNodeName())) {
                cw.setStageKills(Integer.parseInt(cd.getAttributes().getNamedItem("val").getNodeValue()));
              } else if ("transformationId".equalsIgnoreCase(cd.getNodeName())) {
                cw.setTransformationId(Integer.parseInt(cd.getAttributes().getNamedItem("val").getNodeValue()));
              } else if ("transformationTemplateId".equalsIgnoreCase(cd.getNodeName())) {
                cw.setTransformationTemplateId(Integer.parseInt(cd.getAttributes().getNamedItem("val").getNodeValue()));
              } else if ("transformationName".equalsIgnoreCase(cd.getNodeName())) {
                cw.setTransformationName(cd.getAttributes().getNamedItem("val").getNodeValue());
              }
            }
            _cursedWeaponsMap.put(id, cw);
          }
      }
      _cursedWeapons = ((CursedWeapon[])_cursedWeaponsMap.getValues(new CursedWeapon[_cursedWeaponsMap.size()]));
    }
    catch (Exception e)
    {
      _log.error("CursedWeaponsManager: Error parsing cursed_weapons file. " + e);
    }
  }

  private void restore()
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("SELECT * FROM cursed_weapons");
      rset = statement.executeQuery();

      while (rset.next())
      {
        int itemId = rset.getInt("item_id");
        CursedWeapon cw = (CursedWeapon)_cursedWeaponsMap.get(itemId);
        if (cw != null)
        {
          cw.setPlayerId(rset.getInt("player_id"));
          cw.setPlayerKarma(rset.getInt("player_karma"));
          cw.setPlayerPkKills(rset.getInt("player_pkkills"));
          cw.setNbKills(rset.getInt("nb_kills"));
          cw.setLoc(new Location(rset.getInt("x"), rset.getInt("y"), rset.getInt("z")));
          cw.setEndTime(rset.getLong("end_time") * 1000L);

          if (!cw.reActivate())
            endOfLife(cw);
        }
        else
        {
          removeFromDb(itemId);
          _log.warn("CursedWeaponsManager: Unknown cursed weapon " + itemId + ", deleted");
        }
      }
    }
    catch (Exception e)
    {
      _log.warn("CursedWeaponsManager: Could not restore cursed_weapons data: " + e);
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  private void checkConditions()
  {
    Connection con = null;
    PreparedStatement statement1 = null; PreparedStatement statement2 = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement1 = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=?");
      statement2 = con.prepareStatement("SELECT owner_id FROM items WHERE item_id=?");

      for (CursedWeapon cw : _cursedWeapons)
      {
        int itemId = cw.getItemId();

        int skillId = cw.getSkillId();
        boolean foundedInItems = false;

        statement1.setInt(1, skillId);
        statement1.executeUpdate();

        statement2.setInt(1, itemId);
        rset = statement2.executeQuery();

        while (rset.next())
        {
          int playerId = rset.getInt("owner_id");

          if (!foundedInItems)
          {
            if ((playerId != cw.getPlayerId()) || (cw.getPlayerId() == 0))
            {
              emptyPlayerCursedWeapon(playerId, itemId, cw);
              _log.info("CursedWeaponsManager[254]: Player " + playerId + " owns the cursed weapon " + itemId + " but he shouldn't.");
            }
            else {
              foundedInItems = true;
            }
          }
          else {
            emptyPlayerCursedWeapon(playerId, itemId, cw);
            _log.info("CursedWeaponsManager[262]: Player " + playerId + " owns the cursed weapon " + itemId + " but he shouldn't.");
          }
        }

        if ((foundedInItems) || (cw.getPlayerId() == 0))
          continue;
        removeFromDb(cw.getItemId());

        _log.info("CursedWeaponsManager: Unownered weapon, removing from table...");
      }

    }
    catch (Exception e) {
      _log.warn("CursedWeaponsManager: Could not check cursed_weapons data: " + e);
      return;
    }
    finally {
      DbUtils.closeQuietly(statement1);
      DbUtils.closeQuietly(con, statement2, rset);
    }
  }

  private void emptyPlayerCursedWeapon(int playerId, int itemId, CursedWeapon cw)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
      statement.setInt(1, playerId);
      statement.setInt(2, itemId);
      statement.executeUpdate();
      DbUtils.close(statement);

      statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE obj_id=?");
      statement.setInt(1, cw.getPlayerKarma());
      statement.setInt(2, cw.getPlayerPkKills());
      statement.setInt(3, playerId);
      if (statement.executeUpdate() != 1) {
        _log.warn("Error while updating karma & pkkills for userId " + cw.getPlayerId());
      }
      removeFromDb(itemId);
    }
    catch (SQLException e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void removeFromDb(int itemId)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE item_id = ?");
      statement.setInt(1, itemId);
      statement.executeUpdate();

      if (getCursedWeapon(itemId) != null)
        getCursedWeapon(itemId).initWeapon();
    }
    catch (SQLException e)
    {
      _log.error("CursedWeaponsManager: Failed to remove data: " + e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  private void cancelTask()
  {
    if (_removeTask != null)
    {
      _removeTask.cancel(false);
      _removeTask = null;
    }
  }

  public void endOfLife(CursedWeapon cw)
  {
    if (cw.isActivated())
    {
      Player player = cw.getOnlineOwner();
      if (player != null)
      {
        _log.info("CursedWeaponsManager: " + cw.getName() + " being removed online from " + player + ".");

        player.abortAttack(true, true);

        player.setKarma(cw.getPlayerKarma());
        player.setPkKills(cw.getPlayerPkKills());
        player.setCursedWeaponEquippedId(0);
        player.setTransformation(0);
        player.setTransformationName(null);
        player.removeSkill(SkillTable.getInstance().getInfo(cw.getSkillId(), player.getSkillLevel(Integer.valueOf(cw.getSkillId()))), false);
        player.getInventory().destroyItemByItemId(cw.getItemId(), 1L);
        player.broadcastCharInfo();
      }
      else
      {
        _log.info("CursedWeaponsManager: " + cw.getName() + " being removed offline.");

        Connection con = null;
        PreparedStatement statement = null;
        try
        {
          con = DatabaseFactory.getInstance().getConnection();

          statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
          statement.setInt(1, cw.getPlayerId());
          statement.setInt(2, cw.getItemId());
          statement.executeUpdate();
          DbUtils.close(statement);

          statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND skill_id=?");
          statement.setInt(1, cw.getPlayerId());
          statement.setInt(2, cw.getSkillId());
          statement.executeUpdate();
          DbUtils.close(statement);

          statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE obj_Id=?");
          statement.setInt(1, cw.getPlayerKarma());
          statement.setInt(2, cw.getPlayerPkKills());
          statement.setInt(3, cw.getPlayerId());
          statement.executeUpdate();
        }
        catch (SQLException e)
        {
          _log.warn("CursedWeaponsManager: Could not delete : " + e);
        }
        finally
        {
          DbUtils.closeQuietly(con, statement);
        }

      }

    }
    else if ((cw.getPlayer() != null) && (cw.getPlayer().getInventory().getItemByItemId(cw.getItemId()) != null))
    {
      Player player = cw.getPlayer();
      if (!cw.getPlayer().getInventory().destroyItemByItemId(cw.getItemId(), 1L)) {
        _log.info("CursedWeaponsManager[453]: Error! Cursed weapon not found!!!");
      }
      player.sendChanges();
      player.broadcastUserInfo(true);
    }
    else if (cw.getItem() != null)
    {
      cw.getItem().deleteMe();
      cw.getItem().delete();
      _log.info("CursedWeaponsManager: " + cw.getName() + " item has been removed from World.");
    }

    cw.initWeapon();
    removeFromDb(cw.getItemId());

    announce(new SystemMessage(1818).addString(cw.getName()));
  }

  public void saveData(CursedWeapon cw)
  {
    Connection con = null;
    PreparedStatement statement = null;
    synchronized (cw)
    {
      try
      {
        con = DatabaseFactory.getInstance().getConnection();

        statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE item_id = ?");
        statement.setInt(1, cw.getItemId());
        statement.executeUpdate();

        if (cw.isActive())
        {
          DbUtils.close(statement);
          statement = con.prepareStatement("REPLACE INTO cursed_weapons (item_id, player_id, player_karma, player_pkkills, nb_kills, x, y, z, end_time) VALUES (?,?,?,?,?,?,?,?,?)");
          statement.setInt(1, cw.getItemId());
          statement.setInt(2, cw.getPlayerId());
          statement.setInt(3, cw.getPlayerKarma());
          statement.setInt(4, cw.getPlayerPkKills());
          statement.setInt(5, cw.getNbKills());
          statement.setInt(6, cw.getLoc().x);
          statement.setInt(7, cw.getLoc().y);
          statement.setInt(8, cw.getLoc().z);
          statement.setLong(9, cw.getEndTime() / 1000L);
          statement.executeUpdate();
        }
      }
      catch (SQLException e)
      {
        _log.error("CursedWeapon: Failed to save data: " + e);
      }
      finally
      {
        DbUtils.closeQuietly(con, statement);
      }
    }
  }

  public void saveData()
  {
    for (CursedWeapon cw : _cursedWeapons)
      saveData(cw);
  }

  public void checkPlayer(Player player, ItemInstance item)
  {
    if ((player == null) || (item == null) || (player.isInOlympiadMode())) {
      return;
    }
    CursedWeapon cw = (CursedWeapon)_cursedWeaponsMap.get(item.getItemId());
    if (cw == null) {
      return;
    }
    if ((player.getObjectId() == cw.getPlayerId()) || (cw.getPlayerId() == 0) || (cw.isDropped()))
    {
      activate(player, item);
      showUsageTime(player, cw);
    }
    else
    {
      _log.warn("CursedWeaponsManager: " + player + " tried to obtain " + item + " in wrong way");
      player.getInventory().destroyItem(item, item.getCount());
    }
  }

  public void activate(Player player, ItemInstance item)
  {
    if ((player == null) || (player.isInOlympiadMode()))
      return;
    CursedWeapon cw = (CursedWeapon)_cursedWeaponsMap.get(item.getItemId());
    if (cw == null) {
      return;
    }
    if (player.isCursedWeaponEquipped())
    {
      if (player.getCursedWeaponEquippedId() != item.getItemId())
      {
        CursedWeapon cw2 = (CursedWeapon)_cursedWeaponsMap.get(player.getCursedWeaponEquippedId());
        cw2.setNbKills(cw2.getStageKills() - 1);
        cw2.increaseKills();
      }

      endOfLife(cw);
      player.getInventory().destroyItem(item, 1L);
    }
    else if (cw.getTimeLeft() > 0L)
    {
      cw.activate(player, item);
      saveData(cw);
      announce(new SystemMessage(1816).addZoneName(player.getLoc()).addString(cw.getName()));
    }
    else
    {
      endOfLife(cw);
      player.getInventory().destroyItem(item, 1L);
    }
  }

  public void doLogout(Player player)
  {
    for (CursedWeapon cw : _cursedWeapons) {
      if (player.getInventory().getItemByItemId(cw.getItemId()) == null)
        continue;
      cw.setPlayer(null);
      cw.setItem(null);
    }
  }

  public void dropAttackable(NpcInstance attackable, Player killer)
  {
    if ((killer.isInOlympiadMode()) || (killer.isCursedWeaponEquipped()) || (_cursedWeapons.length == 0) || (killer.getReflection() != ReflectionManager.DEFAULT)) {
      return;
    }
    synchronized (_cursedWeapons)
    {
      CursedWeapon[] cursedWeapons = new CursedWeapon[0];
      for (CursedWeapon cw : _cursedWeapons)
      {
        if (cw.isActive())
          continue;
        cursedWeapons = (CursedWeapon[])(CursedWeapon[])ArrayUtils.add(cursedWeapons, cw);
      }

      if (cursedWeapons.length > 0)
      {
        CursedWeapon cw = cursedWeapons[Rnd.get(cursedWeapons.length)];
        if (Rnd.get(100000000) <= cw.getDropRate())
          cw.create(attackable, killer);
      }
    }
  }

  public void dropPlayer(Player player)
  {
    CursedWeapon cw = (CursedWeapon)_cursedWeaponsMap.get(player.getCursedWeaponEquippedId());
    if (cw == null) {
      return;
    }
    if (cw.dropIt(null, null, player))
    {
      saveData(cw);
      announce(new SystemMessage(1815).addZoneName(player.getLoc()).addItemName(cw.getItemId()));
    }
    else {
      endOfLife(cw);
    }
  }

  public void increaseKills(int itemId) {
    CursedWeapon cw = (CursedWeapon)_cursedWeaponsMap.get(itemId);
    if (cw != null)
    {
      cw.increaseKills();
      saveData(cw);
    }
  }

  public int getLevel(int itemId)
  {
    CursedWeapon cw = (CursedWeapon)_cursedWeaponsMap.get(itemId);
    return cw != null ? cw.getLevel() : 0;
  }

  public void announce(SystemMessage sm)
  {
    for (Player player : GameObjectsStorage.getAllPlayersForIterate())
      player.sendPacket(sm);
  }

  public void showUsageTime(Player player, int itemId)
  {
    CursedWeapon cw = (CursedWeapon)_cursedWeaponsMap.get(itemId);
    if (cw != null)
      showUsageTime(player, cw);
  }

  public void showUsageTime(Player player, CursedWeapon cw)
  {
    SystemMessage sm = new SystemMessage(1814);
    sm.addString(cw.getName());
    sm.addNumber(new Long(cw.getTimeLeft() / 60000L).intValue());
    player.sendPacket(sm);
  }

  public boolean isCursed(int itemId)
  {
    return _cursedWeaponsMap.containsKey(itemId);
  }

  public CursedWeapon[] getCursedWeapons()
  {
    return _cursedWeapons;
  }

  public int[] getCursedWeaponsIds()
  {
    return _cursedWeaponsMap.keys();
  }

  public CursedWeapon getCursedWeapon(int itemId)
  {
    return (CursedWeapon)_cursedWeaponsMap.get(itemId);
  }

  private class RemoveTask extends RunnableImpl
  {
    private RemoveTask()
    {
    }

    public void runImpl()
      throws Exception
    {
      for (CursedWeapon cw : _cursedWeapons)
        if ((cw.isActive()) && (cw.getTimeLeft() <= 0L))
          endOfLife(cw);
    }
  }
}