package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.skills.ISkillHandler;

public class CombatPointHeal
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.COMBATPOINTHEAL };

  public void useSkill(L2Character actChar, L2Skill skill, FastList<L2Object> targets)
  {
    L2Character target = null;
    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
    {
      target = (L2Character)n.getValue();
      double cp = target.getCurrentCp() == target.getMaxCp() ? 0.0D : skill.getPower();
      if (cp > 0.0D) {
        target.setCurrentCp(target.getCurrentCp() + cp);
      }
      target.sendPacket(SystemMessage.id(SystemMessageId.S1_CP_WILL_BE_RESTORED).addNumber((int)cp));
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}