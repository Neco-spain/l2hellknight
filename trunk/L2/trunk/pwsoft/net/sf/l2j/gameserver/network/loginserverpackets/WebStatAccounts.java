package net.sf.l2j.gameserver.network.loginserverpackets;

public class WebStatAccounts extends LoginServerBasePacket
{
  private int _count;

  public WebStatAccounts(byte[] decrypt)
  {
    super(decrypt);
    _count = readD();
  }

  public int getCount()
  {
    return _count;
  }
}