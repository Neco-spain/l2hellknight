package l2m.gameserver.data.xml.holder;

import l2p.commons.data.xml.AbstractHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.events.EventType;
import l2m.gameserver.model.entity.events.GlobalEvent;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

public final class EventHolder extends AbstractHolder
{
  private static final EventHolder _instance = new EventHolder();
  private final IntObjectMap<GlobalEvent> _events = new TreeIntObjectMap();

  public static EventHolder getInstance()
  {
    return _instance;
  }

  public void addEvent(EventType type, GlobalEvent event)
  {
    _events.put(type.step() + event.getId(), event);
  }

  public <E extends GlobalEvent> E getEvent(EventType type, int id)
  {
    return (GlobalEvent)_events.get(type.step() + id);
  }

  public void findEvent(Player player)
  {
    for (GlobalEvent event : _events.values())
      if (event.isParticle(player))
        player.addEvent(event);
  }

  public void callInit()
  {
    for (GlobalEvent event : _events.values())
      event.initEvent();
  }

  public int size()
  {
    return _events.size();
  }

  public void clear()
  {
    _events.clear();
  }
}