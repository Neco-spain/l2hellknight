package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class BalanceLife
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.BALANCE_LIFE };

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    try
    {
      ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(L2Skill.SkillType.BUFF);

      if (handler != null)
        handler.useSkill(activeChar, skill, targets);
    }
    catch (Exception e)
    {
    }
    L2Character target = null;

    L2PcInstance player = null;
    if ((activeChar instanceof L2PcInstance)) {
      player = (L2PcInstance)activeChar;
    }
    double fullHP = 0.0D;
    double currentHPs = 0.0D;

    for (int index = 0; index < targets.length; index++)
    {
      target = (L2Character)targets[index];

      if ((target == null) || (target.isDead()) || (target.getCurrentHp() == 0.0D))
      {
        continue;
      }
      if (target != activeChar)
      {
        if (((target instanceof L2PcInstance)) && (((L2PcInstance)target).isCursedWeaponEquiped())) {
          continue;
        }
        if ((player != null) && (player.isCursedWeaponEquiped())) {
          continue;
        }
      }
      fullHP += target.getMaxHp();
      currentHPs += target.getCurrentHp();
    }

    double percentHP = currentHPs / fullHP;

    for (int index = 0; index < targets.length; index++)
    {
      target = (L2Character)targets[index];

      double newHP = target.getMaxHp() * percentHP;
      double totalHeal = newHP - target.getCurrentHp();

      target.setCurrentHp(newHP);

      if (totalHeal > 0.0D) {
        target.setLastHealAmount((int)totalHeal);
      }
      StatusUpdate su = new StatusUpdate(target.getObjectId());
      su.addAttribute(9, (int)target.getCurrentHp());
      target.sendPacket(su);

      SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
      sm.addString("HP of the party has been balanced.");
      target.sendPacket(sm);
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}