package net.sf.l2j.gameserver.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2EnchantSkillLearn;
import net.sf.l2j.gameserver.model.L2PledgeSkillLearn;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;

public class SkillTreeTable
{
  private static final Logger _log = AbstractLogger.getLogger(SkillTreeTable.class.getName());
  private static SkillTreeTable _instance;
  private static FastMap<ClassId, Map<Integer, L2SkillLearn>> _skillTrees = new FastMap().shared("SkillTreeTable._skillTrees");
  private static List<L2SkillLearn> _fishingSkillTrees;
  private static List<L2SkillLearn> _expandDwarfCraftSkillTrees;
  private static List<L2PledgeSkillLearn> _pledgeSkillTrees;
  private static List<L2EnchantSkillLearn> _enchantSkillTrees;

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
    if (learnMap.containsKey(Integer.valueOf(skillHashCode))) {
      return ((L2SkillLearn)learnMap.get(Integer.valueOf(skillHashCode))).getMinLevel();
    }

    _log.severe("Expertise not found for grade " + grade);
    return 0;
  }

  public int getMinSkillLevel(int skillId, ClassId classId, int skillLvl)
  {
    Map map = (Map)getSkillTrees().get(classId);

    int skillHashCode = SkillTable.getSkillHashCode(skillId, skillLvl);

    if (map.containsKey(Integer.valueOf(skillHashCode))) {
      return ((L2SkillLearn)map.get(Integer.valueOf(skillHashCode))).getMinLevel();
    }

    return 0;
  }

  public int getMinSkillLevel(int skillId, int skillLvl) {
    int skillHashCode = SkillTable.getSkillHashCode(skillId, skillLvl);

    for (Map map : getSkillTrees().values())
    {
      if (map.containsKey(Integer.valueOf(skillHashCode))) {
        return ((L2SkillLearn)map.get(Integer.valueOf(skillHashCode))).getMinLevel();
      }
    }
    return 0;
  }

  private SkillTreeTable() {
    _fishingSkillTrees = new FastList();
    _expandDwarfCraftSkillTrees = new FastList();
    _enchantSkillTrees = new FastList();
    _pledgeSkillTrees = new FastList();

    int count = 0;
    int classId = 0;
    Connect con = null;
    PreparedStatement st = null;
    PreparedStatement st2 = null;
    ResultSet rs = null;
    ResultSet rs2 = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT * FROM class_list ORDER BY id");
      rs = st.executeQuery();
      rs.setFetchSize(50);

      while (rs.next()) {
        Map map = new HashMap();
        int parentClassId = rs.getInt("parent_id");
        classId = rs.getInt("id");
        st2 = con.prepareStatement("SELECT class_id, skill_id, level, name, sp, min_level FROM skill_trees where class_id=? ORDER BY skill_id, level");
        st2.setInt(1, classId);
        rs2 = st2.executeQuery();
        rs2.setFetchSize(50);

        if (parentClassId != -1) {
          Map parentMap = (Map)getSkillTrees().get(ClassId.values()[parentClassId]);
          map.putAll(parentMap);
        }

        int prevSkillId = -1;

        while (rs2.next()) {
          int id = rs2.getInt("skill_id");
          int lvl = rs2.getInt("level");

          String name = rs2.getString("name");
          int minLvl = rs2.getInt("min_level");
          int cost = rs2.getInt("sp");

          if (prevSkillId != id) {
            prevSkillId = id;
          }

          L2SkillLearn skillLearn = new L2SkillLearn(id, lvl, minLvl, name, cost, 0, 0);
          map.put(Integer.valueOf(SkillTable.getSkillHashCode(id, lvl)), skillLearn);
        }

        getSkillTrees().put(ClassId.values()[classId], map);
        Close.SR(st2, rs2);

        count += map.size();
      }
      Close.SR(st, rs);
      loadCommonTable(con);
      loadEnchantTable(con);
      loadPledgeTable(con);
    } catch (Exception e) {
      _log.severe("Error while creating skill tree (Class ID " + classId + "):" + e);
    } finally {
      Close.SR(st, rs);
      Close.SR(st2, rs2);
      Close.C(con);
    }
    _log.config("Loading SkillTreeTable... total " + count + " skills.");
    _log.config("Loading FishingSkillTreeTable... total " + _fishingSkillTrees.size() + " general skills.");
    _log.config("Loading DwarvenCraftSkillTreeTable... total " + _expandDwarfCraftSkillTrees.size() + " dwarven skills.");
    _log.config("Loading EnchantSkillTreeTable... total " + _enchantSkillTrees.size() + " enchant skills.");
    _log.config("Loading PledgeSkillTreeTable... total " + _pledgeSkillTrees.size() + " pledge skills");
  }

  private void loadCommonTable(Connect con) throws SQLException {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      st = con.prepareStatement("SELECT skill_id, level, name, sp, min_level, costid, cost, isfordwarf FROM fishing_skill_trees ORDER BY skill_id, level");
      rs = st.executeQuery();
      rs.setFetchSize(50);
      int prevSkillId = -1;
      while (rs.next()) {
        int id = rs.getInt("skill_id");
        int lvl = rs.getInt("level");
        String name = rs.getString("name");
        int minLvl = rs.getInt("min_level");
        int cost = rs.getInt("sp");
        int costId = rs.getInt("costid");
        int costCount = rs.getInt("cost");
        int isDwarven = rs.getInt("isfordwarf");

        if (prevSkillId != id) {
          prevSkillId = id;
        }

        L2SkillLearn skill = new L2SkillLearn(id, lvl, minLvl, name, cost, costId, costCount);

        if (isDwarven == 0)
          _fishingSkillTrees.add(skill);
        else
          _expandDwarfCraftSkillTrees.add(skill);
      }
    }
    catch (Exception e) {
      _log.severe("Error while creating common skill table: " + e);
    } finally {
      Close.SR(st, rs);
    }
  }

  private void loadEnchantTable(Connect con) throws SQLException {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      st = con.prepareStatement("SELECT skill_id, level, name, base_lvl, enchant_type, sp, min_skill_lvl, exp, success_rate76, success_rate77, success_rate78 FROM enchant_skill_trees ORDER BY skill_id, level");
      rs = st.executeQuery();
      rs.setFetchSize(50);
      int prevSkillId = -1;
      while (rs.next()) {
        int id = rs.getInt("skill_id");
        int lvl = rs.getInt("level");
        String name = rs.getString("name");
        int baseLvl = rs.getInt("base_lvl");
        int minSkillLvl = rs.getInt("min_skill_lvl");
        String enchant_type = rs.getString("enchant_type");
        int sp = rs.getInt("sp");
        int exp = rs.getInt("exp");
        byte rate76 = rs.getByte("success_rate76");
        byte rate77 = rs.getByte("success_rate77");
        byte rate78 = rs.getByte("success_rate78");

        if (prevSkillId != id) {
          prevSkillId = id;
        }

        _enchantSkillTrees.add(new L2EnchantSkillLearn(id, lvl, minSkillLvl, baseLvl, name, sp, exp, rate76, rate77, rate78, enchant_type));
      }
    } catch (Exception e) {
      _log.severe("Error while creating enchant skill table: " + e);
    } finally {
      Close.SR(st, rs);
    }
  }

  private void loadPledgeTable(Connect con) throws SQLException {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      st = con.prepareStatement("SELECT skill_id, level, name, clan_lvl, repCost, itemId FROM pledge_skill_trees ORDER BY skill_id, level");
      rs = st.executeQuery();
      rs.setFetchSize(50);
      int prevSkillId = -1;
      while (rs.next()) {
        int id = rs.getInt("skill_id");
        int lvl = rs.getInt("level");
        String name = rs.getString("name");
        int baseLvl = rs.getInt("clan_lvl");
        int sp = rs.getInt("repCost");
        int itemId = rs.getInt("itemId");

        if (prevSkillId != id) {
          prevSkillId = id;
        }

        _pledgeSkillTrees.add(new L2PledgeSkillLearn(id, lvl, baseLvl, name, sp, itemId));
      }
    } catch (Exception e) {
      _log.severe("Error while creating pledge skill table: " + e);
    } finally {
      Close.SR(st, rs);
    }
  }

  private FastMap<ClassId, Map<Integer, L2SkillLearn>> getSkillTrees()
  {
    return _skillTrees;
  }

  public L2SkillLearn[] getAvailableSkills(L2PcInstance cha, ClassId classId) {
    List result = new FastList();
    Collection skills = ((Map)getSkillTrees().get(classId)).values();

    if (skills == null)
    {
      _log.warning("Skilltree for class " + classId + " is not defined !");
      return new L2SkillLearn[0];
    }

    L2Skill[] oldSkills = cha.getAllSkills();

    for (L2SkillLearn temp : skills) {
      if (temp.getMinLevel() <= cha.getLevel()) {
        boolean knownSkill = false;

        for (int j = 0; (j < oldSkills.length) && (!knownSkill); j++) {
          if (oldSkills[j].getId() == temp.getId()) {
            knownSkill = true;

            if (oldSkills[j].getLevel() != temp.getLevel() - 1)
              continue;
            result.add(temp);
          }

        }

        if ((!knownSkill) && (temp.getLevel() == 1))
        {
          result.add(temp);
        }
      }
    }

    return (L2SkillLearn[])result.toArray(new L2SkillLearn[result.size()]);
  }

  public L2SkillLearn[] getAvailableSkills(L2PcInstance cha) {
    List result = new FastList();
    List skills = new FastList();

    skills.addAll(_fishingSkillTrees);

    if (skills == null)
    {
      _log.warning("Skilltree for fishing is not defined !");
      return new L2SkillLearn[0];
    }

    if ((cha.hasDwarvenCraft()) && (_expandDwarfCraftSkillTrees != null)) {
      skills.addAll(_expandDwarfCraftSkillTrees);
    }

    L2Skill[] oldSkills = cha.getAllSkills();

    for (L2SkillLearn temp : skills) {
      if (temp.getMinLevel() <= cha.getLevel()) {
        boolean knownSkill = false;

        for (int j = 0; (j < oldSkills.length) && (!knownSkill); j++) {
          if (oldSkills[j].getId() == temp.getId()) {
            knownSkill = true;

            if (oldSkills[j].getLevel() != temp.getLevel() - 1)
              continue;
            result.add(temp);
          }

        }

        if ((!knownSkill) && (temp.getLevel() == 1))
        {
          result.add(temp);
        }
      }
    }

    return (L2SkillLearn[])result.toArray(new L2SkillLearn[result.size()]);
  }

  public L2EnchantSkillLearn[] getAvailableEnchantSkills(L2PcInstance cha) {
    List result = new FastList();
    List skills = new FastList();

    skills.addAll(_enchantSkillTrees);

    if (skills == null)
    {
      _log.warning("Skilltree for enchanting is not defined !");
      return new L2EnchantSkillLearn[0];
    }

    L2Skill[] oldSkills = cha.getAllSkills();

    for (L2EnchantSkillLearn temp : skills) {
      if (76 <= cha.getLevel()) {
        boolean knownSkill = false;

        for (int j = 0; (j < oldSkills.length) && (!knownSkill); j++) {
          if (oldSkills[j].getId() == temp.getId()) {
            knownSkill = true;

            if (oldSkills[j].getLevel() != temp.getMinSkillLevel())
              continue;
            result.add(temp);
          }
        }

      }

    }

    return (L2EnchantSkillLearn[])result.toArray(new L2EnchantSkillLearn[result.size()]);
  }

  public L2PledgeSkillLearn[] getAvailablePledgeSkills(L2PcInstance cha) {
    List result = new FastList();
    List skills = _pledgeSkillTrees;

    if (skills == null)
    {
      _log.warning("No clan skills defined!");
      return new L2PledgeSkillLearn[0];
    }

    L2Skill[] oldSkills = cha.getClan().getAllSkills();

    for (L2PledgeSkillLearn temp : skills) {
      if (temp.getBaseLevel() <= cha.getClan().getLevel()) {
        boolean knownSkill = false;

        for (int j = 0; (j < oldSkills.length) && (!knownSkill); j++) {
          if (oldSkills[j].getId() == temp.getId()) {
            knownSkill = true;

            if (oldSkills[j].getLevel() != temp.getLevel() - 1)
              continue;
            result.add(temp);
          }

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

  public int getMinLevelForNewSkill(L2PcInstance cha, ClassId classId) {
    int minLevel = 0;
    Collection skills = ((Map)getSkillTrees().get(classId)).values();

    if (skills == null)
    {
      _log.warning("Skilltree for class " + classId + " is not defined !");
      return minLevel;
    }

    for (L2SkillLearn temp : skills) {
      if ((temp.getMinLevel() > cha.getLevel()) && (temp.getSpCost() != 0) && (
        (minLevel == 0) || (temp.getMinLevel() < minLevel))) {
        minLevel = temp.getMinLevel();
      }

    }

    return minLevel;
  }

  public int getMinLevelForNewSkill(L2PcInstance cha) {
    int minLevel = 0;
    List skills = new FastList();

    skills.addAll(_fishingSkillTrees);

    if (skills == null)
    {
      _log.warning("SkillTree for fishing is not defined !");
      return minLevel;
    }

    if ((cha.hasDwarvenCraft()) && (_expandDwarfCraftSkillTrees != null)) {
      skills.addAll(_expandDwarfCraftSkillTrees);
    }

    for (L2SkillLearn s : skills) {
      if ((s.getMinLevel() > cha.getLevel()) && (
        (minLevel == 0) || (s.getMinLevel() < minLevel))) {
        minLevel = s.getMinLevel();
      }

    }

    return minLevel;
  }

  public int getSkillCost(L2PcInstance player, L2Skill skill) {
    int skillCost = 100000000;
    ClassId classId = player.getSkillLearningClassId();
    int skillHashCode = SkillTable.getSkillHashCode(skill);

    if (((Map)getSkillTrees().get(classId)).containsKey(Integer.valueOf(skillHashCode))) {
      L2SkillLearn skillLearn = (L2SkillLearn)((Map)getSkillTrees().get(classId)).get(Integer.valueOf(skillHashCode));
      if (skillLearn.getMinLevel() <= player.getLevel()) {
        skillCost = skillLearn.getSpCost();
        if (!player.getClassId().equalsOrChildOf(classId)) {
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

  public int getSkillSpCost(L2PcInstance player, L2Skill skill) {
    int skillCost = 100000000;
    L2EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);

    for (L2EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList) {
      if (enchantSkillLearn.getId() != skill.getId())
      {
        continue;
      }
      if (enchantSkillLearn.getLevel() != skill.getLevel())
      {
        continue;
      }
      if (76 > player.getLevel())
      {
        continue;
      }
      skillCost = enchantSkillLearn.getSpCost();
    }
    return skillCost;
  }

  public int getSkillExpCost(L2PcInstance player, L2Skill skill) {
    int skillCost = 100000000;
    L2EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);

    for (L2EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList) {
      if (enchantSkillLearn.getId() != skill.getId())
      {
        continue;
      }
      if (enchantSkillLearn.getLevel() != skill.getLevel())
      {
        continue;
      }
      if (76 > player.getLevel())
      {
        continue;
      }
      skillCost = enchantSkillLearn.getExp();
    }
    return skillCost;
  }

  public byte getSkillRate(L2PcInstance player, L2Skill skill) {
    L2EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);

    for (L2EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList) {
      if (enchantSkillLearn.getId() != skill.getId())
      {
        continue;
      }
      if (enchantSkillLearn.getLevel() == skill.getLevel())
      {
        return enchantSkillLearn.getRate(player, (Config.PREMIUM_ENABLE) && (player.isPremium()));
      }
    }
    return 0;
  }
}