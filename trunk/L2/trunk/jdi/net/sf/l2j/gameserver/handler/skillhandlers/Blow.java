package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.status.PcStatus;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.util.Rnd;

public class Blow
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.BLOW };
  private int _successChance;
  public static final int FRONT = Config.FRONT_CHANCE;
  public static final int SIDE = Config.SIDE_CHANCE;
  public static final int BEHIND = Config.BEHIND_CHANCE;

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets) {
    if (activeChar.isAlikeDead()) {
      return;
    }
    for (int index = 0; index < targets.length; index++)
    {
      L2Character target = (L2Character)targets[index];
      boolean skillIsEvaded = Formulas.getInstance().calcPhysicalSkillEvasion(target, skill);
      if (target.isAlikeDead())
      {
        continue;
      }

      if (activeChar.isBehindTarget())
        _successChance = BEHIND;
      else if (activeChar.isFrontTarget())
        _successChance = FRONT;
      else
        _successChance = SIDE;
      if (((!skillIsEvaded) && ((skill.getCondition() & 0x8) != 0) && (_successChance == BEHIND)) || (((skill.getCondition() & 0x10) != 0) && (Formulas.getInstance().calcBlow(activeChar, target, _successChance))))
      {
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
            skill.getEffects(activeChar, target);
          }
        }
        L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
        boolean soul = (weapon != null) && (weapon.getChargedSoulshot() == 1) && (weapon.getItemType() == L2WeaponType.DAGGER);
        boolean shld = Formulas.getInstance().calcShldUse(activeChar, target);

        boolean crit = false;
        if (Formulas.getInstance().calcCrit(skill.getBaseCritRate() * 10 * Formulas.getInstance().getSTRBonus(activeChar)))
          crit = true;
        double damage = (int)Formulas.getInstance().calcBlowDamage(activeChar, target, skill, shld, soul);
        if (crit)
        {
          damage *= 2.0D;
        }

        if ((soul) && (weapon != null)) {
          weapon.setChargedSoulshot(0);
        }
        int RevengeDmg = 0;
        int reflectDmg = 0;
        int twodamage = Rnd.get(100);
        int randomiz = Rnd.get(100);

        if (target.reflectDamageSkill(skill))
        {
          reflectDmg = 1189 * target.getPAtk(null) / activeChar.getPDef(null);
        }

        if (target.reflectRevengeSkill(skill))
        {
          RevengeDmg = (int)(RevengeDmg + damage);
        }
        if ((skill.getDmgDirectlyToHP()) && ((target instanceof L2PcInstance)))
        {
          L2PcInstance player = (L2PcInstance)target;
          if (!player.isInvul())
          {
            if (damage >= player.getCurrentHp())
            {
              if (player.isInDuel()) { player.setCurrentHp(1.0D);
              } else
              {
                player.setCurrentHp(0.0D);
                if (player.isInOlympiadMode())
                {
                  player.abortAttack();
                  player.abortCast();
                  player.getStatus().stopHpMpRegeneration();
                }
                else {
                  player.doDie(activeChar);
                }
              }
            }
            else {
              activeChar.reduceCurrentHp(RevengeDmg, activeChar);
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
              player.setCurrentHp(player.getCurrentHp() - damage);
            }
          }
          SystemMessage smsg = new SystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG);
          smsg.addString(activeChar.getName());
          smsg.addNumber((int)damage);
          player.sendPacket(smsg);
        }
        else
        {
          activeChar.reduceCurrentHp(RevengeDmg, target);
          activeChar.reduceCurrentHp(reflectDmg, target);
          if (twodamage < 80) activeChar.reduceCurrentHp(reflectDmg, target);
          target.reduceCurrentHp(damage, activeChar);
        }
        if ((activeChar instanceof L2PcInstance))
          activeChar.sendPacket(new SystemMessage(SystemMessageId.CRITICAL_HIT));
        SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DID_S1_DMG);
        sm.addNumber((int)damage);
        activeChar.sendPacket(sm);
      }
      if (skillIsEvaded)
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

      }

      if ((!target.isRaid()) && (!(target instanceof L2DoorInstance)) && ((!(target instanceof L2NpcInstance)) || (((L2NpcInstance)target).getNpcId() != 35062)))
      {
        int chance = Rnd.get(100);

        if ((skill.getLethalChance2() > 0) && (chance < Formulas.getInstance().calcLethal(activeChar, target, skill.getLethalChance2())))
        {
          if ((target instanceof L2NpcInstance)) {
            target.reduceCurrentHp(target.getCurrentHp() - 1.0D, activeChar);
          } else if ((target instanceof L2PcInstance))
          {
            L2PcInstance player = (L2PcInstance)target;
            if (!player.isInvul()) {
              player.setCurrentHp(1.0D);
              player.setCurrentCp(1.0D);
            }
          }
          activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
        }
        else if ((skill.getLethalChance1() > 0) && (chance < Formulas.getInstance().calcLethal(activeChar, target, skill.getLethalChance1()))) {
          if ((target instanceof L2PcInstance))
          {
            L2PcInstance player = (L2PcInstance)target;
            if (!player.isInvul())
              player.setCurrentCp(1.0D);
          }
          else if ((target instanceof L2NpcInstance)) {
            target.reduceCurrentHp(target.getCurrentHp() / 2.0D, activeChar);
          }activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
        }
      }
      L2Effect effect = activeChar.getFirstEffect(skill.getId());

      if ((effect != null) && (effect.isSelfEffect()))
        effect.exit();
      skill.getEffectsSelf(activeChar);
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}