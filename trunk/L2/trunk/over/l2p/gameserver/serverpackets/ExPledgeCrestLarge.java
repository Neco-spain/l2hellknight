package l2p.gameserver.serverpackets;

public class ExPledgeCrestLarge extends L2GameServerPacket
{
  private int _crestId;
  private byte[] _data;

  public ExPledgeCrestLarge(int crestId, byte[] data)
  {
    _crestId = crestId;
    _data = data;
  }

  protected final void writeImpl()
  {
    writeEx(27);

    writeD(0);
    writeD(_crestId);
    writeD(_data.length);
    writeB(_data);
  }
}