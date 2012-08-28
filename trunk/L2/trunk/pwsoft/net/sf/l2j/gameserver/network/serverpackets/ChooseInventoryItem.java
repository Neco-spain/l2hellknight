package net.sf.l2j.gameserver.network.serverpackets;

public class ChooseInventoryItem extends L2GameServerPacket
{
  private int _itemId;

  public ChooseInventoryItem(int itemId)
  {
    _itemId = itemId;
  }

  protected final void writeImpl()
  {
    writeC(111);
    writeD(_itemId);
  }
}