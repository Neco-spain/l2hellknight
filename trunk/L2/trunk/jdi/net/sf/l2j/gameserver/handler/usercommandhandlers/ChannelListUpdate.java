package net.sf.l2j.gameserver.handler.usercommandhandlers;

import java.util.List;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ChannelListUpdate
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 97 };

  public boolean useUserCommand(int id, L2PcInstance activeChar)
  {
    if (id != COMMAND_IDS[0]) return false;

    L2CommandChannel channel = activeChar.getParty().getCommandChannel();

    activeChar.sendMessage("================");
    activeChar.sendMessage("Command Channel Information is not fully implemented now.");
    activeChar.sendMessage("There are " + channel.getPartys().size() + " Party's in the Channel.");
    activeChar.sendMessage(channel.getMemberCount() + " Players overall.");
    activeChar.sendMessage("Leader is " + channel.getChannelLeader().getName() + ".");
    activeChar.sendMessage("Partyleader, Membercount:");
    for (L2Party party : channel.getPartys())
    {
      activeChar.sendMessage(((L2PcInstance)party.getPartyMembers().get(0)).getName() + ", " + party.getMemberCount());
    }
    activeChar.sendMessage("================");
    return true;
  }

  public int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}