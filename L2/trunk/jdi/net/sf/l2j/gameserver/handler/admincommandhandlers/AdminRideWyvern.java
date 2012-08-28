package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.Ride;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class AdminRideWyvern
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_ride_wyvern", "admin_ride_strider", "admin_unride_wyvern", "admin_unride_strider", "admin_unride" };

  private static final int REQUIRED_LEVEL = Config.GM_RIDER;
  private int _petRideId;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) return false;

    if (command.startsWith("admin_ride"))
    {
      if ((activeChar.isMounted()) || (activeChar.getPet() != null)) {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
        sm.addString("Already Have a Pet or Mounted.");
        activeChar.sendPacket(sm);
        return false;
      }
      if (command.startsWith("admin_ride_wyvern")) {
        _petRideId = 12621;
      }
      else if (command.startsWith("admin_ride_strider")) {
        _petRideId = 12526;
      }
      else
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
        sm.addString("Command '" + command + "' not recognized");
        activeChar.sendPacket(sm);
        return false;
      }
      if (!activeChar.disarmWeapons()) return false;
      Ride mount = new Ride(activeChar.getObjectId(), 1, _petRideId);
      activeChar.sendPacket(mount);
      activeChar.broadcastPacket(mount);
      activeChar.setMountType(mount.getMountType());
    }
    else if (command.startsWith("admin_unride"))
    {
      if (activeChar.setMountType(0))
      {
        Ride dismount = new Ride(activeChar.getObjectId(), 0, 0);
        activeChar.broadcastPacket(dismount);
      }
    }
    return true;
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }
}