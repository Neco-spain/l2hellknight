package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2CubicInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
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

  public void useSkill(L2Character activeChar, L2Object[] targets)
  {
    if (activeChar.isAlikeDead()) {
      return;
    }
    boolean ss = false;
    boolean bss = false;

    for (int index = 0; index < targets.length; index++)
    {
      L2Character target = (L2Character)targets[index];
      if ((target.isAlikeDead()) && (getTargetType() != L2Skill.SkillTargetType.TARGET_CORPSE_MOB)) {
        continue;
      }
      if ((activeChar != target) && (target.isInvul())) {
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
      else if ((activeChar instanceof L2Summon))
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

      boolean mcrit = Formulas.getInstance().calcMCrit(activeChar.getMCriticalHit(target, this));
      int damage = (int)Formulas.getInstance().calcMagicDam(activeChar, target, this, ss, bss, mcrit);

      int drain = damage;
      drain = (int)(drain - target.getCurrentCp());
      if (drain < 0) drain = 0;
      if ((activeChar instanceof L2MonsterInstance)) drain = damage;
      if (target.getCurrentHp() < drain)
      {
        drain = 0;
        drain = (int)(drain + target.getCurrentHp());
      }
      double hpAdd = _absorbAbs + _absorbPart * drain;
      double hp = activeChar.getCurrentHp() + hpAdd > activeChar.getMaxHp() ? activeChar.getMaxHp() : activeChar.getCurrentHp() + hpAdd;

      activeChar.setCurrentHp(hp);

      StatusUpdate suhp = new StatusUpdate(activeChar.getObjectId());
      suhp.addAttribute(9, (int)hp);
      activeChar.sendPacket(suhp);

      if ((damage > 0) && ((!target.isDead()) || (getTargetType() != L2Skill.SkillTargetType.TARGET_CORPSE_MOB)))
      {
        if ((!target.isRaid()) && (Formulas.getInstance().calcAtkBreak(target, damage)))
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
            getEffects(target, activeChar);
            SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
            sm.addSkillName(getId());
            activeChar.sendPacket(sm);
          }
          else
          {
            target.stopSkillEffects(getId());
            if (Formulas.getInstance().calcSkillSuccess(activeChar, target, this, false, ss, bss)) {
              getEffects(activeChar, target);
            }
            else {
              SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
              sm.addString(target.getName());
              sm.addSkillName(getDisplayId());
              activeChar.sendPacket(sm);
            }
          }
        }

        target.reduceCurrentHp(damage, activeChar);
      }

      if ((target.isDead()) && (getTargetType() == L2Skill.SkillTargetType.TARGET_CORPSE_MOB) && ((target instanceof L2NpcInstance))) {
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

  public void useCubicSkill(L2CubicInstance activeCubic, L2Object[] targets)
  {
    for (int index = 0; index < targets.length; index++)
    {
      L2Character target = (L2Character)targets[index];
      if ((target.isAlikeDead()) && (getTargetType() != L2Skill.SkillTargetType.TARGET_CORPSE_MOB)) {
        continue;
      }
      boolean mcrit = Formulas.getInstance().calcMCrit(activeCubic.getMCriticalHit(target, this));
      int damage = (int)Formulas.getInstance().calcMagicDam(activeCubic, target, this, mcrit);

      double hpAdd = _absorbAbs + _absorbPart * damage;
      L2PcInstance owner = activeCubic.getOwner();
      double hp = owner.getCurrentHp() + hpAdd > owner.getMaxHp() ? owner.getMaxHp() : owner.getCurrentHp() + hpAdd;

      owner.setCurrentHp(hp);

      StatusUpdate suhp = new StatusUpdate(owner.getObjectId());
      suhp.addAttribute(9, (int)hp);
      owner.sendPacket(suhp);

      if ((damage <= 0) || ((target.isDead()) && (getTargetType() == L2Skill.SkillTargetType.TARGET_CORPSE_MOB)))
        continue;
      target.reduceCurrentHp(damage, activeCubic.getOwner());

      if ((!target.isRaid()) && (Formulas.getInstance().calcAtkBreak(target, damage))) {
        target.breakAttack();
        target.breakCast();
      }
      owner.sendDamageMessage(target, damage, mcrit, false, false);
    }
  }
}