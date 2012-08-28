package net.sf.l2j.gameserver.network.serverpackets;

public class ExPledgeCrestLarge extends L2GameServerPacket
{
  private static final String _S__FE_28_EXPLEDGECRESTLARGE = "[S] FE:28 ExPledgeCrestLarge";
  private int _crestId;
  private byte[] _data;

  public ExPledgeCrestLarge(int crestId, byte[] data)
  {
    _crestId = crestId;
    _data = data;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(40);

    writeD(0);
    writeD(_crestId);
    writeD(_data.length);

    writeB(_data);
  }

  public String getType()
  {
    return "[S] FE:28 ExPledgeCrestLarge";
  }
}