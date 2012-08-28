package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.lib.Log;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2CubicInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;

public class Mdam implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS = {SkillType.MDAM, SkillType.DEATHLINK};
    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        if (activeChar.isAlikeDead()) return;

        boolean ss = false;
        boolean bss = false;

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
        else if (activeChar instanceof L2Summon)
        {
            L2Summon activeSummon = (L2Summon) activeChar;

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

        for (int index = 0; index < targets.length; index++)
        {
            L2Character target = (L2Character) targets[index];

            if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance
                && target.isAlikeDead() && target.isFakeDeath())
            {
                target.stopFakeDeath(null);
            }
            else if (target.isAlikeDead())
            {
                continue;
            }

            boolean mcrit = Formulas.getInstance().calcMCrit(activeChar.getMCriticalHit(target, skill));

            int damage = (int) Formulas.getInstance().calcMagicDam(activeChar, target, skill, ss, bss,
                                                                   mcrit);

            if (damage > 5000 && activeChar instanceof L2PcInstance)
            {
                String name = "";
                if (target instanceof L2RaidBossInstance) name = "RaidBoss ";
                if (target instanceof L2NpcInstance)
                    name += target.getName() + "(" + ((L2NpcInstance) target).getTemplate().npcId + ")";
                if (target instanceof L2PcInstance)
                    name = target.getName() + "(" + target.getObjectId() + ") ";
                name += target.getLevel() + " lvl";
                Log.add(activeChar.getName() + "(" + activeChar.getObjectId() + ") "
                    + activeChar.getLevel() + " lvl did damage " + damage + " with skill "
                    + skill.getName() + "(" + skill.getId() + ") to " + name, "damage_mdam");
            }

            if (damage > 0)
            {
                if (!target.isRaid() && Formulas.getInstance().calcAtkBreak(target, damage))
                {
                    target.breakAttack();
                    target.breakCast();
                }

                activeChar.sendDamageMessage(target, damage, mcrit, false, false);

                if (skill.hasEffects())
                {
                	if (target.reflectSkill(skill))
                	{
                		activeChar.stopSkillEffects(skill.getId());
						skill.getEffects((L2Character)null, activeChar);
    					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(skill.getId());
						activeChar.sendPacket(sm);
                	}
                	else
                	{
                        target.stopSkillEffects(skill.getId());
                        if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, ss, bss))
                            skill.getEffects(activeChar, target);
                        else
                        {
                            SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
                            sm.addString(target.getName());
                            sm.addSkillName(skill.getDisplayId());
                            activeChar.sendPacket(sm);
                        }
                	}
                }
				if (target.reflectDamageSkill(skill))
				activeChar.reduceCurrentHp(damage, target);
				else
                target.reduceCurrentHp(damage, activeChar);
            }
			Formulas.getInstance().calcLethalHit(activeChar, target, skill);
        }
        L2Effect effect = activeChar.getFirstEffect(skill.getId());
        if (effect != null && effect.isSelfEffect())
        {
        	effect.exit();
        }
        skill.getEffectsSelf(activeChar);

        if (skill.isSuicideAttack())
        {
        	activeChar.doDie(null);
        	activeChar.setCurrentHp(0);
        }
    }

	public void useCubicSkill(L2CubicInstance activeCubic, L2Skill skill, L2Object[] targets)
	{
		for (int index = 0; index < targets.length; index++)
		{
			L2Character target = (L2Character)targets[index];

			if (target instanceof L2PcInstance && target.isAlikeDead() && target.isFakeDeath())
			{
				target.stopFakeDeath(null);
			}
			else if (target.isAlikeDead())
			{
				continue;
			}

			boolean mcrit = Formulas.getInstance().calcMCrit(activeCubic.getMCriticalHit(target, skill));
			int damage = (int)Formulas.getInstance().calcMagicDam(activeCubic, target, skill, mcrit);

			// if target is reflecting the skill then no damage is done
			if (target.reflectSkill(skill))
				damage = 0;

			if (damage > 0)
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.getInstance().calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				activeCubic.getOwner().sendDamageMessage(target, damage, mcrit, false, false);

				if (skill.hasEffects())
				{
					// activate attacked effects, if any
					target.stopSkillEffects(skill.getId());
					if (target.getFirstEffect(skill) != null)
						target.removeEffect(target.getFirstEffect(skill));
					if (Formulas.getInstance().calcCubicSkillSuccess(activeCubic, target, skill))
						skill.getEffects(activeCubic, target);
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
						sm.addString(target.getName());
						sm.addSkillName(skill.getId());
						activeCubic.getOwner().sendPacket(sm);
					}
				}

				target.reduceCurrentHp(damage, activeCubic.getOwner());
			}
		}
	}

    public SkillType[] getSkillIds()
    {
        return SKILL_IDS;
    }
}
