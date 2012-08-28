package l2m.gameserver.network.clientpackets;

import l2m.gameserver.handler.admincommands.AdminCommandHandler;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;

public class SendBypassBuildCmd extends L2GameClientPacket
{
  private String _command;

  protected void readImpl()
  {
    _command = readS();

    if (_command != null)
      _command = _command.trim();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    String cmd = _command;

    if (!cmd.contains("admin_")) {
      cmd = "admin_" + cmd;
    }
    AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, cmd);
  }
}