package l2m.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ItemInfo;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;

public class ExReplyPostItemList extends L2GameServerPacket
{
  private List<ItemInfo> _itemsList = new ArrayList();

  public ExReplyPostItemList(Player activeChar)
  {
    ItemInstance[] items = activeChar.getInventory().getItems();
    for (ItemInstance item : items)
      if (item.canBeTraded(activeChar))
        _itemsList.add(new ItemInfo(item));
  }

  protected void writeImpl()
  {
    writeEx(178);
    writeD(_itemsList.size());
    for (ItemInfo item : _itemsList)
      writeItemInfo(item);
  }
}