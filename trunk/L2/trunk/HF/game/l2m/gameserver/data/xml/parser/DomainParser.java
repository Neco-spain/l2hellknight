package l2m.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;
import l2p.commons.data.xml.AbstractFileParser;
import l2p.commons.geometry.Polygon;
import l2m.gameserver.Config;
import l2m.gameserver.instancemanager.MapRegionManager;
import l2m.gameserver.model.Territory;
import l2m.gameserver.templates.mapregion.DomainArea;
import org.dom4j.Element;

public class DomainParser extends AbstractFileParser<MapRegionManager>
{
  private static final DomainParser _instance = new DomainParser();

  public static DomainParser getInstance()
  {
    return _instance;
  }

  protected DomainParser()
  {
    super(MapRegionManager.getInstance());
  }

  public File getXMLFile()
  {
    return new File(Config.DATAPACK_ROOT, "data/mapregion/domains.xml");
  }

  public String getDTDFileName()
  {
    return "domains.dtd";
  }

  protected void readData(Element rootElement)
    throws Exception
  {
    for (Iterator iterator = rootElement.elementIterator(); iterator.hasNext(); )
    {
      Element listElement = (Element)iterator.next();

      if ("domain".equals(listElement.getName()))
      {
        int id = Integer.parseInt(listElement.attributeValue("id"));
        Territory territory = null;

        for (Iterator i = listElement.elementIterator(); i.hasNext(); )
        {
          Element n = (Element)i.next();

          if ("polygon".equalsIgnoreCase(n.getName()))
          {
            Polygon shape = ZoneParser.parsePolygon(n);

            if (!shape.validate()) {
              error("DomainParser: invalid territory data : " + shape + "!");
            }
            if (territory == null) {
              territory = new Territory();
            }
            territory.add(shape);
          }
        }

        if (territory == null) {
          throw new RuntimeException("DomainParser: empty territory!");
        }
        ((MapRegionManager)getHolder()).addRegionData(new DomainArea(id, territory));
      }
    }
  }
}