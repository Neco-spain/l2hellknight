package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import scripts.items.IItemHandler;

public class CrystalCarol
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5562, 5563, 5564, 5565, 5566, 5583, 5584, 5585, 5586, 5587, 4411, 4412, 4413, 4414, 4415, 4416, 4417, 5010, 6903, 7061, 7062, 8555 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!playable.isPlayer()) {
      return;
    }
    int songId = 0;
    L2PcInstance activeChar = (L2PcInstance)playable;
    switch (item.getItemId())
    {
    case 5562:
      songId = 2140;
      break;
    case 5563:
      songId = 2141;
      break;
    case 5564:
      songId = 2142;
      break;
    case 5565:
      songId = 2143;
      break;
    case 5566:
      songId = 2144;
      break;
    case 5583:
      songId = 2145;
      break;
    case 5584:
      songId = 2146;
      break;
    case 5585:
      songId = 2147;
      break;
    case 5586:
      songId = 2148;
      break;
    case 5587:
      songId = 2149;
      break;
    case 4411:
      songId = 2069;
      break;
    case 4412:
      songId = 2068;
      break;
    case 4413:
      songId = 2070;
      break;
    case 4414:
      songId = 2072;
      break;
    case 4415:
      songId = 2071;
      break;
    case 4416:
      songId = 2073;
      break;
    case 4417:
      songId = 2067;
      break;
    case 5010:
      songId = 2066;
      break;
    case 6903:
      songId = 2187;
      break;
    case 7061:
      songId = 2073;
      break;
    case 7062:
      songId = 2230;
      break;
    case 8555:
      songId = 2272;
    }

    activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
    activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, songId, 1, 1, 0));
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}