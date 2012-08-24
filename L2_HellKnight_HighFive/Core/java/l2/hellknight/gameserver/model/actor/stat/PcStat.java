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

import javolution.util.FastList;

import l2.hellknight.Config;
import l2.hellknight.ExternalConfig;
import l2.hellknight.gameserver.datatables.ExperienceTable;
import l2.hellknight.gameserver.datatables.NpcTable;
import l2.hellknight.gameserver.instancemanager.BonusExpManager;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.instance.L2ClassMasterInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PetInstance;
import l2.hellknight.gameserver.model.entity.RecoBonus;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.stats.Stats;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2.hellknight.gameserver.network.serverpackets.ExVitalityPointInfo;
import l2.hellknight.gameserver.network.serverpackets.ExVoteSystemInfo;
import l2.hellknight.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2.hellknight.gameserver.network.serverpackets.SocialAction;
import l2.hellknight.gameserver.network.serverpackets.StatusUpdate;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.network.serverpackets.UserInfo;
import l2.hellknight.gameserver.scripting.scriptengine.events.PlayerLevelChangeEvent;
import l2.hellknight.gameserver.scripting.scriptengine.listeners.player.PlayerLevelListener;
import l2.hellknight.gameserver.util.Util;

public class PcStat extends PlayableStat
{	
	private int _oldMaxHp; // stats watch
	private int _oldMaxMp; // stats watch
	private int _oldMaxCp; // stats watch
	private float _vitalityPoints = 1;
	private byte _vitalityLevel = 0;
	
	public static final int VITALITY_LEVELS[] = { 240, 2000, 13000, 17000, 20000 };
	public static final int MAX_VITALITY_POINTS = VITALITY_LEVELS[4];
	public static final int MIN_VITALITY_POINTS = 1;
    
    // Used to check if the player has gained exp since log in
    public long _firstExp;	
    
	public FastList<PlayerLevelListener> levelListeners = new FastList<PlayerLevelListener>().shared();
	public static FastList<PlayerLevelListener> globalLevelListeners = new FastList<PlayerLevelListener>().shared();
	
	public PcStat(L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean addExp(long value)
	{
		L2PcInstance activeChar = getActiveChar();
		
		// Allowed to gain exp?
		if (!getActiveChar().getAccessLevel().canGainExp() || (ExternalConfig.NOXPGAIN_ENABLED && getActiveChar().cantGainXP()))
			return false;
		
		if (!super.addExp(value))
			return false;
		
		// Set new karma
		if (!activeChar.isCursedWeaponEquipped() && activeChar.getKarma() > 0 && (activeChar.isGM() || !activeChar.isInsideZone(L2Character.ZONE_PVP)))
		{
			int karmaLost = activeChar.calculateKarmaLost(value);
			if (karmaLost > 0)
				activeChar.setKarma(activeChar.getKarma() - karmaLost);
		}
		
		// EXP status update currently not used in retail
		activeChar.sendPacket(new UserInfo(activeChar));
		activeChar.sendPacket(new ExBrExtraUserInfo(activeChar));
		return true;
	}
	
	/**
	 * Add Experience and SP rewards to the L2PcInstance, remove its Karma (if necessary) and Launch increase level task.<BR><BR>
	 *
	 * <B><U> Actions </U> :</B><BR><BR>
	 * <li>Remove Karma when the player kills L2MonsterInstance</li>
	 * <li>Send a Server->Client packet StatusUpdate to the L2PcInstance</li>
	 * <li>Send a Server->Client System Message to the L2PcInstance </li>
	 * <li>If the L2PcInstance increases it's level, send a Server->Client packet SocialAction (broadcast) </li>
	 * <li>If the L2PcInstance increases it's level, manage the increase level task (Max MP, Max MP, Recommandation, Expertise and beginner skills...) </li>
	 * <li>If the L2PcInstance increases it's level, send a Server->Client packet UserInfo to the L2PcInstance </li><BR><BR>
	 *
	 * @param addToExp The Experience value to add
	 * @param addToSp The SP value to add
	 */
	@Override
	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		return addExpAndSp(addToExp, addToSp, false);
	}
	
