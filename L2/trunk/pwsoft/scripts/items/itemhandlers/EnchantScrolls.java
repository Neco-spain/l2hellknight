package scripts.items.itemhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.ChooseInventoryItem;
import scripts.items.IItemHandler;

public class EnchantScrolls
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 729, 730, 731, 732, 6569, 6570, 947, 948, 949, 950, 6571, 6572, 951, 952, 953, 954, 6573, 6574, 955, 956, 957, 958, 6575, 6576, 959, 960, 961, 962, 6577, 6578 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!playable.isPlayer()) {
      return;
    }
    L2PcInstance player = (L2PcInstance)playable;

    player.sendActionFailed();

    if (player.isCastingNow()) {
      return;
    }
    if ((player.isOutOfControl()) || (player.isInOlympiadMode())) {
      return;
    }
    if (Config.ENCH_ANTI_CLICK)
    {
      if (player.getEnchClicks() >= Config.ENCH_ANTI_CLICK_STEP)
      {
        player.showAntiClickPWD();
        return;
      }
      player.updateEnchClicks();
    }

    player.setActiveEnchantItem(item);
    player.sendPacket(Static.SELECT_ITEM_TO_ENCHANT);
    player.sendPacket(new ChooseInventoryItem(item.getItemId()));
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}