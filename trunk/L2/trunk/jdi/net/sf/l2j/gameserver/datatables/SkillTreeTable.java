package net.sf.l2j.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2EnchantSkillLearn;
import net.sf.l2j.gameserver.model.L2PledgeSkillLearn;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;

public class SkillTreeTable
{
  private static Logger _log = Logger.getLogger(SkillTreeTable.class.getName());
  private static SkillTreeTable _instance;
  private Map<ClassId, Map<Integer, L2SkillLearn>> _skillTrees;
  private List<L2SkillLearn> _fishingSkillTrees;
  private List<L2SkillLearn> _expandDwarfCraftSkillTrees;
  private List<L2PledgeSkillLearn> _pledgeSkillTrees;
  private List<L2EnchantSkillLearn> _enchantSkillTrees;

  public static SkillTreeTable getInstance()
  {
    if (_instance == null) {
      _instance = new SkillTreeTable();
    }
    return _instance;
  }

  public int getExpertiseLevel(int grade)
  {
    if (grade <= 0) {
      return 0;
    }

    Map learnMap = (Map)getSkillTrees().get(ClassId.paladin);

    int skillHashCode = SkillTable.getSkillHashCode(239, grade);
    if (learnMap.containsKey(Integer.valueOf(skillHashCode)))
    {
      return ((L2SkillLearn)learnMap.get(Integer.valueOf(skillHashCode))).getMinLevel();
    }

    _log.severe("Expertise not found for grade " + grade);
    return 0;
  }

  public int getMinSkillLevel(int skillId, ClassId classId, int skillLvl)
  {
    Map map = (Map)getSkillTrees().get(classId);

    int skillHashCode = SkillTable.getSkillHashCode(skillId, skillLvl);

    if (map.containsKey(Integer.valueOf(skillHashCode)))
    {
      return ((L2SkillLearn)map.get(Integer.valueOf(skillHashCode))).getMinLevel();
    }

    return 0;
  }

  public int getMinSkillLevel(int skillId, int skillLvl)
  {
    int skillHashCode = SkillTable.getSkillHashCode(skillId, skillLvl);

    for (Map map : getSkillTrees().values())
    {
      if (map.containsKey(Integer.valueOf(skillHashCode)))
      {
        return ((L2SkillLearn)map.get(Integer.valueOf(skillHashCode))).getMinLevel();
      }
    }
    return 0;
  }

  private SkillTreeTable()
  {
    int classId = 0;
    int count = 0;

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT * FROM class_list ORDER BY id");
      ResultSet classlist = statement.executeQuery();

      while (classlist.next())
      {
        Map map = new FastMap();
        int parentClassId = classlist.getInt("parent_id");
        classId = classlist.getInt("id");
        PreparedStatement statement2 = con.prepareStatement("SELECT class_id, skill_id, level, name, sp, min_level FROM skill_trees where class_id=? ORDER BY skill_id, level");
        statement2.setInt(1, classId);
        ResultSet skilltree = statement2.executeQuery();

        if (parentClassId != -1)
        {
          Map parentMap = (Map)getSkillTrees().get(ClassId.values()[parentClassId]);
          map.putAll(parentMap);
        }

        int prevSkillId = -1;

        while (skilltree.next())
        {
          int id = skilltree.getInt("skill_id");
          int lvl = skilltree.getInt("level");
          String name = skilltree.getString("name");
          int minLvl = skilltree.getInt("min_level");
          int cost = skilltree.getInt("sp");

          if (prevSkillId != id) {
            prevSkillId = id;
          }
          L2SkillLearn skillLearn = new L2SkillLearn(id, lvl, minLvl, name, cost, 0, 0);
          map.put(Integer.valueOf(SkillTable.getSkillHashCode(id, lvl)), skillLearn);
        }

        getSkillTrees().put(ClassId.values()[classId], map);
        skilltree.close();
        statement2.close();

        count += map.size();
        _log.fine("SkillTreeTable: skill tree for class " + classId + " has " + map.size() + " skills");
      }

      classlist.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.severe("Error while creating skill tree (Class ID " + classId + "):" + e);
    }

    _log.config("SkillTreeTable: Loaded " + count + " skills.");

