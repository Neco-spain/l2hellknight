package net.sf.l2j.gameserver.network.loginserverpackets;

public class KickPlayer extends LoginServerBasePacket
{
  private String _account;

  public KickPlayer(byte[] decrypt)
  {
    super(decrypt);
    _account = readS();
  }

  public String getAccount()
  {
    return _account;
  }
}