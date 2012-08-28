package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;
import scripts.skills.ISkillHandler;

public class ManaHeal
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.MANAHEAL, L2Skill.SkillType.MANARECHARGE, L2Skill.SkillType.MANAHEAL_PERCENT };

  public void useSkill(L2Character caster, L2Skill skill, FastList<L2Object> targets)
  {
    L2Character target = null;
    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
    {
      target = (L2Character)n.getValue();
      double mp = target.getCurrentMp() == target.getMaxMp() ? 0.0D : skill.getPower();

      if (mp > 0.0D)
      {
        switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[skill.getSkillType().ordinal()])
        {
        case 1:
          mp = target.getMaxMp() * mp / 100.0D;
          break;
        case 2:
          mp = target.calcStat(Stats.RECHARGE_MP_RATE, mp, null, null);
          if (!caster.equals(target)) break;
          mp = 0.0D;
        }

        target.setLastHealAmount((int)mp);
        target.setCurrentMp(target.getCurrentMp() + mp);
      }
      SystemMessage sm;
      SystemMessage sm;
      if ((caster.isPlayer()) && (!caster.equals(target)))
        sm = SystemMessage.id(SystemMessageId.S2_MP_RESTORED_BY_S1).addString(caster.getName());
      else
        sm = SystemMessage.id(SystemMessageId.S1_MP_RESTORED);
      sm.addNumber((int)mp);
      target.sendPacket(sm);
    }
    SystemMessage sm = null;
    target = null;
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}