    int count2 = 0;
    int count3 = 0;
    try
    {
      _fishingSkillTrees = new FastList();
      _expandDwarfCraftSkillTrees = new FastList();

      PreparedStatement statement = con.prepareStatement("SELECT skill_id, level, name, sp, min_level, costid, cost, isfordwarf FROM fishing_skill_trees ORDER BY skill_id, level");
      ResultSet skilltree2 = statement.executeQuery();

      int prevSkillId = -1;

      while (skilltree2.next())
      {
        int id = skilltree2.getInt("skill_id");
        int lvl = skilltree2.getInt("level");
        String name = skilltree2.getString("name");
        int minLvl = skilltree2.getInt("min_level");
        int cost = skilltree2.getInt("sp");
        int costId = skilltree2.getInt("costid");
        int costCount = skilltree2.getInt("cost");
        int isDwarven = skilltree2.getInt("isfordwarf");

        if (prevSkillId != id) {
          prevSkillId = id;
        }
        L2SkillLearn skill = new L2SkillLearn(id, lvl, minLvl, name, cost, costId, costCount);

        if (isDwarven == 0)
          _fishingSkillTrees.add(skill);
        else {
          _expandDwarfCraftSkillTrees.add(skill);
        }
      }
      skilltree2.close();
      statement.close();

      count2 = _fishingSkillTrees.size();
      count3 = _expandDwarfCraftSkillTrees.size();
    }
    catch (Exception e)
    {
      _log.severe("Error while creating fishing skill table: " + e);
    }

    int count4 = 0;
    try
    {
      _enchantSkillTrees = new FastList();

      PreparedStatement statement = con.prepareStatement("SELECT skill_id, level, name, base_lvl, sp, min_skill_lvl, exp, success_rate76, success_rate77, success_rate78 FROM enchant_skill_trees ORDER BY skill_id, level");
      ResultSet skilltree3 = statement.executeQuery();

      int prevSkillId = -1;

      while (skilltree3.next())
      {
        int id = skilltree3.getInt("skill_id");
        int lvl = skilltree3.getInt("level");
        String name = skilltree3.getString("name");
        int baseLvl = skilltree3.getInt("base_lvl");
        int minSkillLvl = skilltree3.getInt("min_skill_lvl");
        int sp = skilltree3.getInt("sp");
        int exp = skilltree3.getInt("exp");
        byte rate76 = skilltree3.getByte("success_rate76");
        byte rate77 = skilltree3.getByte("success_rate77");
        byte rate78 = skilltree3.getByte("success_rate78");

        if (prevSkillId != id) {
          prevSkillId = id;
        }
        L2EnchantSkillLearn skill = new L2EnchantSkillLearn(id, lvl, minSkillLvl, baseLvl, name, sp, exp, rate76, rate77, rate78);

        _enchantSkillTrees.add(skill);
      }

      skilltree3.close();
      statement.close();

      count4 = _enchantSkillTrees.size();
    }
    catch (Exception e)
    {
      _log.severe("Error while creating enchant skill table: " + e);
    }

