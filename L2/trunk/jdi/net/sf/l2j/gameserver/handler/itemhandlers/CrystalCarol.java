package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;

public class CrystalCarol
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5562, 5563, 5564, 5565, 5566, 5583, 5584, 5585, 5586, 5587, 4411, 4412, 4413, 4414, 4415, 4416, 4417, 5010, 6903, 7061, 7062, 8555 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!(playable instanceof L2PcInstance))
      return;
    L2PcInstance activeChar = (L2PcInstance)playable;
    int itemId = item.getItemId();
    if (itemId == 5562) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2140, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 5563) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2141, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 5564) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2142, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 5565) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2143, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 5566) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2144, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 5583) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2145, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 5584) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2146, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 5585) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2147, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 5586) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2148, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 5587) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2149, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 4411) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2069, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 4412) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2068, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 4413) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2070, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 4414) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2072, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 4415) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2071, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 4416) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2073, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 4417) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2067, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 5010) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2066, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 6903) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2187, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 7061) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2073, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 7062) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2230, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }
    else if (itemId == 8555) {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2272, 1, 1, 0);
      activeChar.broadcastPacket(MSU);
    }

    activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}