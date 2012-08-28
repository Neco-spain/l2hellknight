package scripts.skills.skillhandlers;

import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.util.Rnd;
import scripts.skills.ISkillHandler;

public class Pdam
  implements ISkillHandler
{
  private static final Logger _log = Logger.getLogger(Pdam.class.getName());

  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.PDAM };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if (activeChar.isAlikeDead()) {
      return;
    }

    int damage = 0;

    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; ) {
      L2Character target = (L2Character)n.getValue();

      if ((target == null) || (target.isDead()) || 
        (target == activeChar))
      {
        continue;
      }
      L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
      if ((activeChar.isPlayer()) && (target.isPlayer()) && (target.isAlikeDead()) && (target.isFakeDeath()))
        target.stopFakeDeath(null);
      else if (target.isAlikeDead())
        {
          continue;
        }
      boolean dual = activeChar.isUsingDualWeapon();
      boolean shld = Formulas.calcShldUse(activeChar, target);

      boolean crit = false;

      if (skill.getBaseCritRate() > 0) {
        crit = Formulas.calcCrit(skill.getBaseCritRate() * 10 * Formulas.getSTRBonus(activeChar));
      }

      boolean soul = (weapon != null) && (weapon.getChargedSoulshot() == 1) && (weapon.getItemType() != L2WeaponType.DAGGER);

      if ((!crit) && ((skill.getCondition() & 0x10) != 0))
        damage = 0;
      else {
        damage = (int)Formulas.calcPhysDam(activeChar, target, skill, shld, false, dual, soul);
      }

      if (crit) {
        damage = (int)Formulas.calcViciousDam(activeChar, damage, true);
      }

      if (skill.getId() == 314) {
        double hp = activeChar.getMaxHp();
        double curhp = activeChar.getCurrentHp();
        double xz = curhp / hp * 100.0D;
        long rounded = Math.round(xz);
        int intper = (int)rounded;
        int prepower = (int)skill.getPower(activeChar);
        int lol = 100 - intper;
        int powper = prepower / 240 * lol;
        damage += powper;
      }

      if ((soul) && (weapon != null)) {
        weapon.setChargedSoulshot(0);
      }

      if (damage > 0) {
        activeChar.sendDamageMessage(target, damage, false, crit, false);

        if (skill.hasEffects()) {
          if (target.reflectSkill(skill)) {
            activeChar.stopSkillEffects(skill.getId());
            skill.getEffects(null, activeChar);
            activeChar.sendPacket(SystemMessage.id(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill.getId()));
          }
          else if (Formulas.calcSkillSuccess(activeChar, target, skill, false, false, false)) {
            skill.getEffects(activeChar, target);
            target.sendPacket(SystemMessage.id(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill.getId()));
          } else {
            activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2).addString(target.getName()).addSkillName(skill.getDisplayId()));
          }

        }

        int chance = Rnd.get(100);
        if ((!target.isRaid()) && (chance < skill.getLethalChance1()) && (!target.isL2Door()) && ((!target.isL2Npc()) || (((L2NpcInstance)target).getNpcId() != 35062)))
        {
          if ((skill.getLethalChance2() > 0) && (chance >= skill.getLethalChance2())) {
            if (target.isPlayer()) {
              L2PcInstance player = (L2PcInstance)target;
              if (!player.isInvul()) {
                player.setCurrentCp(1.0D);
                player.reduceCurrentHp(damage, activeChar);
              }
            } else if (target.isL2Monster())
            {
              target.reduceCurrentHp(damage, activeChar);
              target.reduceCurrentHp(target.getCurrentHp() / 2.0D, activeChar);
            }

          }
          else if (target.isL2Npc()) {
            target.reduceCurrentHp(target.getCurrentHp() - 1.0D, activeChar);
          } else if (target.isPlayer())
          {
            L2PcInstance player = (L2PcInstance)target;
            if (!player.isInvul()) {
              player.setCurrentHp(1.0D);
              player.setCurrentCp(1.0D);
            }

          }

          activeChar.sendPacket(Static.LETHAL_STRIKE_SUCCESSFUL);
        }
        else if (skill.getDmgDirectlyToHP()) {
          if (target.isPlayer()) {
            L2PcInstance player = (L2PcInstance)target;
            if (!player.isInvul())
            {
              player.reduceCurrentHp(damage, activeChar, true, true);
            }

            player.sendPacket(SystemMessage.id(SystemMessageId.S1_GAVE_YOU_S2_DMG).addString(activeChar.getName()).addNumber(damage));
          }
          else {
            target.reduceCurrentHp(damage, activeChar);
          }
        } else {
          target.reduceCurrentHp(damage, activeChar);
        }

        if (target.getFirstEffect(447) != null)
          activeChar.reduceCurrentHp(damage / 2.7D, target);
      }
      else
      {
        activeChar.sendPacket(Static.ATTACK_FAILED);
      }

      if ((skill.getId() == 345) || (skill.getId() == 346))
      {
        if (activeChar.getCharges() < 7) {
          activeChar.increaseCharges();
        }

      }

      L2Effect effect = activeChar.getFirstEffect(skill.getId());
      if ((effect != null) && (effect.isSelfEffect()))
      {
        effect.exit();
      }
      skill.getEffectsSelf(activeChar);
    }

    if (activeChar.isPlayer()) {
      activeChar.rechargeAutoSoulShot(true, false, false);
    }

    if (skill.isSuicideAttack()) {
      activeChar.doDie(null);
      activeChar.setCurrentHp(0.0D);
    }
  }

  public L2Skill.SkillType[] getSkillIds() {
    return SKILL_IDS;
  }
}