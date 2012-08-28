package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.FloodProtector;

public class ItemBuff
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { Config.IB_ITEM_ID1, Config.IB_ITEM_ID2, Config.IB_ITEM_ID3, Config.IB_ITEM_ID4, Config.IB_ITEM_ID5 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!(playable instanceof L2PcInstance)) return;
    L2PcInstance activeChar = (L2PcInstance)playable;
    int itemId = item.getItemId();

    if (!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), 16))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
      sm.addItemName(itemId);
      activeChar.sendPacket(sm);
      return;
    }

    if (itemId == Config.IB_ITEM_ID1)
    {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, Config.IB_SKILL_ID1, 1, 1, 0);
      activeChar.sendPacket(MSU);
      activeChar.broadcastPacket(MSU);
      useBuff(activeChar, Config.IB_SKILL_ID1, Config.IB_SKILL_LVL1);
    }
    else if (itemId == Config.IB_ITEM_ID2)
    {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, Config.IB_SKILL_ID2, 1, 1, 0);
      activeChar.sendPacket(MSU);
      activeChar.broadcastPacket(MSU);
      useBuff(activeChar, Config.IB_SKILL_ID2, Config.IB_SKILL_LVL2);
    }
    else if (itemId == Config.IB_ITEM_ID3)
    {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, Config.IB_SKILL_ID3, 1, 1, 0);
      activeChar.sendPacket(MSU);
      activeChar.broadcastPacket(MSU);
      useBuff(activeChar, Config.IB_SKILL_ID3, Config.IB_SKILL_LVL3);
    }
    else if (itemId == Config.IB_ITEM_ID4)
    {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, Config.IB_SKILL_ID4, 1, 1, 0);
      activeChar.sendPacket(MSU);
      activeChar.broadcastPacket(MSU);
      useBuff(activeChar, Config.IB_SKILL_ID4, Config.IB_SKILL_LVL4);
    }
    else if (itemId == Config.IB_ITEM_ID5)
    {
      MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, Config.IB_SKILL_ID5, 1, 1, 0);
      activeChar.sendPacket(MSU);
      activeChar.broadcastPacket(MSU);
      useBuff(activeChar, Config.IB_SKILL_ID5, Config.IB_SKILL_LVL5);
    }
  }

  public void useBuff(L2PcInstance activeChar, int magicId, int level) {
    L2Skill skill = SkillTable.getInstance().getInfo(magicId, level);
    if (skill != null)
      activeChar.useMagic(skill, false, false);
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}