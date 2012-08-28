package l2m.gameserver.listener.zone.impl;

import l2p.commons.collections.MultiValueSet;
import l2m.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Zone;
import l2m.gameserver.model.entity.residence.Residence;
import l2m.gameserver.model.entity.residence.ResidenceFunction;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.skills.funcs.FuncMul;

public class ResidenceEnterLeaveListenerImpl
  implements OnZoneEnterLeaveListener
{
  public static final OnZoneEnterLeaveListener STATIC = new ResidenceEnterLeaveListenerImpl();

  public void onZoneEnter(Zone zone, Creature actor)
  {
    if (!actor.isPlayer()) {
      return;
    }
    Player player = (Player)actor;
    Residence residence = (Residence)zone.getParams().get("residence");

    if ((residence.getOwner() == null) || (residence.getOwner() != player.getClan())) {
      return;
    }
    if (residence.isFunctionActive(3))
    {
      double value = 1.0D + residence.getFunction(3).getLevel() / 100.0D;

      player.addStatFunc(new FuncMul(Stats.REGENERATE_HP_RATE, 48, residence, value));
    }

    if (residence.isFunctionActive(4))
    {
      double value = 1.0D + residence.getFunction(4).getLevel() / 100.0D;

      player.addStatFunc(new FuncMul(Stats.REGENERATE_MP_RATE, 48, residence, value));
    }
  }

  public void onZoneLeave(Zone zone, Creature actor)
  {
    if (!actor.isPlayer()) {
      return;
    }
    Residence residence = (Residence)zone.getParams().get("residence");

    actor.removeStatsOwner(residence);
  }
}