package net.sf.l2j.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.templates.L2HelperBuff;
import net.sf.l2j.gameserver.templates.StatsSet;

public class HelperBuffTable
{
  private static Logger _log = Logger.getLogger(HennaTable.class.getName());
  private static HelperBuffTable _instance;
  private List<L2HelperBuff> _helperBuff;
  private boolean _initialized = true;

  private int _magicClassLowestLevel = 100;
  private int _physicClassLowestLevel = 100;

  private int _magicClassHighestLevel = 1;
  private int _physicClassHighestLevel = 1;

  public static HelperBuffTable getInstance()
  {
    if (_instance == null)
    {
      _instance = new HelperBuffTable();
    }
    return _instance;
  }

  private HelperBuffTable()
  {
    _helperBuff = new FastList();
    restoreHelperBuffData();
  }

  private void restoreHelperBuffData()
  {
    Connection con = null;
    try
    {
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("SELECT * FROM helper_buff_list");
        ResultSet helperbuffdata = statement.executeQuery();

        fillHelperBuffTable(helperbuffdata);
        helperbuffdata.close();
        statement.close();
      }
      catch (Exception e)
      {
        _log.severe("Table helper_buff_list not found : Update your DataPack" + e);
        e.printStackTrace();
      }
    }
    finally
    {
      try {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  private void fillHelperBuffTable(ResultSet HelperBuffData)
    throws Exception
  {
    while (HelperBuffData.next())
    {
      StatsSet helperBuffDat = new StatsSet();
      int id = HelperBuffData.getInt("id");

      helperBuffDat.set("id", id);
      helperBuffDat.set("skillID", HelperBuffData.getInt("skill_id"));
      helperBuffDat.set("skillLevel", HelperBuffData.getInt("skill_level"));
      helperBuffDat.set("lowerLevel", HelperBuffData.getInt("lower_level"));
      helperBuffDat.set("upperLevel", HelperBuffData.getInt("upper_level"));
      helperBuffDat.set("isMagicClass", HelperBuffData.getString("is_magic_class"));

      if ("false".equals(HelperBuffData.getString("is_magic_class")))
      {
        if (HelperBuffData.getInt("lower_level") < _physicClassLowestLevel) {
          _physicClassLowestLevel = HelperBuffData.getInt("lower_level");
        }
        if (HelperBuffData.getInt("upper_level") > _physicClassHighestLevel)
          _physicClassHighestLevel = HelperBuffData.getInt("upper_level");
      }
      else
      {
        if (HelperBuffData.getInt("lower_level") < _magicClassLowestLevel) {
          _magicClassLowestLevel = HelperBuffData.getInt("lower_level");
        }
        if (HelperBuffData.getInt("upper_level") > _magicClassHighestLevel) {
          _magicClassHighestLevel = HelperBuffData.getInt("upper_level");
        }
      }

      L2HelperBuff template = new L2HelperBuff(helperBuffDat);
      _helperBuff.add(template);
    }

    _log.config("Helper Buff Table: Loaded " + _helperBuff.size() + " Templates.");
  }

  public boolean isInitialized()
  {
    return _initialized;
  }

  public L2HelperBuff getHelperBuffTableItem(int id)
  {
    return (L2HelperBuff)_helperBuff.get(id);
  }

  public List<L2HelperBuff> getHelperBuffTable()
  {
    return _helperBuff;
  }

  public int getMagicClassHighestLevel()
  {
    return _magicClassHighestLevel;
  }

  public void setMagicClassHighestLevel(int magicClassHighestLevel)
  {
    _magicClassHighestLevel = magicClassHighestLevel;
  }

  public int getMagicClassLowestLevel()
  {
    return _magicClassLowestLevel;
  }

  public void setMagicClassLowestLevel(int magicClassLowestLevel)
  {
    _magicClassLowestLevel = magicClassLowestLevel;
  }

  public int getPhysicClassHighestLevel()
  {
    return _physicClassHighestLevel;
  }

  public void setPhysicClassHighestLevel(int physicClassHighestLevel)
  {
    _physicClassHighestLevel = physicClassHighestLevel;
  }

  public int getPhysicClassLowestLevel()
  {
    return _physicClassLowestLevel;
  }

  public void setPhysicClassLowestLevel(int physicClassLowestLevel)
  {
    _physicClassLowestLevel = physicClassLowestLevel;
  }
}