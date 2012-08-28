package scripts.commands.admincommandhandlers;

import java.util.Map;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import scripts.commands.IAdminCommandHandler;

public class AdminBanChat
  implements IAdminCommandHandler
{
  private static final Log _log = LogFactory.getLog(AdminBan.class.getName());

  private static String[] ADMIN_COMMANDS = { "admin_banchat", "admin_unbanchat", "admin_unbanchat_all", "admin_banchat_all" };

  private static final int REQUIRED_LEVEL = Config.GM_BAN_CHAT;

  public boolean useAdminCommand(String command, L2PcInstance admin)
  {
    if (!Config.ALT_PRIVILEGES_ADMIN)
    {
      if (!checkLevel(admin.getAccessLevel()))
      {
        _log.info("Not required level for " + admin.getName());
        return false;
      }
    }

    String[] cmdParams = command.split(" ");

    if ((cmdParams.length < 3) && (command.startsWith("admin_banchat")))
    {
      admin.sendAdmResultMessage("usage:");
      admin.sendAdmResultMessage("  //banchat [<player_name>] [<time_in_seconds>]");
      admin.sendAdmResultMessage("  //banchat [<player_name>] [<time_in_seconds>] [<ban_chat_reason>]");
      return false;
    }
    if ((cmdParams.length < 2) && (command.startsWith("admin_unbanchat")))
    {
      admin.sendAdmResultMessage("UnBanChat Syntax:");
      admin.sendAdmResultMessage("  //unbanchat [<player_name>]");
      return false;
    }
    if (command.startsWith("admin_banchat_all"))
    {
      try
      {
        for (L2PcInstance player : admin.getKnownList().getKnownPlayers().values())
        {
          if (!player.isGM())
          {
            player.setBanChatTimer(7200000L);
            player.setChatBannedForAnnounce(true);
          }
        }
      }
      catch (Exception e)
      {
      }
    }
    else if (command.startsWith("admin_unbanchat_all"))
    {
      try
      {
        for (L2PcInstance player : admin.getKnownList().getKnownPlayers().values())
        {
          player.setChatBannedForAnnounce(false);
        }
      }
      catch (Exception e)
      {
      }
    }

    long banLength = -1L;
    String banReason = "";

    L2PcInstance targetPlayer = null;

    targetPlayer = L2World.getInstance().getPlayer(cmdParams[1]);

    if (targetPlayer == null)
    {
      admin.sendAdmResultMessage("Incorrect parameter or target.");
      return false;
    }

    if (command.startsWith("admin_banchat"))
    {
      try
      {
        banLength = Integer.parseInt(cmdParams[2]);
      }
      catch (NumberFormatException nfe)
      {
      }
      if (cmdParams.length > 3) {
        banReason = cmdParams[3];
      }

      admin.sendMessage(targetPlayer.getName() + "'s chat is banned for " + banLength + " seconds.");
      targetPlayer.setChatBanned(true, banLength, banReason);
    }
    else if (command.startsWith("admin_unbanchat"))
    {
      admin.sendMessage(targetPlayer.getName() + "'s chat ban has now been lifted.");
      targetPlayer.setChatBanned(false, 0L, "");
    }
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
}