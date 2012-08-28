package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FeedableBeastInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import scripts.items.IItemHandler;

public class BeastSpice
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 6643, 6644 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!playable.isPlayer()) {
      return;
    }
    L2PcInstance activeChar = (L2PcInstance)playable;

    if (!(activeChar.getTarget() instanceof L2FeedableBeastInstance))
    {
      activeChar.sendPacket(Static.TARGET_IS_INCORRECT);
      return;
    }

    switch (item.getItemId())
    {
    case 6643:
      activeChar.useMagic(SkillTable.getInstance().getInfo(2188, 1), false, false);
      break;
    case 6644:
      activeChar.useMagic(SkillTable.getInstance().getInfo(2189, 1), false, false);
    }
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}