package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import scripts.items.IItemHandler;

public class Firework
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 6403, 6406, 6407 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!playable.isPlayer())
      return;
    L2PcInstance pl = (L2PcInstance)playable;

    if (System.currentTimeMillis() - pl.gCPBG() < 5500L)
      return;
    pl.sCPBG();

    int fwId = 0;
    switch (item.getItemId())
    {
    case 6403:
      fwId = 2023;
      break;
    case 6406:
      fwId = 2024;
      break;
    case 6407:
      fwId = 2025;
    case 6404:
    case 6405:
    }

    playable.broadcastPacket(new MagicSkillUser(playable, playable, fwId, 1, 1, 0));
    playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
  }

  public void useFw(L2PcInstance pl, int magicId, int level) {
    L2Skill skill = SkillTable.getInstance().getInfo(magicId, level);
    if (skill != null)
      pl.useMagic(skill, false, false);
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}