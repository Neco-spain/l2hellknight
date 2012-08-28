package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
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
import scripts.skills.ISkillHandler;

public class Sweep
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.SWEEP };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if (!activeChar.isPlayer()) {
      return;
    }
    L2PcInstance player = (L2PcInstance)activeChar;
    InventoryUpdate iu = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
    boolean send = false;

    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
    {
      L2Object obj = (L2Object)n.getValue();
      if ((obj == null) || (!obj.isL2Attackable())) {
        continue;
      }
      L2Attackable target = (L2Attackable)obj;
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
            SystemMessage smsg;
            if (ritem.getCount() > 1)
              smsg = SystemMessage.id(SystemMessageId.EARNED_S2_S1_S).addItemName(ritem.getItemId()).addNumber(ritem.getCount());
            else {
              smsg = SystemMessage.id(SystemMessageId.EARNED_ITEM).addItemName(ritem.getItemId());
            }
            player.sendPacket(smsg);
          }
        }
      }
      target.endDecayTask();

      if (send)
      {
        if (iu != null)
          player.sendPacket(iu);
        else
          player.sendPacket(new ItemList(player, false));
      }
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}