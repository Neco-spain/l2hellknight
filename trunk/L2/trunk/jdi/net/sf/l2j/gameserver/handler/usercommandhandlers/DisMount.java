package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.Ride;
import net.sf.l2j.gameserver.util.Broadcast;

public class DisMount
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 62 };

  public synchronized boolean useUserCommand(int id, L2PcInstance activeChar)
  {
    if (id != COMMAND_IDS[0]) return false;

    if (activeChar.isRentedPet())
    {
      activeChar.stopRentPet();
    }
    else if (activeChar.isMounted())
    {
      if (activeChar.setMountType(0))
      {
        if (activeChar.isFlying()) activeChar.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
        Ride dismount = new Ride(activeChar.getObjectId(), 0, 0);
        Broadcast.toSelfAndKnownPlayersInRadius(activeChar, dismount, 810000L);
        activeChar.setMountObjectID(0);
      }
    }

    return true;
  }

  public int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}