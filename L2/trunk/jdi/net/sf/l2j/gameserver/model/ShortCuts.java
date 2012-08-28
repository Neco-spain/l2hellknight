package net.sf.l2j.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.templates.L2EtcItemType;

public class ShortCuts
{
  private static Logger _log = Logger.getLogger(ShortCuts.class.getName());
  private L2PcInstance _owner;
  private Map<Integer, L2ShortCut> _shortCuts = new TreeMap();

  public ShortCuts(L2PcInstance owner)
  {
    _owner = owner;
  }

  public L2ShortCut[] getAllShortCuts()
  {
    return (L2ShortCut[])_shortCuts.values().toArray(new L2ShortCut[_shortCuts.values().size()]);
  }

  public L2ShortCut getShortCut(int slot, int page)
  {
    L2ShortCut sc = (L2ShortCut)_shortCuts.get(Integer.valueOf(slot + page * 12));

    if ((sc != null) && (sc.getType() == 1))
    {
      if (_owner.getInventory().getItemByObjectId(sc.getId()) == null)
      {
        deleteShortCut(sc.getSlot(), sc.getPage());
        sc = null;
      }
    }

    return sc;
  }

  public synchronized void registerShortCut(L2ShortCut shortcut)
  {
    L2ShortCut oldShortCut = (L2ShortCut)_shortCuts.put(Integer.valueOf(shortcut.getSlot() + 12 * shortcut.getPage()), shortcut);
    registerShortCutInDb(shortcut, oldShortCut);
  }

  private void registerShortCutInDb(L2ShortCut shortcut, L2ShortCut oldShortCut)
  {
    if (oldShortCut != null) {
      deleteShortCutFromDb(oldShortCut);
    }
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("REPLACE INTO character_shortcuts (char_obj_id,slot,page,type,shortcut_id,level,class_index) values(?,?,?,?,?,?,?)");
      statement.setInt(1, _owner.getObjectId());
      statement.setInt(2, shortcut.getSlot());
      statement.setInt(3, shortcut.getPage());
      statement.setInt(4, shortcut.getType());
      statement.setInt(5, shortcut.getId());
      statement.setInt(6, shortcut.getLevel());
      statement.setInt(7, _owner.getClassIndex());
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Could not store character shortcut: " + e);
    }
    finally {
      try {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public synchronized void deleteShortCut(int slot, int page) {
    L2ShortCut old = (L2ShortCut)_shortCuts.remove(Integer.valueOf(slot + page * 12));

    if ((old == null) || (_owner == null))
      return;
    deleteShortCutFromDb(old);
    if (old.getType() == 1)
    {
      L2ItemInstance item = _owner.getInventory().getItemByObjectId(old.getId());

      if ((item != null) && (item.getItemType() == L2EtcItemType.SHOT))
      {
        _owner.removeAutoSoulShot(item.getItemId());
        _owner.sendPacket(new ExAutoSoulShot(item.getItemId(), 0));
      }
    }

    _owner.sendPacket(new ShortCutInit(_owner));

    for (Iterator i$ = _owner.getAutoSoulShot().values().iterator(); i$.hasNext(); ) { int shotId = ((Integer)i$.next()).intValue();
      _owner.sendPacket(new ExAutoSoulShot(shotId, 1)); }
  }

  public synchronized void deleteShortCutByObjectId(int objectId)
  {
    L2ShortCut toRemove = null;

    for (L2ShortCut shortcut : _shortCuts.values())
    {
      if ((shortcut.getType() == 1) && (shortcut.getId() == objectId))
      {
        toRemove = shortcut;
        break;
      }
    }

    if (toRemove != null)
      deleteShortCut(toRemove.getSlot(), toRemove.getPage());
  }

  private void deleteShortCutFromDb(L2ShortCut shortcut)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=? AND slot=? AND page=? AND class_index=?");
      statement.setInt(1, _owner.getObjectId());
      statement.setInt(2, shortcut.getSlot());
      statement.setInt(3, shortcut.getPage());
      statement.setInt(4, _owner.getClassIndex());
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Could not delete character shortcut: " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public void restore() {
    _shortCuts.clear();
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT char_obj_id, slot, page, type, shortcut_id, level FROM character_shortcuts WHERE char_obj_id=? AND class_index=?");
      statement.setInt(1, _owner.getObjectId());
      statement.setInt(2, _owner.getClassIndex());

      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        int slot = rset.getInt("slot");
        int page = rset.getInt("page");
        int type = rset.getInt("type");
        int id = rset.getInt("shortcut_id");
        int level = rset.getInt("level");

        L2ShortCut sc = new L2ShortCut(slot, page, type, id, level, 1);
        _shortCuts.put(Integer.valueOf(slot + page * 12), sc);
      }

      rset.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Could not restore character shortcuts: " + e);
    }
    finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
    for (L2ShortCut sc : getAllShortCuts())
    {
      if (sc.getType() != 1)
        continue;
      if (_owner.getInventory().getItemByObjectId(sc.getId()) == null)
        deleteShortCut(sc.getSlot(), sc.getPage());
    }
  }
}