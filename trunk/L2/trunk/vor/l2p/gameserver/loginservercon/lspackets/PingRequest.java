package l2p.gameserver.loginservercon.lspackets;

import l2p.gameserver.loginservercon.LoginServerCommunication;
import l2p.gameserver.loginservercon.ReceivablePacket;
import l2p.gameserver.loginservercon.gspackets.PingResponse;

public class PingRequest extends ReceivablePacket
{
  public void readImpl()
  {
  }

  protected void runImpl()
  {
    LoginServerCommunication.getInstance().sendPacket(new PingResponse());
  }
}