	public boolean addExpAndSp(long addToExp, int addToSp, boolean useBonuses)
	{
		L2PcInstance activeChar = getActiveChar();
		//Add by pmq Start
		double basePercent = addToExp;
		
		if (ExternalConfig.ENABLE_RUNE_BONUS)
		{
			long[] bonus = BonusExpManager.getInstance().getBonusExpAndSp(getActiveChar(), addToExp, addToSp);
			if (bonus[0] > 0)
				addToExp += bonus[0];
			if (bonus[1] > 0)
				addToSp += bonus[1];
		}
		
		if (useBonuses)
		{
			if (Config.ENABLE_VITALITY)
			{
				if (activeChar.isAdventBlessingActive())
				{
					addToExp *= Config.RATE_VITALITY_LEVEL_4;
					addToSp *= Config.RATE_VITALITY_LEVEL_4;
				}
				else
				{
					switch (_vitalityLevel)
					{
						case 1:
							addToExp *= Config.RATE_VITALITY_LEVEL_1;
							addToSp *= Config.RATE_VITALITY_LEVEL_1;
							break;
						case 2:
							addToExp *= Config.RATE_VITALITY_LEVEL_2;
							addToSp *= Config.RATE_VITALITY_LEVEL_2;
							break;
						case 3:
							addToExp *= Config.RATE_VITALITY_LEVEL_3;
							addToSp *= Config.RATE_VITALITY_LEVEL_3;
							break;
						case 4:
							addToExp *= Config.RATE_VITALITY_LEVEL_4;
							addToSp *= Config.RATE_VITALITY_LEVEL_4;
							break;
					}
				}
			}
			// Calculate reco exp/sp bonus
			if (addToExp > 0 && !activeChar.isInsideZone(L2Character.ZONE_PEACE))
			{
				activeChar.startAdventTask();
			}
		}
		basePercent = basePercent / addToExp;
		//Add by pmq End
		// Allowed to gain exp/sp?
		if (!activeChar.getAccessLevel().canGainExp() || (ExternalConfig.NOXPGAIN_ENABLED && getActiveChar().cantGainXP()))
			return false;
		
		long baseExp = addToExp;
		int baseSp = addToSp;
		
		double bonusExp = 1.;
		double bonusSp = 1.;
		
		if (useBonuses)
		{
			bonusExp = getExpBonusMultiplier();
			bonusSp = getSpBonusMultiplier();
		}
		
		addToExp *= bonusExp;
		addToSp  *= bonusSp;
		
		float ratioTakenByPlayer = 0;
		
		// if this player has a pet and it is in his range he takes from the owner's Exp, give the pet Exp now
		if (activeChar.hasPet() && activeChar.getPet().isPet() && Util.checkIfInShortRadius(Config.ALT_PARTY_RANGE, activeChar, activeChar.getPet(), false))
		{
			L2PetInstance pet = (L2PetInstance) activeChar.getPet();
			ratioTakenByPlayer = pet.getPetLevelData().getOwnerExpTaken() / 100f;
			
			// only give exp/sp to the pet by taking from the owner if the pet has a non-zero, positive ratio
			// allow possible customizations that would have the pet earning more than 100% of the owner's exp/sp
			if (ratioTakenByPlayer > 1)
				ratioTakenByPlayer = 1;
			
			if (!pet.isDead())
				pet.addExpAndSp((long) (addToExp * (1 - ratioTakenByPlayer)), (int) (addToSp * (1 - ratioTakenByPlayer)));
		
			// now adjust the max ratio to avoid the owner earning negative exp/sp
			addToExp = (long) (addToExp * ratioTakenByPlayer);
			addToSp = (int) (addToSp * ratioTakenByPlayer);
		}
		
		if (!super.addExpAndSp(addToExp, addToSp))
			return false;
		
		SystemMessage sm = null;
		if (addToExp == 0 && addToSp != 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_SP);
			sm.addNumber(addToSp);
		}
		else if (addToSp == 0 && addToExp != 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_EXPERIENCE);
			sm.addNumber((int) addToExp);
		}
		else
		{
			if ((addToExp - baseExp) > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_BONUS_S2_AND_S3_SP_BONUS_S4);
				sm.addNumber((int) addToExp);
				sm.addNumber((int) (addToExp - baseExp));
				sm.addNumber(addToSp);
				sm.addNumber((addToSp - baseSp));
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_AND_S2_SP);
				sm.addNumber((int) addToExp);
				sm.addNumber(addToSp);
			}
		}
		activeChar.sendPacket(sm);
		return true;
	}
	
	@Override
	public boolean removeExpAndSp(long addToExp, int addToSp)
	{
		return removeExpAndSp(addToExp, addToSp, true);
	}
	
	public boolean removeExpAndSp(long addToExp, int addToSp, boolean sendMessage)
	{
		int level = getLevel();
		if (!super.removeExpAndSp(addToExp, addToSp))
			return false;
		
		if (sendMessage)
		{
			// Send a Server->Client System Message to the L2PcInstance
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EXP_DECREASED_BY_S1);
			sm.addNumber((int) addToExp);
			getActiveChar().sendPacket(sm);
			sm = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
			sm.addNumber(addToSp);
			getActiveChar().sendPacket(sm);
			if (getLevel()<level)
				getActiveChar().broadcastStatusUpdate();
		}
		return true;
	}
	
	@Override
	public final boolean addLevel(byte value)
	{
		if (getLevel() + value > ExperienceTable.getInstance().getMaxLevel() - 1)
			return false;
		fireLevelChangeListeners(value);
		
		boolean levelIncreased = super.addLevel(value);
		if (levelIncreased)
		{
			if (!Config.DISABLE_TUTORIAL)
			{
				QuestState qs = getActiveChar().getQuestState("255_Tutorial");
				if (qs != null)
					qs.getQuest().notifyEvent("CE40", null, getActiveChar());
			}
			
			getActiveChar().setCurrentCp(getMaxCp());
			getActiveChar().broadcastPacket(new SocialAction(getActiveChar().getObjectId(), SocialAction.LEVEL_UP));
			getActiveChar().sendPacket(SystemMessageId.YOU_INCREASED_YOUR_LEVEL);
			
			L2ClassMasterInstance.showQuestionMark(getActiveChar());
		}
		
		//Give AutoGet skills and all normal skills if Auto-Learn is activated.
		getActiveChar().rewardSkills();
		
		if (getActiveChar().getClan() != null)
		{
			getActiveChar().getClan().updateClanMember(getActiveChar());
			getActiveChar().getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(getActiveChar()));
		}
		if (getActiveChar().isInParty())
			getActiveChar().getParty().recalculatePartyLevel(); // Recalculate the party level
		
		if (getActiveChar().isTransformed() || getActiveChar().isInStance())
			getActiveChar().getTransformation().onLevelUp();
		
		StatusUpdate su = new StatusUpdate(getActiveChar());
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		getActiveChar().sendPacket(su);
		
		// Update the overloaded status of the L2PcInstance
		getActiveChar().refreshOverloaded();
		// Update the expertise status of the L2PcInstance
		getActiveChar().refreshExpertisePenalty();
		// Send a Server->Client packet UserInfo to the L2PcInstance
		getActiveChar().sendPacket(new UserInfo(getActiveChar()));
		getActiveChar().sendPacket(new ExBrExtraUserInfo(getActiveChar()));
		getActiveChar().sendPacket(new ExVoteSystemInfo(getActiveChar()));
		getActiveChar().incAdventPoints(2000, false); //Add NevitAdvent by pmq
		return levelIncreased;
	}
	
	@Override
	public boolean addSp(int value)
	{
		if (!super.addSp(value))
			return false;
		
		StatusUpdate su = new StatusUpdate(getActiveChar());
		su.addAttribute(StatusUpdate.SP, getSp());
		getActiveChar().sendPacket(su);
		
		return true;
	}
	
	@Override
	public final long getExpForLevel(int level)
	{
		return ExperienceTable.getInstance().getExpForLevel(level);
	}
	
	@Override
	public final L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}
	
	@Override
	public final long getExp()
	{
		if (getActiveChar().isSubClassActive())
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getExp();
		
		return super.getExp();
	}
	
	public final long getBaseExp()
	{
		return super.getExp();
	}
	
	@Override
	public final void setExp(long value)
	{
		if (getActiveChar().isSubClassActive())
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setExp(value);
		else
			super.setExp(value);
	}
	
	@Override
	public final byte getLevel()
	{
		if (getActiveChar().isSubClassActive())
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getLevel();
		
		return super.getLevel();
	}
	
	public final byte getBaseLevel()
	{
		return super.getLevel();
	}
	
	@Override
	public final void setLevel(byte value)
	{
		if (value > ExperienceTable.getInstance().getMaxLevel() - 1)
			value = (byte)(ExperienceTable.getInstance().getMaxLevel() - 1);
		
		if (getActiveChar().isSubClassActive())
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setLevel(value);
		else
			super.setLevel(value);
	}
	
	@Override
	public final int getMaxCp()
	{
		// Get the Max CP (base+modifier) of the L2PcInstance
		int val = super.getMaxCp();
		if (val != _oldMaxCp)
		{
			_oldMaxCp = val;
			
			// Launch a regen task if the new Max CP is higher than the old one
			if (getActiveChar().getStatus().getCurrentCp() != val)
				getActiveChar().getStatus().setCurrentCp(getActiveChar().getStatus().getCurrentCp()); // trigger start of regeneration
		}
		return val;
	}
	
	@Override
	public final int getMaxHp()
	{
		// Get the Max HP (base+modifier) of the L2PcInstance
		int val = super.getMaxHp();
		if (val != _oldMaxHp)
		{
			_oldMaxHp = val;
			
			// Launch a regen task if the new Max HP is higher than the old one
			if (getActiveChar().getStatus().getCurrentHp() != val)
				getActiveChar().getStatus().setCurrentHp(getActiveChar().getStatus().getCurrentHp()); // trigger start of regeneration
		}
		
		return val;
	}
	
	@Override
	public final int getMaxMp()
	{
		// Get the Max MP (base+modifier) of the L2PcInstance
		int val = super.getMaxMp();
		
		if (val != _oldMaxMp)
		{
			_oldMaxMp = val;
			
			// Launch a regen task if the new Max MP is higher than the old one
			if (getActiveChar().getStatus().getCurrentMp() != val)
				getActiveChar().getStatus().setCurrentMp(getActiveChar().getStatus().getCurrentMp()); // trigger start of regeneration
		}
		
		return val;
	}
	
	@Override
	public final int getSp()
	{
		if (getActiveChar().isSubClassActive())
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getSp();
		
		return super.getSp();
	}
	
	public final int getBaseSp()
	{
		return super.getSp();
	}
	
	@Override
	public final void setSp(int value)
	{
		if (getActiveChar().isSubClassActive())
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setSp(value);
		else
			super.setSp(value);
	}
	
	@Override
	public int getRunSpeed()
	{
		if (getActiveChar() == null)
			return 1;
		
		int val;
		
		L2PcInstance player = getActiveChar();
		if (player.isMounted())
		{
			int baseRunSpd = NpcTable.getInstance().getTemplate(getActiveChar().getMountNpcId()).getBaseRunSpd();
			val = (int) Math.round(calcStat(Stats.RUN_SPEED, baseRunSpd, null, null));
		}
		else
			val = super.getRunSpeed();
		
		val += Config.RUN_SPD_BOOST;
		
		// Apply max run speed cap.
		if (val > Config.MAX_RUN_SPEED && !getActiveChar().isGM())
			return Config.MAX_RUN_SPEED;
		
		return val;
	}
	
	@Override
	public int getPAtkSpd()
	{
		int val = super.getPAtkSpd();
		
		if (val > Config.MAX_PATK_SPEED && !getActiveChar().isGM())
			return Config.MAX_PATK_SPEED;
		
		return val;
	}
	
	@Override
	public int getEvasionRate(L2Character target)
	{
		int val = super.getEvasionRate(target);
		
		if (val > Config.MAX_EVASION && !getActiveChar().isGM())
			return Config.MAX_EVASION;
		
		return val;
	}
	
	@Override
	public int getMAtkSpd()
	{
		int val = super.getMAtkSpd();
		
		if (val > Config.MAX_MATK_SPEED && !getActiveChar().isGM())
			return Config.MAX_MATK_SPEED;
		
		return val;
	}
	
	@Override
	public float getMovementSpeedMultiplier()
	{
		if (getActiveChar() == null)
			return 1;
		
		if (getActiveChar().isMounted())
			return getRunSpeed() * 1f / NpcTable.getInstance().getTemplate(getActiveChar().getMountNpcId()).getBaseRunSpd();
		
		return super.getMovementSpeedMultiplier();
	}
	
	@Override
	public int getWalkSpeed()
	{
		if (getActiveChar() == null)
			return 1;
		
		return (getRunSpeed() * 70) / 100;
	}
	
	private void updateVitalityLevel(boolean quiet)
	{
		final byte level;
		
		if (_vitalityPoints <= VITALITY_LEVELS[0])
			level = 0;
		else if (_vitalityPoints <= VITALITY_LEVELS[1])
			level = 1;
		else if (_vitalityPoints <= VITALITY_LEVELS[2])
			level = 2;
		else if (_vitalityPoints <= VITALITY_LEVELS[3])
			level = 3;
		else
			level = 4;
		
		if (!quiet && level != _vitalityLevel)
		{
			if (level < _vitalityLevel)
				getActiveChar().sendPacket(SystemMessageId.VITALITY_HAS_DECREASED);
			else
				getActiveChar().sendPacket(SystemMessageId.VITALITY_HAS_INCREASED);
			if (level == 0)
				getActiveChar().sendPacket(SystemMessageId.VITALITY_IS_EXHAUSTED);
			else if (level == 4)
				getActiveChar().sendPacket(SystemMessageId.VITALITY_IS_AT_MAXIMUM);
		}
		
		_vitalityLevel = level;
	}
	
	/*
	 * Return current vitality points in integer format
	 */
	public int getVitalityPoints()
	{
		return (int) _vitalityPoints;
	}
	
	/*
	 * Set current vitality points to this value
	 * 
	 * if quiet = true - does not send system messages
	 */
	public void setVitalityPoints(int points, boolean quiet)
	{
		points = Math.min(Math.max(points, MIN_VITALITY_POINTS), MAX_VITALITY_POINTS);
		if (points == _vitalityPoints)
			return;
		
		_vitalityPoints = points;
		updateVitalityLevel(quiet);
		getActiveChar().sendPacket(new ExVitalityPointInfo(getVitalityPoints()));
	}
	
	public synchronized void updateVitalityPoints(float points, boolean useRates, boolean quiet)
	{
		if (points == 0 || !Config.ENABLE_VITALITY)
			return;
		
		if (useRates)
		{
			if (getActiveChar().isLucky())
				return;
			
			if (points < 0) // vitality consumed
			{
				int stat = (int) calcStat(Stats.VITALITY_CONSUME_RATE, 1, getActiveChar(), null);
				if (getActiveChar().isAdventBlessingActive()) //Add NevitAdvent by pmq
					stat -= 10; //Add NevitAdvent by pmq
				if (stat == 0) // is vitality consumption stopped ?
					return;
				if (stat < 0) // is vitality gained ?
					points = -points;
			}
			
			if (points > 0)
			{
				// vitality increased
				points *= Config.RATE_VITALITY_GAIN;
			}
			else
			{
				// vitality decreased
				points *= Config.RATE_VITALITY_LOST;
			}
		}
		
		if (points > 0)
		{
			points = Math.min(_vitalityPoints + points, MAX_VITALITY_POINTS);
		}
		else
		{
			points = Math.max(_vitalityPoints + points, MIN_VITALITY_POINTS);
		}
		
		if (points == _vitalityPoints)
			return;
		
		_vitalityPoints = points;
		updateVitalityLevel(quiet);
	}
	
	
	public double getVitalityMultiplier()
	{
		double vitality = 1.0;
		
		if (Config.ENABLE_VITALITY)
		{
			switch (getVitalityLevel())
			{
				case 1:
					vitality = Config.RATE_VITALITY_LEVEL_1;
					break;
				case 2:
					vitality = Config.RATE_VITALITY_LEVEL_2;
					break;
				case 3:
					vitality = Config.RATE_VITALITY_LEVEL_3;
					break;
				case 4:
					vitality = Config.RATE_VITALITY_LEVEL_4;
					break;
			}
		}
		
		return vitality;
	}
	
	/**
	 * @return the _vitalityLevel
	 */
	public byte getVitalityLevel()
	{
		return _vitalityLevel;
	}
	
	public double getExpBonusMultiplier()
	{
		double bonus = 1.0;
		double vitality = 1.0;
		double nevits = 1.0;
		double hunting = 1.0;
		double bonusExp = 1.0;
		
		// Bonus from Vitality System
		vitality = getVitalityMultiplier();
		
		// Bonus from Nevit's Blessing
		nevits = RecoBonus.getRecoMultiplier(getActiveChar());
		
		// Bonus from Nevit's Hunting
		// TODO: Nevit's hunting bonus
		
		// Bonus exp from skills
		bonusExp = calcStat(Stats.BONUS_EXP, 1.0, null, null);
		
		if (vitality > 1.0)
			bonus += (vitality - 1);
		if (nevits > 1.0)
			bonus += (nevits - 1);
		if (hunting > 1.0)
			bonus += (hunting - 1);
		if (bonusExp > 1.0)
			bonus += (bonusExp -1);
		
		// Check for abnormal bonuses
		bonus = Math.max(bonus, 1);
		bonus = Math.min(bonus, Config.MAX_BONUS_EXP);
		
		if (getActiveChar().isDebug())
		{
			getActiveChar().sendDebugMessage("Vitality Multiplier: " + vitality);
			getActiveChar().sendDebugMessage("Nevit's Multiplier: " + nevits);
			getActiveChar().sendDebugMessage("Hunting Multiplier: " + hunting);
			getActiveChar().sendDebugMessage("Bonus Multiplier: " + bonusExp);
			getActiveChar().sendDebugMessage("Total Exp Multiplier: " + bonus);
		}
		
		return bonus;
	}
	
	public double getSpBonusMultiplier()
	{
		double bonus = 1.0;
		double vitality = 1.0;
		double nevits = 1.0;
		double hunting = 1.0;
		double bonusSp = 1.0;
		
		// Bonus from Vitality System
		vitality = getVitalityMultiplier();
		
		// Bonus from Nevit's Blessing
		nevits = RecoBonus.getRecoMultiplier(getActiveChar());
		
		// Bonus from Nevit's Hunting
		// TODO: Nevit's hunting bonus
		
		// Bonus sp from skills
		bonusSp = calcStat(Stats.BONUS_SP, 1.0, null, null);
		
		if (vitality > 1.0)
			bonus += (vitality - 1);
		if (nevits > 1.0)
			bonus += (nevits - 1);
		if (hunting > 1.0)
			bonus += (hunting - 1);
		if (bonusSp > 1.0)
			bonus += (bonusSp -1);
		
		// Check for abnormal bonuses
		bonus = Math.max(bonus, 1);
		bonus = Math.min(bonus, Config.MAX_BONUS_SP);
		
		if (getActiveChar().isDebug())
		{
			getActiveChar().sendDebugMessage("Vitality Multiplier: " + vitality);
			getActiveChar().sendDebugMessage("Nevit's Multiplier: " + nevits);
			getActiveChar().sendDebugMessage("Hunting Multiplier: " + hunting);
			getActiveChar().sendDebugMessage("Bonus Multiplier: " + bonusSp);
			getActiveChar().sendDebugMessage("Total Sp Multiplier: " + bonus);
		}
		
		return bonus;
	}
	
	/**
	 * Listeners
	 */
	/**
	 * Fires all the level change listeners, if any.
	 * @param value
	 */
	private void fireLevelChangeListeners(byte value)
	{
		if (!levelListeners.isEmpty() || !globalLevelListeners.isEmpty())
		{
			PlayerLevelChangeEvent event = new PlayerLevelChangeEvent();
			event.setPlayer(getActiveChar());
			event.setOldLevel(getLevel());
			event.setNewLevel(getLevel() + value);
			for (PlayerLevelListener listener : levelListeners)
			{
				listener.levelChanged(event);
			}
			for (PlayerLevelListener listener : globalLevelListeners)
			{
				listener.levelChanged(event);
			}
		}
	}
	
	/**
	 * Adds a global player level listener
	 * @param listener
	 */
	public static void addGlobalLevelListener(PlayerLevelListener listener)
	{
		if (!globalLevelListeners.contains(listener))
		{
			globalLevelListeners.add(listener);
		}
	}
	
	/**
	 * Removes a global player level listener
	 * @param listener
	 */
	public static void removeGlobalLevelListener(PlayerLevelListener listener)
	{
		globalLevelListeners.remove(listener);
	}
	
	/**
	 * Adds a player level listener
	 * @param listener
	 */
	public void addLevelListener(PlayerLevelListener listener)
	{
		if (!levelListeners.contains(listener))
		{
			levelListeners.add(listener);
		}
	}
	
	/**
	 * Removes a player level listener
	 * @param listener
	 */
	public void removeLevelListener(PlayerLevelListener listener)
	{
		levelListeners.remove(listener);
	}
    
    /**
     * Sets exp holded by the character on log in
     * @param value
     */
    public void setFirstExp(long value)
    {
        _firstExp = value;
    }
    
    /**
     * Will return true if the player has gained exp
     * since logged in
     * @return
     */
    public boolean hasEarnedExp()
    {
        if(getExp() - _firstExp != 0)
            return true;
        return false;
    }
}
