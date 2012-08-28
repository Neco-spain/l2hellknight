package l2m.gameserver.network.clientpackets;

import java.util.Map;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.data.xml.holder.ItemHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.PremiumItem;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExGetPremiumItemList;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.templates.item.ItemTemplate;

public final class RequestWithDrawPremiumItem extends L2GameClientPacket
{
  private int _itemNum;
  private int _charId;
  private long _itemcount;

  protected void readImpl()
  {
    _itemNum = readD();
    _charId = readD();
    _itemcount = readQ();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();

    if (activeChar == null)
      return;
    if (_itemcount <= 0L) {
      return;
    }
    if (activeChar.getObjectId() != _charId)
    {
      return;
    }if (activeChar.getPremiumItemList().isEmpty())
    {
      return;
    }if ((activeChar.getWeightPenalty() >= 3) || (activeChar.getInventoryLimit() * 0.8D <= activeChar.getInventory().getSize()))
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_RECEIVE_THE_VITAMIN_ITEM_BECAUSE_YOU_HAVE_EXCEED_YOUR_INVENTORY_WEIGHT_QUANTITY_LIMIT);
      return;
    }
    if (activeChar.isProcessingRequest())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_RECEIVE_A_VITAMIN_ITEM_DURING_AN_EXCHANGE);
      return;
    }

    PremiumItem _item = (PremiumItem)activeChar.getPremiumItemList().get(Integer.valueOf(_itemNum));
    if (_item == null)
      return;
    boolean stackable = ItemHolder.getInstance().getTemplate(_item.getItemId()).isStackable();
    if (_item.getCount() < _itemcount)
      return;
    if (!stackable)
      for (int i = 0; i < _itemcount; i++)
        addItem(activeChar, _item.getItemId(), 1L);
    else
      addItem(activeChar, _item.getItemId(), _itemcount);
    if (_itemcount < _item.getCount())
    {
      ((PremiumItem)activeChar.getPremiumItemList().get(Integer.valueOf(_itemNum))).updateCount(_item.getCount() - _itemcount);
      activeChar.updatePremiumItem(_itemNum, _item.getCount() - _itemcount);
    }
    else
    {
      activeChar.getPremiumItemList().remove(Integer.valueOf(_itemNum));
      activeChar.deletePremiumItem(_itemNum);
    }

    if (activeChar.getPremiumItemList().isEmpty())
      activeChar.sendPacket(Msg.THERE_ARE_NO_MORE_VITAMIN_ITEMS_TO_BE_FOUND);
    else
      activeChar.sendPacket(new ExGetPremiumItemList(activeChar));
  }

  private void addItem(Player player, int itemId, long count)
  {
    player.getInventory().addItem(itemId, count);
    player.sendPacket(SystemMessage2.obtainItems(itemId, count, 0));
  }
}