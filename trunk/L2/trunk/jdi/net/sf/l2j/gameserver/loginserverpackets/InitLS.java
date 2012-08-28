package net.sf.l2j.gameserver.loginserverpackets;

public class InitLS extends LoginServerBasePacket
{
  private int _rev;
  private byte[] _key;

  public int getRevision()
  {
    return _rev;
  }

  public byte[] getRSAKey()
  {
    return _key;
  }

  public InitLS(byte[] decrypt)
  {
    super(decrypt);
    _rev = readD();
    int size = readD();
    _key = readB(size);
  }
}