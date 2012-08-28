package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;
import scripts.skills.ISkillHandler;

public class Resurrect
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.RESURRECT };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    L2PcInstance player = null;
    if (activeChar.isPlayer()) player = (L2PcInstance)activeChar;

    L2Character target = null;

    FastList targetToRes = new FastList();

    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
    {
      target = (L2Character)n.getValue();

      if (target.isPlayer())
      {
        L2PcInstance targetPlayer = (L2PcInstance)target;

        if ((skill.getTargetType() == L2Skill.SkillTargetType.TARGET_CORPSE_CLAN) && 
          (player.getClanId() != targetPlayer.getClanId()))
          continue;
      }
      if (!target.isVisible()) continue; targetToRes.add(target);
    }

    if (targetToRes.size() == 0)
    {
      activeChar.abortCast();
      activeChar.sendPacket(SystemMessage.sendString("\u041D\u0435 \u043A\u043E\u0433\u043E \u0440\u0435\u0441\u0430\u0442\u044C"));
    }

    FastList.Node n = targetToRes.head(); for (FastList.Node end = targetToRes.tail(); (n = n.getNext()) != end; )
    {
      L2Character cha = (L2Character)n.getValue();
      if (activeChar.isPlayer())
      {
        if (cha.isPlayer())
          ((L2PcInstance)cha).reviveRequest((L2PcInstance)activeChar, skill, false);
        else if (cha.isPet())
        {
          if (((L2PetInstance)cha).getOwner() == activeChar)
            cha.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), activeChar.getWIT()));
          else
            ((L2PetInstance)cha).getOwner().reviveRequest((L2PcInstance)activeChar, skill, true);
        }
        else cha.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), activeChar.getWIT()));
      }
      else
      {
        DecayTaskManager.getInstance().cancelDecayTask(cha);
        cha.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), activeChar.getWIT()));
      }
    }

    targetToRes.clear();
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}