package net.sf.l2j.gameserver.network.serverpackets;

public class ExAutoSoulShot extends L2GameServerPacket
{
  private int _itemId;
  private int _type;

  public ExAutoSoulShot(int itemId, int type)
  {
    _itemId = itemId;
    _type = type;
  }

  protected final void writeImpl()
  {
    writeC(254);
    writeH(18);
    writeD(_itemId);
    writeD(_type);
  }
}