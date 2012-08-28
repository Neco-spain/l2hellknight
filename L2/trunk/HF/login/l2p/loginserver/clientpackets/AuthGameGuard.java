package l2m.loginserver.clientpackets;

import l2m.loginserver.L2LoginClient;
import l2m.loginserver.L2LoginClient.LoginClientState;
import l2m.loginserver.serverpackets.GGAuth;
import l2m.loginserver.serverpackets.LoginFail.LoginFailReason;

public class AuthGameGuard extends L2LoginClientPacket
{
  private int _sessionId;

  protected void readImpl()
  {
    _sessionId = readD();
  }

  protected void runImpl()
  {
    L2LoginClient client = (L2LoginClient)getClient();

    if ((_sessionId == 0) || (_sessionId == client.getSessionId()))
    {
      client.setState(L2LoginClient.LoginClientState.AUTHED_GG);
      client.sendPacket(new GGAuth(client.getSessionId()));
    }
    else {
      client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
    }
  }
}