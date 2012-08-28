package l2m.gameserver.data.xml.parser;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import l2p.commons.collections.MultiValueSet;
import l2p.commons.data.xml.AbstractFileParser;
import l2m.gameserver.Config;
import l2m.gameserver.data.xml.holder.FishDataHolder;
import l2m.gameserver.templates.item.support.FishGroup;
import l2m.gameserver.templates.item.support.FishTemplate;
import l2m.gameserver.templates.item.support.LureTemplate;
import l2m.gameserver.templates.item.support.LureType;
import org.dom4j.Attribute;
import org.dom4j.Element;

public class FishDataParser extends AbstractFileParser<FishDataHolder>
{
  private static final FishDataParser _instance = new FishDataParser();

  public static FishDataParser getInstance()
  {
    return _instance;
  }

  private FishDataParser()
  {
    super(FishDataHolder.getInstance());
  }

  public File getXMLFile()
  {
    return new File(Config.DATAPACK_ROOT, "data/fishdata.xml");
  }

  public String getDTDFileName()
  {
    return "fishdata.dtd";
  }

  protected void readData(Element rootElement)
    throws Exception
  {
    for (Iterator iterator = rootElement.elementIterator(); iterator.hasNext(); )
    {
      Element e = (Element)iterator.next();
      if ("fish".equals(e.getName()))
      {
        MultiValueSet map = new MultiValueSet();
        for (Iterator attributeIterator = e.attributeIterator(); attributeIterator.hasNext(); )
        {
          Attribute attribute = (Attribute)attributeIterator.next();
          map.put(attribute.getName(), attribute.getValue());
        }

        ((FishDataHolder)getHolder()).addFish(new FishTemplate(map));
      }
      else if ("lure".equals(e.getName()))
      {
        MultiValueSet map = new MultiValueSet();
        for (Iterator attributeIterator = e.attributeIterator(); attributeIterator.hasNext(); )
        {
          Attribute attribute = (Attribute)attributeIterator.next();
          map.put(attribute.getName(), attribute.getValue());
        }

        Map chances = new HashMap();
        for (Iterator elementIterator = e.elementIterator(); elementIterator.hasNext(); )
        {
          Element chanceElement = (Element)elementIterator.next();
          chances.put(FishGroup.valueOf(chanceElement.attributeValue("type")), Integer.valueOf(Integer.parseInt(chanceElement.attributeValue("value"))));
        }
        map.put("chances", chances);
        ((FishDataHolder)getHolder()).addLure(new LureTemplate(map));
      }
      else if ("distribution".equals(e.getName()))
      {
        id = Integer.parseInt(e.attributeValue("id"));

        for (forLureIterator = e.elementIterator(); forLureIterator.hasNext(); )
        {
          Element forLureElement = (Element)forLureIterator.next();

          LureType lureType = LureType.valueOf(forLureElement.attributeValue("type"));
          Map chances = new HashMap();

          for (Iterator chanceIterator = forLureElement.elementIterator(); chanceIterator.hasNext(); )
          {
            Element chanceElement = (Element)chanceIterator.next();
            chances.put(FishGroup.valueOf(chanceElement.attributeValue("type")), Integer.valueOf(Integer.parseInt(chanceElement.attributeValue("value"))));
          }
          ((FishDataHolder)getHolder()).addDistribution(id, lureType, chances);
        }
      }
    }
    int id;
    Iterator forLureIterator;
  }
}