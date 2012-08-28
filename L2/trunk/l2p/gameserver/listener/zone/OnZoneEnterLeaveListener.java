package l2p.gameserver.listener.zone;

import l2p.commons.listener.Listener;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Zone;

public abstract interface OnZoneEnterLeaveListener extends Listener<Zone>
{
  public abstract void onZoneEnter(Zone paramZone, Creature paramCreature);

  public abstract void onZoneLeave(Zone paramZone, Creature paramCreature);
}