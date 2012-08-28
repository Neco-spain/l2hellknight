package scripts.commands.usercommandhandlers;

import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.commands.IUserCommandHandler;

public class ChannelLeave
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 96 };

  public boolean useUserCommand(int id, L2PcInstance activeChar)
  {
    if (id != COMMAND_IDS[0]) return false;

    if (activeChar.isInParty())
    {
      if ((activeChar.getParty().isLeader(activeChar)) && (activeChar.getParty().isInCommandChannel()))
      {
        L2CommandChannel channel = activeChar.getParty().getCommandChannel();
        L2Party party = activeChar.getParty();
        channel.removeParty(party);

        SystemMessage sm = SystemMessage.sendString("Your party has left the CommandChannel.");
        party.broadcastToPartyMembers(sm);
        sm = SystemMessage.sendString(((L2PcInstance)party.getPartyMembers().get(0)).getName() + "'s party has left the CommandChannel.");
        channel.broadcastToChannelMembers(sm);
        return true;
      }
    }

    return false;
  }

  public int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}