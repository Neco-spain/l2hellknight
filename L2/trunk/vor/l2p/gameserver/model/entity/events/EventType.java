package l2p.gameserver.model.entity.events;

public enum EventType
{
  MAIN_EVENT, 
  SIEGE_EVENT, 
  PVP_EVENT, 
  BOAT_EVENT, 
  FUN_EVENT;

  private int _step;

  private EventType() {
    _step = (ordinal() * 1000);
  }

  public int step()
  {
    return _step;
  }
}