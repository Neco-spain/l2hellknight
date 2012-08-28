package l2m.gameserver.model.entity.events.objects;

import l2m.gameserver.model.entity.events.GlobalEvent;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.utils.Location;
import l2m.gameserver.utils.NpcUtils;

public class SpawnSimpleObject
  implements SpawnableObject
{
  private static final long serialVersionUID = 1L;
  private int _npcId;
  private Location _loc;
  private NpcInstance _npc;

  public SpawnSimpleObject(int npcId, Location loc)
  {
    _npcId = npcId;
    _loc = loc;
  }

  public void spawnObject(GlobalEvent event)
  {
    _npc = NpcUtils.spawnSingle(_npcId, _loc, event.getReflection());
    _npc.addEvent(event);
  }

  public void despawnObject(GlobalEvent event)
  {
    _npc.removeEvent(event);
    _npc.deleteMe();
  }

  public void refreshObject(GlobalEvent event)
  {
  }
}