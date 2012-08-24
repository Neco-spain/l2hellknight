/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2.hellknight.gameserver.model.actor.stat;

import l2.hellknight.gameserver.datatables.ExperienceTable;
import l2.hellknight.gameserver.datatables.PetDataTable;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.instance.L2PetInstance;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.SocialAction;
import l2.hellknight.gameserver.network.serverpackets.StatusUpdate;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.skills.Stats;

public class PetStat extends SummonStat
{
	public PetStat(L2PetInstance activeChar)
	{
		super(activeChar);
	}
	
	public boolean addExp(int value)
	{
		if (!super.addExp(value)) return false;
		
		getActiveChar().updateAndBroadcastStatus(1);
		// The PetInfo packet wipes the PartySpelled (list of active  spells' icons).  Re-add them
		getActiveChar().updateEffectIcons(true);
		
		return true;
	}
	
	@Override
	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		if (!super.addExpAndSp(addToExp, addToSp)) return false;
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_EARNED_S1_EXP);
		sm.addNumber((int)addToExp);
		getActiveChar().updateAndBroadcastStatus(1);
		getActiveChar().sendPacket(sm);
		
		return true;
	}
	
	@Override
	public final boolean addLevel(byte value)
	{
		if (getLevel() + value > (getMaxLevel() - 1)) return false;
		
		boolean levelIncreased = super.addLevel(value);
		
		// Sync up exp with current level
		//if (getExp() > getExpForLevel(getLevel() + 1) || getExp() < getExpForLevel(getLevel())) setExp(Experience.LEVEL[getLevel()]);
		
		StatusUpdate su = new StatusUpdate(getActiveChar());
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		getActiveChar().broadcastPacket(su);
		if (levelIncreased)
			getActiveChar().broadcastPacket(new SocialAction(getActiveChar().getObjectId(), SocialAction.LEVEL_UP));
		// Send a Server->Client packet PetInfo to the L2PcInstance
		getActiveChar().updateAndBroadcastStatus(1);
		
		if (getActiveChar().getControlItem() != null)
			getActiveChar().getControlItem().setEnchantLevel(getLevel());
		
		return levelIncreased;
	}
	
	@Override
	public final long getExpForLevel(int level)
	{
		try {
			return PetDataTable.getInstance().getPetLevelData(getActiveChar().getNpcId(), level).getPetMaxExp();
		}
		catch (NullPointerException e)
		{
			if (getActiveChar() != null)
				_log.warning("Pet objectId:" + getActiveChar().getObjectId() + ", NpcId:"+getActiveChar().getNpcId()+", level:"+level+" is missing data from pets_stats table!");
			throw e;
		}
	}
	
	@Override
	public L2PetInstance getActiveChar() { return (L2PetInstance)super.getActiveChar(); }
	
	public final int getFeedBattle() { return getActiveChar().getPetLevelData().getPetFeedBattle(); }
	
	public final int getFeedNormal() { return getActiveChar().getPetLevelData().getPetFeedNormal(); }
	
	@Override
	public void setLevel(byte value)
	{
		getActiveChar().setPetData(PetDataTable.getInstance().getPetLevelData(getActiveChar().getTemplate().getNpcId(), value));
		if (getActiveChar().getPetLevelData() == null)
			throw new IllegalArgumentException("No pet data for npc: " + getActiveChar().getTemplate().getNpcId() + " level: " + value);
		getActiveChar().stopFeed();
		super.setLevel(value);
		
		getActiveChar().startFeed();
		
		if (getActiveChar().getControlItem() != null)
			getActiveChar().getControlItem().setEnchantLevel(getLevel());
	}
	
	public final int getMaxFeed() { return getActiveChar().getPetLevelData().getPetMaxFeed(); }
	
	@Override
	public int getMaxVisibleHp() { return (int)calcStat(Stats.MAX_HP, getActiveChar().getPetLevelData().getPetMaxHP(), null, null); }
	
	@Override
	public int getMaxMp() { return (int)calcStat(Stats.MAX_MP, getActiveChar().getPetLevelData().getPetMaxMP(), null, null); }
	
	@Override
	public int getMAtk(L2Character target, L2Skill skill)
	{
		double attack = getActiveChar().getPetLevelData().getPetMAtk();
		Stats stat = skill == null? null : skill.getStat();
		if (stat != null)
		{
			switch (stat)
			{
				case AGGRESSION: attack += getActiveChar().getTemplate().getBaseAggression(); break;
				case BLEED:      attack += getActiveChar().getTemplate().getBaseBleed();      break;
				case POISON:     attack += getActiveChar().getTemplate().getBasePoison();     break;
				case STUN:       attack += getActiveChar().getTemplate().getBaseStun();       break;
				case ROOT:       attack += getActiveChar().getTemplate().getBaseRoot();       break;
				case MOVEMENT:   attack += getActiveChar().getTemplate().getBaseMovement();   break;
				case CONFUSION:  attack += getActiveChar().getTemplate().getBaseConfusion();  break;
				case SLEEP:      attack += getActiveChar().getTemplate().getBaseSleep();      break;
			}
		}
		if (skill != null) attack += skill.getPower();
		return (int)calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
	}
	
	@Override
	public int getMDef(L2Character target, L2Skill skill)
	{
		double defence = getActiveChar().getPetLevelData().getPetMDef();
		return (int)calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
	}
	
	@Override
	public int getPAtk(L2Character target) { return (int)calcStat(Stats.POWER_ATTACK, getActiveChar().getPetLevelData().getPetPAtk(), target, null); }
	@Override
	public int getPDef(L2Character target) { return (int)calcStat(Stats.POWER_DEFENCE, getActiveChar().getPetLevelData().getPetPDef(), target, null); }

	@Override
	public int getPAtkSpd()
	{
		int val = super.getPAtkSpd();
		if (getActiveChar().isHungry())
			val = val/2;
		return  val;
	}
	
	@Override
	public int getMAtkSpd()
	{
		int val = super.getMAtkSpd();
		if (getActiveChar().isHungry())
			val = val/2;
		return  val;
	}
	
	@Override
	public int getMaxLevel()
	{
		return ExperienceTable.getInstance().getMaxPetLevel();
	}
}
