package l2p.gameserver.data.xml.parser;

import gnu.trove.TIntIntHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import l2p.commons.data.xml.AbstractFileParser;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.CubicHolder;
import l2p.gameserver.model.Skill;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.CubicTemplate;
import l2p.gameserver.templates.CubicTemplate.ActionType;
import l2p.gameserver.templates.CubicTemplate.SkillInfo;
import org.dom4j.Element;

public final class CubicParser extends AbstractFileParser<CubicHolder>
{
  private static CubicParser _instance = new CubicParser();

  public static CubicParser getInstance()
  {
    return _instance;
  }

  protected CubicParser()
  {
    super(CubicHolder.getInstance());
  }

  public File getXMLFile()
  {
    return new File(Config.DATAPACK_ROOT, "data/cubics.xml");
  }

  public String getDTDFileName()
  {
    return "cubics.dtd";
  }

  protected void readData(Element rootElement)
    throws Exception
  {
    for (Iterator iterator = rootElement.elementIterator(); iterator.hasNext(); )
    {
      Element cubicElement = (Element)iterator.next();
      id = Integer.parseInt(cubicElement.attributeValue("id"));
      level = Integer.parseInt(cubicElement.attributeValue("level"));
      int delay = Integer.parseInt(cubicElement.attributeValue("delay"));
      template = new CubicTemplate(id, level, delay);
      ((CubicHolder)getHolder()).addCubicTemplate(template);

      for (skillsIterator = cubicElement.elementIterator(); skillsIterator.hasNext(); )
      {
        Element skillsElement = (Element)skillsIterator.next();
        int chance = Integer.parseInt(skillsElement.attributeValue("chance"));
        List skills = new ArrayList(1);

        for (Iterator skillIterator = skillsElement.elementIterator(); skillIterator.hasNext(); )
        {
          Element skillElement = (Element)skillIterator.next();
          int id2 = Integer.parseInt(skillElement.attributeValue("id"));
          int level2 = Integer.parseInt(skillElement.attributeValue("level"));
          int chance2 = skillElement.attributeValue("chance") == null ? 0 : Integer.parseInt(skillElement.attributeValue("chance"));
          boolean canAttackDoor = Boolean.parseBoolean(skillElement.attributeValue("can_attack_door"));
          CubicTemplate.ActionType type = CubicTemplate.ActionType.valueOf(skillElement.attributeValue("action_type"));

          TIntIntHashMap set = new TIntIntHashMap();
          for (Iterator chanceIterator = skillElement.elementIterator(); chanceIterator.hasNext(); )
          {
            Element chanceElement = (Element)chanceIterator.next();
            int min = Integer.parseInt(chanceElement.attributeValue("min"));
            int max = Integer.parseInt(chanceElement.attributeValue("max"));
            int value = Integer.parseInt(chanceElement.attributeValue("value"));
            for (int i = min; i <= max; i++) {
              set.put(i, value);
            }
          }
          if ((chance2 == 0) && (set.isEmpty()))
          {
            warn("Wrong skill chance. Cubic: " + id + "/" + level);
          }
          Skill skill = SkillTable.getInstance().getInfo(id2, level2);
          if (skill != null)
          {
            skill.setCubicSkill(true);
            skills.add(new CubicTemplate.SkillInfo(skill, chance2, type, canAttackDoor, set));
          }
        }

        template.putSkills(chance, skills);
      }
    }
    int id;
    int level;
    CubicTemplate template;
    Iterator skillsIterator;
  }
}