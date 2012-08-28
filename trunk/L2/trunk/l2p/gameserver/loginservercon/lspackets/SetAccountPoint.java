package l2p.gameserver.loginservercon.lspackets;

import l2p.gameserver.loginservercon.LoginServerCommunication;
import l2p.gameserver.loginservercon.ReceivablePacket;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;

public class SetAccountPoint extends ReceivablePacket
{
  private String account;
  private int points;

  public void readImpl()
  {
    account = readS();
    points = readD();
  }

  protected void runImpl()
  {
    GameClient client = LoginServerCommunication.getInstance().getAuthedClient(account);
    if (client == null) {
      return;
    }
    if (client.getActiveChar() != null)
      client.getActiveChar().setAccountPoints(points);
  }
}