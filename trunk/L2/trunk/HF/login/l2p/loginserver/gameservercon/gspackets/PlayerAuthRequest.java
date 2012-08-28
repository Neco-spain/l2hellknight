package l2m.loginserver.gameservercon.gspackets;

import l2m.loginserver.SessionKey;
import l2m.loginserver.accounts.Account;
import l2m.loginserver.accounts.SessionManager;
import l2m.loginserver.accounts.SessionManager.Session;
import l2m.loginserver.gameservercon.ReceivablePacket;
import l2m.loginserver.gameservercon.lspackets.PlayerAuthResponse;

public class PlayerAuthRequest extends ReceivablePacket
{
  private String account;
  private int playOkId1;
  private int playOkId2;
  private int loginOkId1;
  private int loginOkId2;

  protected void readImpl()
  {
    account = readS();
    playOkId1 = readD();
    playOkId2 = readD();
    loginOkId1 = readD();
    loginOkId2 = readD();
  }

  protected void runImpl()
  {
    SessionKey skey = new SessionKey(loginOkId1, loginOkId2, playOkId1, playOkId2);

    SessionManager.Session session = SessionManager.getInstance().closeSession(skey);
    if ((session == null) || (!session.getAccount().getLogin().equals(account)))
    {
      sendPacket(new PlayerAuthResponse(account));
      return;
    }
    sendPacket(new PlayerAuthResponse(session, session.getSessionKey().equals(skey)));
  }
}