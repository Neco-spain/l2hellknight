package l2m.loginserver.clientpackets;

import l2m.loginserver.L2LoginClient;
import l2m.loginserver.SessionKey;
import l2m.loginserver.serverpackets.LoginFail.LoginFailReason;
import l2m.loginserver.serverpackets.ServerList;

public class RequestServerList extends L2LoginClientPacket
{
  private int _loginOkID1;
  private int _loginOkID2;

  protected void readImpl()
  {
    _loginOkID1 = readD();
    _loginOkID2 = readD();
  }

  protected void runImpl()
  {
    L2LoginClient client = (L2LoginClient)getClient();
    SessionKey skey = client.getSessionKey();
    if ((skey == null) || (!skey.checkLoginPair(_loginOkID1, _loginOkID2)))
    {
      client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
      return;
    }

    client.sendPacket(new ServerList(client.getAccount()));
  }
}