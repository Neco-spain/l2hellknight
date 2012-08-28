package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ChooseInventoryItem;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class EnchantScrolls
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 729, 730, 731, 732, 6569, 6570, 947, 948, 949, 950, 6571, 6572, 951, 952, 953, 954, 6573, 6574, 955, 956, 957, 958, 6575, 6576, 959, 960, 961, 962, 6577, 6578 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!(playable instanceof L2PcInstance)) return;
    L2PcInstance activeChar = (L2PcInstance)playable;
    if (activeChar.isCastingNow()) return;
    if (Shutdown.getCounterInstance() != null)
    {
      activeChar.sendMessage("\u0412\u043E \u0432\u0440\u0435\u043C\u044F \u043E\u0442\u0441\u0447\u0435\u0442\u0430 \u0434\u043E \u0432\u044B\u043A\u043B\u044E\u0447\u0435\u043D\u0438\u044F \u0432\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0442\u043E\u0447\u0438\u0442\u044C \u0432\u0435\u0449\u0438");
      return;
    }
    activeChar.setActiveEnchantItem(item);
    activeChar.sendPacket(new SystemMessage(SystemMessageId.SELECT_ITEM_TO_ENCHANT));
    activeChar.sendPacket(new ChooseInventoryItem(item.getItemId()));
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}