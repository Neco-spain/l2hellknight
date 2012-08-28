package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.util.Util;

public final class SendBypassBuildCmd extends L2GameClientPacket
{
  private static final String _C__5B_SENDBYPASSBUILDCMD = "[C] 5b SendBypassBuildCmd";
  public static final int GM_MESSAGE = 9;
  public static final int ANNOUNCEMENT = 10;
  private String _command;

  protected void readImpl()
  {
    _command = readS();
    if (_command != null)
      _command = _command.trim();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if ((Config.ALT_PRIVILEGES_ADMIN) && (!AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_" + _command))) {
      return;
    }
    if ((!activeChar.isGM()) && (!"gm".equalsIgnoreCase(_command)))
    {
      Util.handleIllegalPlayerAction(activeChar, "Warning!! Non-gm character " + activeChar.getName() + " requests gm bypass handler, hack?", Config.DEFAULT_PUNISH);
      return;
    }

    IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler("admin_" + _command);

    if (ach != null)
    {
      ach.useAdminCommand("admin_" + _command, activeChar);
    }
  }

  public String getType()
  {
    return "[C] 5b SendBypassBuildCmd";
  }
}