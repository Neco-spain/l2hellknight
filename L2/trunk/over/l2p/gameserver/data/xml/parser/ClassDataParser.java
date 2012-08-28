package l2p.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;
import l2p.commons.data.xml.AbstractDirParser;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.ClassDataHolder;
import l2p.gameserver.templates.player.ClassData;
import org.dom4j.Element;

public final class ClassDataParser extends AbstractDirParser<ClassDataHolder>
{
  private static final ClassDataParser _instance = new ClassDataParser();

  public static ClassDataParser getInstance()
  {
    return _instance;
  }

  private ClassDataParser()
  {
    super(ClassDataHolder.getInstance());
  }

  public File getXMLDir()
  {
    return new File(Config.DATAPACK_ROOT, "data/pc_parameters/class_data/");
  }

  public boolean isIgnored(File f)
  {
    return false;
  }

  public String getDTDFileName()
  {
    return "class_data.dtd";
  }

  protected void readData(Element rootElement)
    throws Exception
  {
    for (Iterator iterator = rootElement.elementIterator(); iterator.hasNext(); )
    {
      Element element = (Element)iterator.next();

      int classId = Integer.parseInt(element.attributeValue("class_id"));
      ClassData template = new ClassData(classId);
      for (Iterator subIterator = element.elementIterator(); subIterator.hasNext(); )
      {
        Element subElement = (Element)subIterator.next();

        if ("lvl_up_data".equalsIgnoreCase(subElement.getName()))
        {
          for (Element e : subElement.elements())
          {
            int lvl = Integer.parseInt(e.attributeValue("lvl"));
            double hp = Double.parseDouble(e.attributeValue("hp"));
            double mp = Double.parseDouble(e.attributeValue("mp"));
            double cp = Double.parseDouble(e.attributeValue("cp"));
            template.addLvlUpData(lvl, hp, mp, cp);
          }
        }
      }
      ((ClassDataHolder)getHolder()).addClassData(template);
    }
  }
}