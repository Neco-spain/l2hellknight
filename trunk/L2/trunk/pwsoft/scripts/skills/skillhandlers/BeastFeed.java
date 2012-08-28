package scripts.skills.skillhandlers;

import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2FeedableBeastInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.skills.ISkillHandler;

public class BeastFeed
  implements ISkillHandler
{
  private static Logger _log = Logger.getLogger(BeastFeed.class.getName());
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.BEAST_FEED };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if (!activeChar.isPlayer()) {
      return;
    }

    L2Object target = null;
    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
    {
      target = (L2Object)n.getValue();
      if ((target == null) || (!(target instanceof L2FeedableBeastInstance))) {
        continue;
      }
      ((L2FeedableBeastInstance)target).onSkillUse((L2PcInstance)activeChar, skill.getId());
    }
    target = null;
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}