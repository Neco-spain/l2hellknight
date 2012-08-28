package scripts.events;

public abstract class EventHandler
{
  private Object _owner;

  public EventHandler(Object owner)
  {
    _owner = owner;
  }

  public final Object getOwner()
  {
    return _owner;
  }

  public final boolean equals(Object object)
  {
    return ((object instanceof EventHandler)) && (_owner == ((EventHandler)object)._owner);
  }

  public abstract void handler(Object paramObject, IEventParams paramIEventParams);
}