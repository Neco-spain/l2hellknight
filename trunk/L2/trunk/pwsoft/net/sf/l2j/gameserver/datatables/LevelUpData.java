package net.sf.l2j.gameserver.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2LvlupData;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;

public class LevelUpData
{
  private static String CLASS_LVL = "class_lvl";
  private static String MP_MOD = "defaultmpmod";
  private static String MP_ADD = "defaultmpadd";
  private static String MP_BASE = "defaultmpbase";
  private static String HP_MOD = "defaulthpmod";
  private static String HP_ADD = "defaulthpadd";
  private static String HP_BASE = "defaulthpbase";
  private static String CP_MOD = "defaultcpmod";
  private static String CP_ADD = "defaultcpadd";
  private static String CP_BASE = "defaultcpbase";
  private static String CLASS_ID = "classid";

  private static Logger _log = AbstractLogger.getLogger(LevelUpData.class.getName());
  private static LevelUpData _instance;
  private static FastMap<Integer, L2LvlupData> _lvlTable = new FastMap().shared("LevelUpData._lvlTable");

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
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT classid, defaulthpbase, defaulthpadd, defaulthpmod, defaultcpbase, defaultcpadd, defaultcpmod, defaultmpbase, defaultmpadd, defaultmpmod, class_lvl FROM lvlupgain");
      rs = st.executeQuery();
      rs.setFetchSize(50);

      while (rs.next())
      {
        L2LvlupData lvlDat = new L2LvlupData();
        lvlDat.setClassid(rs.getInt(CLASS_ID));
        lvlDat.setClassLvl(rs.getInt(CLASS_LVL));
        lvlDat.setClassHpBase(rs.getFloat(HP_BASE));
        lvlDat.setClassHpAdd(rs.getFloat(HP_ADD));
        lvlDat.setClassHpModifier(rs.getFloat(HP_MOD));
        lvlDat.setClassCpBase(rs.getFloat(CP_BASE));
        lvlDat.setClassCpAdd(rs.getFloat(CP_ADD));
        lvlDat.setClassCpModifier(rs.getFloat(CP_MOD));
        lvlDat.setClassMpBase(rs.getFloat(MP_BASE));
        lvlDat.setClassMpAdd(rs.getFloat(MP_ADD));
        lvlDat.setClassMpModifier(rs.getFloat(MP_MOD));

        _lvlTable.put(Integer.valueOf(lvlDat.getClassid()), lvlDat);
      }
      _log.config("Loading LevelUpData... total " + _lvlTable.size() + " Templates.");
    }
    catch (Exception e)
    {
      _log.warning("error while creating Lvl up data table " + e);
    }
    finally
    {
      Close.CSR(con, st, rs);
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