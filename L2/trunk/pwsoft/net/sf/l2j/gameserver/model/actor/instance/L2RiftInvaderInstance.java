package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2RiftInvaderInstance extends L2MonsterInstance
{
  public L2RiftInvaderInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public boolean isL2RiftInvader()
  {
    return true;
  }
}