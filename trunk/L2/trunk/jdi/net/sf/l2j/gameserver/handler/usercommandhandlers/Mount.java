package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.Ride;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Broadcast;

public class Mount
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 61 };

  public synchronized boolean useUserCommand(int id, L2PcInstance activeChar)
  {
    if (id != COMMAND_IDS[0]) return false;

    L2Summon pet = activeChar.getPet();

    if ((pet != null) && (pet.isMountable()) && (!activeChar.isMounted()))
    {
      if (activeChar.isDead())
      {
        SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
        activeChar.sendPacket(msg);
      }
      else if (pet.isDead())
      {
        SystemMessage msg = new SystemMessage(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
        activeChar.sendPacket(msg);
      }
      else if (pet.isInCombat())
      {
        SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
        activeChar.sendPacket(msg);
      }
      else if (activeChar.isInCombat())
      {
        SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
        activeChar.sendPacket(msg);
      }
      else if ((activeChar.isSitting()) || (activeChar.isMoving()))
      {
        SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
        activeChar.sendPacket(msg);
      }
      else if ((!pet.isDead()) && (!activeChar.isMounted()))
      {
        if (!activeChar.disarmWeapons()) return false;
        Ride mount = new Ride(activeChar.getObjectId(), 1, pet.getTemplate().npcId);
        Broadcast.toSelfAndKnownPlayersInRadius(activeChar, mount, 810000L);
        activeChar.setMountType(mount.getMountType());
        activeChar.setMountObjectID(pet.getControlItemId());
        pet.unSummon(activeChar);
      }
    }
    else if (activeChar.isRentedPet())
    {
      activeChar.stopRentPet();
    }
    else if (activeChar.isMounted())
    {
      if (activeChar.setMountType(0))
      {
        if (activeChar.isFlying()) activeChar.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
        Ride dismount = new Ride(activeChar.getObjectId(), 0, 0);
        Broadcast.toSelfAndKnownPlayers(activeChar, dismount);
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