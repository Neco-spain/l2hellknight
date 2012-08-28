package net.sf.l2j.gameserver.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import javolution.util.FastMap;
import javolution.util.FastTable;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.templates.L2Henna;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;

public class HennaTreeTable
{
  private static Logger _log = AbstractLogger.getLogger(HennaTreeTable.class.getName());
  private static HennaTreeTable _instance = new HennaTreeTable();
  private static FastMap<ClassId, FastTable<L2HennaInstance>> _hennaTrees;
  private boolean _initialized = true;

  public static HennaTreeTable getInstance()
  {
    return _instance;
  }

  private HennaTreeTable()
  {
    _hennaTrees = new FastMap().shared("HennaTreeTable._hennaTrees");
    int classId = 0;
    int count = 0;
    Connect con = null;
    PreparedStatement st = null;
    PreparedStatement st2 = null;
    ResultSet rs = null;
    ResultSet rs2 = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT class_name, id, parent_id FROM class_list ORDER BY id");
      rs = st.executeQuery();
      rs.setFetchSize(50);
      FastTable table = new FastTable();

      while (rs.next())
      {
        table = new FastTable();
        classId = rs.getInt("id");
        st2 = con.prepareStatement("SELECT class_id, symbol_id FROM henna_trees where class_id=? ORDER BY symbol_id");
        st2.setInt(1, classId);
        rs2 = st2.executeQuery();

        while (rs2.next())
        {
          int id = rs2.getInt("symbol_id");

          L2Henna template = HennaTable.getInstance().getTemplate(id);
          if (template == null) { Close.SR(st2, rs2);
            Close.SR(st, rs);
            return; }
          L2HennaInstance temp = new L2HennaInstance(template);
          temp.setSymbolId(id);
          temp.setItemIdDye(template.getDyeId());
          temp.setAmountDyeRequire(template.getAmountDyeRequire());
          temp.setPrice(template.getPrice());
          temp.setStatINT(template.getStatINT());
          temp.setStatSTR(template.getStatSTR());
          temp.setStatCON(template.getStatCON());
          temp.setStatMEM(template.getStatMEM());
          temp.setStatDEX(template.getStatDEX());
          temp.setStatWIT(template.getStatWIT());

          table.add(temp);
        }
        _hennaTrees.put(ClassId.values()[classId], table);
        Close.SR(st2, rs2);
        count += table.size();
        _log.fine("Henna Tree for Class: " + classId + " has " + table.size() + " Henna Templates.");
      }

    }
    catch (Exception e)
    {
      _log.warning("error while creating henna tree for classId " + classId + "  " + e);
      e.printStackTrace();
    }
    finally
    {
      Close.SR(st2, rs2);
      Close.CSR(con, st, rs);
    }

    _log.config("Loading HennaTreeTable... total " + count + " Templates.");
  }

  public FastTable<L2HennaInstance> getAvailableHenna(ClassId classId)
  {
    return (FastTable)_hennaTrees.get(classId);
  }

  public boolean isInitialized()
  {
    return _initialized;
  }
}