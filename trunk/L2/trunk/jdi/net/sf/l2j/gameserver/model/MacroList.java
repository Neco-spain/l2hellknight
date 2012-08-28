package net.sf.l2j.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.SendMacroList;

public class MacroList
{
  private static Logger _log = Logger.getLogger(MacroList.class.getName());
  private L2PcInstance _owner;
  private int _revision;
  private int _macroId;
  private Map<Integer, L2Macro> _macroses = new FastMap();

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
    L2Macro toRemove = (L2Macro)_macroses.get(Integer.valueOf(id));
    if (toRemove != null)
    {
      deleteMacroFromDb(toRemove);
    }
    _macroses.remove(Integer.valueOf(id));

    L2ShortCut[] allShortCuts = _owner.getAllShortCuts();
    for (L2ShortCut sc : allShortCuts) {
      if ((sc.getId() == id) && (sc.getType() == 4)) {
        _owner.deleteShortCut(sc.getSlot(), sc.getPage());
      }
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
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("INSERT INTO character_macroses (char_obj_id,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)");
      statement.setInt(1, _owner.getObjectId());
      statement.setInt(2, macro.id);
      statement.setInt(3, macro.icon);
      statement.setString(4, macro.name);
      statement.setString(5, macro.descr);
      statement.setString(6, macro.acronym);
      TextBuilder sb = new TextBuilder();
      for (L2Macro.L2MacroCmd cmd : macro.commands) {
        sb.append(cmd.type).append(',');
        sb.append(cmd.d1).append(',');
        sb.append(cmd.d2);
        if ((cmd.cmd != null) && (cmd.cmd.length() > 0))
          sb.append(',').append(cmd.cmd);
        sb.append(';');
      }
      statement.setString(7, sb.toString());
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not store macro:", e);
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

  private void deleteMacroFromDb(L2Macro macro) {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=? AND id=?");
      statement.setInt(1, _owner.getObjectId());
      statement.setInt(2, macro.id);
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not delete macro:", e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public void restore() {
    _macroses.clear();
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT char_obj_id, id, icon, name, descr, acronym, commands FROM character_macroses WHERE char_obj_id=?");
      statement.setInt(1, _owner.getObjectId());
      ResultSet rset = statement.executeQuery();
      while (rset.next())
      {
        int id = rset.getInt("id");
        int icon = rset.getInt("icon");
        String name = rset.getString("name");
        String descr = rset.getString("descr");
        String acronym = rset.getString("acronym");
        List commands = new FastList();
        StringTokenizer st1 = new StringTokenizer(rset.getString("commands"), ";");
        while (st1.hasMoreTokens()) {
          StringTokenizer st = new StringTokenizer(st1.nextToken(), ",");
          if (st.countTokens() < 3)
            continue;
          int type = Integer.parseInt(st.nextToken());
          int d1 = Integer.parseInt(st.nextToken());
          int d2 = Integer.parseInt(st.nextToken());
          String cmd = "";
          if (st.hasMoreTokens())
            cmd = st.nextToken();
          L2Macro.L2MacroCmd mcmd = new L2Macro.L2MacroCmd(commands.size(), type, d1, d2, cmd);
          commands.add(mcmd);
        }

        L2Macro m = new L2Macro(id, icon, name, descr, acronym, (L2Macro.L2MacroCmd[])commands.toArray(new L2Macro.L2MacroCmd[commands.size()]));
        _macroses.put(Integer.valueOf(m.id), m);
      }
      rset.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not store shortcuts:", e);
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
}