package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class CompSpiritShotPacks
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5140, 5141, 5142, 5143, 5144, 5145, 5256, 5257, 5258, 5259, 5260, 5261 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!(playable instanceof L2PcInstance))
      return;
    L2PcInstance activeChar = (L2PcInstance)playable;

    int itemId = item.getItemId();
    int amount;
    int itemToCreateId;
    int amount;
    if (itemId < 5200) {
      int itemToCreateId = itemId - 2631;
      amount = 300;
    } else {
      itemToCreateId = itemId - 2747;
      amount = 1000;
    }

    activeChar.getInventory().destroyItem("Extract", item, activeChar, null);
    activeChar.getInventory().addItem("Extract", itemToCreateId, amount, activeChar, item);

    SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
    sm.addItemName(itemToCreateId);
    sm.addNumber(amount);
    activeChar.sendPacket(sm);

    ItemList playerUI = new ItemList(activeChar, false);
    activeChar.sendPacket(playerUI);
  }

  public int[] getItemIds() {
    return ITEM_IDS;
  }
}