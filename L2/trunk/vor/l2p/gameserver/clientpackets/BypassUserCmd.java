package l2p.gameserver.clientpackets;

import l2p.gameserver.handler.usercommands.IUserCommandHandler;
import l2p.gameserver.handler.usercommands.UserCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.components.CustomMessage;

public class BypassUserCmd extends L2GameClientPacket
{
  private int _command;

  protected void readImpl()
  {
    _command = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    IUserCommandHandler handler = UserCommandHandler.getInstance().getUserCommandHandler(_command);

    if (handler == null)
      activeChar.sendMessage(new CustomMessage("common.S1NotImplemented", activeChar, new Object[0]).addString(String.valueOf(_command)));
    else
      handler.useUserCommand(_command, activeChar);
  }
}