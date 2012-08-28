package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.items.IItemHandler;

public class CompSpiritShotPacks
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5140, 5141, 5142, 5143, 5144, 5145, 5256, 5257, 5258, 5259, 5260, 5261 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!playable.isPlayer())
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

    SystemMessage sm = SystemMessage.id(SystemMessageId.EARNED_S2_S1_S);
    sm.addItemName(itemToCreateId);
    sm.addNumber(amount);
    activeChar.sendPacket(sm);

    activeChar.sendItems(false);
  }

  public int[] getItemIds() {
    return ITEM_IDS;
  }
}