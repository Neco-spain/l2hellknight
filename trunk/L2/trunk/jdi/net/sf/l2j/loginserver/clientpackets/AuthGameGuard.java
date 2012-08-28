package net.sf.l2j.loginserver.clientpackets;

import net.sf.l2j.loginserver.L2LoginClient;
import net.sf.l2j.loginserver.L2LoginClient.LoginClientState;
import net.sf.l2j.loginserver.serverpackets.GGAuth;
import net.sf.l2j.loginserver.serverpackets.LoginFail.LoginFailReason;

public class AuthGameGuard extends L2LoginClientPacket
{
  private int _sessionId;
  private int _data1;
  private int _data2;
  private int _data3;
  private int _data4;

  public int getSessionId()
  {
    return _sessionId;
  }

  public int getData1()
  {
    return _data1;
  }

  public int getData2()
  {
    return _data2;
  }

  public int getData3()
  {
    return _data3;
  }

  public int getData4()
  {
    return _data4;
  }

  protected boolean readImpl()
  {
    if (getAvaliableBytes() >= 20)
    {
      _sessionId = readD();
      _data1 = readD();
      _data2 = readD();
      _data3 = readD();
      _data4 = readD();
      return true;
    }

    return false;
  }

  public void run()
  {
    if (_sessionId == ((L2LoginClient)getClient()).getSessionId())
    {
      ((L2LoginClient)getClient()).setState(L2LoginClient.LoginClientState.AUTHED_GG);
      ((L2LoginClient)getClient()).sendPacket(new GGAuth(((L2LoginClient)getClient()).getSessionId()));
    }
    else
    {
      ((L2LoginClient)getClient()).close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
    }
  }
}