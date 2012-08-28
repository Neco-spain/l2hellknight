package l2p.gameserver.data.xml.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import l2p.commons.data.xml.AbstractDirParser;
import l2p.commons.geometry.Polygon;
import l2p.commons.time.cron.SchedulingPattern;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.DoorHolder;
import l2p.gameserver.data.xml.holder.InstantZoneHolder;
import l2p.gameserver.data.xml.holder.SpawnHolder;
import l2p.gameserver.data.xml.holder.ZoneHolder;
import l2p.gameserver.model.Territory;
import l2p.gameserver.templates.DoorTemplate;
import l2p.gameserver.templates.InstantZone;
import l2p.gameserver.templates.InstantZone.DoorInfo;
import l2p.gameserver.templates.InstantZone.SpawnInfo;
import l2p.gameserver.templates.InstantZone.SpawnInfo2;
import l2p.gameserver.templates.InstantZone.ZoneInfo;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.templates.ZoneTemplate;
import l2p.gameserver.utils.Location;
import org.dom4j.Element;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

public class InstantZoneParser extends AbstractDirParser<InstantZoneHolder>
{
  private static InstantZoneParser _instance = new InstantZoneParser();

  public static InstantZoneParser getInstance()
  {
    return _instance;
  }

  public InstantZoneParser()
  {
    super(InstantZoneHolder.getInstance());
  }

  public File getXMLDir()
  {
    return new File(Config.DATAPACK_ROOT, "data/instances/");
  }

  public boolean isIgnored(File f)
  {
    return false;
  }

  public String getDTDFileName()
  {
    return "instances.dtd";
  }

