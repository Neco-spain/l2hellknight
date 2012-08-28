package l2p.gameserver.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.model.LvlupData;
import l2p.gameserver.model.base.ClassId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelUpTable
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
  private static final Logger _log = LoggerFactory.getLogger(LevelUpTable.class);
  private static LevelUpTable _instance;
  private Map<Integer, LvlupData> _lvltable;

  public static LevelUpTable getInstance()
  {
    if (_instance == null)
      _instance = new LevelUpTable();
    return _instance;
  }

  private LevelUpTable()
  {
    _lvltable = new HashMap();

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT classid, defaulthpbase, defaulthpadd, defaulthpmod, defaultcpbase, defaultcpadd, defaultcpmod, defaultmpbase, defaultmpadd, defaultmpmod, class_lvl FROM lvlupgain");
      rset = statement.executeQuery();

      while (rset.next())
      {
        LvlupData lvlDat = new LvlupData();
        lvlDat.set_classid(rset.getInt("classid"));
        lvlDat.set_classLvl(rset.getInt("class_lvl"));
        lvlDat.set_classHpBase(rset.getDouble("defaulthpbase"));
        lvlDat.set_classHpAdd(rset.getDouble("defaulthpadd"));
        lvlDat.set_classHpModifier(rset.getDouble("defaulthpmod"));
        lvlDat.set_classCpBase(rset.getDouble("defaultcpbase"));
        lvlDat.set_classCpAdd(rset.getDouble("defaultcpadd"));
        lvlDat.set_classCpModifier(rset.getDouble("defaultcpmod"));
        lvlDat.set_classMpBase(rset.getDouble("defaultmpbase"));
        lvlDat.set_classMpAdd(rset.getDouble("defaultmpadd"));
        lvlDat.set_classMpModifier(rset.getDouble("defaultmpmod"));

        _lvltable.put(Integer.valueOf(lvlDat.get_classid()), lvlDat);
      }

      _log.info("LevelUpData: Loaded " + _lvltable.size() + " Character Level Up Templates.");
    }
    catch (Exception e)
    {
      _log.warn("error while creating Lvl up data table " + e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public LvlupData getTemplate(int classId)
  {
    return (LvlupData)_lvltable.get(Integer.valueOf(classId));
  }

  public LvlupData getTemplate(ClassId classId)
  {
    return (LvlupData)_lvltable.get(Integer.valueOf(classId.getId()));
  }
}