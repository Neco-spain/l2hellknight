package scripts.items.itemhandlers;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.datatables.ExtractableItemsData;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.L2ExtractableItem;
import net.sf.l2j.gameserver.model.L2ExtractableProductItem;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.util.Rnd;
import scripts.items.IItemHandler;

public class ExtractableItems
  implements IItemHandler
{
  private static Logger _log = Logger.getLogger(ItemTable.class.getName());

  public void useItem(L2PlayableInstance playable, L2ItemInstance item) {
    if (!playable.isPlayer()) {
      return;
    }
    L2PcInstance activeChar = (L2PcInstance)playable;

    int itemID = item.getItemId();
    L2ExtractableItem exitem = ExtractableItemsData.getInstance().getExtractableItem(itemID);

    if (exitem == null) {
      return;
    }
    int createItemID = 0; int createAmount = 0; int rndNum = Rnd.get(100); int chanceFrom = 0;

    for (L2ExtractableProductItem expi : exitem.getProductItemsArray())
    {
      int chance = expi.getChance();

      if ((rndNum >= chanceFrom) && (rndNum <= chance + chanceFrom))
      {
        createItemID = expi.getId();
        createAmount = expi.getAmmount();
        break;
      }

      chanceFrom += chance;
    }

    if (createItemID == 0)
    {
      activeChar.sendMessage("Nothing happened.");
      return;
    }

    if (createItemID > 0)
    {
      if (ItemTable.getInstance().createDummyItem(createItemID) == null)
      {
        _log.warning("createItemID " + createItemID + " doesn't have template!");
        activeChar.sendMessage("Nothing happened.");
        return;
      }
      if (ItemTable.getInstance().createDummyItem(createItemID).isStackable())
      {
        activeChar.addItem("Extract", createItemID, createAmount, item, false);
      }
      else
        for (int i = 0; i < createAmount; i++)
          activeChar.addItem("Extract", createItemID, 1, item, false);
      SystemMessage sm;
      SystemMessage sm;
      if (createAmount > 1)
        sm = SystemMessage.id(SystemMessageId.EARNED_S2_S1_S).addItemName(createItemID).addNumber(createAmount);
      else
        sm = SystemMessage.id(SystemMessageId.EARNED_ITEM).addItemName(createItemID);
      activeChar.sendPacket(sm);
    }
    else {
      activeChar.sendMessage("Item failed to open");
    }
    activeChar.destroyItemByItemId("Extract", itemID, 1, activeChar.getTarget(), true);
  }

  public int[] getItemIds()
  {
    return ExtractableItemsData.getInstance().itemIDs();
  }
}