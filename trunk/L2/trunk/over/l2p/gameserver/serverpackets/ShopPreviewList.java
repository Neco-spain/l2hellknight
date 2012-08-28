package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2p.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInfo;
import l2p.gameserver.model.items.TradeItem;
import l2p.gameserver.templates.item.ItemTemplate;

public class ShopPreviewList extends L2GameServerPacket
{
  private int _listId;
  private List<ItemInfo> _itemList;
  private long _money;

  public ShopPreviewList(BuyListHolder.NpcTradeList list, Player player)
  {
    _listId = list.getListId();
    _money = player.getAdena();
    List tradeList = list.getItems();
    _itemList = new ArrayList(tradeList.size());
    for (TradeItem item : list.getItems())
      if (item.getItem().isEquipable())
        _itemList.add(item);
  }

  protected final void writeImpl()
  {
    writeC(245);
    writeD(5056);
    writeQ(_money);
    writeD(_listId);
    writeH(_itemList.size());

    for (ItemInfo item : _itemList)
      if (item.getItem().isEquipable())
      {
        writeD(item.getItemId());
        writeH(item.getItem().getType2ForPackets());
        writeH(item.getItem().isEquipable() ? item.getItem().getBodyPart() : 0);
        writeQ(getWearPrice(item.getItem()));
      }
  }

  public static int getWearPrice(ItemTemplate item)
  {
    switch (1.$SwitchMap$l2p$gameserver$templates$item$ItemTemplate$Grade[item.getItemGrade().ordinal()])
    {
    case 1:
      return 50;
    case 2:
      return 100;
    case 3:
      return 200;
    case 4:
      return 500;
    case 5:
      return 1000;
    case 6:
      return 2000;
    case 7:
      return 2500;
    }
    return 10;
  }
}