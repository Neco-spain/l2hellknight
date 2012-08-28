package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.ShowXMasSeal;

public class SpecialXMas
  implements IItemHandler
{
  private static int[] _itemIds = { 5555 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!(playable instanceof L2PcInstance))
      return;
    L2PcInstance activeChar = (L2PcInstance)playable;
    int itemId = item.getItemId();

    if (itemId == 5555)
    {
      ShowXMasSeal SXS = new ShowXMasSeal(5555);
      activeChar.broadcastPacket(SXS);
    }
  }

  public int[] getItemIds()
  {
    return _itemIds;
  }
}