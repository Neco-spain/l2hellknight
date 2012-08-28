package l2m.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ItemInfo;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;

public class TradeStart extends L2GameServerPacket
{
  private List<ItemInfo> _tradelist = new ArrayList();
  private int targetId;

  public TradeStart(Player player, Player target)
  {
    targetId = target.getObjectId();

    ItemInstance[] items = player.getInventory().getItems();
    for (ItemInstance item : items)
      if (item.canBeTraded(player))
        _tradelist.add(new ItemInfo(item));
  }

  protected final void writeImpl()
  {
    writeC(20);
    writeD(targetId);
    writeH(_tradelist.size());
    for (ItemInfo item : _tradelist)
      writeItemInfo(item);
  }
}