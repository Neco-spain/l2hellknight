package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.skills.Calculator;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;

public class CharStat
{
	// =========================================================
	// Data Field
	private L2Character _activeChar;
	private long _exp = 0;
	private int _sp = 0;
	private byte _level = 1;

	// =========================================================
	// Constructor
	public CharStat(L2Character activeChar)
	{
		_activeChar = activeChar;
	}

	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
			return init;

		int id = stat.ordinal();

		Calculator c = _activeChar.getCalculators()[id];

		// If no Func object found, no modifier is applied
		if (c == null || c.size() == 0)
			return init;

		// Create and init an Env object to pass parameters to the Calculator
		Env env = new Env();
		env.player = _activeChar;
		env.target = target;
		env.skill = skill;
		env.value = init;

		// Launch the calculation
		c.calc(env);
		// avoid some troubles with negative stats (some stats should never be
		// negative)
		if (env.value <= 0
				&& ((stat == Stats.MAX_HP) || (stat == Stats.MAX_MP) || (stat == Stats.MAX_CP) || (stat == Stats.MAGIC_DEFENCE)
						|| (stat == Stats.POWER_DEFENCE) || (stat == Stats.POWER_ATTACK) || (stat == Stats.MAGIC_ATTACK) || (stat == Stats.POWER_ATTACK_SPEED)
						|| (stat == Stats.MAGIC_ATTACK_SPEED) || (stat == Stats.SHIELD_DEFENCE) || (stat == Stats.STAT_CON) || (stat == Stats.STAT_DEX)
						|| (stat == Stats.STAT_INT) || (stat == Stats.STAT_MEN) || (stat == Stats.STAT_STR) || (stat == Stats.STAT_WIT)))
		{
			env.value = 1;
		}

