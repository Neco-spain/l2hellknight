package net.sf.l2j.loginserver.gameserverpackets;

import net.sf.l2j.loginserver.clientpackets.ClientBasePacket;

public class PlayerLogout extends ClientBasePacket
{
  private String _account;

  public PlayerLogout(byte[] decrypt)
  {
    super(decrypt);
    _account = readS();
  }

  public String getAccount()
  {
    return _account;
  }
}