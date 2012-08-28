package l2m.gameserver.handler.admincommands.impl;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.instancemanager.MapRegionManager;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.model.Zone;
import l2m.gameserver.model.Zone.ZoneType;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.model.entity.residence.Castle;
import l2m.gameserver.templates.mapregion.DomainArea;

public class AdminZone
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if ((activeChar == null) || (!activeChar.getPlayerAccess().CanTeleport)) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminZone$Commands[command.ordinal()])
    {
    case 1:
      activeChar.sendMessage(new StringBuilder().append("Current region: ").append(activeChar.getCurrentRegion()).toString());
      activeChar.sendMessage("Zone list:");
      List zones = new ArrayList();
      World.getZones(zones, activeChar.getLoc(), activeChar.getReflection());
      for (Zone zone : zones) {
        activeChar.sendMessage(new StringBuilder().append(zone.getType().toString()).append(", name: ").append(zone.getName()).append(", state: ").append(zone.isActive() ? "active" : "not active").append(", inside: ").append(zone.checkIfInZone(activeChar)).append("/").append(zone.checkIfInZone(activeChar.getX(), activeChar.getY(), activeChar.getZ())).toString());
      }
      break;
    case 2:
      activeChar.sendMessage(new StringBuilder().append("Current region: ").append(activeChar.getCurrentRegion()).toString());
      activeChar.sendMessage("Objects list:");
      for (GameObject o : activeChar.getCurrentRegion())
        if (o != null)
          activeChar.sendMessage(o.toString());
      break;
    case 3:
      activeChar.sendMessage(new StringBuilder().append("Current region: ").append(activeChar.getCurrentRegion()).toString());
      activeChar.sendMessage(new StringBuilder().append("Players count: ").append(World.getAroundPlayers(activeChar).size()).toString());
      break;
    case 4:
      String pos = new StringBuilder().append(activeChar.getX()).append(", ").append(activeChar.getY()).append(", ").append(activeChar.getZ()).append(", ").append(activeChar.getHeading()).append(" Geo [").append(activeChar.getX() - World.MAP_MIN_X >> 4).append(", ").append(activeChar.getY() - World.MAP_MIN_Y >> 4).append("] Ref ").append(activeChar.getReflectionId()).toString();
      activeChar.sendMessage(new StringBuilder().append("Pos: ").append(pos).toString());
      break;
    case 5:
      DomainArea domain = (DomainArea)MapRegionManager.getInstance().getRegionData(DomainArea.class, activeChar);
      Castle castle = domain != null ? (Castle)ResidenceHolder.getInstance().getResidence(Castle.class, domain.getId()) : null;
      if (castle != null)
        activeChar.sendMessage(new StringBuilder().append("Domain: ").append(castle.getName()).toString());
      else {
        activeChar.sendMessage("Domain: Unknown");
      }
    }
    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_zone_check, 
    admin_region, 
    admin_pos, 
    admin_vis_count, 
    admin_domain;
  }
}