  protected void readData(Element rootElement)
    throws Exception
  {
    for (Iterator iterator = rootElement.elementIterator(); iterator.hasNext(); )
    {
      Element element = (Element)iterator.next();

      SchedulingPattern resetReuse = new SchedulingPattern("30 6 * * *");
      int timelimit = -1;
      int timer = 60;
      int mapx = -1;
      int mapy = -1;
      boolean dispelBuffs = false;
      boolean onPartyDismiss = true;
      int sharedReuseGroup = 0;
      int collapseIfEmpty = 0;

      int spawnType = 0;
      InstantZone.SpawnInfo spawnDat = null;
      int removedItemId = 0; int removedItemCount = 0; int giveItemId = 0; int givedItemCount = 0; int requiredQuestId = 0;
      int maxChannels = 20;
      boolean removedItemNecessity = false;
      boolean setReuseUponEntry = true;
      StatsSet params = new StatsSet();

      List spawns = new ArrayList();
      IntObjectMap doors = Containers.emptyIntObjectMap();
      Map zones = Collections.emptyMap();
      Map spawns2 = Collections.emptyMap();
      int instanceId = Integer.parseInt(element.attributeValue("id"));
      String name = element.attributeValue("name");

      String n = element.attributeValue("timelimit");
      if (n != null) {
        timelimit = Integer.parseInt(n);
      }
      n = element.attributeValue("collapseIfEmpty");
      collapseIfEmpty = Integer.parseInt(n);

      n = element.attributeValue("maxChannels");
      maxChannels = Integer.parseInt(n);

      n = element.attributeValue("dispelBuffs");
      dispelBuffs = (n != null) && (Boolean.parseBoolean(n));

      int minLevel = 0; int maxLevel = 0; int minParty = 1; int maxParty = 9;
      List teleportLocs = Collections.emptyList();
      Location ret = null;

      for (Iterator subIterator = element.elementIterator(); subIterator.hasNext(); )
      {
        Element subElement = (Element)subIterator.next();

        if ("level".equalsIgnoreCase(subElement.getName()))
        {
          minLevel = Integer.parseInt(subElement.attributeValue("min"));
          maxLevel = Integer.parseInt(subElement.attributeValue("max"));
        }
        else if ("collapse".equalsIgnoreCase(subElement.getName()))
        {
          onPartyDismiss = Boolean.parseBoolean(subElement.attributeValue("on-party-dismiss"));
          timer = Integer.parseInt(subElement.attributeValue("timer"));
        }
        else if ("party".equalsIgnoreCase(subElement.getName()))
        {
          minParty = Integer.parseInt(subElement.attributeValue("min"));
          maxParty = Integer.parseInt(subElement.attributeValue("max"));
        }
        else if ("return".equalsIgnoreCase(subElement.getName())) {
          ret = Location.parseLoc(subElement.attributeValue("loc"));
        } else if ("teleport".equalsIgnoreCase(subElement.getName()))
        {
          if (teleportLocs.isEmpty())
            teleportLocs = new ArrayList(1);
          teleportLocs.add(Location.parseLoc(subElement.attributeValue("loc")));
        }
        else if ("remove".equalsIgnoreCase(subElement.getName()))
        {
          removedItemId = Integer.parseInt(subElement.attributeValue("itemId"));
          removedItemCount = Integer.parseInt(subElement.attributeValue("count"));
          removedItemNecessity = Boolean.parseBoolean(subElement.attributeValue("necessary"));
        }
        else if ("give".equalsIgnoreCase(subElement.getName()))
        {
          giveItemId = Integer.parseInt(subElement.attributeValue("itemId"));
          givedItemCount = Integer.parseInt(subElement.attributeValue("count"));
        }
        else if ("quest".equalsIgnoreCase(subElement.getName()))
        {
          requiredQuestId = Integer.parseInt(subElement.attributeValue("id"));
        }
        else if ("reuse".equalsIgnoreCase(subElement.getName()))
        {
          resetReuse = new SchedulingPattern(subElement.attributeValue("resetReuse"));
          sharedReuseGroup = Integer.parseInt(subElement.attributeValue("sharedReuseGroup"));
          setReuseUponEntry = Boolean.parseBoolean(subElement.attributeValue("setUponEntry"));
        }
        else if ("geodata".equalsIgnoreCase(subElement.getName()))
        {
          String[] rxy = subElement.attributeValue("map").split("_");
          mapx = Integer.parseInt(rxy[0]);
          mapy = Integer.parseInt(rxy[1]);
        }
        else if ("doors".equalsIgnoreCase(subElement.getName()))
        {
          for (Element e : subElement.elements())
          {
            if (doors.isEmpty()) {
              doors = new HashIntObjectMap();
            }
            boolean opened = (e.attributeValue("opened") != null) && (Boolean.parseBoolean(e.attributeValue("opened")));
            boolean invul = (e.attributeValue("invul") == null) || (Boolean.parseBoolean(e.attributeValue("invul")));
            DoorTemplate template = DoorHolder.getInstance().getTemplate(Integer.parseInt(e.attributeValue("id")));
            doors.put(template.getNpcId(), new InstantZone.DoorInfo(template, opened, invul));
          }
        }
        else if ("zones".equalsIgnoreCase(subElement.getName()))
        {
          for (Element e : subElement.elements())
          {
            if (zones.isEmpty()) {
              zones = new HashMap();
            }
            boolean active = (e.attributeValue("active") != null) && (Boolean.parseBoolean(e.attributeValue("active")));
            ZoneTemplate template = ZoneHolder.getInstance().getTemplate(e.attributeValue("name"));
            if (template == null)
            {
              error("Zone: " + e.attributeValue("name") + " not found; file: " + getCurrentFileName());
              continue;
            }
            zones.put(template.getName(), new InstantZone.ZoneInfo(template, active));
          }
        }
        else if ("add_parameters".equalsIgnoreCase(subElement.getName()))
        {
          for (Element e : subElement.elements())
            if ("param".equalsIgnoreCase(e.getName()))
              params.set(e.attributeValue("name"), e.attributeValue("value"));
        }
        else if ("spawns".equalsIgnoreCase(subElement.getName()))
        {
          for (Element e : subElement.elements()) {
            if ("group".equalsIgnoreCase(e.getName()))
            {
              String group = e.attributeValue("name");
              boolean spawned = (e.attributeValue("spawned") != null) && (Boolean.parseBoolean(e.attributeValue("spawned")));
              List templates = SpawnHolder.getInstance().getSpawn(group);
              if (templates == null) {
                info("not find spawn group: " + group + " in file: " + getCurrentFileName());
              }
              else {
                if (spawns2.isEmpty()) {
                  spawns2 = new Hashtable();
                }
                spawns2.put(group, new InstantZone.SpawnInfo2(templates, spawned));
              }
            }
            else if ("spawn".equalsIgnoreCase(e.getName()))
            {
              String[] mobs = e.attributeValue("mobId").split(" ");

              String respawnNode = e.attributeValue("respawn");
              int respawn = respawnNode != null ? Integer.parseInt(respawnNode) : 0;

              String respawnRndNode = e.attributeValue("respawnRnd");
              int respawnRnd = respawnRndNode != null ? Integer.parseInt(respawnRndNode) : 0;

              String countNode = e.attributeValue("count");
              int count = countNode != null ? Integer.parseInt(countNode) : 1;

              List coords = new ArrayList();
              spawnType = 0;

              String spawnTypeNode = e.attributeValue("type");
              if ((spawnTypeNode == null) || (spawnTypeNode.equalsIgnoreCase("point")))
                spawnType = 0;
              else if (spawnTypeNode.equalsIgnoreCase("rnd"))
                spawnType = 1;
              else if (spawnTypeNode.equalsIgnoreCase("loc"))
                spawnType = 2;
              else {
                error("Spawn type  '" + spawnTypeNode + "' is unknown!");
              }
              for (Element e2 : e.elements()) {
                if ("coords".equalsIgnoreCase(e2.getName()))
                  coords.add(Location.parseLoc(e2.attributeValue("loc")));
              }
              Territory territory = null;
              if (spawnType == 2)
              {
                Polygon poly = new Polygon();
                for (Location loc : coords) {
                  poly.add(loc.x, loc.y).setZmin(loc.z).setZmax(loc.z);
                }
                if (!poly.validate()) {
                  error("invalid spawn territory for instance id : " + instanceId + " - " + poly + "!");
                }
                territory = new Territory().add(poly);
              }

              for (String mob : mobs)
              {
                int mobId = Integer.parseInt(mob);
                spawnDat = new InstantZone.SpawnInfo(spawnType, mobId, count, respawn, respawnRnd, coords, territory);
                spawns.add(spawnDat);
              }
            }
          }
        }
      }
      InstantZone instancedZone = new InstantZone(instanceId, name, resetReuse, sharedReuseGroup, timelimit, dispelBuffs, minLevel, maxLevel, minParty, maxParty, timer, onPartyDismiss, teleportLocs, ret, mapx, mapy, doors, zones, spawns2, spawns, collapseIfEmpty, maxChannels, removedItemId, removedItemCount, removedItemNecessity, giveItemId, givedItemCount, requiredQuestId, setReuseUponEntry, params);
      ((InstantZoneHolder)getHolder()).addInstantZone(instancedZone);
    }
  }
}