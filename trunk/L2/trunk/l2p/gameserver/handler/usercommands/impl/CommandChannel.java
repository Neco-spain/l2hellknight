package l2p.gameserver.handler.usercommands.impl;

import java.util.List;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.handler.usercommands.IUserCommandHandler;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.ExMultiPartyCommandChannelInfo;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;

public class CommandChannel
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 92, 93, 96, 97 };

  public boolean useUserCommand(int id, Player activeChar)
  {
    if ((id != COMMAND_IDS[0]) && (id != COMMAND_IDS[1]) && (id != COMMAND_IDS[2]) && (id != COMMAND_IDS[3])) {
      return false;
    }
    switch (id)
    {
    case 92:
      activeChar.sendMessage(new CustomMessage("usercommandhandlers.CommandChannel", activeChar, new Object[0]));
      break;
    case 93:
      if ((!activeChar.isInParty()) || (!activeChar.getParty().isInCommandChannel()))
        return true;
      if (activeChar.getParty().getCommandChannel().getChannelLeader() == activeChar)
      {
        l2p.gameserver.model.CommandChannel channel = activeChar.getParty().getCommandChannel();
        channel.disbandChannel();
      }
      else {
        activeChar.sendPacket(Msg.ONLY_THE_CREATOR_OF_A_CHANNEL_CAN_USE_THE_CHANNEL_DISMISS_COMMAND);
      }break;
    case 96:
      if ((!activeChar.isInParty()) || (!activeChar.getParty().isInCommandChannel()))
        return true;
      if (!activeChar.getParty().isLeader(activeChar))
      {
        activeChar.sendPacket(Msg.ONLY_A_PARTY_LEADER_CAN_CHOOSE_THE_OPTION_TO_LEAVE_A_CHANNEL);
        return true;
      }
      l2p.gameserver.model.CommandChannel channel = activeChar.getParty().getCommandChannel();

      if (channel.getChannelLeader() == activeChar)
      {
        if (channel.getParties().size() > 1) {
          return false;
        }

        channel.disbandChannel();
        return true;
      }

      Party party = activeChar.getParty();
      channel.removeParty(party);
      party.broadCast(new IStaticPacket[] { Msg.YOU_HAVE_QUIT_THE_COMMAND_CHANNEL });
      channel.broadCast(new IStaticPacket[] { new SystemMessage(1587).addString(activeChar.getName()) });
      break;
    case 97:
      if ((!activeChar.isInParty()) || (!activeChar.getParty().isInCommandChannel()))
        return false;
      activeChar.sendPacket(new ExMultiPartyCommandChannelInfo(activeChar.getParty().getCommandChannel()));
    case 94:
    case 95:
    }return true;
  }

  public final int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}