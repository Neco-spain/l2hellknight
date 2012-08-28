package l2p.gameserver.serverpackets;

public class KeyPacket extends L2GameServerPacket
{
  private byte[] _key;

  public KeyPacket(byte[] key)
  {
    _key = key;
  }

  public void writeImpl()
  {
    writeC(46);
    if ((_key == null) || (_key.length == 0))
    {
      writeC(0);
      return;
    }
    writeC(1);
    writeB(_key);
    writeD(1);
    writeD(0);
    writeC(0);
    writeD(0);
  }
}