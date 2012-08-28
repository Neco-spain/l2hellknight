package scripts.commands.admincommandhandlers;

import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.LeaveWorld;
import scripts.commands.IAdminCommandHandler;
import scripts.communitybbs.Manager.RegionBBSManager;

public class AdminKick
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_kick", "admin_kick_non_gm", "admin_sniffspy" };
  private static final int REQUIRED_LEVEL = Config.GM_KICK;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) {
      return false;
    }

    String target = activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target";
    GMAudit.auditGMAction(activeChar.getName(), command, target, "");

    if (command.startsWith("admin_kick")) {
      StringTokenizer st = new StringTokenizer(command);
      if (st.countTokens() > 1) {
        st.nextToken();
        String player = st.nextToken();
        L2PcInstance plyr = L2World.getInstance().getPlayer(player);
        if (plyr != null) {
          if (plyr.isFantome())
          {
            plyr.decayMe();
            L2World.getInstance().removePlayer(plyr);
          }
          else
          {
            plyr.kick();
          }
          activeChar.sendAdmResultMessage("You kicked " + plyr.getName() + " from the game.");
        }
      }
    }
    else if (command.startsWith("admin_kick_non_gm")) {
      int counter = 0;
      for (L2PcInstance player : L2World.getInstance().getAllPlayers()) {
        if (!player.isGM()) {
          counter++;
          player.sendPacket(new LeaveWorld());
          player.logout();
          RegionBBSManager.getInstance().changeCommunityBoard();
        }
      }
      activeChar.sendAdmResultMessage("Kicked " + counter + " players");
    } else if (command.startsWith("admin_sniffspy")) {
      StringTokenizer st = new StringTokenizer(command);
      if (st.countTokens() > 1) {
        st.nextToken();
        String player = st.nextToken();
        L2PcInstance plyr = L2World.getInstance().getPlayer(player);
        if (plyr != null) {
          if (plyr.isSpyPckt()) {
            plyr.setSpyPacket(false);
            activeChar.sendAdmResultMessage("\u0412\u044B\u043A\u043B\u044E\u0447\u0435\u043D \u043B\u043E\u0433 \u043F\u0430\u043A\u0435\u0442\u043E\u0432 \u0438\u0433\u0440\u043E\u043A\u0430: " + plyr.getName());
          } else {
            plyr.setSpyPacket(true);
            activeChar.sendAdmResultMessage("\u0412\u043A\u043B\u044E\u0447\u0435\u043D \u043B\u043E\u0433 \u043F\u0430\u043A\u0435\u0442\u043E\u0432 \u0438\u0433\u0440\u043E\u043A\u0430: " + plyr.getName());
          }
        }
      }
    }

    return true;
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }
}