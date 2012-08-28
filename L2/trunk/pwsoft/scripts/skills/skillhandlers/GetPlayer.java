package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.skills.ISkillHandler;

public class GetPlayer
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.GET_PLAYER };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if (activeChar.isAlikeDead())
      return;
    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
    {
      L2Object target = (L2Object)n.getValue();
      if (target.isPlayer())
      {
        L2PcInstance trg = (L2PcInstance)target;
        if (trg.isAlikeDead())
          continue;
        trg.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ(), true);
      }
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}