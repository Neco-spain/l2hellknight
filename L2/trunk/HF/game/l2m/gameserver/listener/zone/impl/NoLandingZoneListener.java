package l2m.gameserver.listener.zone.impl;

import l2p.commons.collections.MultiValueSet;
import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Zone;
import l2m.gameserver.model.entity.residence.Residence;
import l2m.gameserver.network.serverpackets.components.SystemMsg;

public class NoLandingZoneListener
  implements OnZoneEnterLeaveListener
{
  public static final OnZoneEnterLeaveListener STATIC = new NoLandingZoneListener();

  public void onZoneEnter(Zone zone, Creature actor)
  {
    Player player = actor.getPlayer();
    if ((player != null) && 
      (player.isFlying()) && (player.getMountNpcId() == 12621))
    {
      Residence residence = ResidenceHolder.getInstance().getResidence(zone.getParams().getInteger("residence", 0));
      if ((residence == null) || (player.getClan() == null) || (residence.getOwner() != player.getClan()))
      {
        player.stopMove();
        player.sendPacket(SystemMsg.THIS_AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_ATOP_OF_A_WYVERN);
        player.setMount(0, 0, 0);
      }
    }
  }

  public void onZoneLeave(Zone zone, Creature cha)
  {
  }
}