package scripts.commands.admincommandhandlers;

import java.io.PrintStream;
import java.util.Map;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.commands.IAdminCommandHandler;

public class AdminHeal
  implements IAdminCommandHandler
{
  private static Logger _log = Logger.getLogger(AdminRes.class.getName());
  private static final String[] ADMIN_COMMANDS = { "admin_heal" };
  private static final int REQUIRED_LEVEL = Config.GM_HEAL;

  public boolean useAdminCommand(String command, L2PcInstance activeChar) {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) {
      return false;
    }
    GMAudit.auditGMAction(activeChar.getName(), command, activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target", "");

    if (command.equals("admin_heal")) handleRes(activeChar);
    else if (command.startsWith("admin_heal"))
    {
      try
      {
        String healTarget = command.substring(11);
        handleRes(activeChar, healTarget);
      }
      catch (StringIndexOutOfBoundsException e)
      {
        if (Config.DEVELOPER)
          System.out.println("Heal error: " + e);
        SystemMessage sm = SystemMessage.id(SystemMessageId.S1_S2);
        sm.addString("Incorrect target/radius specified.");
        activeChar.sendPacket(sm);
      }
    }
    return true;
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }

  private void handleRes(L2PcInstance activeChar)
  {
    handleRes(activeChar, null);
  }

  private void handleRes(L2PcInstance activeChar, String player)
  {
    L2Object obj = activeChar.getTarget();
    if (player != null)
    {
      L2PcInstance plyr = L2World.getInstance().getPlayer(player);

      if (plyr != null) {
        obj = plyr;
      }
      else
        try
        {
          int radius = Integer.parseInt(player);
          for (L2Object object : activeChar.getKnownList().getKnownObjects().values())
          {
            if (object.isL2Character())
            {
              L2Character character = (L2Character)object;
              character.setCurrentHpMp(character.getMaxHp(), character.getMaxMp());
              if (object.isPlayer()) character.setCurrentCp(character.getMaxCp());
              character.broadcastPacket(new MagicSkillUser(character, character, 2241, 1, 1, 0));
            }
          }
          activeChar.sendAdmResultMessage("Healed within " + radius + " unit radius.");
          return;
        }
        catch (NumberFormatException nbe) {
        }
    }
    if (obj == null)
      obj = activeChar;
    if ((obj != null) && (obj.isL2Character()))
    {
      L2Character target = (L2Character)obj;
      target.setCurrentHpMp(target.getMaxHp(), target.getMaxMp());
      if (target.isPlayer())
        target.setCurrentCp(target.getMaxCp());
      target.broadcastPacket(new MagicSkillUser(target, target, 2241, 1, 1, 0));
    }
    else {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
    }
  }
}