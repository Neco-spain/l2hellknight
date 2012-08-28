package net.sf.l2j.gameserver.skills.targets;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;

public class TargetAngelArea extends TargetList
{
  public final FastList<L2Object> getTargetList(FastList<L2Object> targets, L2Character activeChar, boolean onlyFirst, L2Character target, L2Skill skill)
  {
    if ((target == null) || (target.isDead())) {
      return targets;
    }
    if ((skill.getCastRange() >= 0) && (target.isAngel()))
    {
      if (onlyFirst)
      {
        targets.add(target);
        return targets;
      }
      targets.add(target);
    }
    else
    {
      activeChar.sendPacket(Static.TARGET_IS_INCORRECT);
      return targets;
    }

    FastList objs = target.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius());
    if ((objs == null) || (objs.isEmpty())) {
      return targets;
    }
    L2Character cha = null;
    FastList.Node n = objs.head(); for (FastList.Node end = objs.tail(); (n = n.getNext()) != end; )
    {
      cha = (L2Character)n.getValue();
      if ((cha == null) || 
        (!cha.isAngel()) || 
        (!activeChar.canSeeTarget(cha))) {
        continue;
      }
      targets.add(cha);
    }
    return targets;
  }
}