package net.sf.l2j.loginserver.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.L2LoginClient;
import net.sf.l2j.loginserver.LoginController;
import net.sf.l2j.loginserver.SessionKey;
import net.sf.l2j.loginserver.serverpackets.LoginFail.LoginFailReason;
import net.sf.l2j.loginserver.serverpackets.PlayFail.PlayFailReason;
import net.sf.l2j.loginserver.serverpackets.PlayOk;

public class RequestServerLogin extends L2LoginClientPacket
{
  private int _skey1;
  private int _skey2;
  private int _serverId;

  public int getSessionKey1()
  {
    return _skey1;
  }

  public int getSessionKey2()
  {
    return _skey2;
  }

  public int getServerID()
  {
    return _serverId;
  }

  public boolean readImpl()
  {
    if (getAvaliableBytes() >= 9)
    {
      _skey1 = readD();
      _skey2 = readD();
      _serverId = readC();
      return true;
    }

    return false;
  }

  public void run()
  {
    SessionKey sk = ((L2LoginClient)getClient()).getSessionKey();

    if ((!Config.SHOW_LICENCE) || (sk.checkLoginPair(_skey1, _skey2)))
    {
      if (LoginController.getInstance().isLoginPossible((L2LoginClient)getClient(), _serverId))
      {
        ((L2LoginClient)getClient()).setJoinedGS(true);
        ((L2LoginClient)getClient()).sendPacket(new PlayOk(sk));
      }
      else
      {
        ((L2LoginClient)getClient()).close(PlayFail.PlayFailReason.REASON_TOO_MANY_PLAYERS);
      }
    }
    else
    {
      ((L2LoginClient)getClient()).close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
    }
  }
}