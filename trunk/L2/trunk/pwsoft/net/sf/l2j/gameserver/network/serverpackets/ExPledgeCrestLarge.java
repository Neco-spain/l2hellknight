package net.sf.l2j.gameserver.network.serverpackets;

public class ExPledgeCrestLarge extends L2GameServerPacket
{
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
}