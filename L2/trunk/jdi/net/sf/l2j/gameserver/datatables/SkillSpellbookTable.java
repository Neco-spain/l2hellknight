package net.sf.l2j.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2Skill;

public class SkillSpellbookTable
{
  private static Logger _log = Logger.getLogger(SkillTreeTable.class.getName());
  private static SkillSpellbookTable _instance;
  private static Map<Integer, Integer> _skillSpellbooks;

  public static SkillSpellbookTable getInstance()
  {
    if (_instance == null) {
      _instance = new SkillSpellbookTable();
    }
    return _instance;
  }

  private SkillSpellbookTable()
  {
    _skillSpellbooks = new FastMap();
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT skill_id, item_id FROM skill_spellbooks");
      ResultSet spbooks = statement.executeQuery();

      while (spbooks.next()) {
        _skillSpellbooks.put(Integer.valueOf(spbooks.getInt("skill_id")), Integer.valueOf(spbooks.getInt("item_id")));
      }
      spbooks.close();
      statement.close();

      _log.config("SkillSpellbookTable: Loaded " + _skillSpellbooks.size() + " Spellbooks.");
    }
    catch (Exception e)
    {
      _log.warning("Error while loading spellbook data: " + e);
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e) {
      }
    }
  }

  public int getBookForSkill(int skillId) {
    if (!_skillSpellbooks.containsKey(Integer.valueOf(skillId))) {
      return -1;
    }
    return ((Integer)_skillSpellbooks.get(Integer.valueOf(skillId))).intValue();
  }

  public int getBookForSkill(L2Skill skill)
  {
    return getBookForSkill(skill.getId());
  }
}