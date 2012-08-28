package scripts.commands.usercommandhandlers;

import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExMultiPartyCommandChannelInfo;
import scripts.commands.IUserCommandHandler;

public class ChannelListUpdate
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 97 };

  public boolean useUserCommand(int id, L2PcInstance activeChar)
  {
    if (id != COMMAND_IDS[0]) return false;

    if ((!activeChar.isInParty()) || (!activeChar.getParty().isInCommandChannel())) {
      return false;
    }
    activeChar.sendPacket(new ExMultiPartyCommandChannelInfo(activeChar.getParty().getCommandChannel()));
    return true;
  }

  public int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}