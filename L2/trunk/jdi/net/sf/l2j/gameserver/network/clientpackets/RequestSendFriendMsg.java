package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.FriendRecvMsg;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestSendFriendMsg extends L2GameClientPacket
{
  private static final String _C__CC_REQUESTSENDMSG = "[C] CC RequestSendMsg";
  private static Logger _logChat = Logger.getLogger("chat");
  private String _message;
  private String _reciever;

  protected void readImpl()
  {
    _message = readS();
    _reciever = readS();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;

    L2PcInstance targetPlayer = L2World.getInstance().getPlayer(_reciever);
    if (targetPlayer == null)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME));
      return;
    }

    if (Config.LOG_CHAT)
    {
      LogRecord record = new LogRecord(Level.INFO, _message);
      record.setLoggerName("chat");
      record.setParameters(new Object[] { "PRIV_MSG", "[" + activeChar.getName() + " to " + _reciever + "]" });

      _logChat.log(record);
    }

    FriendRecvMsg frm = new FriendRecvMsg(activeChar.getName(), _reciever, _message);
    targetPlayer.sendPacket(frm);
  }

  public String getType()
  {
    return "[C] CC RequestSendMsg";
  }
}