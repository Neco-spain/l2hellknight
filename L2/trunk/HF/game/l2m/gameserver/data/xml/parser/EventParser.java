package l2m.gameserver.data.xml.parser;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import l2p.commons.collections.MultiValueSet;
import l2p.commons.data.xml.AbstractDirParser;
import l2m.gameserver.Config;
import l2m.gameserver.data.xml.holder.EventHolder;
import l2m.gameserver.model.entity.events.EventAction;
import l2m.gameserver.model.entity.events.EventType;
import l2m.gameserver.model.entity.events.GlobalEvent;
import l2m.gameserver.model.entity.events.actions.ActiveDeactiveAction;
import l2m.gameserver.model.entity.events.actions.AnnounceAction;
import l2m.gameserver.model.entity.events.actions.GiveItemAction;
import l2m.gameserver.model.entity.events.actions.IfElseAction;
import l2m.gameserver.model.entity.events.actions.InitAction;
import l2m.gameserver.model.entity.events.actions.NpcSayAction;
import l2m.gameserver.model.entity.events.actions.OpenCloseAction;
import l2m.gameserver.model.entity.events.actions.PlaySoundAction;
import l2m.gameserver.model.entity.events.actions.RefreshAction;
import l2m.gameserver.model.entity.events.actions.SayAction;
import l2m.gameserver.model.entity.events.actions.SpawnDespawnAction;
import l2m.gameserver.model.entity.events.actions.StartStopAction;
import l2m.gameserver.model.entity.events.actions.TeleportPlayersAction;
import l2m.gameserver.model.entity.events.objects.BoatPoint;
import l2m.gameserver.model.entity.events.objects.CTBTeamObject;
import l2m.gameserver.model.entity.events.objects.CastleDamageZoneObject;
import l2m.gameserver.model.entity.events.objects.DoorObject;
import l2m.gameserver.model.entity.events.objects.FortressCombatFlagObject;
import l2m.gameserver.model.entity.events.objects.SiegeToggleNpcObject;
import l2m.gameserver.model.entity.events.objects.SpawnExObject;
import l2m.gameserver.model.entity.events.objects.StaticObjectObject;
import l2m.gameserver.model.entity.events.objects.TerritoryWardObject;
import l2m.gameserver.model.entity.events.objects.ZoneObject;
import l2m.gameserver.network.serverpackets.PlaySound.Type;
import l2m.gameserver.network.serverpackets.components.ChatType;
import l2m.gameserver.network.serverpackets.components.NpcString;
import l2m.gameserver.network.serverpackets.components.SysString;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.utils.Location;
import org.dom4j.Element;

public final class EventParser extends AbstractDirParser<EventHolder>
{
  private static final EventParser _instance = new EventParser();

  public static EventParser getInstance()
  {
    return _instance;
  }

  protected EventParser()
  {
    super(EventHolder.getInstance());
  }

  public File getXMLDir()
  {
    return new File(Config.DATAPACK_ROOT, "data/events/");
  }

  public boolean isIgnored(File f)
  {
    return false;
  }

  public String getDTDFileName()
  {
    return "events.dtd";
  }

  protected void readData(Element rootElement)
    throws Exception
  {
    for (Iterator iterator = rootElement.elementIterator("event"); iterator.hasNext(); )
    {
      Element eventElement = (Element)iterator.next();
      int id = Integer.parseInt(eventElement.attributeValue("id"));
      String name = eventElement.attributeValue("name");
      String impl = eventElement.attributeValue("impl");
      EventType type = EventType.valueOf(eventElement.attributeValue("type"));
      Class eventClass = null;
      try
      {
        eventClass = Class.forName("l2p.gameserver.model.entity.events.impl." + impl + "Event");
      }
      catch (ClassNotFoundException e)
      {
        info("Not found impl class: " + impl + "; File: " + getCurrentFileName());
      }continue;

      Constructor constructor = eventClass.getConstructor(new Class[] { MultiValueSet.class });

      MultiValueSet set = new MultiValueSet();
      set.set("id", id);
      set.set("name", name);

      for (Iterator parameterIterator = eventElement.elementIterator("parameter"); parameterIterator.hasNext(); )
      {
        Element parameterElement = (Element)parameterIterator.next();
        set.set(parameterElement.attributeValue("name"), parameterElement.attributeValue("value"));
      }

      GlobalEvent event = (GlobalEvent)constructor.newInstance(new Object[] { set });

      event.addOnStartActions(parseActions(eventElement.element("on_start"), 2147483647));
      event.addOnStopActions(parseActions(eventElement.element("on_stop"), 2147483647));
      event.addOnInitActions(parseActions(eventElement.element("on_init"), 2147483647));

      Element onTime = eventElement.element("on_time");
      Iterator onTimeIterator;
      if (onTime != null) {
        for (onTimeIterator = onTime.elementIterator("on"); onTimeIterator.hasNext(); )
        {
          Element on = (Element)onTimeIterator.next();
          int time = Integer.parseInt(on.attributeValue("time"));

          List actions = parseActions(on, time);

          event.addOnTimeActions(time, actions);
        }
      }
      for (Iterator objectIterator = eventElement.elementIterator("objects"); objectIterator.hasNext(); )
      {
        Element objectElement = (Element)objectIterator.next();
        String objectsName = objectElement.attributeValue("name");
        List objects = parseObjects(objectElement);

        event.addObjects(objectsName, objects);
      }

      ((EventHolder)getHolder()).addEvent(type, event);
    }
  }

