package l2m.gameserver.loginservercon.lspackets;

import l2m.gameserver.loginservercon.LoginServerCommunication;
import l2m.gameserver.loginservercon.ReceivablePacket;
import l2m.gameserver.loginservercon.gspackets.PingResponse;

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