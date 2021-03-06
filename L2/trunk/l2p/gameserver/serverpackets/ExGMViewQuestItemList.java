package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.templates.item.ItemTemplate;

public class ExGMViewQuestItemList extends L2GameServerPacket
{
  private int _size;
  private ItemInstance[] _items;
  private int _limit;
  private String _name;

  public ExGMViewQuestItemList(Player player, ItemInstance[] items, int size)
  {
    _items = items;
    _size = size;
    _name = player.getName();
    _limit = Config.QUEST_INVENTORY_MAXIMUM;
  }

  protected final void writeImpl()
  {
    writeEx(199);
    writeS(_name);
    writeD(_limit);
    writeH(_size);
    for (ItemInstance temp : _items)
      if (temp.getTemplate().isQuest())
        writeItemInfo(temp);
  }
}