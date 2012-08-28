package scripts.commands.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.util.Util;
import scripts.commands.IAdminCommandHandler;

public class AdminExpSp
  implements IAdminCommandHandler
{
  private static Logger _log = Logger.getLogger(AdminExpSp.class.getName());

  private static final String[] ADMIN_COMMANDS = { "admin_add_exp_sp_to_character", "admin_add_exp_sp", "admin_remove_exp_sp" };
  private static final int REQUIRED_LEVEL = Config.GM_CHAR_EDIT;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM())))
      return false;
    GMAudit.auditGMAction(activeChar.getName(), command, activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target", "");
    if (command.startsWith("admin_add_exp_sp"))
    {
      try
      {
        String val = command.substring(16);
        if (!adminAddExpSp(activeChar, val))
          activeChar.sendAdmResultMessage("Usage: //add_exp_sp exp sp");
      }
      catch (StringIndexOutOfBoundsException e)
      {
        activeChar.sendAdmResultMessage("Usage: //add_exp_sp exp sp");
      }
    }
    else if (command.startsWith("admin_remove_exp_sp"))
    {
      try
      {
        String val = command.substring(19);
        if (!adminRemoveExpSP(activeChar, val))
          activeChar.sendAdmResultMessage("Usage: //remove_exp_sp exp sp");
      }
      catch (StringIndexOutOfBoundsException e)
      {
        activeChar.sendAdmResultMessage("Usage: //remove_exp_sp exp sp");
      }
    }
    addExpSp(activeChar);
    return true;
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }

  private void addExpSp(L2PcInstance activeChar)
  {
    L2Object target = activeChar.getTarget();
    L2PcInstance player = null;
    if (target.isPlayer()) {
      player = (L2PcInstance)target;
    }
    else {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
      return;
    }
    NpcHtmlMessage adminReply = NpcHtmlMessage.id(5);
    adminReply.setFile("data/html/admin/expsp.htm");
    adminReply.replace("%name%", player.getName());
    adminReply.replace("%level%", String.valueOf(player.getLevel()));
    adminReply.replace("%xp%", String.valueOf(player.getExp()));
    adminReply.replace("%sp%", String.valueOf(player.getSp()));
    adminReply.replace("%class%", player.getTemplate().className);
    activeChar.sendPacket(adminReply);
  }

  private boolean adminAddExpSp(L2PcInstance activeChar, String ExpSp)
  {
    L2Object target = activeChar.getTarget();
    L2PcInstance player = null;
    if (target.isPlayer())
    {
      player = (L2PcInstance)target;
    }
    else
    {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
      return false;
    }
    StringTokenizer st = new StringTokenizer(ExpSp);
    if (st.countTokens() != 2)
    {
      return false;
    }

    String exp = st.nextToken();
    String sp = st.nextToken();
    long expval = 0L;
    int spval = 0;
    try
    {
      expval = Long.parseLong(exp);
      spval = Integer.parseInt(sp);
    }
    catch (Exception e)
    {
      return false;
    }

    if ((Config.GM_EDIT) && ((expval != 0L) || (spval != 0)) && (!player.isGM()))
    {
      player.sendAdmResultMessage("A GM tried to edit you in " + expval + " exp points and in " + spval + " sp points.You will both be banned.");
      Util.handleIllegalPlayerAction(player, "The player " + player.getName() + " has been edited. BAN!!", 3);

      player.sendAdmResultMessage("You tried to edit " + player.getName() + " by " + expval + " exp points and " + spval + ". You both be banned now.");
      Util.handleIllegalPlayerAction(activeChar, "El GM " + activeChar.getName() + " ha editado a alguien. BAN!!", 3);
      _log.severe("GM " + activeChar.getName() + " tried to edit " + player.getName() + ". They both have been Banned.");
    }
    else if ((expval != 0L) || (spval != 0))
    {
      player.sendAdmResultMessage("Admin is adding you " + expval + " xp and " + spval + " sp.");
      player.addExpAndSp(expval, spval);

      activeChar.sendAdmResultMessage("Added " + expval + " xp and " + spval + " sp to " + player.getName() + ".");
      if (Config.DEBUG) {
        _log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") added " + expval + " xp and " + spval + " sp to " + player.getObjectId() + ".");
      }
    }

    return true;
  }

  private boolean adminRemoveExpSP(L2PcInstance activeChar, String ExpSp)
  {
    L2Object target = activeChar.getTarget();
    L2PcInstance player = null;
    if (target.isPlayer())
    {
      player = (L2PcInstance)target;
    }
    else
    {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
      return false;
    }
    StringTokenizer st = new StringTokenizer(ExpSp);
    if (st.countTokens() != 2) {
      return false;
    }

    String exp = st.nextToken();
    String sp = st.nextToken();
    long expval = 0L;
    int spval = 0;
    try
    {
      expval = Long.parseLong(exp);
      spval = Integer.parseInt(sp);
    }
    catch (Exception e)
    {
      return false;
    }
    if ((expval != 0L) || (spval != 0))
    {
      player.sendAdmResultMessage("Admin is removing you " + expval + " xp and " + spval + " sp.");
      player.removeExpAndSp(expval, spval);

      activeChar.sendAdmResultMessage("Removed " + expval + " xp and " + spval + " sp from " + player.getName() + ".");
      if (Config.DEBUG) {
        _log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") removed " + expval + " xp and " + spval + " sp from " + player.getObjectId() + ".");
      }
    }

    return true;
  }
}