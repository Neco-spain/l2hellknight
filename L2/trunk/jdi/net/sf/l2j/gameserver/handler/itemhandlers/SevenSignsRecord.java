package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.SSQStatus;

public class SevenSignsRecord
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5707 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    L2PcInstance activeChar;
    if ((playable instanceof L2PcInstance)) {
      activeChar = (L2PcInstance)playable;
    }
    else
    {
      L2PcInstance activeChar;
      if ((playable instanceof L2PetInstance))
        activeChar = ((L2PetInstance)playable).getOwner();
      else
        return;
    }
    L2PcInstance activeChar;
    SSQStatus ssqs = new SSQStatus(activeChar, 1);
    activeChar.sendPacket(ssqs);
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}