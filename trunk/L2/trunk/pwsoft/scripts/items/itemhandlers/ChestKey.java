package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2ChestInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import scripts.items.IItemHandler;

public class ChestKey
  implements IItemHandler
{
  public static final int INTERACTION_DISTANCE = 100;
  private static final int[] ITEM_IDS = { 6665, 6666, 6667, 6668, 6669, 6670, 6671, 6672 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!playable.isPlayer()) return;

    L2PcInstance activeChar = (L2PcInstance)playable;
    L2Skill skill = SkillTable.getInstance().getInfo(2229, item.getItemId() - 6664);
    L2Object target = activeChar.getTarget();

    if ((target == null) || (!(target instanceof L2ChestInstance)))
    {
      activeChar.sendPacket(Static.INCORRECT_TARGET);
      activeChar.sendActionFailed();
    }
    else
    {
      L2ChestInstance chest = (L2ChestInstance)target;
      if ((chest.isDead()) || (chest.isInteracted()))
      {
        activeChar.sendMessage("The chest Is empty.");
        activeChar.sendActionFailed();
        return;
      }
      activeChar.useMagic(skill, false, false);
    }
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}