package net.sf.l2j.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2LvlupData;
import net.sf.l2j.gameserver.model.base.ClassId;

public class LevelUpData
{
  private static final String SELECT_ALL = "SELECT classid, defaulthpbase, defaulthpadd, defaulthpmod, defaultcpbase, defaultcpadd, defaultcpmod, defaultmpbase, defaultmpadd, defaultmpmod, class_lvl FROM lvlupgain";
  private static final String CLASS_LVL = "class_lvl";
  private static final String MP_MOD = "defaultmpmod";
  private static final String MP_ADD = "defaultmpadd";
  private static final String MP_BASE = "defaultmpbase";
  private static final String HP_MOD = "defaulthpmod";
  private static final String HP_ADD = "defaulthpadd";
  private static final String HP_BASE = "defaulthpbase";
  private static final String CP_MOD = "defaultcpmod";
  private static final String CP_ADD = "defaultcpadd";
  private static final String CP_BASE = "defaultcpbase";
  private static final String CLASS_ID = "classid";
  private static Logger _log = Logger.getLogger(LevelUpData.class.getName());
  private static LevelUpData _instance;
  private Map<Integer, L2LvlupData> _lvlTable;

  public static LevelUpData getInstance()
  {
    if (_instance == null)
    {
      _instance = new LevelUpData();
    }
    return _instance;
  }

  private LevelUpData()
  {
    _lvlTable = new FastMap();
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT classid, defaulthpbase, defaulthpadd, defaulthpmod, defaultcpbase, defaultcpadd, defaultcpmod, defaultmpbase, defaultmpadd, defaultmpmod, class_lvl FROM lvlupgain");
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        L2LvlupData lvlDat = new L2LvlupData();
        lvlDat.setClassid(rset.getInt("classid"));
        lvlDat.setClassLvl(rset.getInt("class_lvl"));
        lvlDat.setClassHpBase(rset.getFloat("defaulthpbase"));
        lvlDat.setClassHpAdd(rset.getFloat("defaulthpadd"));
        lvlDat.setClassHpModifier(rset.getFloat("defaulthpmod"));
        lvlDat.setClassCpBase(rset.getFloat("defaultcpbase"));
        lvlDat.setClassCpAdd(rset.getFloat("defaultcpadd"));
        lvlDat.setClassCpModifier(rset.getFloat("defaultcpmod"));
        lvlDat.setClassMpBase(rset.getFloat("defaultmpbase"));
        lvlDat.setClassMpAdd(rset.getFloat("defaultmpadd"));
        lvlDat.setClassMpModifier(rset.getFloat("defaultmpmod"));

        _lvlTable.put(new Integer(lvlDat.getClassid()), lvlDat);
      }

      rset.close();
      statement.close();

      _log.config("LevelUpData: Loaded " + _lvlTable.size() + " Character Level Up Templates.");
    }
    catch (Exception e)
    {
      _log.warning("error while creating Lvl up data table " + e);
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

  public L2LvlupData getTemplate(int classId)
  {
    return (L2LvlupData)_lvlTable.get(Integer.valueOf(classId));
  }

  public L2LvlupData getTemplate(ClassId classId) {
    return (L2LvlupData)_lvlTable.get(Integer.valueOf(classId.getId()));
  }
}