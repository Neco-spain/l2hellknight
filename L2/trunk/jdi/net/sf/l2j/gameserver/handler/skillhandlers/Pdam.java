package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.lib.Log;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.model.actor.status.PcStatus;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.effects.EffectCharge;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.util.Rnd;

public class Pdam
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.PDAM, L2Skill.SkillType.FATALCOUNTER };

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    if (activeChar.isAlikeDead()) return;

    int damage = 0;

    for (int index = 0; index < targets.length; index++)
    {
      L2Character target = (L2Character)targets[index];
      Formulas f = Formulas.getInstance();
      L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
      if (((activeChar instanceof L2PcInstance)) && ((target instanceof L2PcInstance)) && (target.isAlikeDead()) && (target.isFakeDeath()))
      {
        target.stopFakeDeath(null);
      }
      else if (target.isAlikeDead())
          continue;
      boolean dual = activeChar.isUsingDualWeapon();
      boolean shld = f.calcShldUse(activeChar, target);

      boolean crit = false;
      if (skill.getBaseCritRate() > 0) {
        crit = f.calcCrit(skill.getBaseCritRate() * 10 * f.getSTRBonus(activeChar));
      }
      boolean soul = (weapon != null) && (weapon.getChargedSoulshot() == 1) && (weapon.getItemType() != L2WeaponType.DAGGER);

      if ((!crit) && ((skill.getCondition() & 0x10) != 0)) damage = 0; else
        damage = (int)f.calcPhysDam(activeChar, target, skill, shld, false, dual, soul);
      if (crit) damage *= 2;

      if ((damage > 5000) && ((activeChar instanceof L2PcInstance)))
      {
        String name = "";
        if ((target instanceof L2RaidBossInstance)) name = "RaidBoss ";
        if ((target instanceof L2NpcInstance)) {
          name = name + target.getName() + "(" + ((L2NpcInstance)target).getTemplate().npcId + ")";
        }
        if ((target instanceof L2PcInstance))
          name = target.getName() + "(" + target.getObjectId() + ") ";
        name = name + target.getLevel() + " lvl";
        Log.add(activeChar.getName() + "(" + activeChar.getObjectId() + ") " + activeChar.getLevel() + " lvl did damage " + damage + " with skill " + skill.getName() + "(" + skill.getId() + ") to " + name, "damage_pdam");
      }

      if ((soul) && (weapon != null)) weapon.setChargedSoulshot(0);

      boolean skillIsEvaded = f.calcPhysicalSkillEvasion(target, skill);

      if (!skillIsEvaded)
      {
        if (damage > 0)
        {
          activeChar.sendDamageMessage(target, damage, false, crit, false);

          if (skill.hasEffects())
          {
            if (target.reflectSkill(skill))
            {
              activeChar.stopSkillEffects(skill.getId());
              skill.getEffects(target, activeChar);
              SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
              sm.addSkillName(skill.getId());
              activeChar.sendPacket(sm);
            }
            else
            {
              target.stopSkillEffects(skill.getId());
              if (f.calcSkillSuccess(activeChar, target, skill, false, false, false))
              {
                skill.getEffects(activeChar, target);

                SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
                sm.addSkillName(skill.getId());
                target.sendPacket(sm);
              }
              else
              {
                SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
                sm.addString(target.getName());
                sm.addSkillName(skill.getDisplayId());
                activeChar.sendPacket(sm);
              }
            }

          }

          boolean lethal = Formulas.getInstance().calcLethalHit(activeChar, target, skill);

          int chance = Rnd.get(100);
          if ((!target.isRaid()) && (chance < skill.getLethalChance1()) && (!(target instanceof L2DoorInstance)) && ((!(target instanceof L2NpcInstance)) || (((L2NpcInstance)target).getNpcId() != 35062)))
          {
            if ((skill.getLethalChance2() > 0) && (chance >= skill.getLethalChance2()))
            {
              if ((target instanceof L2PcInstance))
              {
                L2PcInstance player = (L2PcInstance)target;
                if (!player.isInvul())
                {
                  player.setCurrentCp(1.0D);
                  player.reduceCurrentHp(damage, activeChar);
                }
              }
              else if ((target instanceof L2MonsterInstance))
              {
                target.reduceCurrentHp(damage, activeChar);
                target.reduceCurrentHp(target.getCurrentHp() / 2.0D, activeChar);
              }

            }
            else if ((target instanceof L2NpcInstance)) {
              target.reduceCurrentHp(target.getCurrentHp() - 1.0D, activeChar);
            } else if ((target instanceof L2PcInstance))
            {
              L2PcInstance player = (L2PcInstance)target;
              if (!player.isInvul())
              {
                player.setCurrentHp(1.0D);
                player.setCurrentCp(1.0D);
              }

            }

            activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE_SUCCESSFUL));
          }
          else
          {
            int reflectDmg = 0;
            int RevengeDmg = 0;
            int twodamage = Rnd.get(100);
            int randomiz = Rnd.get(100);
            if (target.reflectDamageSkill(skill))
            {
              reflectDmg = 1189 * target.getPAtk(null) / activeChar.getPDef(null);
            }

            if (target.reflectRevengeSkill(skill))
            {
              RevengeDmg += damage;
              RevengeDmg = (int)(RevengeDmg * 0.4D);
              damage -= RevengeDmg;
            }
            if ((!lethal) && (skill.getDmgDirectlyToHP()))
            {
              if ((target instanceof L2PcInstance))
              {
                L2PcInstance player = (L2PcInstance)target;
                if (!player.isInvul())
                {
                  if (damage >= player.getCurrentHp())
                  {
                    if (player.isInDuel()) {
                      player.setCurrentHp(1.0D);
                    }
                    else {
                      player.setCurrentHp(0.0D);
                      if (player.isInOlympiadMode())
                      {
                        player.abortAttack();
                        player.abortCast();
                        player.getStatus().stopHpMpRegeneration();
                        player.setIsPendingRevive(true);
                      }
                      else {
                        player.doDie(activeChar);
                      }
                    }
                  }
                  else {
                    if (skill.getCastRange() < 100)
                    {
                      activeChar.reduceCurrentHp(RevengeDmg, target);
                      if (reflectDmg > 0)
                      {
                        if ((target instanceof L2PcInstance))
                        {
                          target.sendPacket(new SystemMessage(SystemMessageId.COUNTERED_S1_ATTACK).addString(activeChar.getName()));
                        }
                        if ((activeChar instanceof L2PcInstance))
                        {
                          activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_IS_PERFORMING_A_COUNTER_ATTACK).addString(target.getName()));
                        }
                        activeChar.reduceCurrentHp(reflectDmg, target);
                        if (twodamage < 80) activeChar.reduceCurrentHp(reflectDmg, target);
                      }
                    }
                    player.setCurrentHp(player.getCurrentHp() - damage);
                  }
                }

                SystemMessage smsg = new SystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG);
                smsg.addString(activeChar.getName());
                smsg.addNumber(damage);
                player.sendPacket(smsg);
              }
              else if (skill.getCastRange() < 100)
              {
                activeChar.reduceCurrentHp(RevengeDmg, target);
                activeChar.reduceCurrentHp(reflectDmg, target);
                if (twodamage < 80) activeChar.reduceCurrentHp(reflectDmg, target);
              }
              target.reduceCurrentHp(damage, activeChar);
            }
            else
            {
              if (skill.getCastRange() < 100)
              {
                activeChar.reduceCurrentHp(RevengeDmg, target);
                activeChar.reduceCurrentHp(reflectDmg, target);
                if (twodamage < 80) activeChar.reduceCurrentHp(reflectDmg, target);
              }
              target.reduceCurrentHp(damage, activeChar);
            }
          }
        }
        else
        {
          activeChar.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
        }
      }
      else
      {
        if ((activeChar instanceof L2PcInstance))
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_DODGES_ATTACK);
          sm.addString(target.getName());
          ((L2PcInstance)activeChar).sendPacket(sm);
        }
        if ((target instanceof L2PcInstance))
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.AVOIDED_S1_ATTACK);
          sm.addString(activeChar.getName());
          ((L2PcInstance)target).sendPacket(sm);
        }

        Formulas.getInstance().calcLethalHit(activeChar, target, skill);
      }

      if ((skill.getId() == 345) || (skill.getId() == 346))
      {
        EffectCharge effect = (EffectCharge)activeChar.getFirstEffect(L2Effect.EffectType.CHARGE);
        if (effect != null)
        {
          int effectcharge = effect.getLevel();
          if (effectcharge < 7)
          {
            effectcharge++;
            effect.addNumCharges(1);
            if ((activeChar instanceof L2PcInstance))
            {
              activeChar.sendPacket(new EtcStatusUpdate((L2PcInstance)activeChar));
              SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
              sm.addNumber(effectcharge);
              activeChar.sendPacket(sm);
            }
          }
          else
          {
            SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_MAXLEVEL_REACHED);
            activeChar.sendPacket(sm);
          }

        }
        else if (skill.getId() == 345)
        {
          L2Skill dummy = SkillTable.getInstance().getInfo(8, 7);
          dummy.getEffects(activeChar, activeChar);
        }
        else if (skill.getId() == 346)
        {
          L2Skill dummy = SkillTable.getInstance().getInfo(50, 7);
          dummy.getEffects(activeChar, activeChar);
        }

      }

      L2Effect effect = activeChar.getFirstEffect(skill.getId());
      if ((effect != null) && (effect.isSelfEffect()))
      {
        effect.exit();
      }
      skill.getEffectsSelf(activeChar);
    }

    if (skill.isSuicideAttack())
    {
      activeChar.doDie(null);
      activeChar.setCurrentHp(0.0D);
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}