package net.sf.l2j.gameserver.skills;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.skills.conditions.Condition;
import net.sf.l2j.gameserver.templates.StatsSet;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

final class DocumentSkill extends DocumentBase
{
  private Skill _currentSkill;
  private List<L2Skill> _skillsInFile = new FastList();

  DocumentSkill(File file) {
    super(file);
  }

  private void setCurrentSkill(Skill skill) {
    _currentSkill = skill;
  }

  protected StatsSet getStatsSet()
  {
    return _currentSkill.sets[_currentSkill.currentLevel];
  }

  protected List<L2Skill> getSkills() {
    return _skillsInFile;
  }

  protected String getTableValue(String name)
  {
    try {
      return ((String[])_tables.get(name))[_currentSkill.currentLevel];
    } catch (RuntimeException e) {
      _log.log(Level.SEVERE, "Error in table: " + name + " of Skill Id " + _currentSkill.id, e);
    }return "";
  }

  protected String getTableValue(String name, int idx)
  {
    try
    {
      return ((String[])_tables.get(name))[(idx - 1)];
    } catch (RuntimeException e) {
      _log.log(Level.SEVERE, "wrong level count in skill Id " + _currentSkill.id, e);
    }return "";
  }

  protected void parseDocument(Document doc)
  {
    for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
      if ("list".equalsIgnoreCase(n.getNodeName())) {
        for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
          if ("skill".equalsIgnoreCase(d.getNodeName())) {
            setCurrentSkill(new Skill());
            parseSkill(d);
            _skillsInFile.addAll(_currentSkill.skills);
            resetTable();
          }
      }
      else if ("skill".equalsIgnoreCase(n.getNodeName())) {
        setCurrentSkill(new Skill());
        parseSkill(n);
        _skillsInFile.addAll(_currentSkill.skills);
      }
  }

  protected void parseSkill(Node n)
  {
    NamedNodeMap attrs = n.getAttributes();
    int enchantLevels1 = 0;
    int enchantLevels2 = 0;
    int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
    String skillName = attrs.getNamedItem("name").getNodeValue();
    String levels = attrs.getNamedItem("levels").getNodeValue();
    int lastLvl = Integer.parseInt(levels);
    if (attrs.getNamedItem("enchantLevels1") != null) {
      enchantLevels1 = Integer.parseInt(attrs.getNamedItem("enchantLevels1").getNodeValue());
    }
    if (attrs.getNamedItem("enchantLevels2") != null) {
      enchantLevels2 = Integer.parseInt(attrs.getNamedItem("enchantLevels2").getNodeValue());
    }

    _currentSkill.id = skillId;
    _currentSkill.name = skillName;
    _currentSkill.sets = new StatsSet[lastLvl];
    _currentSkill.enchsets1 = new StatsSet[enchantLevels1];
    _currentSkill.enchsets2 = new StatsSet[enchantLevels2];

    for (int i = 0; i < lastLvl; i++) {
      _currentSkill.sets[i] = new StatsSet();
      _currentSkill.sets[i].set("skill_id", _currentSkill.id);
      _currentSkill.sets[i].set("level", i + 1);
      _currentSkill.sets[i].set("name", _currentSkill.name);
    }

    if (_currentSkill.sets.length != lastLvl) {
      throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + lastLvl + " levels expected");
    }

