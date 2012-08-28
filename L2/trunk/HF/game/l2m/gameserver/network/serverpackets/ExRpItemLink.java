package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.items.ItemInfo;

public class ExRpItemLink extends L2GameServerPacket
{
  private ItemInfo _item;

  public ExRpItemLink(ItemInfo item)
  {
    _item = item;
  }

  protected final void writeImpl()
  {
    writeEx(108);
    writeItemInfo(_item);
  }
}