package scripts.skills.skillhandlers;

import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import scripts.skills.ISkillHandler;

public class DrainSoul
  implements ISkillHandler
{
  private static Logger _log = Logger.getLogger(DrainSoul.class.getName());
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.DRAIN_SOUL };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if (!activeChar.isPlayer()) {
      return;
    }
    FastList targetList = skill.getTargetList(activeChar);

    if (targetList.isEmpty()) {
      return;
    }
    _log.fine("Soul Crystal casting succeded.");
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}