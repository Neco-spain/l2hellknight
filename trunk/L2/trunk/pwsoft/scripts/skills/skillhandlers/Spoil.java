package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import scripts.skills.ISkillHandler;

public class Spoil
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.SPOIL };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if (!activeChar.isPlayer()) {
      return;
    }
    if (targets == null) {
      return;
    }
    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
    {
      L2Object obj = (L2Object)n.getValue();
      if ((obj == null) || (!obj.isL2Monster())) {
        continue;
      }
      L2MonsterInstance target = (L2MonsterInstance)obj;

      if (target.isSpoil()) {
        activeChar.sendPacket(Static.ALREDAY_SPOILED);
        continue;
      }

      boolean spoil = false;
      if (!target.isDead())
      {
        spoil = Formulas.calcMagicSuccess(activeChar, (L2Character)obj, skill);

        if (spoil)
        {
          target.setSpoil(true);
          target.setIsSpoiledBy(activeChar.getObjectId());
          activeChar.sendPacket(Static.SPOIL_SUCCESS);
        }
        else {
          activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2).addString(target.getName()).addSkillName(skill.getDisplayId()));
        }
        target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
      }
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}