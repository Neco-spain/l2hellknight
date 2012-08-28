package scripts.commands.usercommandhandlers;

import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.commands.IUserCommandHandler;

public class PartyInfo
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 81 };

  public boolean useUserCommand(int id, L2PcInstance activeChar)
  {
    if (id != COMMAND_IDS[0]) return false;

    if (!activeChar.isInParty())
    {
      activeChar.sendMessage("You are not in a party.");
      return false;
    }

    L2Party playerParty = activeChar.getParty();
    int memberCount = playerParty.getMemberCount();
    int lootDistribution = playerParty.getLootDistribution();
    String partyLeader = ((L2PcInstance)playerParty.getPartyMembers().get(0)).getName();

    activeChar.sendPacket(SystemMessage.id(SystemMessageId.PARTY_INFORMATION));

    switch (lootDistribution) {
    case 0:
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.LOOTING_FINDERS_KEEPERS));
      break;
    case 3:
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.LOOTING_BY_TURN));
      break;
    case 4:
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.LOOTING_BY_TURN_INCLUDE_SPOIL));
      break;
    case 1:
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.LOOTING_RANDOM));
      break;
    case 2:
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.LOOTING_RANDOM_INCLUDE_SPOIL));
    }

    activeChar.sendPacket(SystemMessage.id(SystemMessageId.PARTY_LEADER_S1).addString(partyLeader));

    return true;
  }

  public int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}