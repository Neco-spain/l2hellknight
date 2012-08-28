package net.sf.l2j.gameserver.handler.skillhandlers;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class Charge
  implements ISkillHandler
{
  static Logger _log = Logger.getLogger(Charge.class.getName());

  private static final L2Skill.SkillType[] SKILL_IDS = new L2Skill.SkillType[0];

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    for (int index = 0; index < targets.length; index++)
    {
      if (!(targets[index] instanceof L2PcInstance))
        continue;
      L2PcInstance target = (L2PcInstance)targets[index];
      skill.getEffects(activeChar, target);
    }

    L2Effect effect = activeChar.getFirstEffect(skill.getId());
    if ((effect != null) && (effect.isSelfEffect()))
    {
      effect.exit();
    }
    skill.getEffectsSelf(activeChar);
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}