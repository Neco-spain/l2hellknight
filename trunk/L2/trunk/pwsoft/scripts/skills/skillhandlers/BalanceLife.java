package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.skills.ISkillHandler;
import scripts.skills.SkillHandler;

public class BalanceLife
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.BALANCE_LIFE };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    try
    {
      ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(L2Skill.SkillType.BUFF);

      if (handler != null) {
        handler.useSkill(activeChar, skill, targets);
      }
    }
    catch (Exception e)
    {
    }
    L2Character target = null;

    L2PcInstance player = null;
    if (activeChar.isPlayer()) {
      player = (L2PcInstance)activeChar;
    }
    double fullHP = 0.0D;
    double currentHPs = 0.0D;

    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
    {
      target = (L2Character)n.getValue();

      if ((target == null) || (target.isDead()) || (target.isAlikeDead()) || (target.getCurrentHp() <= 0.0D) || (
        (target != activeChar) && (
        ((target.isPlayer()) && (((L2PcInstance)target).isCursedWeaponEquiped())) || (
        (player != null) && (player.isCursedWeaponEquiped())))))
      {
        continue;
      }
      fullHP += target.getMaxHp();
      currentHPs += target.getCurrentHp();
    }

    double percentHP = currentHPs / fullHP;

    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
    {
      target = (L2Character)n.getValue();

      if ((target == null) || (target.isDead()) || (target.isAlikeDead()) || (target.getCurrentHp() <= 0.0D)) {
        continue;
      }
      double newHP = target.getMaxHp() * percentHP;
      double totalHeal = newHP - target.getCurrentHp();

      target.setCurrentHp(newHP);

      if (totalHeal > 0.0D) {
        target.setLastHealAmount((int)totalHeal);
      }

      if (target.isPlayer())
        ((L2PcInstance)target).sendMessage("HP of the party has been balanced.");
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}