package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2FeedableBeastInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class BeastSpice
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 6643, 6644 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!(playable instanceof L2PcInstance)) {
      return;
    }
    L2PcInstance activeChar = (L2PcInstance)playable;

    if (!(activeChar.getTarget() instanceof L2FeedableBeastInstance))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
      return;
    }

    L2Object[] targets = new L2Object[1];
    targets[0] = activeChar.getTarget();

    int itemId = item.getItemId();
    if (itemId == 6643)
    {
      activeChar.useMagic(SkillTable.getInstance().getInfo(2188, 1), false, false);
    }
    else if (itemId == 6644)
    {
      activeChar.useMagic(SkillTable.getInstance().getInfo(2189, 1), false, false);
    }
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}