package l2p.gameserver.data.xml.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import l2p.commons.data.xml.AbstractFileParser;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.AirshipDockHolder;
import l2p.gameserver.model.entity.events.objects.BoatPoint;
import l2p.gameserver.serverpackets.components.SceneMovie;
import l2p.gameserver.templates.AirshipDock;
import l2p.gameserver.templates.AirshipDock.AirshipPlatform;
import org.dom4j.Element;

public final class AirshipDockParser extends AbstractFileParser<AirshipDockHolder>
{
  private static final AirshipDockParser _instance = new AirshipDockParser();

  public static AirshipDockParser getInstance()
  {
    return _instance;
  }

  protected AirshipDockParser()
  {
    super(AirshipDockHolder.getInstance());
  }

  public File getXMLFile()
  {
    return new File(Config.DATAPACK_ROOT, "data/airship_docks.xml");
  }

  public String getDTDFileName()
  {
    return "airship_docks.dtd";
  }

  protected void readData(Element rootElement)
    throws Exception
  {
    for (Iterator iterator = rootElement.elementIterator(); iterator.hasNext(); )
    {
      Element dockElement = (Element)iterator.next();
      int id = Integer.parseInt(dockElement.attributeValue("id"));

      List teleportList = parsePoints(dockElement.element("teleportlist"));

      for (BoatPoint point : teleportList)
      {
        point.setTeleport(true);
        point.setSpeed1(-1);
        point.setSpeed2(-1);
      }

      List platformList = new ArrayList(2);
      for (Iterator platformIterator = dockElement.elementIterator("platform"); platformIterator.hasNext(); )
      {
        Element platformElement = (Element)platformIterator.next();
        SceneMovie movie = SceneMovie.valueOf(platformElement.attributeValue("movie"));
        BoatPoint oustLoc = BoatPoint.parse(platformElement.element("oust"));
        BoatPoint spawnLoc = BoatPoint.parse(platformElement.element("spawn"));
        List arrivalList = parsePoints(platformElement.element("arrival"));
        List departList = parsePoints(platformElement.element("depart"));

        AirshipDock.AirshipPlatform platform = new AirshipDock.AirshipPlatform(movie, oustLoc, spawnLoc, arrivalList, departList);
        platformList.add(platform);
      }

      ((AirshipDockHolder)getHolder()).addDock(new AirshipDock(id, teleportList, platformList));
    }
  }

  private List<BoatPoint> parsePoints(Element listElement)
  {
    if (listElement == null)
      return Collections.emptyList();
    List list = new ArrayList(5);
    for (Iterator iterator = listElement.elementIterator(); iterator.hasNext(); ) {
      list.add(BoatPoint.parse((Element)iterator.next()));
    }
    return list.isEmpty() ? Collections.emptyList() : list;
  }
}