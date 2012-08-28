package l2p.gameserver.data.xml.parser;

import gnu.trove.TIntObjectHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import l2p.commons.data.xml.AbstractDirParser;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.SkillAcquireHolder;
import l2p.gameserver.model.SkillLearn;
import org.dom4j.Element;

public final class SkillAcquireParser extends AbstractDirParser<SkillAcquireHolder>
{
  private static final SkillAcquireParser _instance = new SkillAcquireParser();

  public static SkillAcquireParser getInstance()
  {
    return _instance;
  }

  protected SkillAcquireParser()
  {
    super(SkillAcquireHolder.getInstance());
  }

  public File getXMLDir()
  {
    return new File(Config.DATAPACK_ROOT, "data/skill_tree/");
  }

  public boolean isIgnored(File b)
  {
    return false;
  }

  public String getDTDFileName()
  {
    return "tree.dtd";
  }

  protected void readData(Element rootElement)
    throws Exception
  {
    for (Iterator iterator = rootElement.elementIterator("certification_skill_tree"); iterator.hasNext(); ) {
      ((SkillAcquireHolder)getHolder()).addAllCertificationLearns(parseSkillLearn((Element)iterator.next()));
    }
    for (Iterator iterator = rootElement.elementIterator("sub_unit_skill_tree"); iterator.hasNext(); ) {
      ((SkillAcquireHolder)getHolder()).addAllSubUnitLearns(parseSkillLearn((Element)iterator.next()));
    }
    for (Iterator iterator = rootElement.elementIterator("pledge_skill_tree"); iterator.hasNext(); ) {
      ((SkillAcquireHolder)getHolder()).addAllPledgeLearns(parseSkillLearn((Element)iterator.next()));
    }
    for (Iterator iterator = rootElement.elementIterator("collection_skill_tree"); iterator.hasNext(); ) {
      ((SkillAcquireHolder)getHolder()).addAllCollectionLearns(parseSkillLearn((Element)iterator.next()));
    }
    for (Iterator iterator = rootElement.elementIterator("fishing_skill_tree"); iterator.hasNext(); )
    {
      Element nxt = (Element)iterator.next();
      for (classIterator = nxt.elementIterator("race"); classIterator.hasNext(); )
      {
        Element classElement = (Element)classIterator.next();
        int race = Integer.parseInt(classElement.attributeValue("id"));
        List learns = parseSkillLearn(classElement);
        ((SkillAcquireHolder)getHolder()).addAllFishingLearns(race, learns);
      }
    }
    Iterator classIterator;
    for (Iterator iterator = rootElement.elementIterator("transfer_skill_tree"); iterator.hasNext(); )
    {
      Element nxt = (Element)iterator.next();
      for (classIterator = nxt.elementIterator("class"); classIterator.hasNext(); )
      {
        Element classElement = (Element)classIterator.next();
        int classId = Integer.parseInt(classElement.attributeValue("id"));
        List learns = parseSkillLearn(classElement);
        ((SkillAcquireHolder)getHolder()).addAllTransferLearns(classId, learns);
      }
    }
    Iterator classIterator;
    for (Iterator iterator = rootElement.elementIterator("normal_skill_tree"); iterator.hasNext(); )
    {
      TIntObjectHashMap map = new TIntObjectHashMap();
      Element nxt = (Element)iterator.next();
      for (Iterator classIterator = nxt.elementIterator("class"); classIterator.hasNext(); )
      {
        Element classElement = (Element)classIterator.next();
        int classId = Integer.parseInt(classElement.attributeValue("id"));
        List learns = parseSkillLearn(classElement);

        map.put(classId, learns);
      }

      ((SkillAcquireHolder)getHolder()).addAllNormalSkillLearns(map);
    }

    for (Iterator iterator = rootElement.elementIterator("transformation_skill_tree"); iterator.hasNext(); )
    {
      Element nxt = (Element)iterator.next();
      for (classIterator = nxt.elementIterator("race"); classIterator.hasNext(); )
      {
        Element classElement = (Element)classIterator.next();
        int race = Integer.parseInt(classElement.attributeValue("id"));
        List learns = parseSkillLearn(classElement);
        ((SkillAcquireHolder)getHolder()).addAllTransformationLearns(race, learns);
      }
    }
    Iterator classIterator;
  }

  private List<SkillLearn> parseSkillLearn(Element tree) {
    List skillLearns = new ArrayList();
    for (Iterator iterator = tree.elementIterator("skill"); iterator.hasNext(); )
    {
      Element element = (Element)iterator.next();

      int id = Integer.parseInt(element.attributeValue("id"));
      int level = Integer.parseInt(element.attributeValue("level"));
      int cost = element.attributeValue("cost") == null ? 0 : Integer.parseInt(element.attributeValue("cost"));
      int min_level = Integer.parseInt(element.attributeValue("min_level"));
      int item_id = element.attributeValue("item_id") == null ? 0 : Integer.parseInt(element.attributeValue("item_id"));
      long item_count = element.attributeValue("item_count") == null ? 1L : Long.parseLong(element.attributeValue("item_count"));
      boolean clicked = (element.attributeValue("clicked") != null) && (Boolean.parseBoolean(element.attributeValue("clicked")));

      skillLearns.add(new SkillLearn(id, level, min_level, cost, item_id, item_count, clicked));
    }

    return skillLearns;
  }
}