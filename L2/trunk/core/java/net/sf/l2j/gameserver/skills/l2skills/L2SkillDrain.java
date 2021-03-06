package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
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

public class L2SkillDrain extends L2Skill {

	private float _absorbPart;
	private int   _absorbAbs;

	public L2SkillDrain(StatsSet set)
    {
		super(set);

		_absorbPart = set.getFloat ("absorbPart", 0.f);
		_absorbAbs  = set.getInteger("absorbAbs", 0);
	}

	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
    {
		if (activeChar.isAlikeDead())
			return;

		boolean ss = false;
		boolean bss = false;

        for(int index = 0;index < targets.length;index++)
        {
			L2Character target = (L2Character)targets[index];
			if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
				continue;

            if (activeChar != target && target.isInvul())
                continue; // No effect on invulnerable chars unless they cast it themselves.

			L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

			if (weaponInst != null)
			{
			    if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			    {
			        bss = true;
			        weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			    }
			    else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			    {
			        ss = true;
			        weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			    }
            }
            // If there is no weapon equipped, check for an active summon.
            else if (activeChar instanceof L2Summon)
            {
                L2Summon activeSummon = (L2Summon)activeChar;

                if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
                {
                    bss = true;
                    activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
                }
                else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
                {
                    ss = true;
                    activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
                }
            }

			boolean mcrit = Formulas.getInstance().calcMCrit(activeChar.getMCriticalHit(target, this));
			int damage = (int)Formulas.getInstance().calcMagicDam(
					activeChar, target, this, ss, bss, mcrit);
			int drain = damage;
			drain -= target.getCurrentCp();
			if (drain < 0) drain = 0;
			if (activeChar instanceof L2MonsterInstance) drain = damage;
			if (target.getCurrentHp() < drain)
			{
				drain = 0;
				drain += target.getCurrentHp();
			}
			double hpAdd = _absorbAbs + _absorbPart * drain;
			double hp = ((activeChar.getCurrentHp() + hpAdd) > activeChar.getMaxHp() ? activeChar.getMaxHp() : (activeChar.getCurrentHp() + hpAdd));

            activeChar.setCurrentHp(hp);

			StatusUpdate suhp = new StatusUpdate(activeChar.getObjectId());
			suhp.addAttribute(StatusUpdate.CUR_HP, (int)hp);
			activeChar.sendPacket(suhp);

            // Check to see if we should damage the target
            if (damage > 0 && (!target.isDead() || getTargetType() != SkillTargetType.TARGET_CORPSE_MOB))
            {
                // Manage attack or cast break of the target (calculating rate, sending message...)
                if (!target.isRaid() && Formulas.getInstance().calcAtkBreak(target, damage))
                {
                    target.breakAttack();
                    target.breakCast();
                }

            	activeChar.sendDamageMessage(target, damage, mcrit, false, false);
                
                if (hasEffects() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
                {
                	if (target.reflectSkill(this))
                	{
                		activeChar.stopSkillEffects(getId());
    					getEffects((L2Character)null, activeChar);
    					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(getId());
						activeChar.sendPacket(sm);
                	}
                	else
                	{
                		// activate attacked effects, if any
                        target.stopSkillEffects(getId());
                        if (Formulas.getInstance().calcSkillSuccess(activeChar, target, this, false, ss, bss))
                            getEffects(activeChar, target);
                        else
                        {
                            SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
                            sm.addString(target.getName());
                            sm.addSkillName(getDisplayId());
                            activeChar.sendPacket(sm);
                        }
                	}
                }
                
                target.reduceCurrentHp(damage, activeChar);
            }

            // Check to see if we should do the decay right after the cast
            if (target.isDead() && getTargetType() == SkillTargetType.TARGET_CORPSE_MOB && target instanceof L2NpcInstance) {
                ((L2NpcInstance)target).endDecayTask();
            }
		}
        //effect self :]
        L2Effect effect = activeChar.getFirstEffect(getId());
        if (effect != null && effect.isSelfEffect())
        {
            //Replace old effect with new one.
            effect.exit();
        }
        // cast self effect if any
        getEffectsSelf(activeChar);
	}

	public void useCubicSkill(L2CubicInstance activeCubic, L2Object[] targets)
    {
        for(int index = 0;index < targets.length;index++)
        {
			L2Character target = (L2Character)targets[index];
			if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
				continue;

			boolean mcrit = Formulas.getInstance().calcMCrit(activeCubic.getMCriticalHit(target, this));
			int damage = (int)Formulas.getInstance().calcMagicDam(activeCubic, target, this, mcrit);
			
			double hpAdd = _absorbAbs + _absorbPart * damage;
			L2PcInstance owner = activeCubic.getOwner();
			double hp = ((owner.getCurrentHp() + hpAdd) > owner.getMaxHp() ? owner.getMaxHp() : (owner.getCurrentHp() + hpAdd));

            owner.setCurrentHp(hp); 
            
			StatusUpdate suhp = new StatusUpdate(owner.getObjectId()); 
			suhp.addAttribute(StatusUpdate.CUR_HP, (int)hp); 
			owner.sendPacket(suhp);
			
            // Check to see if we should damage the target
            if (damage > 0 && (!target.isDead() || getTargetType() != SkillTargetType.TARGET_CORPSE_MOB))
            {
    			target.reduceCurrentHp(damage, activeCubic.getOwner());
                
                // Manage attack or cast break of the target (calculating rate, sending message...)
                if (!target.isRaid() && Formulas.getInstance().calcAtkBreak(target, damage)){
                    target.breakAttack();
                    target.breakCast();
                }
            	owner.sendDamageMessage(target, damage, mcrit, false, false);
            }
		}
	}

}
