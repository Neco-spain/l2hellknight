package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Attackable.RewardItem;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class Sweep
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.SWEEP };

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    if (!(activeChar instanceof L2PcInstance))
    {
      return;
    }

    L2PcInstance player = (L2PcInstance)activeChar;
    InventoryUpdate iu = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
    boolean send = false;

    for (int index = 0; index < targets.length; index++)
    {
      if (!(targets[index] instanceof L2Attackable))
        continue;
      L2Attackable target = (L2Attackable)targets[index];
      L2Attackable.RewardItem[] items = null;
      boolean isSweeping = false;
      synchronized (target) {
        if (target.isSweepActive())
        {
          items = target.takeSweep();
          isSweeping = true;
        }
      }
      if (isSweeping)
      {
        if ((items == null) || (items.length == 0))
          continue;
        for (L2Attackable.RewardItem ritem : items)
        {
          if (player.isInParty()) {
            player.getParty().distributeItem(player, ritem, true, target);
          }
          else {
            L2ItemInstance item = player.getInventory().addItem("Sweep", ritem.getItemId(), ritem.getCount(), player, target);
            if (iu != null) iu.addItem(item);
            send = true;
            SystemMessage smsg;
            if (ritem.getCount() > 1)
            {
              SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
              smsg.addItemName(ritem.getItemId());
              smsg.addNumber(ritem.getCount());
            }
            else
            {
              smsg = new SystemMessage(SystemMessageId.EARNED_ITEM);
              smsg.addItemName(ritem.getItemId());
            }
            player.sendPacket(smsg);
          }
        }
      }
      target.endDecayTask();

      if (!send)
        continue;
      if (iu != null)
        player.sendPacket(iu);
      else
        player.sendPacket(new ItemList(player, false));
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}