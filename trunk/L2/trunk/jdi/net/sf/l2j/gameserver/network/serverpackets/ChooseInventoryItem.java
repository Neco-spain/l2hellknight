package net.sf.l2j.gameserver.network.serverpackets;

public class ChooseInventoryItem extends L2GameServerPacket
{
  private static final String _S__6F_CHOOSEINVENTORYITEM = "[S] 6f ChooseInventoryItem";
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

  public String getType()
  {
    return "[S] 6f ChooseInventoryItem";
  }
}