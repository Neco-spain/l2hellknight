package l2m.gameserver.serverpackets;

import java.util.Map;
import l2m.gameserver.model.items.Inventory;

public class ShopPreviewInfo extends L2GameServerPacket
{
  private Map<Integer, Integer> _itemlist;

  public ShopPreviewInfo(Map<Integer, Integer> itemlist)
  {
    _itemlist = itemlist;
  }

  protected void writeImpl()
  {
    writeC(246);
    writeD(26);

    for (int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
      writeD(getFromList(PAPERDOLL_ID));
  }

  private int getFromList(int key)
  {
    return _itemlist.get(Integer.valueOf(key)) != null ? ((Integer)_itemlist.get(Integer.valueOf(key))).intValue() : 0;
  }
}