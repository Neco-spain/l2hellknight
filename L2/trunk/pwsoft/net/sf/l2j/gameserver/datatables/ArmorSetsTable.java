package net.sf.l2j.gameserver.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2ArmorSet;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;

public class ArmorSetsTable
{
  private static Logger _log = AbstractLogger.getLogger(ArmorSetsTable.class.getName());
  private static ArmorSetsTable _instance;
  private static FastMap<Integer, L2ArmorSet> _armorSets = new FastMap().shared("ArmorSetsTable._armorSets");

  public static ArmorSetsTable getInstance()
  {
    if (_instance == null)
      _instance = new ArmorSetsTable();
    return _instance;
  }

  private ArmorSetsTable()
  {
    loadData();
  }

  private void loadData() {
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id, enchant6skill FROM armorsets");
      rs = st.executeQuery();
      rs.setFetchSize(50);

      while (rs.next())
      {
        int chest = rs.getInt("chest");
        int legs = rs.getInt("legs");
        int head = rs.getInt("head");
        int gloves = rs.getInt("gloves");
        int feet = rs.getInt("feet");
        int skill_id = rs.getInt("skill_id");
        int shield = rs.getInt("shield");
        int shield_skill_id = rs.getInt("shield_skill_id");
        int enchant6skill = rs.getInt("enchant6skill");
        _armorSets.put(Integer.valueOf(chest), new L2ArmorSet(chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id, enchant6skill));
      }

      _log.config("Loading ArmorSetsTable... total " + _armorSets.size() + " armor sets.");
    }
    catch (Exception e)
    {
      _log.severe("ArmorSetsTable: Error reading ArmorSets table: " + e);
    }
    finally
    {
      Close.CSR(con, st, rs);
    }
  }

  public boolean setExists(int chestId) {
    return _armorSets.containsKey(Integer.valueOf(chestId));
  }

  public L2ArmorSet getSet(int chestId) {
    return (L2ArmorSet)_armorSets.get(Integer.valueOf(chestId));
  }
}