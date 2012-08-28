package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.SendMacroList;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;

public class MacroList
{
  private static Logger _log = AbstractLogger.getLogger(MacroList.class.getName());
  private L2PcInstance _owner;
  private int _revision;
  private int _macroId;
  private Map<Integer, L2Macro> _macroses = new FastMap();

  private Map<Integer, L2Macro> _toSave = new ConcurrentHashMap();
  private ConcurrentLinkedQueue<Integer> _toRemove = new ConcurrentLinkedQueue();

  public MacroList(L2PcInstance owner)
  {
    _owner = owner;
    _revision = 1;
    _macroId = 1000;
  }

  public int getRevision() {
    return _revision;
  }

  public L2Macro[] getAllMacroses()
  {
    return (L2Macro[])_macroses.values().toArray(new L2Macro[_macroses.size()]);
  }

  public L2Macro getMacro(int id)
  {
    return (L2Macro)_macroses.get(Integer.valueOf(id - 1));
  }

  public void registerMacro(L2Macro macro)
  {
    if (macro.id == 0) {
      macro.id = (_macroId++);
      while (_macroses.get(Integer.valueOf(macro.id)) != null)
        macro.id = (_macroId++);
      _macroses.put(Integer.valueOf(macro.id), macro);
      registerMacroInDb(macro);
    } else {
      L2Macro old = (L2Macro)_macroses.put(Integer.valueOf(macro.id), macro);
      if (old != null)
        deleteMacroFromDb(old);
      registerMacroInDb(macro);
    }
    sendUpdate();
  }

  public void deleteMacro(int id)
  {
    L2Macro macro = (L2Macro)_macroses.get(Integer.valueOf(id));
    if (macro != null)
    {
      deleteMacroFromDb(macro);
      _owner.sendMessage("\u041C\u0430\u043A\u0440\u043E\u0441 " + macro.name + " \u0443\u0434\u0430\u043B\u0435\u043D");
    }
    _macroses.remove(Integer.valueOf(id));

    for (L2ShortCut sc : _owner.getAllShortCuts())
    {
      if (sc == null) {
        continue;
      }
      if ((sc.getId() == id) && (sc.getType() == 4))
        _owner.deleteShortCut(sc.getSlot(), sc.getPage());
    }
    sendUpdate();
  }

  public void sendUpdate() {
    _revision += 1;
    L2Macro[] all = getAllMacroses();
    if (all.length == 0)
      _owner.sendPacket(new SendMacroList(_revision, all.length, null));
    else
      for (L2Macro m : all)
        _owner.sendPacket(new SendMacroList(_revision, all.length, m));
  }

  private void registerMacroInDb(L2Macro macro)
  {
    _toSave.put(Integer.valueOf(macro.id), macro);
  }

  private void deleteMacroFromDb(L2Macro macro)
  {
    _toRemove.add(Integer.valueOf(macro.id));
  }

  public void restore(Connect con)
  {
    _macroses.clear();

    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      st = con.prepareStatement("SELECT char_obj_id, id, icon, name, descr, acronym, commands FROM character_macroses WHERE char_obj_id=?");
      st.setInt(1, _owner.getObjectId());
      rs = st.executeQuery();
      while (rs.next())
      {
        int id = rs.getInt("id");
        int icon = rs.getInt("icon");
        String name = rs.getString("name");
        String descr = rs.getString("descr");
        String acronym = rs.getString("acronym");
        List commands = new FastList();
        StringTokenizer st1 = new StringTokenizer(rs.getString("commands"), ";");
        while (st1.hasMoreTokens())
        {
          StringTokenizer stk = new StringTokenizer(st1.nextToken(), ",");
          if (stk.countTokens() < 3)
            continue;
          int type = Integer.parseInt(stk.nextToken());
          int d1 = Integer.parseInt(stk.nextToken());
          int d2 = Integer.parseInt(stk.nextToken());
          String cmd = "";
          if (stk.hasMoreTokens()) {
            cmd = stk.nextToken();
          }
          commands.add(new L2Macro.L2MacroCmd(commands.size(), type, d1, d2, cmd));
        }

        _macroses.put(Integer.valueOf(id), new L2Macro(id, icon, name, descr, acronym, (L2Macro.L2MacroCmd[])commands.toArray(new L2Macro.L2MacroCmd[commands.size()])));
      }
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not restore character_macroses:", e);
    }
    finally
    {
      Close.SR(st, rs);
    }
  }

  public void store(Connect con)
  {
    ResultSet rs = null;
    PreparedStatement st = null;
    TextBuilder tb = new TextBuilder();
    try
    {
      con.setAutoCommit(false);

      st = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=? AND id=?");
      for (Integer macro_id : _toRemove)
      {
        st.setInt(1, _owner.getObjectId());
        st.setInt(2, macro_id.intValue());
        st.addBatch();
      }
      st.executeBatch();
      Close.S(st);

      st = con.prepareStatement("REPLACE INTO character_macroses (char_obj_id,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)");
      for (L2Macro macro : _toSave.values())
      {
        st.setInt(1, _owner.getObjectId());
        st.setInt(2, macro.id);
        st.setInt(3, macro.icon);
        st.setString(4, macro.name);
        st.setString(5, macro.descr);
        st.setString(6, macro.acronym);
        for (L2Macro.L2MacroCmd cmd : macro.commands)
        {
          tb.append(cmd.type).append(',');
          tb.append(cmd.d1).append(',');
          tb.append(cmd.d2);
          if ((cmd.cmd != null) && (cmd.cmd.length() > 0))
            tb.append(',').append(cmd.cmd);
          tb.append(';');
        }
        String lenta = tb.toString();
        if (lenta.length() > 255)
          lenta = lenta.substring(255);
        st.setString(7, lenta);
        st.addBatch();
        tb.clear();
      }
      st.executeBatch();
      con.commit();
      con.setAutoCommit(true);
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not store character_macroses:", e);
    }
    finally
    {
      Close.SR(st, rs);
      tb.clear();
      tb = null;
    }
  }
}