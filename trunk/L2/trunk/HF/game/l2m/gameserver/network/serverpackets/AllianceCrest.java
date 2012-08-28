package l2m.gameserver.network.serverpackets;

public class AllianceCrest extends L2GameServerPacket
{
  private int _crestId;
  private byte[] _data;

  public AllianceCrest(int crestId, byte[] data)
  {
    _crestId = crestId;
    _data = data;
  }

  protected final void writeImpl()
  {
    writeC(175);
    writeD(_crestId);
    writeD(_data.length);
    writeB(_data);
  }
}