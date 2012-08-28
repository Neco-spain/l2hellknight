package net.sf.l2j.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.templates.L2Henna;

public class HennaTreeTable
{
  private static Logger _log = Logger.getLogger(HennaTreeTable.class.getName());
  private static final HennaTreeTable _instance = new HennaTreeTable();
  private Map<ClassId, List<L2HennaInstance>> _hennaTrees;
  private boolean _initialized = true;

  public static HennaTreeTable getInstance()
  {
    return _instance;
  }

  private HennaTreeTable()
  {
    _hennaTrees = new FastMap();
    int classId = 0;
    int count = 0;
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT class_name, id, parent_id FROM class_list ORDER BY id");
      ResultSet classlist = statement.executeQuery();

      while (classlist.next())
      {
        List list = new FastList();
        classId = classlist.getInt("id");
        PreparedStatement statement2 = con.prepareStatement("SELECT class_id, symbol_id FROM henna_trees where class_id=? ORDER BY symbol_id");
        statement2.setInt(1, classId);
        ResultSet hennatree = statement2.executeQuery();

        while (hennatree.next())
        {
          int id = hennatree.getInt("symbol_id");

          L2Henna template = HennaTable.getInstance().getTemplate(id);
          if (template == null) { hennatree.close();
            statement2.close();
            classlist.close();
            statement.close();
            return; } L2HennaInstance temp = new L2HennaInstance(template);
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

          list.add(temp);
        }
        _hennaTrees.put(ClassId.values()[classId], list);
        hennatree.close();
        statement2.close();
        count += list.size();
        _log.fine("Henna Tree for Class: " + classId + " has " + list.size() + " Henna Templates.");
      }

      classlist.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("error while creating henna tree for classId " + classId + "  " + e);
      e.printStackTrace();
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    _log.config("HennaTreeTable: Loaded " + count + " Henna Tree Templates.");
  }

  public L2HennaInstance[] getAvailableHenna(ClassId classId)
  {
    List result = new FastList();
    List henna = (List)_hennaTrees.get(classId);
    if (henna == null)
    {
      _log.warning("Hennatree for class " + classId + " is not defined !");
      return new L2HennaInstance[0];
    }

    for (int i = 0; i < henna.size(); i++)
    {
      L2HennaInstance temp = (L2HennaInstance)henna.get(i);
      result.add(temp);
    }

    return (L2HennaInstance[])result.toArray(new L2HennaInstance[result.size()]);
  }

  public boolean isInitialized()
  {
    return _initialized;
  }
}