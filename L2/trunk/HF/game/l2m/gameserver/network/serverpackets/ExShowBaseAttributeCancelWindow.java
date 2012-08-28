package l2m.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.Element;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.templates.item.ItemTemplate;

public class ExShowBaseAttributeCancelWindow extends L2GameServerPacket
{
  private final List<ItemInstance> _items = new ArrayList();

  public ExShowBaseAttributeCancelWindow(Player activeChar)
  {
    for (ItemInstance item : activeChar.getInventory().getItems())
    {
      if ((item.getAttributeElement() == Element.NONE) || (!item.canBeEnchanted(true)) || (getAttributeRemovePrice(item) == 0L))
        continue;
      _items.add(item);
    }
  }

  protected final void writeImpl()
  {
    writeEx(116);
    writeD(_items.size());
    for (ItemInstance item : _items)
    {
      writeD(item.getObjectId());
      writeQ(getAttributeRemovePrice(item));
    }
  }

  public static long getAttributeRemovePrice(ItemInstance item)
  {
    switch (1.$SwitchMap$l2p$gameserver$templates$item$ItemTemplate$Grade[item.getCrystalType().ordinal()])
    {
    case 1:
      return item.getTemplate().getType2() == 0 ? 50000L : 40000L;
    case 2:
      return item.getTemplate().getType2() == 0 ? 100000L : 80000L;
    case 3:
      return item.getTemplate().getType2() == 0 ? 200000L : 160000L;
    }
    return 0L;
  }
}