package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.handler.UserCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class RequestUserCommand extends L2GameClientPacket
{
  private static final String _C__AA_REQUESTUSERCOMMAND = "[C] aa RequestUserCommand";
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
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
      sm.addString("user commandID " + _command + " not implemented yet");
      player.sendPacket(sm);
      sm = null;
    }
    else
    {
      handler.useUserCommand(_command, ((L2GameClient)getClient()).getActiveChar());
    }
  }

  public String getType()
  {
    return "[C] aa RequestUserCommand";
  }
}