package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.RadarControl;
import net.sf.l2j.gameserver.network.serverpackets.ShowMiniMap;

public class Maps
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 1665, 1863, 7063 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!(playable instanceof L2PcInstance))
      return;
    L2PcInstance activeChar = (L2PcInstance)playable;
    int itemId = item.getItemId();
    if (itemId == 7063)
    {
      activeChar.sendPacket(new ShowMiniMap(1665));
      activeChar.sendPacket(new RadarControl(0, 1, 51995, -51265, -3104));
    }
    else {
      activeChar.sendPacket(new ShowMiniMap(itemId));
    }
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}