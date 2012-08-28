package scripts.skills.skillhandlers;

import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.skills.ISkillHandler;

public class Charge
  implements ISkillHandler
{
  static Logger _log = Logger.getLogger(Charge.class.getName());

  private static final L2Skill.SkillType[] SKILL_IDS = new L2Skill.SkillType[0];

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
    {
      L2Character ctarget = (L2Character)n.getValue();
      if (!ctarget.isPlayer())
        continue;
      L2PcInstance target = (L2PcInstance)ctarget;
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