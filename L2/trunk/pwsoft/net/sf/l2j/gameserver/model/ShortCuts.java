package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.templates.L2EtcItemType;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;

public class ShortCuts
{
  private static Logger _log = AbstractLogger.getLogger(ShortCuts.class.getName());
  private L2PcInstance _owner;
  private Map<Integer, L2ShortCut> _shortCuts = new ConcurrentHashMap();

  public ShortCuts(L2PcInstance owner)
  {
    _owner = owner;
  }

  public FastTable<L2ShortCut> getAllShortCuts()
  {
    FastTable sc = new FastTable();
    for (L2ShortCut shc : _shortCuts.values())
    {
      if (shc == null)
        continue;
      sc.add(shc);
    }

    return sc;
  }

  public L2ShortCut getShortCut(int slot, int page)
  {
    L2ShortCut sc = (L2ShortCut)_shortCuts.get(Integer.valueOf(slot + page * 12));
    if (sc == null) {
      return null;
    }
    if (sc.getType() == 1)
    {
      if (_owner.getInventory().getItemByObjectId(sc.getId()) == null)
      {
        deleteShortCut(sc.getSlot(), sc.getPage());
        sc = null;
      }
    }
    return sc;
  }

  public void registerShortCut(L2ShortCut shortcut)
  {
    _shortCuts.put(Integer.valueOf(shortcut.getSlot() + 12 * shortcut.getPage()), shortcut);
  }

  public void deleteShortCut(int slot, int page)
  {
    if (_owner == null) {
      return;
    }
    L2ShortCut old = (L2ShortCut)_shortCuts.remove(Integer.valueOf(slot + page * 12));
    if (old == null) {
      return;
    }

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

  public void deleteShortCutByObjectId(int objectId)
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

  public void restore(Connect con)
  {
    _shortCuts.clear();

    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      st = con.prepareStatement("SELECT char_obj_id, slot, page, type, shortcut_id, level FROM character_shortcuts WHERE char_obj_id=? AND class_index=?");
      st.setInt(1, _owner.getObjectId());
      st.setInt(2, _owner.getClassIndex());
      rs = st.executeQuery();
      while (rs.next())
      {
        int slot = rs.getInt("slot");
        int page = rs.getInt("page");
        int type = rs.getInt("type");
        int id = rs.getInt("shortcut_id");
        int level = rs.getInt("level");

        _shortCuts.put(Integer.valueOf(slot + page * 12), new L2ShortCut(slot, page, type, id, level, 1));
      }
    }
    catch (Exception e)
    {
      _log.warning("Could not restore character shortcuts: " + e);
    }
    finally
    {
      Close.SR(st, rs);
    }

    for (L2ShortCut sc : _shortCuts.values())
    {
      if (sc == null) {
        continue;
      }
      if (sc.getType() == 1)
      {
        if (_owner.getInventory().getItemByObjectId(sc.getId()) == null)
          deleteShortCut(sc.getSlot(), sc.getPage());
      }
    }
  }

  public void store(Connect con)
  {
    ResultSet rs = null;
    PreparedStatement st = null;
    try
    {
      con.setAutoCommit(false);

      st = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?");
      st.setInt(1, _owner.getObjectId());
      st.setInt(2, _owner.getClassIndex());
      st.execute();
      Close.S(st);

      st = con.prepareStatement("REPLACE INTO character_shortcuts (char_obj_id,slot,page,type,shortcut_id,level,class_index) values(?,?,?,?,?,?,?)");
      for (L2ShortCut sc : _shortCuts.values())
      {
        st.setInt(1, _owner.getObjectId());
        st.setInt(2, sc.getSlot());
        st.setInt(3, sc.getPage());
        st.setInt(4, sc.getType());
        st.setInt(5, sc.getId());
        st.setInt(6, sc.getLevel());
        st.setInt(7, _owner.getClassIndex());
        st.addBatch();
      }
      st.executeBatch();
      con.commit();
      con.setAutoCommit(true);
    }
    catch (Exception e)
    {
      _log.warning("Could not store character shortcuts: " + e);
    }
    finally
    {
      Close.SR(st, rs);
    }
  }
}