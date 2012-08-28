package l2m.gameserver.loginservercon.lspackets;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.loginservercon.LoginServerCommunication;
import l2m.gameserver.loginservercon.ReceivablePacket;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ServerClose;

public class KickPlayer extends ReceivablePacket
{
  String account;

  public void readImpl()
  {
    account = readS();
  }

  protected void runImpl()
  {
    GameClient client = LoginServerCommunication.getInstance().removeWaitingClient(account);
    if (client == null)
      client = LoginServerCommunication.getInstance().removeAuthedClient(account);
    if (client == null) {
      return;
    }
    Player activeChar = client.getActiveChar();
    if (activeChar != null)
    {
      activeChar.sendPacket(Msg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
      activeChar.kick();
    }
    else
    {
      client.close(ServerClose.STATIC);
    }
  }
}