  private List<Serializable> parseObjects(Element element)
  {
    if (element == null) {
      return Collections.emptyList();
    }
    List objects = new ArrayList(2);
    for (Iterator objectsIterator = element.elementIterator(); objectsIterator.hasNext(); )
    {
      Element objectsElement = (Element)objectsIterator.next();
      String nodeName = objectsElement.getName();
      if (nodeName.equalsIgnoreCase("boat_point")) {
        objects.add(BoatPoint.parse(objectsElement));
      } else if (nodeName.equalsIgnoreCase("point")) {
        objects.add(Location.parse(objectsElement));
      } else if (nodeName.equalsIgnoreCase("spawn_ex")) {
        objects.add(new SpawnExObject(objectsElement.attributeValue("name")));
      } else if (nodeName.equalsIgnoreCase("door")) {
        objects.add(new DoorObject(Integer.parseInt(objectsElement.attributeValue("id"))));
      } else if (nodeName.equalsIgnoreCase("static_object")) {
        objects.add(new StaticObjectObject(Integer.parseInt(objectsElement.attributeValue("id"))));
      } else if (nodeName.equalsIgnoreCase("combat_flag"))
      {
        int x = Integer.parseInt(objectsElement.attributeValue("x"));
        int y = Integer.parseInt(objectsElement.attributeValue("y"));
        int z = Integer.parseInt(objectsElement.attributeValue("z"));
        objects.add(new FortressCombatFlagObject(new Location(x, y, z)));
      }
      else if (nodeName.equalsIgnoreCase("territory_ward"))
      {
        int x = Integer.parseInt(objectsElement.attributeValue("x"));
        int y = Integer.parseInt(objectsElement.attributeValue("y"));
        int z = Integer.parseInt(objectsElement.attributeValue("z"));
        int itemId = Integer.parseInt(objectsElement.attributeValue("item_id"));
        int npcId = Integer.parseInt(objectsElement.attributeValue("npc_id"));
        objects.add(new TerritoryWardObject(itemId, npcId, new Location(x, y, z)));
      }
      else if (nodeName.equalsIgnoreCase("siege_toggle_npc"))
      {
        int id = Integer.parseInt(objectsElement.attributeValue("id"));
        int fakeId = Integer.parseInt(objectsElement.attributeValue("fake_id"));
        int x = Integer.parseInt(objectsElement.attributeValue("x"));
        int y = Integer.parseInt(objectsElement.attributeValue("y"));
        int z = Integer.parseInt(objectsElement.attributeValue("z"));
        int hp = Integer.parseInt(objectsElement.attributeValue("hp"));
        Set set = Collections.emptySet();
        for (Iterator oIterator = objectsElement.elementIterator(); oIterator.hasNext(); )
        {
          Element sub = (Element)oIterator.next();
          if (set.isEmpty())
            set = new HashSet();
          set.add(sub.attributeValue("name"));
        }
        objects.add(new SiegeToggleNpcObject(id, fakeId, new Location(x, y, z), hp, set));
      }
      else if (nodeName.equalsIgnoreCase("castle_zone"))
      {
        long price = Long.parseLong(objectsElement.attributeValue("price"));
        objects.add(new CastleDamageZoneObject(objectsElement.attributeValue("name"), price));
      }
      else if (nodeName.equalsIgnoreCase("zone"))
      {
        objects.add(new ZoneObject(objectsElement.attributeValue("name")));
      }
      else if (nodeName.equalsIgnoreCase("ctb_team"))
      {
        int mobId = Integer.parseInt(objectsElement.attributeValue("mob_id"));
        int flagId = Integer.parseInt(objectsElement.attributeValue("id"));
        Location loc = Location.parse(objectsElement);

        objects.add(new CTBTeamObject(mobId, flagId, loc));
      }
    }

    return objects;
  }

