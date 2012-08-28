package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import scripts.commands.AdminCommandHandler;
import scripts.commands.IAdminCommandHandler;

public final class SendBypassBuildCmd extends L2GameClientPacket
{
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
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if ((Config.ALT_PRIVILEGES_ADMIN) && (!AdminCommandHandler.getInstance().checkPrivileges(player, "admin_" + _command))) {
      return;
    }
    if (player.isParalyzed()) {
      return;
    }
    if ((!player.isGM()) && (!"gm".equalsIgnoreCase(_command)))
    {
      return;
    }

    IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler("admin_" + _command);

    if (ach != null)
      ach.useAdminCommand("admin_" + _command, player);
  }
}