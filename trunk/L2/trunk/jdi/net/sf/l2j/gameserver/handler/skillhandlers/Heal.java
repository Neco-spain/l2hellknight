package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;

public class Heal
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.HEAL, L2Skill.SkillType.HEAL_PERCENT, L2Skill.SkillType.HEAL_STATIC };

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    try
    {
      ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(L2Skill.SkillType.BUFF);

      if (handler != null)
        handler.useSkill(activeChar, skill, targets);
    }
    catch (Exception e) {
    }
    L2Character target = null;
    L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

    L2PcInstance player = null;
    if ((activeChar instanceof L2PcInstance)) {
      player = (L2PcInstance)activeChar;
    }
    for (int index = 0; index < targets.length; index++)
    {
      target = (L2Character)targets[index];

      if ((target == null) || (target.isDead()))
      {
        continue;
      }
      if ((target instanceof L2DoorInstance))
      {
        continue;
      }

      if (!Config.RB_HEAL)
      {
        if ((target instanceof L2RaidBossInstance))
        {
          player.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u0430\u0432\u043B\u0438\u0432\u0430\u0442\u044C \u0445\u043F \u0440\u0435\u0439\u0434 \u0431\u043E\u0441\u0441\u0430\u043C!");
          continue;
        }
        if (((target instanceof L2GrandBossInstance)) && ((player instanceof L2PcInstance)))
        {
          player.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u0430\u0432\u043B\u0438\u0432\u0430\u0442\u044C \u0445\u043F \u0433\u0440\u0430\u043D\u0434 \u0431\u043E\u0441\u0441\u043E\u0432!");
          continue;
        }
      }

      if (target != activeChar)
      {
        if (((target instanceof L2PcInstance)) && (((L2PcInstance)target).isCursedWeaponEquiped()))
          continue;
        if ((player != null) && (player.isCursedWeaponEquiped())) {
          continue;
        }
      }
      double hp = skill.getPower() + 0.1D * skill.getPower() * Math.sqrt(activeChar.getMAtk(target, skill) / 333);

      if (skill.getSkillType() == L2Skill.SkillType.HEAL_PERCENT)
      {
        hp = target.getMaxHp() * hp / 100.0D;
      }
      else if (weaponInst != null)
      {
        if (weaponInst.getChargedSpiritshot() == 2)
        {
          hp *= 1.5D;
          weaponInst.setChargedSpiritshot(0);
        }
        else if (weaponInst.getChargedSpiritshot() == 1)
        {
          hp *= 1.3D;
          weaponInst.setChargedSpiritshot(0);
        }

      }
      else if ((activeChar instanceof L2Summon))
      {
        L2Summon activeSummon = (L2Summon)activeChar;

        if (activeSummon.getChargedSpiritShot() == 2)
        {
          hp *= 1.5D;
          activeSummon.setChargedSpiritShot(0);
        }
        else if (activeSummon.getChargedSpiritShot() == 1)
        {
          hp *= 1.3D;
          activeSummon.setChargedSpiritShot(0);
        }

      }

      if (skill.getSkillType() == L2Skill.SkillType.HEAL_STATIC)
        hp = skill.getPower();
      else if (skill.getSkillType() != L2Skill.SkillType.HEAL_PERCENT) {
        hp *= target.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0D, null, null) / 100.0D;
      }
      if (hp + target.getCurrentHp() >= target.getMaxHp())
      {
        hp = target.getMaxHp() - target.getCurrentHp();
      }

      target.setCurrentHp(hp + target.getCurrentHp());
      target.setLastHealAmount((int)hp);
      StatusUpdate su = new StatusUpdate(target.getObjectId());
      su.addAttribute(9, (int)target.getCurrentHp());
      target.sendPacket(su);

      if (!(target instanceof L2PcInstance))
        continue;
      if (skill.getId() == 4051)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.REJUVENATING_HP);
        target.sendPacket(sm);
      }
      else if (((activeChar instanceof L2PcInstance)) && (activeChar != target))
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S2_HP_RESTORED_BY_S1);
        sm.addString(activeChar.getName());
        sm.addNumber((int)hp);
        target.sendPacket(sm);
      }
      else
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_HP_RESTORED);
        sm.addNumber((int)hp);
        target.sendPacket(sm);
      }
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}