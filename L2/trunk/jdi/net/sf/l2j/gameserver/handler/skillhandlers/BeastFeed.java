package net.sf.l2j.gameserver.handler.skillhandlers;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class BeastFeed
  implements ISkillHandler
{
  private static Logger _log = Logger.getLogger(BeastFeed.class.getName());
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.BEAST_FEED };

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    if (!(activeChar instanceof L2PcInstance)) {
      return;
    }
    L2Object[] targetList = skill.getTargetList(activeChar);

    if (targetList == null) return;

    _log.fine("Beast Feed casting succeded.");
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}