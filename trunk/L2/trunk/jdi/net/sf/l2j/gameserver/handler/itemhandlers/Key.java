package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2ChestInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class Key
  implements IItemHandler
{
  public static final int INTERACTION_DISTANCE = 100;
  private static final int[] ITEM_IDS = { 6665, 6666, 6667, 6668, 6669, 6670, 6671, 6672 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!(playable instanceof L2PcInstance)) return;

    L2PcInstance activeChar = (L2PcInstance)playable;
    int itemId = item.getItemId();
    L2Skill skill = SkillTable.getInstance().getInfo(2229, itemId - 6664);
    L2Object target = activeChar.getTarget();

    if ((!(target instanceof L2ChestInstance)) || (target == null))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
      activeChar.sendPacket(new ActionFailed());
    }
    else
    {
      L2ChestInstance chest = (L2ChestInstance)target;
      if ((chest.isDead()) || (chest.isInteracted()))
      {
        activeChar.sendMessage("The chest Is empty.");
        activeChar.sendPacket(new ActionFailed());
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