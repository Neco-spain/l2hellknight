package l2m.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;
import l2p.commons.data.xml.AbstractFileParser;
import l2m.gameserver.Config;
import l2m.gameserver.data.xml.holder.StaticObjectHolder;
import l2m.gameserver.templates.StaticObjectTemplate;
import l2m.gameserver.templates.StatsSet;
import org.dom4j.Element;

public final class StaticObjectParser extends AbstractFileParser<StaticObjectHolder>
{
  private static StaticObjectParser _instance = new StaticObjectParser();

  public static StaticObjectParser getInstance()
  {
    return _instance;
  }

  private StaticObjectParser()
  {
    super(StaticObjectHolder.getInstance());
  }

  public File getXMLFile()
  {
    return new File(Config.DATAPACK_ROOT, "data/staticobjects.xml");
  }

  public String getDTDFileName()
  {
    return "staticobjects.dtd";
  }

  protected void readData(Element rootElement)
    throws Exception
  {
    for (Iterator iterator = rootElement.elementIterator(); iterator.hasNext(); )
    {
      Element staticObjectElement = (Element)iterator.next();

      StatsSet set = new StatsSet();
      set.set("uid", staticObjectElement.attributeValue("id"));
      set.set("stype", staticObjectElement.attributeValue("stype"));
      set.set("path", staticObjectElement.attributeValue("path"));
      set.set("map_x", staticObjectElement.attributeValue("map_x"));
      set.set("map_y", staticObjectElement.attributeValue("map_y"));
      set.set("name", staticObjectElement.attributeValue("name"));
      set.set("x", staticObjectElement.attributeValue("x"));
      set.set("y", staticObjectElement.attributeValue("y"));
      set.set("z", staticObjectElement.attributeValue("z"));
      set.set("spawn", staticObjectElement.attributeValue("spawn"));

      ((StaticObjectHolder)getHolder()).addTemplate(new StaticObjectTemplate(set));
    }
  }
}