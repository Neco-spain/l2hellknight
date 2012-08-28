package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ServerClose;

public class AdminKick
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_kick", "admin_kick_non_gm" };
  private static final int REQUIRED_LEVEL = Config.GM_KICK;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) {
      return false;
    }
    String target = activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target";
    GMAudit.auditGMAction(activeChar.getName(), command, target, "");

    if (command.startsWith("admin_kick"))
    {
      StringTokenizer st = new StringTokenizer(command);
      if (st.countTokens() > 1)
      {
        st.nextToken();
        String player = st.nextToken();
        L2PcInstance plyr = L2World.getInstance().getPlayer(player);
        if (plyr != null)
        {
          plyr.sendMessage("You kicked from the game");
          plyr.closeNetConnection(false);
          activeChar.sendMessage("You kicked " + plyr.getName() + " from the game.");
          RegionBBSManager.getInstance().changeCommunityBoard();
        }
      }
    }
    if (command.startsWith("admin_kick_non_gm"))
    {
      int counter = 0;
      for (L2PcInstance player : L2World.getInstance().getAllPlayers())
      {
        if (!player.isGM())
        {
          counter++;
          player.sendPacket(new ServerClose());
          player.closeNetConnection(false);
          RegionBBSManager.getInstance().changeCommunityBoard();
        }
      }
      activeChar.sendMessage("Kicked " + counter + " players");
    }
    return true;
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }
}