    int count5 = 0;
    try
    {
      _pledgeSkillTrees = new FastList();

      PreparedStatement statement = con.prepareStatement("SELECT skill_id, level, name, clan_lvl, repCost, itemId FROM pledge_skill_trees ORDER BY skill_id, level");
      ResultSet skilltree4 = statement.executeQuery();

      int prevSkillId = -1;

      while (skilltree4.next())
      {
        int id = skilltree4.getInt("skill_id");
        int lvl = skilltree4.getInt("level");
        String name = skilltree4.getString("name");
        int baseLvl = skilltree4.getInt("clan_lvl");
        int sp = skilltree4.getInt("repCost");
        int itemId = skilltree4.getInt("itemId");

        if (prevSkillId != id) {
          prevSkillId = id;
        }
        L2PledgeSkillLearn skill = new L2PledgeSkillLearn(id, lvl, baseLvl, name, sp, itemId);

        _pledgeSkillTrees.add(skill);
      }

      skilltree4.close();
      statement.close();

      count5 = _pledgeSkillTrees.size();
    }
    catch (Exception e)
    {
      _log.severe("Error while creating fishing skill table: " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    _log.config("FishingSkillTreeTable: Loaded " + count2 + " general skills.");
    _log.config("FishingSkillTreeTable: Loaded " + count3 + " dwarven skills.");
    _log.config("EnchantSkillTreeTable: Loaded " + count4 + " enchant skills.");
    _log.config("PledgeSkillTreeTable: Loaded " + count5 + " pledge skills");
  }

  private Map<ClassId, Map<Integer, L2SkillLearn>> getSkillTrees()
  {
    if (_skillTrees == null) {
      _skillTrees = new FastMap();
    }
    return _skillTrees;
  }

  public L2SkillLearn[] getAvailableSkills(L2PcInstance cha, ClassId classId)
  {
    List result = new FastList();
    Collection skills = ((Map)getSkillTrees().get(classId)).values();

    if (skills == null)
    {
      _log.warning("Skilltree for class " + classId + " is not defined !");
      return new L2SkillLearn[0];
    }

    L2Skill[] oldSkills = cha.getAllSkills();

    for (L2SkillLearn temp : skills)
    {
      if (temp.getMinLevel() <= cha.getLevel())
      {
        boolean knownSkill = false;

        for (int j = 0; (j < oldSkills.length) && (!knownSkill); j++)
        {
          if (oldSkills[j].getId() != temp.getId())
            continue;
          knownSkill = true;

          if (oldSkills[j].getLevel() != temp.getLevel() - 1) {
            continue;
          }
          result.add(temp);
        }

        if ((!knownSkill) && (temp.getLevel() == 1))
        {
          result.add(temp);
        }
      }
    }

    return (L2SkillLearn[])result.toArray(new L2SkillLearn[result.size()]);
  }

  public L2SkillLearn[] getAvailableSkills(L2PcInstance cha)
  {
    List result = new FastList();
    List skills = new FastList();

    skills.addAll(_fishingSkillTrees);

    if (skills == null)
    {
      _log.warning("Skilltree for fishing is not defined !");
      return new L2SkillLearn[0];
    }

    if ((cha.hasDwarvenCraft()) && (_expandDwarfCraftSkillTrees != null))
    {
      skills.addAll(_expandDwarfCraftSkillTrees);
    }

    L2Skill[] oldSkills = cha.getAllSkills();

    for (L2SkillLearn temp : skills)
    {
      if (temp.getMinLevel() <= cha.getLevel())
      {
        boolean knownSkill = false;

        for (int j = 0; (j < oldSkills.length) && (!knownSkill); j++)
        {
          if (oldSkills[j].getId() != temp.getId())
            continue;
          knownSkill = true;

          if (oldSkills[j].getLevel() != temp.getLevel() - 1) {
            continue;
          }
          result.add(temp);
        }

        if ((!knownSkill) && (temp.getLevel() == 1))
        {
          result.add(temp);
        }
      }
    }

    return (L2SkillLearn[])result.toArray(new L2SkillLearn[result.size()]);
  }

  public L2EnchantSkillLearn[] getAvailableEnchantSkills(L2PcInstance cha)
  {
    List result = new FastList();
    List skills = new FastList();

    skills.addAll(_enchantSkillTrees);

    if (skills == null)
    {
      _log.warning("Skilltree for enchanting is not defined !");
      return new L2EnchantSkillLearn[0];
    }

    L2Skill[] oldSkills = cha.getAllSkills();

    for (L2EnchantSkillLearn temp : skills)
    {
      if (76 <= cha.getLevel())
      {
        boolean knownSkill = false;

        for (int j = 0; (j < oldSkills.length) && (!knownSkill); j++)
        {
          if (oldSkills[j].getId() != temp.getId())
            continue;
          knownSkill = true;

          if (oldSkills[j].getLevel() != temp.getMinSkillLevel()) {
            continue;
          }
          result.add(temp);
        }

      }

    }

    return (L2EnchantSkillLearn[])result.toArray(new L2EnchantSkillLearn[result.size()]);
  }

  public L2PledgeSkillLearn[] getAvailablePledgeSkills(L2PcInstance cha)
  {
    List result = new FastList();
    List skills = _pledgeSkillTrees;

    if (skills == null)
    {
      _log.warning("No clan skills defined!");
      return new L2PledgeSkillLearn[0];
    }

    L2Skill[] oldSkills = cha.getClan().getAllSkills();

    for (L2PledgeSkillLearn temp : skills)
    {
      if (temp.getBaseLevel() <= cha.getClan().getLevel())
      {
        boolean knownSkill = false;

        for (int j = 0; (j < oldSkills.length) && (!knownSkill); j++)
        {
          if (oldSkills[j].getId() != temp.getId())
            continue;
          knownSkill = true;

          if (oldSkills[j].getLevel() != temp.getLevel() - 1) {
            continue;
          }
          result.add(temp);
        }

        if ((!knownSkill) && (temp.getLevel() == 1))
        {
          result.add(temp);
        }
      }
    }

    return (L2PledgeSkillLearn[])result.toArray(new L2PledgeSkillLearn[result.size()]);
  }

  public Collection<L2SkillLearn> getAllowedSkills(ClassId classId)
  {
    return ((Map)getSkillTrees().get(classId)).values();
  }

  public int getMinLevelForNewSkill(L2PcInstance cha, ClassId classId)
  {
    int minLevel = 0;
    Collection skills = ((Map)getSkillTrees().get(classId)).values();

    if (skills == null)
    {
      _log.warning("Skilltree for class " + classId + " is not defined !");
      return minLevel;
    }

    for (L2SkillLearn temp : skills)
    {
      if ((temp.getMinLevel() > cha.getLevel()) && (temp.getSpCost() != 0) && (
        (minLevel == 0) || (temp.getMinLevel() < minLevel))) {
        minLevel = temp.getMinLevel();
      }
    }
    return minLevel;
  }

  public int getMinLevelForNewSkill(L2PcInstance cha)
  {
    int minLevel = 0;
    List skills = new FastList();

    skills.addAll(_fishingSkillTrees);

    if (skills == null)
    {
      _log.warning("SkillTree for fishing is not defined !");
      return minLevel;
    }

    if ((cha.hasDwarvenCraft()) && (_expandDwarfCraftSkillTrees != null))
    {
      skills.addAll(_expandDwarfCraftSkillTrees);
    }

    for (L2SkillLearn s : skills)
    {
      if ((s.getMinLevel() > cha.getLevel()) && (
        (minLevel == 0) || (s.getMinLevel() < minLevel))) {
        minLevel = s.getMinLevel();
      }
    }
    return minLevel;
  }

  public int getSkillCost(L2PcInstance player, L2Skill skill)
  {
    int skillCost = 100000000;
    ClassId classId = player.getSkillLearningClassId();
    int skillHashCode = SkillTable.getSkillHashCode(skill);

    if (((Map)getSkillTrees().get(classId)).containsKey(Integer.valueOf(skillHashCode)))
    {
      L2SkillLearn skillLearn = (L2SkillLearn)((Map)getSkillTrees().get(classId)).get(Integer.valueOf(skillHashCode));
      if (skillLearn.getMinLevel() <= player.getLevel())
      {
        skillCost = skillLearn.getSpCost();
        if (!player.getClassId().equalsOrChildOf(classId))
        {
          if (skill.getCrossLearnAdd() < 0) {
            return skillCost;
          }
          skillCost += skill.getCrossLearnAdd();
          skillCost = (int)(skillCost * skill.getCrossLearnMul());
        }

        if ((classId.getRace() != player.getRace()) && (!player.isSubClassActive())) {
          skillCost = (int)(skillCost * skill.getCrossLearnRace());
        }
        if (classId.isMage() != player.getClassId().isMage()) {
          skillCost = (int)(skillCost * skill.getCrossLearnProf());
        }
      }
    }
    return skillCost;
  }

  public int getSkillSpCost(L2PcInstance player, L2Skill skill)
  {
    int skillCost = 100000000;
    L2EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);

    for (L2EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList)
    {
      if (enchantSkillLearn.getId() != skill.getId()) {
        continue;
      }
      if (enchantSkillLearn.getLevel() != skill.getLevel()) {
        continue;
      }
      if (76 > player.getLevel()) {
        continue;
      }
      skillCost = enchantSkillLearn.getSpCost();
    }
    return skillCost;
  }

  public int getSkillExpCost(L2PcInstance player, L2Skill skill)
  {
    int skillCost = 100000000;
    L2EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);

    for (L2EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList)
    {
      if (enchantSkillLearn.getId() != skill.getId()) {
        continue;
      }
      if (enchantSkillLearn.getLevel() != skill.getLevel()) {
        continue;
      }
      if (76 > player.getLevel()) {
        continue;
      }
      skillCost = enchantSkillLearn.getExp();
    }
    return skillCost;
  }

  public byte getSkillRate(L2PcInstance player, L2Skill skill)
  {
    L2EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);

    for (L2EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList)
    {
      if (enchantSkillLearn.getId() != skill.getId()) {
        continue;
      }
      if (enchantSkillLearn.getLevel() == skill.getLevel())
      {
        return enchantSkillLearn.getRate(player);
      }
    }
    return 0;
  }
}