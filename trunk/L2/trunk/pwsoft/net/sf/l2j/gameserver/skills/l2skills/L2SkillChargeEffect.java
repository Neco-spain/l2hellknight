package net.sf.l2j.gameserver.skills.l2skills;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillChargeEffect extends L2Skill
{
  final int chargeSkillId;

  public L2SkillChargeEffect(StatsSet set)
  {
    super(set);
    chargeSkillId = set.getInteger("charge_skill_id");
  }

  public boolean checkCondition(L2Character activeChar, L2Object target, boolean itemOrWeapon)
  {
    if (activeChar.isPlayer())
    {
      if (activeChar.getCharges() < getNumCharges())
      {
        activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(getId()));
        return false;
      }
    }
    return super.checkCondition(activeChar, target, itemOrWeapon);
  }

  public void useSkill(L2Character activeChar, FastList<L2Object> targets)
  {
    if (activeChar.isAlikeDead()) {
      return;
    }

    if (activeChar.getCharges() < getNumCharges())
    {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(getId()));
      return;
    }

    activeChar.decreaseCharges(getNumCharges());
    FastList.Node n;
    if (hasEffects())
    {
      n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
      {
        L2Character target = (L2Character)n.getValue();
        if (target == null) {
          continue;
        }
        getEffects(activeChar, target);
      }
    }

    if (getId() == 461)
    {
      activeChar.stopEffects(L2Effect.EffectType.ROOT);
      activeChar.stopSlowEffects();
    }
    activeChar.sendEtcStatusUpdate();
  }
}