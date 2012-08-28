package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.items.IItemHandler;

public class CompShotPacks
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5134, 5135, 5136, 5137, 5138, 5139, 5250, 5251, 5252, 5253, 5254, 5255 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!playable.isPlayer())
      return;
    L2PcInstance activeChar = (L2PcInstance)playable;

    int itemId = item.getItemId();
    int itemToCreateId = 0;
    int amount = 0;

    if ((itemId >= 5134) && (itemId <= 5139))
    {
      if (itemId == 5134)
        itemToCreateId = 1835;
      else {
        itemToCreateId = itemId - 3672;
      }
      amount = 300;
    }
    else if ((itemId >= 5250) && (itemId <= 5255))
    {
      if (itemId == 5250)
        itemToCreateId = 1835;
      else {
        itemToCreateId = itemId - 3788;
      }
      amount = 1000;
    }
    else if ((itemId < 5140) || (itemId > 5145))
    {
      if ((itemId < 5256) || (itemId > 5261));
    }

    activeChar.getInventory().destroyItem("Extract", item, activeChar, null);
    activeChar.getInventory().addItem("Extract", itemToCreateId, amount, activeChar, item);

    SystemMessage sm = SystemMessage.id(SystemMessageId.EARNED_S2_S1_S);
    sm.addItemName(itemToCreateId);
    sm.addNumber(amount);
    activeChar.sendPacket(sm);

    activeChar.sendItems(false);
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}