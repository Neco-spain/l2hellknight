package l2m.gameserver.listener.zone.impl;

import l2m.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Zone;
import l2m.gameserver.model.entity.boat.ClanAirShip;
import l2m.gameserver.model.instances.ClanAirShipControllerInstance;

public class AirshipControllerZoneListener
  implements OnZoneEnterLeaveListener
{
  private ClanAirShipControllerInstance _controllerInstance;

  public void onZoneEnter(Zone zone, Creature actor)
  {
    if ((_controllerInstance == null) && ((actor instanceof ClanAirShipControllerInstance)))
      _controllerInstance = ((ClanAirShipControllerInstance)actor);
    else if (actor.isClanAirShip())
      _controllerInstance.setDockedShip((ClanAirShip)actor);
  }

  public void onZoneLeave(Zone zone, Creature actor)
  {
    if (actor.isClanAirShip())
      _controllerInstance.setDockedShip(null);
  }
}