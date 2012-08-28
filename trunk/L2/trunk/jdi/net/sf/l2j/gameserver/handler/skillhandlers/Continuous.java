package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.instancemanager.DuelManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2ClanHallManagerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2CubicInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;

public class Continuous
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.BUFF, L2Skill.SkillType.DEBUFF, L2Skill.SkillType.DOT, L2Skill.SkillType.MDOT, L2Skill.SkillType.POISON, L2Skill.SkillType.BLEED, L2Skill.SkillType.HOT, L2Skill.SkillType.CPHOT, L2Skill.SkillType.MPHOT, L2Skill.SkillType.FEAR, L2Skill.SkillType.CONT, L2Skill.SkillType.WEAKNESS, L2Skill.SkillType.REFLECT, L2Skill.SkillType.UNDEAD_DEFENSE, L2Skill.SkillType.AGGDEBUFF, L2Skill.SkillType.FORCE_BUFF };
  private L2Skill _skill;

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    L2Character target = null;
    boolean acted = true;

    L2PcInstance player = null;
    if ((activeChar instanceof L2PcInstance))
      player = (L2PcInstance)activeChar;
    if (skill.getEffectId() != 0)
    {
      int skillLevel = skill.getEffectLvl();
      int skillEffectId = skill.getEffectId();

      if (skillLevel == 0)
      {
        _skill = SkillTable.getInstance().getInfo(skillEffectId, 1);
      }
      else
      {
        _skill = SkillTable.getInstance().getInfo(skillEffectId, skillLevel);
      }

      if (_skill != null)
        skill = _skill;
    }
    for (int index = 0; index < targets.length; index++)
    {
      target = (L2Character)targets[index];

      switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[skill.getSkillType().ordinal()])
      {
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        break;
      default:
        if (!target.reflectSkill(skill)) break;
        target = activeChar;
      }

      if (((target instanceof L2DoorInstance)) && ((skill.getSkillType() == L2Skill.SkillType.BUFF) || (skill.getSkillType() == L2Skill.SkillType.HOT)))
      {
        continue;
      }
      if (((activeChar instanceof L2PlayableInstance)) && (target != activeChar) && (target.isBuffBlocked()) && (!skill.isHeroSkill()) && ((skill.getSkillType() == L2Skill.SkillType.BUFF) || (skill.getSkillType() == L2Skill.SkillType.HEAL_PERCENT) || (skill.getSkillType() == L2Skill.SkillType.FORCE_BUFF) || (skill.getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT) || (skill.getSkillType() == L2Skill.SkillType.COMBATPOINTHEAL) || (skill.getSkillType() == L2Skill.SkillType.REFLECT)))
      {
        continue;
      }

      if ((skill.getSkillType() == L2Skill.SkillType.BUFF) && (!(activeChar instanceof L2ClanHallManagerInstance)))
      {
        if (target != activeChar)
        {
          if (((target instanceof L2PcInstance)) && (((L2PcInstance)target).isCursedWeaponEquiped()))
            continue;
          if ((player != null) && (player.isCursedWeaponEquiped())) {
            continue;
          }
        }
      }
      if (skill.isOffensive())
      {
        boolean ss = false;
        boolean sps = false;
        boolean bss = false;
        if (player != null)
        {
          L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
          if (weaponInst != null)
          {
            if (skill.isMagic())
            {
              if (weaponInst.getChargedSpiritshot() == 2)
              {
                bss = true;
                if (skill.getId() != 1020)
                  weaponInst.setChargedSpiritshot(0);
              }
              else if (weaponInst.getChargedSpiritshot() == 1)
              {
                sps = true;
                if (skill.getId() != 1020) {
                  weaponInst.setChargedSpiritshot(0);
                }
              }
            }
            else if (weaponInst.getChargedSoulshot() == 1)
            {
              ss = true;
              if (skill.getId() != 1020)
                weaponInst.setChargedSoulshot(0);
            }
          }
        }
        else if ((activeChar instanceof L2Summon))
        {
          L2Summon activeSummon = (L2Summon)activeChar;
          if (skill.isMagic())
          {
            if (activeSummon.getChargedSpiritShot() == 2)
            {
              bss = true;
              activeSummon.setChargedSpiritShot(0);
            }
            else if (activeSummon.getChargedSpiritShot() == 1)
            {
              sps = true;
              activeSummon.setChargedSpiritShot(0);
            }

          }
          else if (activeSummon.getChargedSoulShot() == 1)
          {
            ss = true;
            activeSummon.setChargedSoulShot(0);
          }
        }

        acted = Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss);
      }

      if (acted)
      {
        if (skill.isToggle())
        {
          L2Effect[] effects = target.getAllEffects();
          if (effects != null)
          {
            for (L2Effect e : effects)
            {
              if ((e == null) || (skill == null))
                continue;
              if (e.getSkill().getId() != skill.getId())
                continue;
              e.exit();
              return;
            }

          }

        }

        if (((target instanceof L2PcInstance)) && (((L2PcInstance)target).isInDuel()) && ((skill.getSkillType() == L2Skill.SkillType.DEBUFF) || (skill.getSkillType() == L2Skill.SkillType.BUFF)) && (player != null) && (player.getDuelId() == ((L2PcInstance)target).getDuelId()))
        {
          DuelManager dm = DuelManager.getInstance();
          for (L2Effect buff : skill.getEffects(activeChar, target)) {
            if (buff == null) continue; dm.onBuff((L2PcInstance)target, buff);
          }
        } else {
          skill.getEffects(activeChar, target);
        }
        if (skill.getSkillType() == L2Skill.SkillType.AGGDEBUFF)
        {
          if ((target instanceof L2Attackable))
            target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, Integer.valueOf((int)skill.getPower()));
          else if ((target instanceof L2PlayableInstance))
          {
            if (target.getTarget() == activeChar)
              target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
            else
              target.setTarget(activeChar);
          }
        }
      }
      else
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
      }
      Formulas.getInstance().calcLethalHit(activeChar, target, skill);
    }

    L2Effect effect = activeChar.getFirstEffect(skill.getId());
    if ((effect != null) && (effect.isSelfEffect()))
    {
      effect.exit();
    }
    skill.getEffectsSelf(activeChar);
  }

  public void useCubicSkill(L2CubicInstance activeCubic, L2Skill skill, L2Object[] targets)
  {
    L2Character target = null;

    for (int index = 0; index < targets.length; index++)
    {
      target = (L2Character)targets[index];

      if (skill.isOffensive())
      {
        boolean acted = Formulas.getInstance().calcCubicSkillSuccess(activeCubic, target, skill);
        if (!acted) {
          activeCubic.getOwner().sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
          continue;
        }

      }

      if (((target instanceof L2PcInstance)) && (((L2PcInstance)target).isInDuel()) && (skill.getSkillType() == L2Skill.SkillType.DEBUFF) && (activeCubic.getOwner().getDuelId() == ((L2PcInstance)target).getDuelId()))
      {
        DuelManager dm = DuelManager.getInstance();
        for (L2Effect debuff : skill.getEffects(activeCubic.getOwner(), target)) {
          if (debuff == null) continue; dm.onBuff((L2PcInstance)target, debuff);
        }
      } else {
        skill.getEffects(activeCubic, target);
      }
    }
  }

  public L2Skill.SkillType[] getSkillIds() {
    return SKILL_IDS;
  }
}