package scripts.commands.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2ControllableMobInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.commands.IAdminCommandHandler;

public class AdminKill
  implements IAdminCommandHandler
{
  private static Logger _log = Logger.getLogger(AdminKill.class.getName());
  private static final String[] ADMIN_COMMANDS = { "admin_kill", "admin_kill_monster" };
  private static final int REQUIRED_LEVEL = Config.GM_NPC_EDIT;

  private boolean checkLevel(int level)
  {
    return level >= REQUIRED_LEVEL;
  }

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) {
      return false;
    }
    String target = activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target";
    GMAudit.auditGMAction(activeChar.getName(), command, target, "");

    if (command.startsWith("admin_kill"))
    {
      StringTokenizer st = new StringTokenizer(command, " ");
      st.nextToken();

      if (st.hasMoreTokens())
      {
        String firstParam = st.nextToken();
        L2PcInstance plyr = L2World.getInstance().getPlayer(firstParam);
        if (plyr != null)
        {
          if (st.hasMoreTokens())
          {
            try
            {
              int radius = Integer.parseInt(st.nextToken());
              for (L2Character knownChar : plyr.getKnownList().getKnownCharactersInRadius(radius))
              {
                if ((knownChar == null) || ((knownChar instanceof L2ControllableMobInstance)) || (knownChar.equals(activeChar)))
                  continue;
                kill(activeChar, knownChar);
              }

              activeChar.sendAdmResultMessage("Killed all characters within a " + radius + " unit radius.");
              return true;
            }
            catch (NumberFormatException e) {
              activeChar.sendAdmResultMessage("Invalid radius.");
              return false;
            }
          }

          kill(activeChar, plyr);
        }
        else
        {
          try
          {
            int radius = Integer.parseInt(firstParam);

            for (L2Character knownChar : activeChar.getKnownList().getKnownCharactersInRadius(radius))
            {
              if ((knownChar == null) || ((knownChar instanceof L2ControllableMobInstance)) || (knownChar.equals(activeChar)))
                continue;
              kill(activeChar, knownChar);
            }

            activeChar.sendAdmResultMessage("Killed all characters within a " + radius + " unit radius.");
            return true;
          }
          catch (NumberFormatException e) {
            activeChar.sendAdmResultMessage("Usage: //kill <player_name | radius>");
            return false;
          }
        }
      }
      else
      {
        L2Object obj = activeChar.getTarget();
        if ((obj == null) || ((obj instanceof L2ControllableMobInstance)) || (!obj.isL2Character()))
          activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
        else
          kill(activeChar, (L2Character)obj);
      }
    }
    return true;
  }

  private void kill(L2PcInstance activeChar, L2Character target)
  {
    if (target.isPlayer())
    {
      if (!((L2PcInstance)target).isGM())
        target.stopAllEffects();
      target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar);
    }
    else if ((Config.L2JMOD_CHAMPION_ENABLE) && (target.isChampion())) {
      target.reduceCurrentHp(target.getMaxHp() * Config.L2JMOD_CHAMPION_HP + 1, activeChar);
    } else {
      target.reduceCurrentHp(target.getMaxHp() + 1, activeChar);
    }if (Config.DEBUG)
      _log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ")" + " killed character " + target.getObjectId());
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }
}