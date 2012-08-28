package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.commands.IUserCommandHandler;
import scripts.commands.UserCommandHandler;

public class RequestUserCommand extends L2GameClientPacket
{
  static Logger _log = Logger.getLogger(RequestUserCommand.class.getName());
  private int _command;

  protected void readImpl()
  {
    _command = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    IUserCommandHandler handler = UserCommandHandler.getInstance().getUserCommandHandler(_command);
    if (handler == null)
      player.sendPacket(SystemMessage.id(SystemMessageId.S1_S2).addString("user commandID " + _command + " not implemented yet"));
    else
      handler.useUserCommand(_command, ((L2GameClient)getClient()).getActiveChar());
  }
}