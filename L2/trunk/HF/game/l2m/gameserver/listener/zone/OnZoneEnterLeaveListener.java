package l2m.gameserver.listener.zone;

import l2p.commons.listener.Listener;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Zone;

public abstract interface OnZoneEnterLeaveListener extends Listener<Zone>
{
  public abstract void onZoneEnter(Zone paramZone, Creature paramCreature);

  public abstract void onZoneLeave(Zone paramZone, Creature paramCreature);
}