package l2m.loginserver.gameservercon.lspackets;

import l2m.loginserver.SessionKey;
import l2m.loginserver.accounts.Account;
import l2m.loginserver.accounts.SessionManager.Session;
import l2m.loginserver.gameservercon.SendablePacket;

public class PlayerAuthResponse extends SendablePacket
{
  private String login;
  private boolean authed;
  private int playOkID1;
  private int playOkID2;
  private int loginOkID1;
  private int loginOkID2;
  private double bonus;
  private int bonusExpire;

  public PlayerAuthResponse(SessionManager.Session session, boolean authed)
  {
    Account account = session.getAccount();
    login = account.getLogin();
    this.authed = authed;
    if (authed)
    {
      SessionKey skey = session.getSessionKey();
      playOkID1 = skey.playOkID1;
      playOkID2 = skey.playOkID2;
      loginOkID1 = skey.loginOkID1;
      loginOkID2 = skey.loginOkID2;
      bonus = account.getBonus();
      bonusExpire = account.getBonusExpire();
    }
  }

  public PlayerAuthResponse(String account)
  {
    login = account;
    authed = false;
  }

  protected void writeImpl()
  {
    writeC(2);
    writeS(login);
    writeC(authed ? 1 : 0);
    if (authed)
    {
      writeD(playOkID1);
      writeD(playOkID2);
      writeD(loginOkID1);
      writeD(loginOkID2);
      writeF(bonus);
      writeD(bonusExpire);
    }
  }
}