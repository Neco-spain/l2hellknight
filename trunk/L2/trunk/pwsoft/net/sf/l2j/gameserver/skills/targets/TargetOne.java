package net.sf.l2j.gameserver.skills.targets;

import javolution.util.FastList;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;

public class TargetOne extends TargetList
{
  public final FastList<L2Object> getTargetList(FastList<L2Object> targets, L2Character activeChar, boolean onlyFirst, L2Character target, L2Skill skill)
  {
    if ((target == null) || (target.isDead()) || ((target.equals(activeChar)) && (!skill.canTargetSelf())))
    {
      activeChar.sendPacket(Static.TARGET_IS_INCORRECT);
      return targets;
    }

    if ((activeChar.isPlayer()) && (target.isPlayer()))
    {
      if ((skill.isPvpSkill()) && (activeChar.getTarget() != activeChar) && (activeChar.getParty() != null) && (activeChar.getParty().getPartyMembers().contains(target)))
      {
        activeChar.sendPacket(Static.TARGET_IS_INCORRECT);
        return targets;
      }

    }

    targets.add(target);
    return targets;
  }
}