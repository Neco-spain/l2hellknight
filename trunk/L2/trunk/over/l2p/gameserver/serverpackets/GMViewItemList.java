package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.templates.item.ItemTemplate;

public class GMViewItemList extends L2GameServerPacket
{
  private int _size;
  private ItemInstance[] _items;
  private int _limit;
  private String _name;

  public GMViewItemList(Player cha, ItemInstance[] items, int size)
  {
    _size = size;
    _items = items;
    _name = cha.getName();
    _limit = cha.getInventoryLimit();
  }

  protected final void writeImpl()
  {
    writeC(154);
    writeS(_name);
    writeD(_limit);
    writeH(1);

    writeH(_size);
    for (ItemInstance temp : _items)
      if (!temp.getTemplate().isQuest())
        writeItemInfo(temp);
  }
}