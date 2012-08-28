package net.sf.l2j.gameserver.handler.skillhandlers;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.handler.ISkillHandler;
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

public class Resurrect
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.RESURRECT };

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    L2PcInstance player = null;
    if ((activeChar instanceof L2PcInstance)) player = (L2PcInstance)activeChar;

    L2Character target = null;

    List targetToRes = new FastList();

    for (int index = 0; index < targets.length; index++)
    {
      target = (L2Character)targets[index];

      if ((target instanceof L2PcInstance))
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
      activeChar.sendPacket(SystemMessage.sendString("No valid target to resurrect"));
    }

    for (L2Character cha : targetToRes)
      if ((activeChar instanceof L2PcInstance))
      {
        if ((cha instanceof L2PcInstance))
          ((L2PcInstance)cha).reviveRequest((L2PcInstance)activeChar, skill, false);
        else if ((cha instanceof L2PetInstance))
        {
          if (((L2PetInstance)cha).getOwner() == activeChar)
            cha.doRevive(Formulas.getInstance().calculateSkillResurrectRestorePercent(skill.getPower(), activeChar.getWIT()));
          else
            ((L2PetInstance)cha).getOwner().reviveRequest((L2PcInstance)activeChar, skill, true);
        }
        else cha.doRevive(Formulas.getInstance().calculateSkillResurrectRestorePercent(skill.getPower(), activeChar.getWIT()));
      }
      else
      {
        DecayTaskManager.getInstance().cancelDecayTask(cha);
        cha.doRevive(Formulas.getInstance().calculateSkillResurrectRestorePercent(skill.getPower(), activeChar.getWIT()));
      }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}