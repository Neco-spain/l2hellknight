package net.sf.l2j.loginserver.serverpackets;

import net.sf.l2j.loginserver.SessionKey;

public final class LoginOk extends L2LoginServerPacket
{
  private int _loginOk1;
  private int _loginOk2;

  public LoginOk(SessionKey sessionKey)
  {
    _loginOk1 = sessionKey.loginOkID1;
    _loginOk2 = sessionKey.loginOkID2;
  }

  protected void write()
  {
    writeC(3);
    writeD(_loginOk1);
    writeD(_loginOk2);
    writeD(0);
    writeD(0);
    writeD(1002);
    writeD(0);
    writeD(0);
    writeD(0);
    writeB(new byte[16]);
  }
}