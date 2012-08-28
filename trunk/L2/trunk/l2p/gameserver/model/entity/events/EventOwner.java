package l2p.gameserver.model.entity.events;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public abstract class EventOwner
  implements Serializable
{
  public static final long serialVersionUID = 1L;
  private Set<GlobalEvent> _events = new HashSet(2);

  public <E extends GlobalEvent> E getEvent(Class<E> eventClass)
  {
    for (GlobalEvent e : _events)
    {
      if (e.getClass() == eventClass)
        return e;
      if (eventClass.isAssignableFrom(e.getClass())) {
        return e;
      }
    }
    return null;
  }

  public void addEvent(GlobalEvent event)
  {
    _events.add(event);
  }

  public void removeEvent(GlobalEvent event)
  {
    _events.remove(event);
  }

  public Set<GlobalEvent> getEvents()
  {
    return _events;
  }
}