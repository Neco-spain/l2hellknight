package net.sf.l2j.gameserver.skills.l2skills;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillChargeDmg extends L2Skill
{
  final int chargeSkillId;

  public L2SkillChargeDmg(StatsSet set)
  {
    super(set);
    chargeSkillId = set.getInteger("charge_skill_id");
  }

  public boolean checkCondition(L2Character activeChar, L2Object target, boolean itemOrWeapon)
  {
    if (activeChar.isPlayer())
    {
      if (activeChar.getCharges() < getNumCharges())
      {
        activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(getId()));
        return false;
      }
    }
    return super.checkCondition(activeChar, target, itemOrWeapon);
  }

  public void useSkill(L2Character caster, FastList<L2Object> targets)
  {
    if (caster.isAlikeDead()) {
      return;
    }

    int charges = caster.getCharges();
    if (charges < getNumCharges())
    {
      caster.sendPacket(SystemMessage.id(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(getId()));
      return;
    }
    double modifier = 0.0D;
    modifier = 1.1D + 0.201D * charges;
    if ((getTargetType() != L2Skill.SkillTargetType.TARGET_AREA) && (getTargetType() != L2Skill.SkillTargetType.TARGET_MULTIFACE)) {
      caster.decreaseCharges(getNumCharges());
    }
    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
    {
      L2Character target = (L2Character)n.getValue();
      if ((target == null) || (target.isAlikeDead())) {
        continue;
      }
      L2ItemInstance weapon = caster.getActiveWeaponInstance();

      boolean shld = Formulas.calcShldUse(caster, target);
      boolean crit = Formulas.calcCrit(caster.getCriticalHit(target, this));
      boolean soul = (weapon != null) && (weapon.getChargedSoulshot() == 1) && (weapon.getItemType() != L2WeaponType.DAGGER);

      int damage = (int)Formulas.calcPhysDam(caster, target, this, shld, false, false, soul);

      if (crit) {
        damage = (int)Formulas.calcViciousDam(caster, damage, true);
      }
      if (damage > 0)
      {
        double finalDamage = damage * modifier;
        target.reduceCurrentHp(finalDamage, caster);

        caster.sendDamageMessage(target, (int)finalDamage, false, crit, false);

        if ((soul) && (weapon != null))
          weapon.setChargedSoulshot(0);
      }
      else {
        caster.sendDamageMessage(target, 0, false, false, true);
      }
    }
    L2Effect seffect = caster.getFirstEffect(getId());
    if ((seffect != null) && (seffect.isSelfEffect()))
    {
      seffect.exit();
    }

    getEffectsSelf(caster);
  }
}