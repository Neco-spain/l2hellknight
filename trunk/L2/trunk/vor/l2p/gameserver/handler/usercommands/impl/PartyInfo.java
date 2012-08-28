package l2p.gameserver.handler.usercommands.impl;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.handler.usercommands.IUserCommandHandler;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.CustomMessage;

public class PartyInfo
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 81 };

  public boolean useUserCommand(int id, Player activeChar)
  {
    if (id != COMMAND_IDS[0]) {
      return false;
    }
    Party playerParty = activeChar.getParty();
    if (!activeChar.isInParty()) {
      return false;
    }
    Player partyLeader = playerParty.getPartyLeader();
    if (partyLeader == null) {
      return false;
    }
    int memberCount = playerParty.getMemberCount();
    int lootDistribution = playerParty.getLootDistribution();

    activeChar.sendPacket(Msg._PARTY_INFORMATION_);

    switch (lootDistribution)
    {
    case 0:
      activeChar.sendPacket(Msg.LOOTING_METHOD_FINDERS_KEEPERS);
      break;
    case 3:
      activeChar.sendPacket(Msg.LOOTING_METHOD_BY_TURN);
      break;
    case 4:
      activeChar.sendPacket(Msg.LOOTING_METHOD_BY_TURN_INCLUDING_SPOIL);
      break;
    case 1:
      activeChar.sendPacket(Msg.LOOTING_METHOD_RANDOM);
      break;
    case 2:
      activeChar.sendPacket(Msg.LOOTING_METHOD_RANDOM_INCLUDING_SPOIL);
    }

    activeChar.sendPacket(new SystemMessage(1611).addString(partyLeader.getName()));
    activeChar.sendMessage(new CustomMessage("scripts.commands.user.PartyInfo.Members", activeChar, new Object[0]).addNumber(memberCount));
    activeChar.sendPacket(Msg.__DASHES__);
    return true;
  }

  public final int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}