package l2m.gameserver.skills;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Skill.SkillType;
import l2m.gameserver.model.base.EnchantSkillLearn;
import l2m.gameserver.skills.conditions.Condition;
import l2m.gameserver.data.tables.SkillTreeTable;
import l2m.gameserver.templates.StatsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class DocumentSkill extends DocumentBase
{
  private static final Logger _log = LoggerFactory.getLogger(DocumentSkill.class);
  private Skill currentSkill;
  private Set<String> usedTables = new HashSet();
  private List<Skill> skillsInFile = new LinkedList();

  DocumentSkill(File file)
  {
    super(file);
  }

  protected void resetTable()
  {
    if (!usedTables.isEmpty())
      for (String table : tables.keySet())
        if ((!usedTables.contains(table)) && 
          (_log.isDebugEnabled()))
          _log.debug("Unused table " + table + " for skill " + currentSkill.id);
    usedTables.clear();
    super.resetTable();
  }

  private void setCurrentSkill(Skill skill)
  {
    currentSkill = skill;
  }

  protected List<Skill> getSkills()
  {
    return skillsInFile;
  }

  protected Object getTableValue(String name)
  {
    try
    {
      usedTables.add(name);
      Object[] a = (Object[])tables.get(name);
      if (a.length - 1 >= currentSkill.currentLevel)
        return a[currentSkill.currentLevel];
      return a[(a.length - 1)];
    }
    catch (RuntimeException e)
    {
      _log.error("Error in table " + name + " of skill Id " + currentSkill.id, e);
    }return Integer.valueOf(0);
  }

  protected Object getTableValue(String name, int idx)
  {
    idx--;
    try
    {
      usedTables.add(name);
      Object[] a = (Object[])tables.get(name);
      if (a.length - 1 >= idx)
        return a[idx];
      return a[(a.length - 1)];
    }
    catch (Exception e)
    {
      _log.error("Wrong level count in skill Id " + currentSkill.id + " table " + name + " level " + idx, e);
    }return Integer.valueOf(0);
  }

  protected void parseDocument(Document doc)
  {
    for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
      if ("list".equalsIgnoreCase(n.getNodeName()))
      {
        for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
          if (!"skill".equalsIgnoreCase(d.getNodeName()))
            continue;
          setCurrentSkill(new Skill());
          parseSkill(d);
          skillsInFile.addAll(currentSkill.skills);
          resetTable();
        }
      } else {
        if (!"skill".equalsIgnoreCase(n.getNodeName()))
          continue;
        setCurrentSkill(new Skill());
        parseSkill(n);
        skillsInFile.addAll(currentSkill.skills);
      }
  }

  protected void parseSkill(Node n)
  {
    NamedNodeMap attrs = n.getAttributes();
    int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
    String skillName = attrs.getNamedItem("name").getNodeValue();
    String levels = attrs.getNamedItem("levels").getNodeValue();
    int lastLvl = Integer.parseInt(levels);
    try
    {
      Map displayLevels = new HashMap();

      Node enchant = null;
      Map etables = new HashMap();
      int count = 0; int eLevels = 0;
      Node d = n.cloneNode(true);
      for (int k = 0; k < d.getChildNodes().getLength(); k++)
      {
        enchant = d.getChildNodes().item(k);
        if (!enchant.getNodeName().startsWith("enchant"))
          continue;
        if (eLevels == 0)
          if (enchant.getAttributes().getNamedItem("levels") != null)
            eLevels = Integer.parseInt(enchant.getAttributes().getNamedItem("levels").getNodeValue());
          else
            eLevels = 30;
        String ename = enchant.getAttributes().getNamedItem("name").getNodeValue();
        for (int r = 1; r <= eLevels; r++)
        {
          int level = lastLvl + eLevels * count + r;
          EnchantSkillLearn e = new EnchantSkillLearn(skillId, 100 * (count + 1) + r, skillName, "+" + r + " " + ename, r == 1 ? lastLvl : 100 * (count + 1) + r - 1, lastLvl, eLevels);

          List t = (List)SkillTreeTable._enchant.get(Integer.valueOf(skillId));
          if (t == null)
            t = new ArrayList();
          t.add(e);
          SkillTreeTable._enchant.put(Integer.valueOf(skillId), t);
          displayLevels.put(Integer.valueOf(level), Integer.valueOf((count + 1) * 100 + r));
        }
        count++;
        Node first = enchant.getFirstChild();
        Node curr = null;
        for (curr = first; curr != null; curr = curr.getNextSibling()) {
          if (!"table".equalsIgnoreCase(curr.getNodeName()))
            continue;
          NamedNodeMap a = curr.getAttributes();
          String name = a.getNamedItem("name").getNodeValue();
          Object[] table = parseTable(curr);
          table = fillTableToSize(table, eLevels);
          Object[] fulltable = (Object[])etables.get(name);
          if (fulltable == null)
            fulltable = new Object[lastLvl + eLevels * 8 + 1];
          System.arraycopy(table, 0, fulltable, lastLvl + (count - 1) * eLevels, eLevels);
          etables.put(name, fulltable);
        }
      }
      lastLvl += eLevels * count;

      currentSkill.id = skillId;
      currentSkill.name = skillName;
      currentSkill.sets = new StatsSet[lastLvl];

      for (int i = 0; i < lastLvl; i++)
      {
        currentSkill.sets[i] = new StatsSet();
        currentSkill.sets[i].set("skill_id", currentSkill.id);
        currentSkill.sets[i].set("level", i + 1);
        currentSkill.sets[i].set("name", currentSkill.name);
        currentSkill.sets[i].set("base_level", levels);
      }

      if (currentSkill.sets.length != lastLvl) {
        throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + lastLvl + " levels expected");
      }
      Node first = n.getFirstChild();
      for (n = first; n != null; n = n.getNextSibling()) {
        if ("table".equalsIgnoreCase(n.getNodeName())) {
          parseTable(n);
        }
      }
      for (String tn : tables.keySet())
      {
        Object[] et = (Object[])etables.get(tn);
        if (et != null)
        {
          Object[] t = (Object[])tables.get(tn);
          Object max = t[(t.length - 1)];
          System.arraycopy(t, 0, et, 0, t.length);
          for (int j = 0; j < et.length; j++)
            if (et[j] == null)
              et[j] = max;
          tables.put(tn, et);
        }
      }

      for (int i = 1; i <= lastLvl; i++) {
        for (n = first; n != null; n = n.getNextSibling())
          if ("set".equalsIgnoreCase(n.getNodeName()))
            parseBeanSet(n, currentSkill.sets[(i - 1)], i);
      }
      makeSkills();
      for (int i = 0; i < lastLvl; i++)
      {
        currentSkill.currentLevel = i;
        Skill current = (Skill)currentSkill.currentSkills.get(i);
        if (displayLevels.get(Integer.valueOf(current.getLevel())) != null)
          current.setDisplayLevel(((Integer)displayLevels.get(Integer.valueOf(current.getLevel()))).intValue());
        current.setEnchantLevelCount(eLevels);

        for (n = first; n != null; n = n.getNextSibling())
        {
          if ("cond".equalsIgnoreCase(n.getNodeName()))
          {
            Condition condition = parseCondition(n.getFirstChild());
            if (condition != null)
            {
              Node msgAttribute = n.getAttributes().getNamedItem("msgId");
              if (msgAttribute != null)
              {
                int msgId = parseNumber(msgAttribute.getNodeValue()).intValue();
                condition.setSystemMsg(msgId);
              }
              current.attach(condition);
            }
          }
          else if ("for".equalsIgnoreCase(n.getNodeName())) {
            parseTemplate(n, current);
          } else if ("triggers".equalsIgnoreCase(n.getNodeName())) {
            parseTrigger(n, current);
          }
        }
      }
      currentSkill.skills.addAll(currentSkill.currentSkills);
    }
    catch (Exception e)
    {
      _log.error("Error loading skill " + skillId, e);
    }
  }

  private Object[] fillTableToSize(Object[] table, int size)
  {
    if (table.length < size)
    {
      Object[] ret = new Object[size];
      System.arraycopy(table, 0, ret, 0, table.length);
      table = ret;
    }
    for (int j = 1; j < size; j++)
      if (table[j] == null)
        table[j] = table[(j - 1)];
    return table;
  }

  private void makeSkills()
  {
    currentSkill.currentSkills = new ArrayList(currentSkill.sets.length);

    for (int i = 0; i < currentSkill.sets.length; i++)
      currentSkill.currentSkills.add(i, ((Skill.SkillType)currentSkill.sets[i].getEnum("skillType", Skill.SkillType.class)).makeSkill(currentSkill.sets[i]));
  }

  public class Skill
  {
    public int id;
    public String name;
    public StatsSet[] sets;
    public int currentLevel;
    public List<Skill> skills = new ArrayList();
    public List<Skill> currentSkills = new ArrayList();

    public Skill()
    {
    }
  }
}