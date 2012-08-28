package net.sf.l2j.loginserver.gameserverpackets;

import net.sf.l2j.loginserver.clientpackets.ClientBasePacket;

public class ChangeAccessLevel extends ClientBasePacket
{
  private int _level;
  private String _account;

  public ChangeAccessLevel(byte[] decrypt)
  {
    super(decrypt);
    _level = readD();
    _account = readS();
  }

  public String getAccount()
  {
    return _account;
  }

  public int getLevel()
  {
    return _level;
  }
}