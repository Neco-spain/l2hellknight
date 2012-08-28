package l2m.gameserver.model.entity.events.objects;

import java.util.Set;
import l2m.gameserver.data.xml.holder.NpcHolder;
import l2m.gameserver.model.entity.events.GlobalEvent;
import l2m.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.utils.Location;

public class SiegeToggleNpcObject
  implements SpawnableObject
{
  private static final long serialVersionUID = 1L;
  private SiegeToggleNpcInstance _toggleNpc;
  private Location _location;

  public SiegeToggleNpcObject(int id, int fakeNpcId, Location loc, int hp, Set<String> set)
  {
    _location = loc;

    _toggleNpc = ((SiegeToggleNpcInstance)NpcHolder.getInstance().getTemplate(id).getNewInstance());

    _toggleNpc.initFake(fakeNpcId);
    _toggleNpc.setMaxHp(hp);
    _toggleNpc.setZoneList(set);
  }

  public void spawnObject(GlobalEvent event)
  {
    _toggleNpc.decayFake();

    if (event.isInProgress())
      _toggleNpc.addEvent(event);
    else {
      _toggleNpc.removeEvent(event);
    }
    _toggleNpc.setCurrentHp(_toggleNpc.getMaxHp(), true);
    _toggleNpc.spawnMe(_location);
  }

  public void despawnObject(GlobalEvent event)
  {
    _toggleNpc.removeEvent(event);
    _toggleNpc.decayFake();
    _toggleNpc.decayMe();
  }

  public void refreshObject(GlobalEvent event)
  {
  }

  public SiegeToggleNpcInstance getToggleNpc()
  {
    return _toggleNpc;
  }

  public boolean isAlive()
  {
    return _toggleNpc.isVisible();
  }
}