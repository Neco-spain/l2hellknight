package net.sf.l2j.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.templates.L2Henna;
import net.sf.l2j.gameserver.templates.StatsSet;

public class HennaTable
{
  private static Logger _log = Logger.getLogger(HennaTable.class.getName());
  private static HennaTable _instance;
  private Map<Integer, L2Henna> _henna;
  private boolean _initialized = true;

  public static HennaTable getInstance()
  {
    if (_instance == null)
    {
      _instance = new HennaTable();
    }
    return _instance;
  }

  private HennaTable()
  {
    _henna = new FastMap();
    restoreHennaData();
  }

  private void restoreHennaData()
  {
    Connection con = null;
    try
    {
      try {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("SELECT symbol_id, symbol_name, dye_id, dye_amount, price, stat_INT, stat_STR, stat_CON, stat_MEM, stat_DEX, stat_WIT FROM henna");
        ResultSet hennadata = statement.executeQuery();

        fillHennaTable(hennadata);
        hennadata.close();
        statement.close();
      } catch (Exception e) {
        _log.severe("error while creating henna table " + e);
        e.printStackTrace();
      }
    } finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  private void fillHennaTable(ResultSet HennaData) throws Exception {
    while (HennaData.next())
    {
      StatsSet hennaDat = new StatsSet();
      int id = HennaData.getInt("symbol_id");

      hennaDat.set("symbol_id", id);

      hennaDat.set("dye", HennaData.getInt("dye_id"));
      hennaDat.set("price", HennaData.getInt("price"));

      hennaDat.set("amount", HennaData.getInt("dye_amount"));
      hennaDat.set("stat_INT", HennaData.getInt("stat_INT"));
      hennaDat.set("stat_STR", HennaData.getInt("stat_STR"));
      hennaDat.set("stat_CON", HennaData.getInt("stat_CON"));
      hennaDat.set("stat_MEM", HennaData.getInt("stat_MEM"));
      hennaDat.set("stat_DEX", HennaData.getInt("stat_DEX"));
      hennaDat.set("stat_WIT", HennaData.getInt("stat_WIT"));

      L2Henna template = new L2Henna(hennaDat);
      _henna.put(Integer.valueOf(id), template);
    }
    _log.config("HennaTable: Loaded " + _henna.size() + " Templates.");
  }

  public boolean isInitialized()
  {
    return _initialized;
  }

  public L2Henna getTemplate(int id)
  {
    return (L2Henna)_henna.get(Integer.valueOf(id));
  }
}