  private List<EventAction> parseActions(Element element, int time)
  {
    if (element == null) {
      return Collections.emptyList();
    }
    IfElseAction lastIf = null;
    List actions = new ArrayList(0);
    for (Iterator iterator = element.elementIterator(); iterator.hasNext(); )
    {
      Element actionElement = (Element)iterator.next();
      if (actionElement.getName().equalsIgnoreCase("start"))
      {
        String name = actionElement.attributeValue("name");
        StartStopAction startStopAction = new StartStopAction(name, true);
        actions.add(startStopAction);
      }
      else if (actionElement.getName().equalsIgnoreCase("stop"))
      {
        String name = actionElement.attributeValue("name");
        StartStopAction startStopAction = new StartStopAction(name, false);
        actions.add(startStopAction);
      }
      else if (actionElement.getName().equalsIgnoreCase("spawn"))
      {
        String name = actionElement.attributeValue("name");
        SpawnDespawnAction spawnDespawnAction = new SpawnDespawnAction(name, true);
        actions.add(spawnDespawnAction);
      }
      else if (actionElement.getName().equalsIgnoreCase("despawn"))
      {
        String name = actionElement.attributeValue("name");
        SpawnDespawnAction spawnDespawnAction = new SpawnDespawnAction(name, false);
        actions.add(spawnDespawnAction);
      }
      else if (actionElement.getName().equalsIgnoreCase("open"))
      {
        String name = actionElement.attributeValue("name");
        OpenCloseAction a = new OpenCloseAction(true, name);
        actions.add(a);
      }
      else if (actionElement.getName().equalsIgnoreCase("close"))
      {
        String name = actionElement.attributeValue("name");
        OpenCloseAction a = new OpenCloseAction(false, name);
        actions.add(a);
      }
      else if (actionElement.getName().equalsIgnoreCase("active"))
      {
        String name = actionElement.attributeValue("name");
        ActiveDeactiveAction a = new ActiveDeactiveAction(true, name);
        actions.add(a);
      }
      else if (actionElement.getName().equalsIgnoreCase("deactive"))
      {
        String name = actionElement.attributeValue("name");
        ActiveDeactiveAction a = new ActiveDeactiveAction(false, name);
        actions.add(a);
      }
      else if (actionElement.getName().equalsIgnoreCase("refresh"))
      {
        String name = actionElement.attributeValue("name");
        RefreshAction a = new RefreshAction(name);
        actions.add(a);
      }
      else if (actionElement.getName().equalsIgnoreCase("init"))
      {
        String name = actionElement.attributeValue("name");
        InitAction a = new InitAction(name);
        actions.add(a);
      }
      else if (actionElement.getName().equalsIgnoreCase("npc_say"))
      {
        int npc = Integer.parseInt(actionElement.attributeValue("npc"));
        ChatType chat = ChatType.valueOf(actionElement.attributeValue("chat"));
        int range = Integer.parseInt(actionElement.attributeValue("range"));
        NpcString string = NpcString.valueOf(actionElement.attributeValue("text"));
        NpcSayAction action = new NpcSayAction(npc, range, chat, string);
        actions.add(action);
      }
      else if (actionElement.getName().equalsIgnoreCase("play_sound"))
      {
        int range = Integer.parseInt(actionElement.attributeValue("range"));
        String sound = actionElement.attributeValue("sound");
        PlaySound.Type type = PlaySound.Type.valueOf(actionElement.attributeValue("type"));

        PlaySoundAction action = new PlaySoundAction(range, sound, type);
        actions.add(action);
      }
      else if (actionElement.getName().equalsIgnoreCase("give_item"))
      {
        int itemId = Integer.parseInt(actionElement.attributeValue("id"));
        long count = Integer.parseInt(actionElement.attributeValue("count"));

        GiveItemAction action = new GiveItemAction(itemId, count);
        actions.add(action);
      }
      else if (actionElement.getName().equalsIgnoreCase("announce"))
      {
        String val = actionElement.attributeValue("val");
        if ((val == null) && (time == 2147483647))
        {
          info("Can't get announce time." + getCurrentFileName());
          continue;
        }

        int val2 = val == null ? time : Integer.parseInt(val);
        EventAction action = new AnnounceAction(val2);
        actions.add(action);
      }
      else if (actionElement.getName().equalsIgnoreCase("if"))
      {
        String name = actionElement.attributeValue("name");
        IfElseAction action = new IfElseAction(name, false);

        action.setIfList(parseActions(actionElement, time));
        actions.add(action);

        lastIf = action;
      }
      else if (actionElement.getName().equalsIgnoreCase("ifnot"))
      {
        String name = actionElement.attributeValue("name");
        IfElseAction action = new IfElseAction(name, true);

        action.setIfList(parseActions(actionElement, time));
        actions.add(action);

        lastIf = action;
      }
      else if (actionElement.getName().equalsIgnoreCase("else"))
      {
        if (lastIf == null)
          info("Not find <if> for <else> tag");
        else
          lastIf.setElseList(parseActions(actionElement, time));
      }
      else if (actionElement.getName().equalsIgnoreCase("say"))
      {
        ChatType chat = ChatType.valueOf(actionElement.attributeValue("chat"));
        int range = Integer.parseInt(actionElement.attributeValue("range"));

        String how = actionElement.attributeValue("how");
        String text = actionElement.attributeValue("text");

        SysString sysString = SysString.valueOf2(how);

        SayAction sayAction = null;
        if (sysString != null)
          sayAction = new SayAction(range, chat, sysString, SystemMsg.valueOf(text));
        else {
          sayAction = new SayAction(range, chat, how, NpcString.valueOf(text));
        }
        actions.add(sayAction);
      }
      else if (actionElement.getName().equalsIgnoreCase("teleport_players"))
      {
        String name = actionElement.attributeValue("id");
        TeleportPlayersAction a = new TeleportPlayersAction(name);
        actions.add(a);
      }
    }

    return actions.isEmpty() ? Collections.emptyList() : actions;
  }
}