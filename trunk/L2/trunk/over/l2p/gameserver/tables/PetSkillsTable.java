package l2p.gameserver.tables;

import gnu.trove.TIntObjectHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.model.SkillLearn;
import l2p.gameserver.model.Summon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PetSkillsTable
{
  private static final Logger _log = LoggerFactory.getLogger(PetSkillsTable.class);
  private TIntObjectHashMap<List<SkillLearn>> _skillTrees = new TIntObjectHashMap();

  private static PetSkillsTable _instance = new PetSkillsTable();

  public static PetSkillsTable getInstance()
  {
    return _instance;
  }

  public void reload()
  {
    _instance = new PetSkillsTable();
  }

  private PetSkillsTable()
  {
    load();
  }

  private void load()
  {
    int npcId = 0;
    int count = 0;
    int id = 0;
    int lvl = 0;
    int minLvl = 0;

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT * FROM pets_skills ORDER BY templateId");
      rset = statement.executeQuery();

      while (rset.next())
      {
        npcId = rset.getInt("templateId");
        id = rset.getInt("skillId");
        lvl = rset.getInt("skillLvl");
        minLvl = rset.getInt("minLvl");

        List list = (List)_skillTrees.get(npcId);
        if (list == null) {
          _skillTrees.put(npcId, list = new ArrayList());
        }
        SkillLearn skillLearn = new SkillLearn(id, lvl, minLvl, 0, 0, 0L, false);
        list.add(skillLearn);
        count++;
      }
    }
    catch (Exception e)
    {
      _log.error("Error while creating pet skill tree (Pet ID " + npcId + ")", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    _log.info("PetSkillsTable: Loaded " + count + " skills.");
  }

  public int getAvailableLevel(Summon cha, int skillId)
  {
    List skills = (List)_skillTrees.get(cha.getNpcId());
    if (skills == null) {
      return 0;
    }
    int lvl = 0;
    for (SkillLearn temp : skills)
    {
      if (temp.getId() != skillId)
        continue;
      if (temp.getLevel() == 0)
      {
        if (cha.getLevel() < 70)
        {
          lvl = cha.getLevel() / 10;
          if (lvl <= 0)
            lvl = 1;
        }
        else {
          lvl = 7 + (cha.getLevel() - 70) / 5;
        }

        int maxLvl = SkillTable.getInstance().getMaxLevel(temp.getId());
        if (lvl <= maxLvl) break;
        lvl = maxLvl; break;
      }

      if ((temp.getMinLevel() <= cha.getLevel()) && 
        (temp.getLevel() > lvl))
        lvl = temp.getLevel();
    }
    return lvl;
  }
}