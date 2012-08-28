package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.ShowXMasSeal;
import scripts.items.IItemHandler;

public class SpecialXMas
  implements IItemHandler
{
  private static int[] _itemIds = { 5555 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!playable.isPlayer())
      return;
    L2PcInstance activeChar = (L2PcInstance)playable;

    if (item.getItemId() == 5555)
      activeChar.broadcastPacket(new ShowXMasSeal(5555));
  }

  public int[] getItemIds()
  {
    return _itemIds;
  }
}