package l2p.gameserver.model.entity.events.objects;

import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.entity.events.GlobalEvent;
import l2p.gameserver.model.instances.DoorInstance;

public class DoorObject
  implements SpawnableObject, InitableObject
{
  private int _id;
  private DoorInstance _door;
  private boolean _weak;

  public DoorObject(int id)
  {
    _id = id;
  }

  public void initObject(GlobalEvent e)
  {
    _door = e.getReflection().getDoor(_id);
  }

  public void spawnObject(GlobalEvent event)
  {
    refreshObject(event);
  }

  public void despawnObject(GlobalEvent event)
  {
    Reflection ref = event.getReflection();
    if (ref == ReflectionManager.DEFAULT)
    {
      refreshObject(event);
    }
  }

  public void refreshObject(GlobalEvent event)
  {
    if (!event.isInProgress())
      _door.removeEvent(event);
    else {
      _door.addEvent(event);
    }
    if (_door.getCurrentHp() <= 0.0D)
    {
      _door.decayMe();
      _door.spawnMe();
    }

    _door.setCurrentHp(_door.getMaxHp() * (isWeak() ? 0.5D : 1.0D), true);
    close(event);
  }

  public int getUId()
  {
    return _door.getDoorId();
  }

  public int getUpgradeValue()
  {
    return _door.getUpgradeHp();
  }

  public void setUpgradeValue(GlobalEvent event, int val)
  {
    _door.setUpgradeHp(val);
    refreshObject(event);
  }

  public void open(GlobalEvent e)
  {
    _door.openMe(null, !e.isInProgress());
  }

  public void close(GlobalEvent e)
  {
    _door.closeMe(null, !e.isInProgress());
  }

  public DoorInstance getDoor()
  {
    return _door;
  }

  public boolean isWeak()
  {
    return _weak;
  }

  public void setWeak(boolean weak)
  {
    _weak = weak;
  }
}