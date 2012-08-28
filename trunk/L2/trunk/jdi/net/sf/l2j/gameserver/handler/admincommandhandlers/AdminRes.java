package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2ControllableMobInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;

public class AdminRes
  implements IAdminCommandHandler
{
  private static Logger _log = Logger.getLogger(AdminRes.class.getName());
  private static final String[] ADMIN_COMMANDS = { "admin_res", "admin_res_monster" };
  private static final int REQUIRED_LEVEL = Config.GM_RES;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) {
      return false;
    }
    String target = activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target";
    GMAudit.auditGMAction(activeChar.getName(), command, target, "");

    if (command.startsWith("admin_res "))
      handleRes(activeChar, command.split(" ")[1]);
    else if (command.equals("admin_res"))
      handleRes(activeChar);
    else if (command.startsWith("admin_res_monster "))
      handleNonPlayerRes(activeChar, command.split(" ")[1]);
    else if (command.equals("admin_res_monster")) {
      handleNonPlayerRes(activeChar);
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

  private void handleRes(L2PcInstance activeChar)
  {
    handleRes(activeChar, null);
  }

  private void handleRes(L2PcInstance activeChar, String resParam)
  {
    L2Object obj = activeChar.getTarget();

    if (resParam != null)
    {
      L2PcInstance plyr = L2World.getInstance().getPlayer(resParam);

      if (plyr != null)
      {
        obj = plyr;
      }
      else
      {
        try
        {
          int radius = Integer.parseInt(resParam);

          for (L2PcInstance knownPlayer : activeChar.getKnownList().getKnownPlayersInRadius(radius)) {
            doResurrect(knownPlayer);
          }
          activeChar.sendMessage("Resurrected all players within a " + radius + " unit radius.");
          return;
        }
        catch (NumberFormatException e) {
          activeChar.sendMessage("Enter a valid player name or radius.");
          return;
        }
      }
    }

    if (obj == null) {
      obj = activeChar;
    }
    if ((obj instanceof L2ControllableMobInstance))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
      return;
    }

    doResurrect((L2Character)obj);

    if (Config.DEBUG)
      _log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") resurrected character " + obj.getObjectId());
  }

  private void handleNonPlayerRes(L2PcInstance activeChar)
  {
    handleNonPlayerRes(activeChar, "");
  }

  private void handleNonPlayerRes(L2PcInstance activeChar, String radiusStr)
  {
    L2Object obj = activeChar.getTarget();
    try
    {
      int radius = 0;

      if (!radiusStr.equals(""))
      {
        radius = Integer.parseInt(radiusStr);

        for (L2Character knownChar : activeChar.getKnownList().getKnownCharactersInRadius(radius))
          if ((!(knownChar instanceof L2PcInstance)) && (!(knownChar instanceof L2ControllableMobInstance)))
          {
            doResurrect(knownChar);
          }
        activeChar.sendMessage("Resurrected all non-players within a " + radius + " unit radius.");
      }
    }
    catch (NumberFormatException e) {
      activeChar.sendMessage("Enter a valid radius.");
      return;
    }

    if ((obj == null) || ((obj instanceof L2PcInstance)) || ((obj instanceof L2ControllableMobInstance)))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
      return;
    }

    doResurrect((L2Character)obj);
  }

  private void doResurrect(L2Character targetChar)
  {
    if (!targetChar.isDead()) return;

    if ((targetChar instanceof L2PcInstance)) {
      ((L2PcInstance)targetChar).restoreExp(100.0D);
    }
    else
    {
      DecayTaskManager.getInstance().cancelDecayTask(targetChar);
    }
    targetChar.doRevive(true);
  }
}