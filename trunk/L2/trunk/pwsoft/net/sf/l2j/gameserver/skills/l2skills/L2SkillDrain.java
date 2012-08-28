package net.sf.l2j.gameserver.skills.l2skills;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillDrain extends L2Skill
{
  private float _absorbPart;
  private int _absorbAbs;

  public L2SkillDrain(StatsSet set)
  {
    super(set);

    _absorbPart = set.getFloat("absorbPart", 0.0F);
    _absorbAbs = set.getInteger("absorbAbs", 0);
  }

  public void useSkill(L2Character activeChar, FastList<L2Object> targets)
  {
    if (activeChar.isAlikeDead()) {
      return;
    }
    boolean ss = false;
    boolean bss = false;

    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
    {
      L2Character target = (L2Character)n.getValue();

      if (((target.isAlikeDead()) || ((target.isL2Npc()) && (!target.isMonster()))) && ((getTargetType() != L2Skill.SkillTargetType.TARGET_CORPSE_MOB) || (
        (activeChar != target) && (target.isInvul())))) {
        continue;
      }
      L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

      if (weaponInst != null)
      {
        if (weaponInst.getChargedSpiritshot() == 2)
        {
          bss = true;
          weaponInst.setChargedSpiritshot(0);
        }
        else if (weaponInst.getChargedSpiritshot() == 1)
        {
          ss = true;
          weaponInst.setChargedSpiritshot(0);
        }

      }
      else if (activeChar.isL2Summon())
      {
        L2Summon activeSummon = (L2Summon)activeChar;

        if (activeSummon.getChargedSpiritShot() == 2)
        {
          bss = true;
          activeSummon.setChargedSpiritShot(0);
        }
        else if (activeSummon.getChargedSpiritShot() == 1)
        {
          ss = true;
          activeSummon.setChargedSpiritShot(0);
        }
      }

      double trgCP = target.getCurrentCp();
      boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
      int damage = (int)Formulas.calcMagicDam(activeChar, target, this, ss, bss, mcrit);
      double hpAdd = _absorbAbs + _absorbPart * damage;
      if ((getId() == 1245) && (target.isPlayer()))
      {
        if (damage - trgCP > 0.0D)
          hpAdd = _absorbAbs + _absorbPart * (damage - trgCP);
        else
          hpAdd = 1.0D;
      }
      double hp = activeChar.getCurrentHp() + hpAdd > activeChar.getMaxHp() ? activeChar.getMaxHp() : activeChar.getCurrentHp() + hpAdd;
      activeChar.setCurrentHp(hp);

      if ((damage > 0) && ((!target.isDead()) || (getTargetType() != L2Skill.SkillTargetType.TARGET_CORPSE_MOB)))
      {
        if ((!target.isRaid()) && (Formulas.calcAtkBreak(target, damage)))
        {
          target.breakAttack();
          target.breakCast();
        }

        activeChar.sendDamageMessage(target, damage, mcrit, false, false);

        if ((hasEffects()) && (getTargetType() != L2Skill.SkillTargetType.TARGET_CORPSE_MOB))
        {
          if (target.reflectSkill(this))
          {
            activeChar.stopSkillEffects(getId());
            getEffects(null, activeChar);
            activeChar.sendPacket(SystemMessage.id(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(getId()));
          }
          else
          {
            target.stopSkillEffects(getId());
            if (Formulas.calcSkillSuccess(activeChar, target, this, false, ss, bss))
              getEffects(activeChar, target);
            else {
              activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2).addString(target.getName()).addSkillName(getDisplayId()));
            }
          }
        }
        target.reduceCurrentHp(damage, activeChar);
      }

      if ((target.isDead()) && (getTargetType() == L2Skill.SkillTargetType.TARGET_CORPSE_MOB) && (target.isL2Npc())) {
        ((L2NpcInstance)target).endDecayTask();
      }
    }

    L2Effect effect = activeChar.getFirstEffect(getId());
    if ((effect != null) && (effect.isSelfEffect()))
    {
      effect.exit();
    }

    getEffectsSelf(activeChar);
  }
}