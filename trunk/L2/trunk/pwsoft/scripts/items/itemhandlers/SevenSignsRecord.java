package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.SSQStatus;
import scripts.items.IItemHandler;

public class SevenSignsRecord
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5707 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    L2PcInstance activeChar;
    if (playable.isPlayer()) {
      activeChar = (L2PcInstance)playable;
    }
    else
    {
      L2PcInstance activeChar;
      if (playable.isPet())
        activeChar = ((L2PetInstance)playable).getOwner();
      else
        return;
    }
    L2PcInstance activeChar;
    activeChar.sendPacket(new SSQStatus(activeChar, 1));
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}