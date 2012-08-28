package net.sf.l2j.gameserver.network.serverpackets;

public class PledgeCrest extends L2GameServerPacket
{
  private static final String _S__84_PLEDGECREST = "[S] 6c PledgeCrest";
  private int _crestId;
  private int _crestSize;
  private byte[] _data;

  public PledgeCrest(int crestId, byte[] data)
  {
    _crestId = crestId;
    _data = data;
    _crestSize = _data.length;
  }

  protected final void writeImpl()
  {
    writeC(108);
    writeD(_crestId);
    writeD(_crestSize);
    writeB(_data);
    _data = null;
  }

  public String getType()
  {
    return "[S] 6c PledgeCrest";
  }
}