		return env.value;
	}

	public int getAccuracy()
	{
		if (_activeChar == null)
			return 0;

		return (int) (calcStat(Stats.ACCURACY_COMBAT, 0, null, null) / _activeChar.getWeaponExpertisePenalty());
	}

	public L2Character getActiveChar()
	{
		return _activeChar;
	}

	/**
	 * Return the Attack Speed multiplier (base+modifier) of the L2Character to
	 * get proper animations.
	 */
	public final float getAttackSpeedMultiplier()
	{
		if (_activeChar == null)
			return 1;

		return (float) ((1.1) * getPAtkSpd() / _activeChar.getTemplate().basePAtkSpd);
	}

	/** Return the CON of the L2Character (base+modifier). */
	public final int getCON()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.STAT_CON, _activeChar.getTemplate().baseCON, null, null);
	}

	/** Return the Critical Damage rate (base+modifier) of the L2Character. */
	public final double getCriticalDmg(L2Character target, double init)
	{
		return calcStat(Stats.CRITICAL_DAMAGE, init, target, null);
	}

	/** Return the Critical Hit rate (base+modifier) of the L2Character. */
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
			return 1;

		int criticalHit = (int) calcStat(Stats.CRITICAL_RATE, _activeChar.getTemplate().baseCritRate, target, skill);

		// Set a cap of Critical Hit at 500
		if (criticalHit > Config.ALT_PCRITICAL_CAP)
			criticalHit = Config.ALT_PCRITICAL_CAP;

		return criticalHit;
	}

	/** Return the DEX of the L2Character (base+modifier). */
	public final int getDEX()
	{
    	if (_activeChar == null)
    		return 1;

		return (int) calcStat(Stats.STAT_DEX, _activeChar.getTemplate().baseDEX, null, null);
	}

	/** Return the Attack Evasion rate (base+modifier) of the L2Character. */
	public int getEvasionRate(L2Character target)
	{
    	if (_activeChar == null)
    		return 1;

		return (int) (calcStat(Stats.EVASION_RATE, 0, target, null) / _activeChar.getArmourExpertisePenalty());
	}

	public long getExp()
	{
		return _exp;
	}

	public void setExp(long value)
	{
		_exp = value;
	}

	/** Return the INT of the L2Character (base+modifier). */
	public int getINT()
	{
    	if (_activeChar == null)
    		return 1;

		return (int) calcStat(Stats.STAT_INT, _activeChar.getTemplate().baseINT, null, null);
	}

	public byte getLevel()
	{
		return _level;
	}

	public void setLevel(byte value)
	{
		_level = value;
	}

	/** Return the Magical Attack range (base+modifier) of the L2Character. */
	public final int getMagicalAttackRange(L2Skill skill)
	{
		if (skill != null)
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);

    	if (_activeChar == null)
    		return 1;

		return _activeChar.getTemplate().baseAtkRange;
	}

	public int getMaxCp()
	{
    	if (_activeChar == null)
    		return 1;

		return (int) calcStat(Stats.MAX_CP, _activeChar.getTemplate().baseCpMax, null, null);
	}

	public int getMaxHp()
	{
    	if (_activeChar == null)
    		return 1;

		return (int) calcStat(Stats.MAX_HP, _activeChar.getTemplate().baseHpMax, null, null);
	}

	public int getMaxMp()
	{
    	if (_activeChar == null)
    		return 1;

		return (int) calcStat(Stats.MAX_MP, _activeChar.getTemplate().baseMpMax, null, null);
	}

	public int getMAtk(L2Character target, L2Skill skill)
	{
    	if (_activeChar == null)
    		return 1;
		float bonusAtk = 1;
		if (Config.CHAMPION_ENABLE && _activeChar.isChampion())
			bonusAtk = Config.CHAMPION_ATK;
    	double attack = _activeChar.getTemplate().baseMAtk*bonusAtk;
		// Get the skill type to calculate its effect in function of base stats
		// of the L2Character target
		Stats stat = skill == null ? null : skill.getStat();

		if (stat != null)
		{
			switch (stat)
			{
			case AGGRESSION:
				attack += _activeChar.getTemplate().baseAggression;
				break;
			case BLEED:
				attack += _activeChar.getTemplate().baseBleed;
				break;
			case POISON:
				attack += _activeChar.getTemplate().basePoison;
				break;
			case STUN:
				attack += _activeChar.getTemplate().baseStun;
				break;
			case ROOT:
				attack += _activeChar.getTemplate().baseRoot;
				break;
			case MOVEMENT:
				attack += _activeChar.getTemplate().baseMovement;
				break;
			case CONFUSION:
				attack += _activeChar.getTemplate().baseConfusion;
				break;
			case SLEEP:
				attack += _activeChar.getTemplate().baseSleep;
				break;
			case FIRE:
				attack += _activeChar.getTemplate().baseFire;
				break;
			case WIND:
				attack += _activeChar.getTemplate().baseWind;
				break;
			case WATER:
				attack += _activeChar.getTemplate().baseWater;
				break;
			case EARTH:
				attack += _activeChar.getTemplate().baseEarth;
				break;
			case HOLY:
				attack += _activeChar.getTemplate().baseHoly;
				break;
			case DARK:
				attack += _activeChar.getTemplate().baseDark;
				break;
			}
		}

		// Add the power of the skill to the attack effect
		if (skill != null)
			attack += skill.getPower();

		// Calculate modifiers Magic Attack
		return (int) calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
	}

	/**
	 * Return the MAtk Speed (base+modifier) of the L2Character in function of
	 * the Armour Expertise Penalty.
	 */
	public int getMAtkSpd()
	{
    	if (_activeChar == null)
    		return 1;
		float bonusSpdAtk = 1;
		if (Config.CHAMPION_ENABLE && _activeChar.isChampion())
			bonusSpdAtk = Config.CHAMPION_SPD_ATK;
		double val = calcStat(Stats.MAGIC_ATTACK_SPEED, _activeChar.getTemplate().baseMAtkSpd*bonusSpdAtk, null, null);
		val /= _activeChar.getArmourExpertisePenalty();
		return (int) val;
	}

	/** Return the Magic Critical Hit rate (base+modifier) of the L2Character. */
	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
			return 1;
		double baseMCritRate = 0;
		int baseWIT = _activeChar.getWIT();
		
		if (baseWIT >= 31)
			baseMCritRate = 13.6;
		else if (baseWIT == 30)
			baseMCritRate = 13.0;
		else if (baseWIT == 29)
			baseMCritRate = 12.4;
		else if (baseWIT == 28)
			baseMCritRate = 11.8;
		else if (baseWIT == 27)
			baseMCritRate = 11.2;
		else if (baseWIT == 26)
			baseMCritRate = 10.7;
		else if (baseWIT == 25)
			baseMCritRate = 10.2;
		else if (baseWIT == 24)
			baseMCritRate = 9.7;
		else if (baseWIT == 23)
			baseMCritRate = 9.2;
		else if (baseWIT == 22)
			baseMCritRate = 8.8;
		else if (baseWIT == 21)
			baseMCritRate = 8.4;
		else if (baseWIT == 20)
			baseMCritRate = 8;
		else if (baseWIT == 19)
			baseMCritRate = 7.6;
		else if (baseWIT == 18)
			baseMCritRate = 7.2;
		else if (baseWIT == 17)
			baseMCritRate = 6.8;
		else if (baseWIT == 16)
			baseMCritRate = 6.5;
		else if (baseWIT == 15)
			baseMCritRate = 6.2;
		else if (baseWIT == 14)
			baseMCritRate = 6;
		else if (baseWIT == 13)
			baseMCritRate = 5.7;
		else if (baseWIT == 12)
			baseMCritRate = 5.4;
		else if (baseWIT == 11)
			baseMCritRate = 5.1;
		else if (baseWIT == 10)
			baseMCritRate = 4.8;
		else if (baseWIT == 9)
			baseMCritRate = 4.6;
		else if (baseWIT == 8)
			baseMCritRate = 4.4;
		else if (baseWIT == 7)
			baseMCritRate = 4.2;
		else if (baseWIT == 6)
			baseMCritRate = 4;
		else if (baseWIT == 5)
			baseMCritRate = 3.8;
		else if (baseWIT == 4)
			baseMCritRate = 3.6;
		else if (baseWIT == 3)
			baseMCritRate = 3.5;
		else if (baseWIT == 2)
			baseMCritRate = 3.3;
		else if (baseWIT == 1)
			baseMCritRate = 3.2;


		double mrate = calcStat(Stats.MCRITICAL_RATE,  baseMCritRate*Config.M_CRIT_CHANCE, target, skill);
		if (mrate > Config.ALT_MCRITICAL_CAP)
			mrate = Config.ALT_MCRITICAL_CAP;
		return (int)mrate;
	}

	public int getMDef(L2Character target, L2Skill skill)
	{
    	if (_activeChar == null)
    		return 1;

		// Get the base MAtk of the L2Character
		double defence = _activeChar.getTemplate().baseMDef;

		// Calculate modifier for Raid Bosses
		if (_activeChar.isRaid())
			defence *= Config.RAID_DEFENCE_MULTIPLIER;

		// Calculate modifiers Magic Attack
		return (int) calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
	}

	/** Return the MEN of the L2Character (base+modifier). */
	public final int getMEN()
	{
    	if (_activeChar == null)
    		return 1;

		return (int) calcStat(Stats.STAT_MEN, _activeChar.getTemplate().baseMEN, null, null);
	}

	public final float getMovementSpeedMultiplier()
	{
    	if (_activeChar == null)
    		return 1;

		return getRunSpeed() * 1f / _activeChar.getTemplate().baseRunSpd;
	}

	/**
	 * Return the RunSpeed (base+modifier) or WalkSpeed (base+modifier) of the
	 * L2Character in function of the movement type.
	 */
	public final float getMoveSpeed()
	{
    	if (_activeChar == null)
    		return 1;

		if (_activeChar.isRunning())
			return getRunSpeed();
		return getWalkSpeed();
	}

	/** Return the MReuse rate (base+modifier) of the L2Character. */
	public final double getMReuseRate(L2Skill skill)
	{
    	if (_activeChar == null)
    		return 1;

		return calcStat(Stats.MAGIC_REUSE_RATE, _activeChar.getTemplate().baseMReuseRate, null, skill);
	}

	public final double getPReuseRate(L2Skill skill)
	{
		if (_activeChar == null)
			return 1;

		return calcStat(Stats.P_REUSE, _activeChar.getTemplate().baseMReuseRate, null, skill);
	}

	/** Return the PAtk (base+modifier) of the L2Character. */
	public int getPAtk(L2Character target)
	{
    	if (_activeChar == null)
    		return 1;
		float bonusAtk = 1;
		if (Config.CHAMPION_ENABLE && _activeChar.isChampion())
			bonusAtk = Config.CHAMPION_ATK;
		return (int) calcStat(Stats.POWER_ATTACK, _activeChar.getTemplate().basePAtk*bonusAtk, target, null);
	}

	/** Return the PAtk Modifier against animals. */
	public final double getPAtkAnimals(L2Character target)
	{
		return calcStat(Stats.PATK_ANIMALS, 1, target, null);
	}

	/** Return the PAtk Modifier against dragons. */
	public final double getPAtkDragons(L2Character target)
	{
		return calcStat(Stats.PATK_DRAGONS, 1, target, null);
	}

	/** Return the PAtk Modifier against insects. */
	public final double getPAtkInsects(L2Character target)
	{
		return calcStat(Stats.PATK_INSECTS, 1, target, null);
	}

	/** Return the PAtk Modifier against monsters. */
	public final double getPAtkMonsters(L2Character target)
	{
		return calcStat(Stats.PATK_MONSTERS, 1, target, null);
	}

	/** Return the PAtk Modifier against plants. */
	public final double getPAtkPlants(L2Character target)
	{
		return calcStat(Stats.PATK_PLANTS, 1, target, null);
	}

	/**
	 * Return the PAtk Speed (base+modifier) of the L2Character in function of
	 * the Armour Expertise Penalty.
	 */
	public int getPAtkSpd()
	{
    	if (_activeChar == null)
    		return 1;
		float bonusAtk = 1;
		if (Config.CHAMPION_ENABLE && _activeChar.isChampion())
			bonusAtk = Config.CHAMPION_SPD_ATK;
		return (int) (calcStat(Stats.POWER_ATTACK_SPEED, _activeChar.getTemplate().basePAtkSpd*bonusAtk, null, null) / _activeChar.getArmourExpertisePenalty());
	}

	/** Return the PAtk Modifier against undead. */
	public final double getPAtkUndead(L2Character target)
	{
		return calcStat(Stats.PATK_UNDEAD, 1, target, null);
	}

	public final double getPDefUndead(L2Character target)
	{
		return calcStat(Stats.PDEF_UNDEAD, 1, target, null);
	}

	/** Return the PDef Modifier against animals. */
	public final double getPDefAnimals(L2Character target)
	{
		return calcStat(Stats.PDEF_ANIMALS, 1, target, null);
	}

	/** Return the PDef Modifier against dragons. */
	public final double getPDefDragons(L2Character target)
	{
		return calcStat(Stats.PDEF_DRAGONS, 1, target, null);
	}

	/** Return the PDef Modifier against insects. */
	public final double getPDefInsects(L2Character target)
	{
		return calcStat(Stats.PDEF_INSECTS, 1, target, null);
	}

	/** Return the PDef Modifier against monsters. */
	public final double getPDefMonsters(L2Character target)
	{
		return calcStat(Stats.PDEF_MONSTERS, 1, target, null);
	}

	/** Return the PDef Modifier against plants. */
	public final double getPDefPlants(L2Character target)
	{
		return calcStat(Stats.PDEF_PLANTS, 1, target, null);
	}

	/** Return the PDef Modifier against giants. */
	public final double getPDefGiants(L2Character target)
	{
		return calcStat(Stats.PDEF_GIANTS, 1, target, null);
	}

	/** Return the PDef (base+modifier) of the L2Character. */
	public int getPDef(L2Character target)
	{
    	if (_activeChar == null)
    		return 1;

		return (int) calcStat(Stats.POWER_DEFENCE, (_activeChar.isRaid()) ? _activeChar.getTemplate().basePDef * Config.RAID_DEFENCE_MULTIPLIER : _activeChar.getTemplate().basePDef, target, null);
	}

	/** Return the Physical Attack range (base+modifier) of the L2Character. */
	public final int getPhysicalAttackRange()
	{
    	if (_activeChar == null)
    		return 1;

		return (int) calcStat(Stats.POWER_ATTACK_RANGE, _activeChar.getTemplate().baseAtkRange, null, null);
	}

	/** Return the Skill/Spell reuse modifier. */
	public final double getReuseModifier(L2Character target)
	{
		return calcStat(Stats.ATK_REUSE, 1, target, null);
	}

	/**
	 * Return the RunSpeed (base+modifier) of the L2Character in function of the
	 * Armour Expertise Penalty.
	 */
	public int getRunSpeed()
	{
    	if (_activeChar == null)
    		return 1;

		// err we should be adding TO the persons run speed
		// not making it a constant
		int val =(int) calcStat(Stats.RUN_SPEED, _activeChar.getTemplate().baseRunSpd, null, null);

		if (_activeChar.isFlying())
		{
			val += Config.WYVERN_SPEED;
			return val;
		}
		if (_activeChar.isRiding())
		{
			val += Config.STRIDER_SPEED;
			return val;
		}

		if (_activeChar instanceof L2PlayableInstance && _activeChar.isInsideZone(L2Character.ZONE_WATER))
			val /= 2;

		if (_activeChar instanceof L2PlayableInstance && _activeChar.isInsideZone(L2Character.ZONE_SWAMP))
			val /= 2;

		val /= _activeChar.getArmourExpertisePenalty();
		return val;
	}

	/** Return the ShieldDef rate (base+modifier) of the L2Character. */
	public final int getShldDef()
	{
		return (int) calcStat(Stats.SHIELD_DEFENCE, 0, null, null);
	}

	public int getSp()
	{
		return _sp;
	}

	public void setSp(int value)
	{
		_sp = value;
	}

	/** Return the STR of the L2Character (base+modifier). */
	public final int getSTR()
	{
    	if (_activeChar == null)
    		return 1;

		return (int) calcStat(Stats.STAT_STR, _activeChar.getTemplate().baseSTR, null, null);
	}

	/** Return the WalkSpeed (base+modifier) of the L2Character. */
	public final int getWalkSpeed()
	{
    	
		if (_activeChar == null)
    		return 1;

		if(_activeChar instanceof L2PcInstance )
		{
			return (getRunSpeed() * 70) / 100;	
		}
		else
		{	
			return (int) calcStat(Stats.WALK_SPEED, _activeChar.getTemplate().baseWalkSpd, null, null);
		}
		
	}

	/** Return the WIT of the L2Character (base+modifier). */
	public final int getWIT()
	{
    	if (_activeChar == null)
    		return 1;

		return (int) calcStat(Stats.STAT_WIT, _activeChar.getTemplate().baseWIT, null, null);
	}

	/** Return the mpConsume. */
	public final int getMpConsume(L2Skill skill)
	{
    	if (skill == null)
    		return 1;
		int mpconsume = skill.getMpConsume();
		if (skill.isDance() && _activeChar != null && _activeChar.getDanceCount() > 0)
			mpconsume += _activeChar.getDanceCount() * skill.getNextDanceMpCost();
		return (int) calcStat(Stats.MP_CONSUME, mpconsume, null, skill);
	}

	/** Return the mpInitialConsume. */
	public final int getMpInitialConsume(L2Skill skill)
	{
    	if (skill == null)
    		return 1;

		return (int) calcStat(Stats.MP_CONSUME, skill.getMpInitialConsume(), null, skill);
	}
}
