package scripts.commands.admincommandhandlers;

import java.util.Collection;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.commands.IAdminCommandHandler;

public class AdminCountdown
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_countdown" };
  private static final int REQUIRED_LEVEL = Config.GM_MIN;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) return false;

    if (command.startsWith("admin_countdown"))
      handleGmChat(command, activeChar);
    return true;
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level)
  {
    return level >= REQUIRED_LEVEL;
  }

  private void handleGmChat(String command, L2PcInstance activeChar)
  {
    try
    {
      String pre_countdown = command.substring(16);
      int countdown = Integer.parseInt(pre_countdown);

      for (int i = countdown; i >= 0; i--)
      {
        SystemMessage sm = null;
        if (i > 0)
          sm = SystemMessage.id(SystemMessageId.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS).addNumber(i);
        else {
          sm = SystemMessage.id(SystemMessageId.LET_THE_DUEL_BEGIN);
        }
        sendMessageToPlayers(activeChar, sm);
        try
        {
          Thread.sleep(1000L);
        }
        catch (InterruptedException e)
        {
          return;
        }
      }
    }
    catch (StringIndexOutOfBoundsException e)
    {
    }
  }

  public void sendMessageToPlayers(L2PcInstance activeChar, L2GameServerPacket packet)
  {
    Collection players = activeChar.getKnownList().getKnownPlayersInRadius(1250);
    for (L2PcInstance player : players)
    {
      player.sendPacket(packet);
    }
    activeChar.sendPacket(packet);
  }
}