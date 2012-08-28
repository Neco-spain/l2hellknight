package net.sf.l2j.loginserver.clientpackets;

import net.sf.l2j.loginserver.L2LoginClient;
import net.sf.l2j.loginserver.SessionKey;
import net.sf.l2j.loginserver.serverpackets.LoginFail.LoginFailReason;
import net.sf.l2j.loginserver.serverpackets.ServerList;

public class RequestServerList extends L2LoginClientPacket
{
  private int _skey1;
  private int _skey2;
  private int _data3;

  public int getSessionKey1()
  {
    return _skey1;
  }

  public int getSessionKey2()
  {
    return _skey2;
  }

  public int getData3()
  {
    return _data3;
  }

  public boolean readImpl()
  {
    if (getAvaliableBytes() >= 8)
    {
      _skey1 = readD();
      _skey2 = readD();
      return true;
    }

    return false;
  }

  public void run()
  {
    if (((L2LoginClient)getClient()).getSessionKey().checkLoginPair(_skey1, _skey2))
    {
      ((L2LoginClient)getClient()).sendPacket(new ServerList((L2LoginClient)getClient()));
    }
    else
    {
      ((L2LoginClient)getClient()).close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
    }
  }
}