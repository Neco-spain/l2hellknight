package scripts.commands.admincommandhandlers;

import java.io.PrintStream;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.entity.TvTEventTeleporter;
import scripts.commands.IAdminCommandHandler;

public class AdminTvTEvent
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_tvt_add", "admin_tvt_remove", "admin_tvt_start" };

  private static final int REQUIRED_LEVEL = Config.GM_MIN;

  public boolean useAdminCommand(String command, L2PcInstance adminInstance)
  {
    GMAudit.auditGMAction(adminInstance.getName(), command, adminInstance.getTarget() != null ? adminInstance.getTarget().getName() : "no-target", "");

    if (command.equals("admin_tvt_add"))
    {
      L2Object target = adminInstance.getTarget();

      if ((target == null) || (!target.isPlayer()))
      {
        adminInstance.sendAdmResultMessage("You should select a player!");
        return true;
      }

      add(adminInstance, (L2PcInstance)target);
    }
    else if (command.equals("admin_tvt_remove"))
    {
      L2Object target = adminInstance.getTarget();

      if ((target == null) || (!target.isPlayer()))
      {
        adminInstance.sendAdmResultMessage("You should select a player!");
        return true;
      }

      remove(adminInstance, (L2PcInstance)target);
    }
    else if (command.equals("admin_tvt_start"))
    {
      if (!TvTEvent.startParticipation())
      {
        Announcements.getInstance().announceToAll("TvT Event: \u041E\u0442\u043C\u0435\u043D\u0435\u043D.");
        System.out.println("TvTEventEngine[TvTManager.run()]: Error spawning event npc for participation.");
        return false;
      }

      Announcements.getInstance().announceToAll("TvT Event: \u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F \u043E\u0442\u043A\u0440\u044B\u0442\u0430 \u043D\u0430 " + Config.TVT_EVENT_PARTICIPATION_TIME + " \u043C\u0438\u043D\u0443\u0442.");
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

  private void add(L2PcInstance adminInstance, L2PcInstance playerInstance)
  {
    if (TvTEvent.isPlayerParticipant(playerInstance.getName()))
    {
      adminInstance.sendAdmResultMessage("Player already participated in the event!");
      return;
    }

    if (!TvTEvent.addParticipant(playerInstance))
    {
      adminInstance.sendAdmResultMessage("Player instance could not be added, it seems to be null!");
      return;
    }

    if (TvTEvent.isStarted())
    {
      new TvTEventTeleporter(playerInstance, TvTEvent.getParticipantTeamCoordinates(playerInstance.getName()), true, false);
    }
  }

  private void remove(L2PcInstance adminInstance, L2PcInstance playerInstance) {
    if (!TvTEvent.removeParticipant(playerInstance.getName()))
    {
      adminInstance.sendAdmResultMessage("Player is not part of the event!");
      return;
    }

    new TvTEventTeleporter(playerInstance, TvTEvent.getRandomLoc(), true, true);
  }
}