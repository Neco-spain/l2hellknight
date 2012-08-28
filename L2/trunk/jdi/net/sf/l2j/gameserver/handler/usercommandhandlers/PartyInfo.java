package net.sf.l2j.gameserver.handler.usercommandhandlers;

import java.util.List;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class PartyInfo
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 81 };

  public boolean useUserCommand(int id, L2PcInstance activeChar)
  {
    if (id != COMMAND_IDS[0]) return false;

    if (!activeChar.isInParty())
    {
      SystemMessage sm = SystemMessage.sendString("You are not in a party.");
      activeChar.sendPacket(sm);
      return false;
    }

    L2Party playerParty = activeChar.getParty();
    int memberCount = playerParty.getMemberCount();
    int lootDistribution = playerParty.getLootDistribution();
    String partyLeader = ((L2PcInstance)playerParty.getPartyMembers().get(0)).getName();

    activeChar.sendPacket(new SystemMessage(SystemMessageId.PARTY_INFORMATION));

    switch (lootDistribution) {
    case 0:
      activeChar.sendPacket(new SystemMessage(SystemMessageId.LOOTING_FINDERS_KEEPERS));
      break;
    case 3:
      activeChar.sendPacket(new SystemMessage(SystemMessageId.LOOTING_BY_TURN));
      break;
    case 4:
      activeChar.sendPacket(new SystemMessage(SystemMessageId.LOOTING_BY_TURN_INCLUDE_SPOIL));
      break;
    case 1:
      activeChar.sendPacket(new SystemMessage(SystemMessageId.LOOTING_RANDOM));
      break;
    case 2:
      activeChar.sendPacket(new SystemMessage(SystemMessageId.LOOTING_RANDOM_INCLUDE_SPOIL));
    }

    SystemMessage sm = new SystemMessage(SystemMessageId.PARTY_LEADER_S1);
    sm.addString(partyLeader);
    activeChar.sendPacket(sm);

    sm = new SystemMessage(SystemMessageId.S1_S2);
    sm.addString("Members: " + memberCount + "/9");

    activeChar.sendPacket(new SystemMessage(SystemMessageId.WAR_LIST));
    return true;
  }

  public int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}