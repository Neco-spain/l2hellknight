package net.sf.l2j.gameserver.network.serverpackets;

public class AllyCrest extends L2GameServerPacket
{
  private static final String _S__C7_ALLYCREST = "[S] ae AllyCrest";
  private int _crestId;
  private int _crestSize;
  private byte[] _data;

  public AllyCrest(int crestId, byte[] data)
  {
    _crestId = crestId;
    _data = data;
    _crestSize = _data.length;
  }

  protected final void writeImpl()
  {
    writeC(174);
    writeD(_crestId);
    writeD(_crestSize);
    writeB(_data);
    _data = null;
  }

  public String getType()
  {
    return "[S] ae AllyCrest";
  }
}