//L2DDT
package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.effects.EffectCharge;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.Rnd;

public class L2SkillChargeDmg extends L2Skill
{

	final int chargeSkillId;

	public L2SkillChargeDmg(StatsSet set)
    {
		super(set);
		chargeSkillId = set.getInteger("charge_skill_id");
	}

	@Override
	public boolean checkCondition(L2Character activeChar, L2Object target, boolean itemOrWeapon)
	{
		if (activeChar instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)activeChar;
			EffectCharge e = (EffectCharge)player.getFirstEffect(chargeSkillId);
			if(e == null || e.numCharges < getNumCharges())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(getId());
				activeChar.sendPacket(sm);
				return false;
			}
		}
		return super.checkCondition(activeChar, target, itemOrWeapon);
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
    {
		if (caster.isAlikeDead())
        {
			return;
        }

		// get the effect
		EffectCharge effect = (EffectCharge) caster.getFirstEffect(chargeSkillId);
		if (effect == null || effect.numCharges < getNumCharges())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(getId());
			caster.sendPacket(sm);
			return;
		}
        double modifier = 0;
        modifier = (effect.numCharges-getNumCharges())*0.33;
		if (getTargetType() != SkillTargetType.TARGET_AREA)
			effect.numCharges -= getNumCharges();
		if (caster instanceof L2PcInstance)
			caster.sendPacket(new EtcStatusUpdate((L2PcInstance)caster));
        if (effect.numCharges == 0)
        	{effect.exit();}
        for (int index = 0;index < targets.length;index++)
        {
        	L2ItemInstance weapon = caster.getActiveWeaponInstance();
        	L2Character target = (L2Character)targets[index];
        	if (target.isAlikeDead())
        		continue;

			boolean shld = Formulas.getInstance().calcShldUse(caster, target);
			boolean crit = Formulas.getInstance().calcCrit(caster.getCriticalHit(target, this));
			boolean soul = (weapon != null
							&& weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT
							&& weapon.getItemType() != L2WeaponType.DAGGER );

			// damage calculation, crit is static 2x
			int damage = (int)Formulas.getInstance().calcPhysDam(caster, target, this, shld, false, false, soul);
			if (crit) damage *= 2;

			if (damage > 0)
            {
                double finalDamage = damage;
                finalDamage = finalDamage+(modifier*finalDamage);

				int reflectDmg = 0;
				int RevengeDmg = 0;
				int twodamage = Rnd.get(100);
				int randomiz = Rnd.get(100);
				if (target.reflectDamageSkill(this))
				{
					reflectDmg += damage;
					reflectDmg *= randomiz;
					reflectDmg *= 0.01;
				}
				if (target.reflectRevengeSkill(this))
				{
					RevengeDmg += damage;
					RevengeDmg *= 0.4;
					damage -= RevengeDmg;
				}
				caster.reduceCurrentHp(RevengeDmg, target);
				caster.reduceCurrentHp(reflectDmg, target);
				if(twodamage<80) caster.reduceCurrentHp(reflectDmg, target);
				target.reduceCurrentHp(finalDamage, caster);

				caster.sendDamageMessage(target, (int)finalDamage, false, crit, false);

				if (soul && weapon!= null)
					weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
			}
            else
            {
				caster.sendDamageMessage(target, 0, false, false, true);
			}
		}
        // effect self :]
        L2Effect seffect = caster.getFirstEffect(getId());
        if (seffect != null && seffect.isSelfEffect())
        {
            //Replace old effect with new one.
            seffect.exit();
        }
        // cast self effect if any
        getEffectsSelf(caster);
	}

}
