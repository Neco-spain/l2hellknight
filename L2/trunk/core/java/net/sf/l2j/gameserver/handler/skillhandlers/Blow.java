//L2DDT
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
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.util.Rnd;

public class Blow implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS = {SkillType.BLOW};

	private int _successChance;
	public static int FRONT = Config.FRONT_CHANCE;
	public static int SIDE = Config.SIDE_CHANCE;
	public static int BEHIND = Config.BEHIND_CHANCE;

	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(activeChar.isAlikeDead())
			return;
		if(skill.getId() == 30)
		{
			FRONT = 0;
			SIDE = 0;
			BEHIND = Config.BACKSTAB_CHANCE;
		
		}	
        for(int index = 0;index < targets.length;index++)
        {
			L2Character target = (L2Character)targets[index];
			boolean skillIsEvaded = Formulas.getInstance().calcPhysicalSkillEvasion(target, skill);
			if(target.isAlikeDead())
				continue;
			/*
			if (skill.getId() == 358 || skill.getId() == 321)
			{
				target.broadcastPacket(new BeginRotation(target.getObjectId(), target.getHeading(), 1, 65535));
			    target.broadcastPacket(new StopRotation(target.getObjectId(), this.getHeading(), 65535));
			    target.setHeading(this.getHeading());
			    target.setTarget(null);
			}*/
			if(activeChar.isBehindTarget())
				_successChance = BEHIND;
			else if(activeChar.isFrontTarget())
				_successChance = FRONT;
			else
				_successChance = SIDE;
			if(!skillIsEvaded && ((skill.getCondition() & L2Skill.COND_BEHIND) != 0) && _successChance == BEHIND || ((skill.getCondition() & L2Skill.COND_CRIT) != 0) && Formulas.getInstance().calcBlow(activeChar, target, _successChance))
			{
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
				            skill.getEffects(activeChar, target);
				     }
				}
	            L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
	            boolean soul = (weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() == L2WeaponType.DAGGER);
	            boolean shld = Formulas.getInstance().calcShldUse(activeChar, target);

	            // Crit rate base crit rate for skill, modified with STR bonus
	            boolean crit = false;
				if(Formulas.getInstance().calcCrit(skill.getBaseCritRate()*10*Formulas.getInstance().getSTRBonus(activeChar)))
					crit = true;
				double damage = (int)Formulas.getInstance().calcBlowDamage(activeChar, target, skill, shld, soul);
				if (crit)
				{
					damage *= 2;
				}

				if (soul && weapon != null)
	            	weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
				// Added by eXtrem L2DDT
				int RevengeDmg = 0;
				int reflectDmg = 0;
				int twodamage = Rnd.get(100);
				int randomiz = Rnd.get(100);
				// CounterAttack
				if (target.reflectDamageSkill(skill))
				{
					reflectDmg += damage;
					reflectDmg *= randomiz;
					reflectDmg *= 0.01;
				}
				// Shield of Revenge
				if (target.reflectRevengeSkill(skill))
				{
					RevengeDmg += damage;
				}
				if(skill.getDmgDirectlyToHP() && target instanceof L2PcInstance)
	        	{
					L2PcInstance player = (L2PcInstance)target;
	        		if (!player.isInvul())
					{
	        	       if (damage >= player.getCurrentHp())
	        	       {
	        	    	   if(player.isInDuel()) player.setCurrentHp(1);
	        	    	   else
	        	    	   {
	        	    		   player.setCurrentHp(0);
	        	    		   if (player.isInOlympiadMode())
	        	    		   {
	        	    			   player.abortAttack();
	        	    			   player.abortCast();
	        	    			   player.getStatus().stopHpMpRegeneration();
	        	    		   }
	        	    		   else
	        	    			   player.doDie(activeChar);
	        	    	   }
	        	       }
	        	       else
						  activeChar.reduceCurrentHp(RevengeDmg, activeChar);
						  activeChar.reduceCurrentHp(reflectDmg, activeChar);
						  if(twodamage<80)activeChar.reduceCurrentHp(reflectDmg, activeChar);
	        		      player.setCurrentHp(player.getCurrentHp() - damage);
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
					if(twodamage<80) activeChar.reduceCurrentHp(reflectDmg, target);
	        		target.reduceCurrentHp(damage, activeChar);
				}
				if(activeChar instanceof L2PcInstance)
					activeChar.sendPacket(new SystemMessage(SystemMessageId.CRITICAL_HIT));
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DID_S1_DMG);
	            sm.addNumber((int)damage);
	            activeChar.sendPacket(sm);
			}
			if (skillIsEvaded)
			{
				if (activeChar instanceof L2PcInstance)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_DODGES_ATTACK);
					sm.addString(target.getName());
					((L2PcInstance) activeChar).sendPacket(sm);
				}
				if (target instanceof L2PcInstance)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.AVOIDED_S1_ATTACK);
					sm.addString(activeChar.getName());
					((L2PcInstance) target).sendPacket(sm);
				}
			}
			
			//Possibility of a lethal strike
			if(!target.isRaid()
					&& !(target instanceof L2DoorInstance)
					&& !(target instanceof L2NpcInstance && ((L2NpcInstance)target).getNpcId() == 35062))
			{
				int chance = Rnd.get(100);
				//2nd lethal effect activate (cp,hp to 1 or if target is npc then hp to 1)
				if(skill.getLethalChance2() > 0 && chance < Formulas.getInstance().calcLethal(activeChar, target, skill.getLethalChance2()))
	            {
	            	if (target instanceof L2NpcInstance)
                        target.reduceCurrentHp(target.getCurrentHp()-1, activeChar);
        			else if (target instanceof L2PcInstance) // If is a active player set his HP and CP to 1
        			{
        				L2PcInstance player = (L2PcInstance)target;
        				if (!player.isInvul()){
        					player.setCurrentHp(1);
    						player.setCurrentCp(1);
        				}
        			}
	            	activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
	            }
	            else if(skill.getLethalChance1() > 0 && chance < Formulas.getInstance().calcLethal(activeChar, target, skill.getLethalChance1())){
            		if (target instanceof L2PcInstance)
         		   	{
            			L2PcInstance player = (L2PcInstance)target;
        				if (!player.isInvul())
        					player.setCurrentCp(1); // Set CP to 1
         		   	}
            		else if (target instanceof L2NpcInstance) // If is a monster remove first damage and after 50% of current hp
            			target.reduceCurrentHp(target.getCurrentHp()/2, activeChar);
	            	activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
				}
			}
            L2Effect effect = activeChar.getFirstEffect(skill.getId());
            //Self Effect
            if (effect != null && effect.isSelfEffect())
            	effect.exit();
            skill.getEffectsSelf(activeChar);
        }
	}

	public SkillType[] getSkillIds()
    {
        return SKILL_IDS;
    }
}
