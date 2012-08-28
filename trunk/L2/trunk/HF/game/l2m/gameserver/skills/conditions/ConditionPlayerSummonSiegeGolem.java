package l2m.gameserver.skills.conditions;

import l2p.commons.collections.MultiValueSet;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Zone;
import l2m.gameserver.model.Zone.ZoneType;
import l2m.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2m.gameserver.model.entity.events.impl.SiegeEvent;
import l2m.gameserver.skills.Env;

public class ConditionPlayerSummonSiegeGolem extends Condition
{
  protected boolean testImpl(Env env)
  {
    Player player = env.character.getPlayer();
    if (player == null)
      return false;
    Zone zone = player.getZone(Zone.ZoneType.RESIDENCE);
    if (zone != null)
      return false;
    zone = player.getZone(Zone.ZoneType.SIEGE);
    if (zone == null)
      return false;
    SiegeEvent event = (SiegeEvent)player.getEvent(SiegeEvent.class);
    if (event == null)
      return false;
    if ((event instanceof CastleSiegeEvent))
    {
      if (zone.getParams().getInteger("residence") != event.getId())
        return false;
      if (event.getSiegeClan("attackers", player.getClan()) == null) {
        return false;
      }
    }
    else if (event.getSiegeClan("defenders", player.getClan()) == null) {
      return false;
    }
    return true;
  }
}