    Node first = n.getFirstChild();
    for (n = first; n != null; n = n.getNextSibling()) {
      if ("table".equalsIgnoreCase(n.getNodeName())) {
        parseTable(n);
      }
    }
    for (int i = 1; i <= lastLvl; i++) {
      for (n = first; n != null; n = n.getNextSibling()) {
        if ("set".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, _currentSkill.sets[(i - 1)], Integer.valueOf(i));
        }
      }
    }
    for (int i = 0; i < enchantLevels1; i++) {
      _currentSkill.enchsets1[i] = new StatsSet();
      _currentSkill.enchsets1[i].set("skill_id", _currentSkill.id);

      _currentSkill.enchsets1[i].set("level", i + 101);
      _currentSkill.enchsets1[i].set("name", _currentSkill.name);

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("set".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, _currentSkill.enchsets1[i], Integer.valueOf(_currentSkill.sets.length));
        }
      }

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant1".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, _currentSkill.enchsets1[i], Integer.valueOf(i + 1));
        }
      }
    }

    if (_currentSkill.enchsets1.length != enchantLevels1) {
      throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + enchantLevels1 + " levels expected");
    }

    for (int i = 0; i < enchantLevels2; i++) {
      _currentSkill.enchsets2[i] = new StatsSet();

      _currentSkill.enchsets2[i].set("skill_id", _currentSkill.id);
      _currentSkill.enchsets2[i].set("level", i + 141);
      _currentSkill.enchsets2[i].set("name", _currentSkill.name);

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("set".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, _currentSkill.enchsets2[i], Integer.valueOf(_currentSkill.sets.length));
        }
      }

      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant2".equalsIgnoreCase(n.getNodeName())) {
          parseBeanSet(n, _currentSkill.enchsets2[i], Integer.valueOf(i + 1));
        }
      }
    }

    if (_currentSkill.enchsets2.length != enchantLevels2) {
      throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + enchantLevels2 + " levels expected");
    }

    makeSkills();
    for (int i = 0; i < lastLvl; i++) {
      _currentSkill.currentLevel = i;
      for (n = first; n != null; n = n.getNextSibling()) {
        if ("cond".equalsIgnoreCase(n.getNodeName())) {
          Condition condition = parseCondition(n.getFirstChild(), _currentSkill.currentSkills.get(i));
          Node msg = n.getAttributes().getNamedItem("msg");
          if ((condition != null) && (msg != null)) {
            condition.setMessage(msg.getNodeValue());
          }
          ((L2Skill)_currentSkill.currentSkills.get(i)).attach(condition, false);
        }
        if ("for".equalsIgnoreCase(n.getNodeName())) {
          parseTemplate(n, _currentSkill.currentSkills.get(i));
        }
      }
    }
    for (int i = lastLvl; i < lastLvl + enchantLevels1; i++) {
      _currentSkill.currentLevel = (i - lastLvl);
      boolean found = false;
      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant1cond".equalsIgnoreCase(n.getNodeName())) {
          found = true;
          Condition condition = parseCondition(n.getFirstChild(), _currentSkill.currentSkills.get(i));
          Node msg = n.getAttributes().getNamedItem("msg");
          if ((condition != null) && (msg != null)) {
            condition.setMessage(msg.getNodeValue());
          }
          ((L2Skill)_currentSkill.currentSkills.get(i)).attach(condition, false);
        }
        if ("enchant1for".equalsIgnoreCase(n.getNodeName())) {
          found = true;
          parseTemplate(n, _currentSkill.currentSkills.get(i));
        }
      }

      if (!found) {
        _currentSkill.currentLevel = (lastLvl - 1);
        for (n = first; n != null; n = n.getNextSibling()) {
          if ("cond".equalsIgnoreCase(n.getNodeName())) {
            Condition condition = parseCondition(n.getFirstChild(), _currentSkill.currentSkills.get(i));
            Node msg = n.getAttributes().getNamedItem("msg");
            if ((condition != null) && (msg != null)) {
              condition.setMessage(msg.getNodeValue());
            }
            ((L2Skill)_currentSkill.currentSkills.get(i)).attach(condition, false);
          }
          if ("for".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, _currentSkill.currentSkills.get(i));
          }
        }
      }
    }
    for (int i = lastLvl + enchantLevels1; i < lastLvl + enchantLevels1 + enchantLevels2; i++) {
      boolean found = false;
      _currentSkill.currentLevel = (i - lastLvl - enchantLevels1);
      for (n = first; n != null; n = n.getNextSibling()) {
        if ("enchant2cond".equalsIgnoreCase(n.getNodeName())) {
          found = true;
          Condition condition = parseCondition(n.getFirstChild(), _currentSkill.currentSkills.get(i));
          Node msg = n.getAttributes().getNamedItem("msg");
          if ((condition != null) && (msg != null)) {
            condition.setMessage(msg.getNodeValue());
          }
          ((L2Skill)_currentSkill.currentSkills.get(i)).attach(condition, false);
        }
        if ("enchant2for".equalsIgnoreCase(n.getNodeName())) {
          found = true;
          parseTemplate(n, _currentSkill.currentSkills.get(i));
        }
      }

      if (!found) {
        _currentSkill.currentLevel = (lastLvl - 1);
        for (n = first; n != null; n = n.getNextSibling()) {
          if ("cond".equalsIgnoreCase(n.getNodeName())) {
            Condition condition = parseCondition(n.getFirstChild(), _currentSkill.currentSkills.get(i));
            Node msg = n.getAttributes().getNamedItem("msg");
            if ((condition != null) && (msg != null)) {
              condition.setMessage(msg.getNodeValue());
            }
            ((L2Skill)_currentSkill.currentSkills.get(i)).attach(condition, false);
          }
          if ("for".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, _currentSkill.currentSkills.get(i));
          }
        }
      }
    }
    _currentSkill.skills.addAll(_currentSkill.currentSkills);
  }

  private void makeSkills() {
    int count = 0;
    _currentSkill.currentSkills = new FastList(_currentSkill.sets.length + _currentSkill.enchsets1.length + _currentSkill.enchsets2.length);

    for (int i = 0; i < _currentSkill.sets.length; i++) {
      try {
        _currentSkill.currentSkills.add(i, ((L2Skill.SkillType)_currentSkill.sets[i].getEnum("skillType", L2Skill.SkillType.class)).makeSkill(_currentSkill.sets[i]));
        count++;
      } catch (Exception e) {
        _log.log(Level.SEVERE, "Skill id=" + ((L2Skill.SkillType)_currentSkill.sets[i].getEnum("skillType", L2Skill.SkillType.class)).makeSkill(_currentSkill.sets[i]).getDisplayId() + "level" + ((L2Skill.SkillType)_currentSkill.sets[i].getEnum("skillType", L2Skill.SkillType.class)).makeSkill(_currentSkill.sets[i]).getLevel(), e);
      }
    }
    int _count = count;
    for (int i = 0; i < _currentSkill.enchsets1.length; i++) {
      try {
        _currentSkill.currentSkills.add(_count + i, ((L2Skill.SkillType)_currentSkill.enchsets1[i].getEnum("skillType", L2Skill.SkillType.class)).makeSkill(_currentSkill.enchsets1[i]));
        count++;
      } catch (Exception e) {
        _log.log(Level.SEVERE, "Skill id=" + ((L2Skill.SkillType)_currentSkill.enchsets1[i].getEnum("skillType", L2Skill.SkillType.class)).makeSkill(_currentSkill.enchsets1[i]).getDisplayId() + " level=" + ((L2Skill.SkillType)_currentSkill.enchsets1[i].getEnum("skillType", L2Skill.SkillType.class)).makeSkill(_currentSkill.enchsets1[i]).getLevel(), e);
      }
    }
    _count = count;
    for (int i = 0; i < _currentSkill.enchsets2.length; i++)
      try {
        _currentSkill.currentSkills.add(_count + i, ((L2Skill.SkillType)_currentSkill.enchsets2[i].getEnum("skillType", L2Skill.SkillType.class)).makeSkill(_currentSkill.enchsets2[i]));
        count++;
      } catch (Exception e) {
        _log.log(Level.SEVERE, "Skill id=" + ((L2Skill.SkillType)_currentSkill.enchsets2[i].getEnum("skillType", L2Skill.SkillType.class)).makeSkill(_currentSkill.enchsets2[i]).getDisplayId() + " level=" + ((L2Skill.SkillType)_currentSkill.enchsets2[i].getEnum("skillType", L2Skill.SkillType.class)).makeSkill(_currentSkill.enchsets2[i]).getLevel(), e);
      }
  }

  public static class Skill
  {
    public int id;
    public String name;
    public StatsSet[] sets;
    public StatsSet[] enchsets1;
    public StatsSet[] enchsets2;
    public int currentLevel;
    public List<L2Skill> skills = new FastList();
    public List<L2Skill> currentSkills = new FastList();
  }
}