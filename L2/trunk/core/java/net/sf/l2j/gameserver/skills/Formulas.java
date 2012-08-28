//L2E
package net.sf.l2j.gameserver.skills;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.menu;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.CustomZoneManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2CubicInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerState;
import net.sf.l2j.gameserver.skills.conditions.ConditionUsingItemType;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerState.CheckPlayerState;
import net.sf.l2j.gameserver.skills.funcs.Func;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public final class Formulas
{
	protected static final Logger _log = Logger.getLogger(L2Character.class.getName());
	private static final int HP_REGENERATE_PERIOD = 3000; // 3 secs

	public static final int MAX_STAT_VALUE = 100;

    private static final double[] STRCompute = new double[]{1.036, 34.845}; //{1.016, 28.515}; for C1
    private static final double[] INTCompute = new double[]{1.020, 31.375}; //{1.020, 31.375}; for C1
    private static final double[] DEXCompute = new double[]{1.009, 19.360}; //{1.009, 19.360}; for C1
    private static final double[] WITCompute = new double[]{1.050, 20.000}; //{1.050, 20.000}; for C1
    private static final double[] CONCompute = new double[]{1.030, 27.632}; //{1.015, 12.488}; for C1
    private static final double[] MENCompute = new double[]{1.010, -0.060}; //{1.010, -0.060}; for C1

    protected static final double[] WITbonus = new double[MAX_STAT_VALUE];
    protected static final double[] MENbonus = new double[MAX_STAT_VALUE];
    protected static final double[] INTbonus = new double[MAX_STAT_VALUE];
    protected static final double[] STRbonus = new double[MAX_STAT_VALUE];
    protected static final double[] DEXbonus = new double[MAX_STAT_VALUE];
    protected static final double[] CONbonus = new double[MAX_STAT_VALUE];
	
    static
    {
        for (int i=0; i < STRbonus.length; i++)
            STRbonus[i] = Math.floor(Math.pow(STRCompute[0], i - STRCompute[1]) *100 +.5d) /100;
        for (int i=0; i < INTbonus.length; i++)
            INTbonus[i] = Math.floor(Math.pow(INTCompute[0], i - INTCompute[1]) *100 +.5d) /100;
        for (int i=0; i < DEXbonus.length; i++)
            DEXbonus[i] = Math.floor(Math.pow(DEXCompute[0], i - DEXCompute[1]) *100 +.5d) /100;
        for (int i=0; i < WITbonus.length; i++)
            WITbonus[i] = Math.floor(Math.pow(WITCompute[0], i - WITCompute[1]) *100 +.5d) /100;
        for (int i=0; i < CONbonus.length; i++)
            CONbonus[i] = Math.floor(Math.pow(CONCompute[0], i - CONCompute[1]) *100 +.5d) /100;
        for (int i=0; i < MENbonus.length; i++)
            MENbonus[i] = Math.floor(Math.pow(MENCompute[0], i - MENCompute[1]) *100 +.5d) /100;
    }

	static class FuncAddLevel3 extends Func
	{
		static final FuncAddLevel3[] _instancies = new FuncAddLevel3[Stats.NUM_STATS];

		static Func getInstance(Stats stat)
		{
			int pos = stat.ordinal();
			if (_instancies[pos] == null) _instancies[pos] = new FuncAddLevel3(stat);
			return _instancies[pos];
		}

		private FuncAddLevel3(Stats pStat)
		{
			super(pStat, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value += env.player.getLevel() / 3.0;
		}
	}

	static class FuncMultLevelMod extends Func
	{
		static final FuncMultLevelMod[] _instancies = new FuncMultLevelMod[Stats.NUM_STATS];

		static Func getInstance(Stats stat)
		{
			int pos = stat.ordinal();
			if (_instancies[pos] == null) _instancies[pos] = new FuncMultLevelMod(stat);
			return _instancies[pos];
		}

		private FuncMultLevelMod(Stats pStat)
		{
			super(pStat, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= env.player.getLevelMod();
		}
	}

	static class FuncMultRegenResting extends Func
	{
		static final FuncMultRegenResting[] _instancies = new FuncMultRegenResting[Stats.NUM_STATS];

		static Func getInstance(Stats stat)
		{
			int pos = stat.ordinal();

			if (_instancies[pos] == null) _instancies[pos] = new FuncMultRegenResting(stat);

			return _instancies[pos];
		}

		private FuncMultRegenResting(Stats pStat)
		{
			super(pStat, 0x20, null);
			setCondition(new ConditionPlayerState(CheckPlayerState.RESTING, true));
		}

		@Override
		public void calc(Env env)
		{
			if (!cond.test(env))
				return;
			env.value *= 1.5;
		}
	}

	static class FuncPAtkMod extends Func
	{
		static final FuncPAtkMod _fpa_instance = new FuncPAtkMod();

		static Func getInstance()
		{
			return _fpa_instance;
		}

		private FuncPAtkMod()
		{
			super(Stats.POWER_ATTACK, 0x30, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= STRbonus[env.player.getSTR()] * env.player.getLevelMod();
		}
	}

	static class FuncMAtkMod extends Func
	{
		static final FuncMAtkMod _fma_instance = new FuncMAtkMod();

		static Func getInstance()
		{
			return _fma_instance;
		}

		private FuncMAtkMod()
		{
			super(Stats.MAGIC_ATTACK, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			double intb = INTbonus[env.player.getINT()];
			double lvlb = env.player.getLevelMod();
			env.value *= (lvlb * lvlb) * (intb * intb);
		}
	}

	static class FuncMDefMod extends Func
	{
		static final FuncMDefMod _fmm_instance = new FuncMDefMod();

		static Func getInstance()
		{
			return _fmm_instance;
		}

		private FuncMDefMod()
		{
			super(Stats.MAGIC_DEFENCE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
            if (env.player instanceof L2PcInstance)
            {
    			L2PcInstance p = (L2PcInstance) env.player;
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) != null) env.value -= 5;
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) != null) env.value -= 5;
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) != null) env.value -= 9;
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) != null) env.value -= 9;
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) != null) env.value -= 13;
            }
			env.value *= MENbonus[env.player.getMEN()] * env.player.getLevelMod();
		}
	}

	static class FuncPDefMod extends Func
	{
		static final FuncPDefMod _fmm_instance = new FuncPDefMod();

		static Func getInstance()
		{
			return _fmm_instance;
		}

		private FuncPDefMod()
		{
			super(Stats.POWER_DEFENCE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			if (env.player instanceof L2PcInstance)
            {
                L2PcInstance p = (L2PcInstance) env.player;
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) != null) env.value -= 12;
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST) != null)
    				env.value -= ((p.getClassId().isMage()) ? 15 : 31);
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) != null)
    				env.value -= ((p.getClassId().isMage()) ? 8 : 18);
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) != null) env.value -= 8;
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) != null) env.value -= 7;
            }
			env.value *= env.player.getLevelMod();
		}
	}

	static class FuncBowAtkRange extends Func
	{
		private static final FuncBowAtkRange _fbar_instance = new FuncBowAtkRange();

		static Func getInstance()
		{
			return _fbar_instance;
		}

		private FuncBowAtkRange()
		{
			super(Stats.POWER_ATTACK_RANGE, 0x10, null);
			setCondition(new ConditionUsingItemType(L2WeaponType.BOW.mask()));
		}

		@Override
		public void calc(Env env)
		{
			if (!cond.test(env)) return;
			env.value += 450;
		}
	}

	static class FuncAtkAccuracy extends Func
	{
		static final FuncAtkAccuracy _faa_instance = new FuncAtkAccuracy();

		static Func getInstance()
		{
			return _faa_instance;
		}

		private FuncAtkAccuracy()
		{
			super(Stats.ACCURACY_COMBAT, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Character p = env.player;
			//[Square(DEX)]*6 + lvl + weapon hitbonus;
			env.value += Math.sqrt(p.getDEX()) * 6;
			env.value += p.getLevel();
			if( p instanceof L2Summon) env.value += (p.getLevel() < 60) ? 4 : 5;
		}
	}

	static class FuncAtkEvasion extends Func
	{
		static final FuncAtkEvasion _fae_instance = new FuncAtkEvasion();

		static Func getInstance()
		{
			return _fae_instance;
		}

		private FuncAtkEvasion()
		{
			super(Stats.EVASION_RATE, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Character p = env.player;
			//[Square(DEX)]*6 + lvl;
			env.value += Math.sqrt(p.getDEX()) * 6;
			env.value += p.getLevel();
		}
	}

	static class FuncAtkCritical extends Func
	{
		static final FuncAtkCritical _fac_instance = new FuncAtkCritical();

		static Func getInstance()
		{
			return _fac_instance;
		}

		private FuncAtkCritical()
		{
			super(Stats.CRITICAL_RATE, 0x30, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Character p = env.player;
			if( p instanceof L2Summon) env.value = 40;
			else if (p instanceof L2PcInstance && p.getActiveWeaponInstance() == null) env.value = 40;
			else
			{
				env.value *= DEXbonus[p.getDEX()];
				env.value *= 10;

			}
			env.baseValue = env.value;
		}
	}

	static class FuncMoveSpeed extends Func
	{
		static final FuncMoveSpeed _fms_instance = new FuncMoveSpeed();

		static Func getInstance()
		{
			return _fms_instance;
		}

		private FuncMoveSpeed()
		{
			super(Stats.RUN_SPEED, 0x30, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env.player;
			env.value *= DEXbonus[p.getDEX()];
		}
	}

	static class FuncPAtkSpeed extends Func
	{
		static final FuncPAtkSpeed _fas_instance = new FuncPAtkSpeed();

		static Func getInstance()
		{
			return _fas_instance;
		}

		private FuncPAtkSpeed()
		{
			super(Stats.POWER_ATTACK_SPEED, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env.player;
			env.value *= DEXbonus[p.getDEX()];
		}
	}

	static class FuncMAtkSpeed extends Func
	{
		static final FuncMAtkSpeed _fas_instance = new FuncMAtkSpeed();

		static Func getInstance()
		{
			return _fas_instance;
		}

		private FuncMAtkSpeed()
		{
			super(Stats.MAGIC_ATTACK_SPEED, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env.player;
			env.value *= WITbonus[p.getWIT()];
		}
	}

	static class FuncHennaSTR extends Func
	{
		static final FuncHennaSTR _fh_instance = new FuncHennaSTR();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaSTR()
		{
			super(Stats.STAT_STR, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null) env.value += pc.getHennaStatSTR();
		}
	}

	static class FuncHennaDEX extends Func
	{
		static final FuncHennaDEX _fh_instance = new FuncHennaDEX();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaDEX()
		{
			super(Stats.STAT_DEX, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null) env.value += pc.getHennaStatDEX();
		}
	}

	static class FuncHennaINT extends Func
	{
		static final FuncHennaINT _fh_instance = new FuncHennaINT();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaINT()
		{
			super(Stats.STAT_INT, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null) env.value += pc.getHennaStatINT();
		}
	}

	static class FuncHennaMEN extends Func
	{
		static final FuncHennaMEN _fh_instance = new FuncHennaMEN();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaMEN()
		{
			super(Stats.STAT_MEN, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null) env.value += pc.getHennaStatMEN();
		}
	}

	static class FuncHennaCON extends Func
	{
		static final FuncHennaCON _fh_instance = new FuncHennaCON();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaCON()
		{
			super(Stats.STAT_CON, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null) env.value += pc.getHennaStatCON();
		}
	}

	static class FuncHennaWIT extends Func
	{
		static final FuncHennaWIT _fh_instance = new FuncHennaWIT();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaWIT()
		{
			super(Stats.STAT_WIT, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null) env.value += pc.getHennaStatWIT();
		}
	}

	static class FuncMaxHpAdd extends Func
	{
		static final FuncMaxHpAdd _fmha_instance = new FuncMaxHpAdd();

		static Func getInstance()
		{
			return _fmha_instance;
		}

		private FuncMaxHpAdd()
		{
			super(Stats.MAX_HP, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
			int lvl = env.player.getLevel() - t.classBaseLevel;
			double hpmod = t.lvlHpMod * lvl;
			double hpmax = (t.lvlHpAdd + hpmod) * lvl;
			double hpmin = (t.lvlHpAdd * lvl) + hpmod;
			env.value += (hpmax + hpmin) / 2;
		}
	}

	static class FuncMaxHpMul extends Func
	{
		static final FuncMaxHpMul _fmhm_instance = new FuncMaxHpMul();

		static Func getInstance()
		{
			return _fmhm_instance;
		}

		private FuncMaxHpMul()
		{
			super(Stats.MAX_HP, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env.player;
			env.value *= CONbonus[p.getCON()];
		}
	}

	static class FuncMaxCpAdd extends Func
	{
		static final FuncMaxCpAdd _fmca_instance = new FuncMaxCpAdd();

		static Func getInstance()
		{
			return _fmca_instance;
		}

		private FuncMaxCpAdd()
		{
			super(Stats.MAX_CP, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
			int lvl = env.player.getLevel() - t.classBaseLevel;
			double cpmod = t.lvlCpMod * lvl;
			double cpmax = (t.lvlCpAdd + cpmod) * lvl;
			double cpmin = (t.lvlCpAdd * lvl) + cpmod;
			env.value += (cpmax + cpmin) / 2;
		}
	}

	static class FuncMaxCpMul extends Func
	{
		static final FuncMaxCpMul _fmcm_instance = new FuncMaxCpMul();

		static Func getInstance()
		{
			return _fmcm_instance;
		}

		private FuncMaxCpMul()
		{
			super(Stats.MAX_CP, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env.player;
			env.value *= CONbonus[p.getCON()];
		}
	}

	static class FuncMaxMpAdd extends Func
	{
		static final FuncMaxMpAdd _fmma_instance = new FuncMaxMpAdd();

		static Func getInstance()
		{
			return _fmma_instance;
		}

		private FuncMaxMpAdd()
		{
			super(Stats.MAX_MP, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
			int lvl = env.player.getLevel() - t.classBaseLevel;
			double mpmod = t.lvlMpMod * lvl;
			double mpmax = (t.lvlMpAdd + mpmod) * lvl;
			double mpmin = (t.lvlMpAdd * lvl) + mpmod;
			env.value += (mpmax + mpmin) / 2;
		}
	}

	static class FuncMaxMpMul extends Func
	{
		static final FuncMaxMpMul _fmmm_instance = new FuncMaxMpMul();

		static Func getInstance()
		{
			return _fmmm_instance;
		}

		private FuncMaxMpMul()
		{
			super(Stats.MAX_MP, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env.player;
			env.value *= MENbonus[p.getMEN()];
		}
	}

	private static final Formulas _instance = new Formulas();

	public static Formulas getInstance()
	{
		return _instance;
	}

	private Formulas()
	{
	}

	/**
	 * Return the period between 2 regenerations task (3s for L2Character, 5 min for L2DoorInstance).<BR><BR>
	 */
	public int getRegeneratePeriod(L2Character cha)
	{
		if (cha instanceof L2DoorInstance) return HP_REGENERATE_PERIOD * 100; // 5 mins

		return HP_REGENERATE_PERIOD; // 3s
	}

	/**
	 * Return the standard NPC Calculator set containing ACCURACY_COMBAT and EVASION_RATE.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...).
	 * In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <BR><BR>
	 *
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR><BR>
	 *
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 *
	 */
	public Calculator[] getStdNPCCalculators()
	{
		Calculator[] std = new Calculator[Stats.NUM_STATS];

		// Add the FuncAtkAccuracy to the Standard Calculator of ACCURACY_COMBAT
		std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
		std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());

		// Add the FuncAtkEvasion to the Standard Calculator of EVASION_RATE
		std[Stats.EVASION_RATE.ordinal()] = new Calculator();
		std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());

		return std;
	}

	/**
	 * Add basics Func objects to L2PcInstance and L2Summon.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...).
	 * In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <BR><BR>
	 *
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR><BR>
	 *
	 * @param cha L2PcInstance or L2Summon that must obtain basic Func objects
	 */
	public void addFuncsToNewCharacter(L2Character cha)
	{
		if (cha instanceof L2PcInstance)
		{
			cha.addStatFunc(FuncMaxHpAdd.getInstance());
			cha.addStatFunc(FuncMaxHpMul.getInstance());
			cha.addStatFunc(FuncMaxCpAdd.getInstance());
			cha.addStatFunc(FuncMaxCpMul.getInstance());
			cha.addStatFunc(FuncMaxMpAdd.getInstance());
			cha.addStatFunc(FuncMaxMpMul.getInstance());
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_CP_RATE));
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncBowAtkRange.getInstance());
			//cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_ATTACK));
			//cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_DEFENCE));
			//cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.MAGIC_DEFENCE));
			cha.addStatFunc(FuncPAtkMod.getInstance());
			cha.addStatFunc(FuncMAtkMod.getInstance());
            cha.addStatFunc(FuncPDefMod.getInstance());
			cha.addStatFunc(FuncMDefMod.getInstance());
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
			cha.addStatFunc(FuncPAtkSpeed.getInstance());
			cha.addStatFunc(FuncMAtkSpeed.getInstance());
			cha.addStatFunc(FuncMoveSpeed.getInstance());

			cha.addStatFunc(FuncHennaSTR.getInstance());
			cha.addStatFunc(FuncHennaDEX.getInstance());
			cha.addStatFunc(FuncHennaINT.getInstance());
			cha.addStatFunc(FuncHennaMEN.getInstance());
			cha.addStatFunc(FuncHennaCON.getInstance());
			cha.addStatFunc(FuncHennaWIT.getInstance());
		}
        else if (cha instanceof L2PetInstance)
        {
            cha.addStatFunc(FuncPAtkMod.getInstance());
            cha.addStatFunc(FuncMAtkMod.getInstance());
            cha.addStatFunc(FuncPDefMod.getInstance());
            cha.addStatFunc(FuncMDefMod.getInstance());
        }
		else if (cha instanceof L2Summon)
		{
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
		}
	}

	/**
	 * Calculate the HP regen rate (base + modifiers).<BR><BR>
	 */
	public final double calcHpRegen(L2Character cha)
	{
        double init = cha.getTemplate().baseHpReg;
		double hpRegenMultiplier = cha.isRaid() ? Config.RAID_HP_REGEN_MULTIPLIER : Config.HP_REGEN_MULTIPLIER;
		double hpRegenBonus = 0;

		if (cha instanceof L2GrandBossInstance)
		{
			L2GrandBossInstance boss = (L2GrandBossInstance) cha;
			if ((boss.getNpcId() == 29022) && (CustomZoneManager.getInstance().checkIfInZone("SunlightRoom", boss)))
				hpRegenMultiplier *= 0.75;
		}
		
		if (Config.CHAMPION_ENABLE && cha.isChampion())
			hpRegenMultiplier *= Config.CHAMPION_HP_REGEN;

		if (cha instanceof L2PcInstance)
		{
            L2PcInstance player = (L2PcInstance) cha;

            // Calculate correct baseHpReg value for certain level of PC
            init += (player.getLevel() > 10) ? ((player.getLevel()-1)/10.0) : 0.5;

            // SevenSigns Festival modifier
			if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant())
                hpRegenMultiplier *= calcFestivalRegenModifier(player);
			else
			{
				double siegeModifier = calcSiegeRegenModifer(player);
				if (siegeModifier > 0) hpRegenMultiplier *= siegeModifier;
			}

            if (player.isInsideZone(L2Character.ZONE_CLANHALL) && player.getClan() != null)
            {
            	int clanHallIndex = player.getClan().getHasHideout();
            	if (clanHallIndex > 0)
            	{
            		ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
            		if(clansHall != null)
            			if (clansHall.getFunction(ClanHall.FUNC_RESTORE_HP) != null)
            				hpRegenMultiplier *= 1+ clansHall.getFunction(ClanHall.FUNC_RESTORE_HP).getLvl()/100;
            	}
            }

			// Mother Tree effect is calculated at last
			if (player.isInsideZone(L2Character.ZONE_MOTHERTREE)) hpRegenBonus += 2;

            // Calculate Movement bonus
            if (player.isSitting())
				if (player.getLevel() <= 40) hpRegenMultiplier *= 6.0;
				else
					if (player.isSitting()) hpRegenMultiplier *=1.5;
            else if (!player.isMoving()) hpRegenMultiplier *= 1.1;
            else if (player.isRunning()) hpRegenMultiplier *= 0.7;

            // Add CON bonus
            init *= cha.getLevelMod() * CONbonus[cha.getCON()];
		}

        if (init < 1) init = 1;

        return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null) * hpRegenMultiplier + hpRegenBonus;
	}

	/**
	 * Calculate the MP regen rate (base + modifiers).<BR><BR>
	 */
	public final double calcMpRegen(L2Character cha)
	{
        double init = cha.getTemplate().baseMpReg;
        double mpRegenMultiplier = cha.isRaid() ? Config.RAID_MP_REGEN_MULTIPLIER : Config.MP_REGEN_MULTIPLIER;
		double mpRegenBonus = 0;

		if (cha instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) cha;

            // Calculate correct baseMpReg value for certain level of PC
            init += 0.3*((player.getLevel()-1)/10.0);

            // SevenSigns Festival modifier
            if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant())
				mpRegenMultiplier *= calcFestivalRegenModifier(player);

			// Mother Tree effect is calculated at last
			if (player.isInsideZone(L2Character.ZONE_MOTHERTREE)) mpRegenBonus += 1;

            if (player.isInsideZone(L2Character.ZONE_CLANHALL) && player.getClan() != null)
            {
            	int clanHallIndex = player.getClan().getHasHideout();
            	if (clanHallIndex > 0)
            	{
            		ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
            		if(clansHall != null)
            			if (clansHall.getFunction(ClanHall.FUNC_RESTORE_MP) != null)
            				mpRegenMultiplier *= 1+ clansHall.getFunction(ClanHall.FUNC_RESTORE_MP).getLvl()/100;
            	}
            }

			// Calculate Movement bonus
            if (player.isSitting()) mpRegenMultiplier *= 1.5;      // Sitting
            else if (!player.isMoving()) mpRegenMultiplier *= 1.1; // Staying
            else if (player.isRunning()) mpRegenMultiplier *= 0.7; // Running

            // Add MEN bonus
            init *= cha.getLevelMod() * MENbonus[cha.getMEN()];
		}

		if (init < 1) init = 1;

		return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null) * mpRegenMultiplier + mpRegenBonus;
	}

	/**
	 * Calculate the CP regen rate (base + modifiers).<BR><BR>
	 */
	public final double calcCpRegen(L2Character cha)
	{
        double init = cha.getTemplate().baseHpReg;
        double cpRegenMultiplier = Config.CP_REGEN_MULTIPLIER;
        double cpRegenBonus = 0;

        if (cha instanceof L2PcInstance)
        {
	        L2PcInstance player = (L2PcInstance) cha;

	        // Calculate correct baseHpReg value for certain level of PC
	        init += (player.getLevel() > 10) ? ((player.getLevel()-1)/10.0) : 0.5;

	        // Calculate Movement bonus
	        if (player.isSitting()) cpRegenMultiplier *= 1.5;      // Sitting
	        else if (!player.isMoving()) cpRegenMultiplier *= 1.1; // Staying
	        else if (player.isRunning()) cpRegenMultiplier *= 0.7; // Running
        } else
        {
	        // Calculate Movement bonus
        	if (!cha.isMoving()) cpRegenMultiplier *= 1.1; // Staying
	        else if (cha.isRunning()) cpRegenMultiplier *= 0.7; // Running
        }

        // Apply CON bonus
        init *= cha.getLevelMod() * CONbonus[cha.getCON()];
        if (init < 1) init = 1;

        return cha.calcStat(Stats.REGENERATE_CP_RATE, init, null, null) * cpRegenMultiplier + cpRegenBonus;
	}

	@SuppressWarnings("deprecation")
	public final double calcFestivalRegenModifier(L2PcInstance activeChar)
	{
		final int[] festivalInfo = SevenSignsFestival.getInstance().getFestivalForPlayer(activeChar);
		final int oracle = festivalInfo[0];
		final int festivalId = festivalInfo[1];
		int[] festivalCenter;

		// If the player isn't found in the festival, leave the regen rate as it is.
		if (festivalId < 0) return 0;

		// Retrieve the X and Y coords for the center of the festival arena the player is in.
		if (oracle == SevenSigns.CABAL_DAWN) festivalCenter = SevenSignsFestival.FESTIVAL_DAWN_PLAYER_SPAWNS[festivalId];
		else festivalCenter = SevenSignsFestival.FESTIVAL_DUSK_PLAYER_SPAWNS[festivalId];

		// Check the distance between the player and the player spawn point, in the center of the arena.
		double distToCenter = activeChar.getDistance(festivalCenter[0], festivalCenter[1]);

		if (Config.DEBUG)
			_log.info("Distance: " + distToCenter + ", RegenMulti: " + (distToCenter * 2.5) / 50);

		return 1.0 - (distToCenter * 0.0005); // Maximum Decreased Regen of ~ -65%;
	}

	public final double calcSiegeRegenModifer(L2PcInstance activeChar)
	{
		if (activeChar == null || activeChar.getClan() == null) return 0;

		Siege siege = SiegeManager.getInstance().getSiege(activeChar.getPosition().getX(),
															activeChar.getPosition().getY(),
															activeChar.getPosition().getZ());
		if (siege == null || !siege.getIsInProgress()) return 0;

		L2SiegeClan siegeClan = siege.getAttackerClan(activeChar.getClan().getClanId());
		if (siegeClan == null || siegeClan.getFlag().size() == 0
			|| !Util.checkIfInRange(200, activeChar, siegeClan.getFlag().get(0), true)) return 0;

		return 1.5; // If all is true, then modifer will be 50% more
	}
	/** Calculate blow damage based on cAtk */
	public double calcBlowDamage(L2Character attacker, L2Character target, L2Skill skill, boolean shld, boolean ss)
	{
		if (Rnd.chance(target.calcStat(Stats.P_SKILL_EVASION, 0.0D, null, skill)))
	    {
	      return 0.0D;
	    }
		
		double power = skill.getPower();
		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);
		L2Effect vicious = attacker.getFirstEffect(312);
		if(vicious != null && damage > 1)
		{
			for(Func func: vicious.getStatFuncs())
			{
				Env env = new Env();
				env.player = attacker;
				env.target = target;
				env.skill = skill;
				env.value = damage;
				func.calc(env);
				damage = (int)env.value;
			}
		}
		if(ss)
			damage *= 2.;
		if(shld)
			defence += target.getShldDef();
		if(ss && skill.getSSBoost()>0)
			power *= skill.getSSBoost();
		damage += attacker.calcStat(Stats.CRITICAL_DAMAGE, damage+power, target, skill);
		if (attacker.isBehindTarget()) damage=1.5*damage;
		if (target instanceof L2NpcInstance)
		{
			damage *= ((L2NpcInstance) target).getTemplate().getVulnerability(Stats.DAGGER_WPN_VULN);
		}
		damage = target.calcStat(Stats.DAGGER_WPN_VULN, damage, target, null);
		damage *= 70. / defence;
		damage += Rnd.get() * attacker.getRandomDamage(target);
		if (target instanceof L2PcInstance)
		{
			L2Armor armor = ((L2PcInstance)target).getActiveChestArmorItem();
			if (armor != null)
			{
				if(((L2PcInstance)target).isWearingHeavyArmor())
					damage /= Config.BLOW_DAMAGE_HEAVY; // 2
				if(((L2PcInstance)target).isWearingLightArmor())
					damage /= Config.BLOW_DAMAGE_LIGHT; // 1.5
				if(((L2PcInstance)target).isWearingMagicArmor())
					damage /= Config.BLOW_DAMAGE_ROBE;   // 1.3
			}
		}
		return damage < 1 ? 1. : damage;
	}
	/** Calculated damage caused by ATTACK of attacker on target,
	 * called separatly for each weapon, if dual-weapon is used.
	 *
	 * @param attacker player or NPC that makes ATTACK
	 * @param target player or NPC, target of ATTACK
	 * @param miss one of ATTACK_XXX constants
	 * @param crit if the ATTACK have critical success
	 * @param dual if dual weapon is used
	 * @param ss if weapon item was charged by soulshot
	 * @return damage points
	 */
	public final double calcPhysDam(L2Character attacker, L2Character target, L2Skill skill,
									boolean shld, boolean crit, boolean dual, boolean ss)
	{
		if (attacker instanceof L2PcInstance)
		{
			L2PcInstance pcInst = (L2PcInstance)attacker;
			if (pcInst.isGM() && pcInst.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)
					return 0;
		}

		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);
		if (ss) damage *= 2;
		if (skill != null)
		{
			
			if (Rnd.chance(target.calcStat(Stats.P_SKILL_EVASION, 0.0D, null, skill)))
		      {
		        return 0.0D;
		      }
			
			double skillpower = skill.getPower();
			float ssboost = skill.getSSBoost();
			if (ssboost <= 0)
				damage += skillpower;
			else if (ssboost > 0)
			{
				if (ss)
				{
					skillpower *= ssboost;
					damage += skillpower;
				}
				else
					damage += skillpower;
			}
		}
		// In C5 summons make 10 % less dmg in PvP.
		if(attacker instanceof L2Summon && target instanceof L2PcInstance) damage *= 0.9;

		// defence modifier depending of the attacker weapon
		L2Weapon weapon = attacker.getActiveWeaponItem();
		Stats stat = null;
		if (weapon != null)
		{
			switch (weapon.getItemType())
			{
				case BOW:
					stat = Stats.BOW_WPN_VULN;
					break;
				case BLUNT:
				case BIGBLUNT:
					stat = Stats.BLUNT_WPN_VULN;
					break;
				case DAGGER:
					stat = Stats.DAGGER_WPN_VULN;
					break;
				case DUAL:
					stat = Stats.DUAL_WPN_VULN;
					break;
				case DUALFIST:
					stat = Stats.DUALFIST_WPN_VULN;
					break;
				case ETC:
					stat = Stats.ETC_WPN_VULN;
					break;
				case FIST:
					stat = Stats.FIST_WPN_VULN;
					break;
				case POLE:
					stat = Stats.POLE_WPN_VULN;
					break;
				case SWORD:
					stat = Stats.SWORD_WPN_VULN;
					break;
				case BIGSWORD: //TODO: have a proper resistance/vulnerability for Big swords
					stat = Stats.SWORD_WPN_VULN;
					break;
			}
		}


		if (crit) 
		{
			if (skill != null && skill.getStandartCritFormulas())
				damage += attacker.getCriticalDmg(target, damage);
			else
				damage = Config.ALT_CRIT_DAMAGE * (2*(attacker.getCriticalDmg(target, damage)));
		}
		if (shld)
		{
			defence += target.getShldDef();
		}
		//if (!(attacker instanceof L2RaidBossInstance) &&
		/*
		if ((attacker instanceof L2NpcInstance || attacker instanceof L2SiegeGuardInstance))
		{
			if (attacker instanceof L2RaidBossInstance) damage *= 1; // was 10 changed for temp fix
			//			else
			//			damage *= 2;
			//			if (attacker instanceof L2NpcInstance || attacker instanceof L2SiegeGuardInstance){
			//damage = damage * attacker.getSTR() * attacker.getAccuracy() * 0.05 / defence;
			//			damage = damage * attacker.getSTR()*  (attacker.getSTR() + attacker.getLevel()) * 0.025 / defence;
			//			damage += _rnd.nextDouble() * damage / 10 ;
		}
		*/
		//		else {
		//if (skill == null)
		damage = 70 * damage / defence;

		if (stat != null)
		{
			// get the vulnerability due to skills (buffs, passives, toggles, etc)
			damage = target.calcStat(stat, damage, target, null);
			if (target instanceof L2NpcInstance)
			{
				// get the natural vulnerability for the template
				damage *= ((L2NpcInstance) target).getTemplate().getVulnerability(stat);
			}
		}

		damage += Rnd.nextDouble() * damage / 10;
		//		damage += _rnd.nextDouble()* attacker.getRandomDamage(target);
		//		}
		

		if (attacker instanceof L2NpcInstance)
		{
			//Skill Race : Undead
			if (((L2NpcInstance) attacker).getTemplate().getRace() == L2NpcTemplate.Race.UNDEAD)
				damage /= attacker.getPDefUndead(target);
		}
		if (target instanceof L2NpcInstance)
		{
			switch (((L2NpcInstance) target).getTemplate().getRace())
			{
				case UNDEAD:
					damage *= attacker.getPAtkUndead(target);
					break;
				case BEAST:
					damage *= attacker.getPAtkMonsters(target);
					break;
				case ANIMAL:
					damage *= attacker.getPAtkAnimals(target);
					break;
				case PLANT:
					damage *= attacker.getPAtkPlants(target);
					break;
				case DRAGON:
					damage *= attacker.getPAtkDragons(target);
					break;
				case BUG:
					damage *= attacker.getPAtkInsects(target);
					break;
				default:
					// nothing
					break;
			}
		}

		if (skill != null) 
        {
            if (skill.getSkillType() == SkillType.FATALCOUNTER)
                damage *= (1.0 - attacker.getStatus().getCurrentHp()/attacker.getMaxHp()) * 3.0;
        }

        if (shld)
        {
            if (100 - Config.ALT_PERFECT_SHLD_BLOCK < Rnd.get(100)) 
            {
                damage = 1;
                target.sendPacket(new SystemMessage(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS));
            }
        }
		if (damage > 0 && damage < 1)
		{
			damage = 1;
		}
		else if (damage < 0)
		{
			damage = 0;
		}
		
		// Dmg bonusses in PvP fight
		if((attacker instanceof L2PcInstance || attacker instanceof L2Summon)
				&& (target instanceof L2PcInstance || target instanceof L2Summon))
		{
			if(skill == null)
				damage *= attacker.calcStat(Stats.PVP_PHYSICAL_DMG, 1, null, null);
			else
				damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);	
		}
		if (attacker instanceof L2PcInstance){
           if (((L2PcInstance) attacker).getClassId().isMage())
            damage = damage*Config.ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
           else damage = damage*Config.ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;}
        else if (attacker instanceof L2Summon)
           damage = damage*Config.ALT_PETS_PHYSICAL_DAMAGE_MULTI;
        else if (attacker instanceof L2NpcInstance)
            damage = damage*Config.ALT_NPC_PHYSICAL_DAMAGE_MULTI;
		if(attacker instanceof L2PcInstance)
		{
			
			int ClassId=((L2PcInstance) attacker).getClassId().getId();
					switch(ClassId)
				{
					
					//Human
				case 0:		damage = damage * Config.HDAMAGE_FIGHTER; 			break;
				case 1:		damage = damage * Config.HDAMAGE_WARRIOR;			break;
				case 2:		damage = damage * Config.HDAMAGE_GLADIATOR;			break;
				case 3:		damage = damage * Config.HDAMAGE_WARLORD;			break;
				case 4:		damage = damage * Config.HDAMAGE_KNIGHT;			break;
				case 5:		damage = damage * Config.HDAMAGE_PALADIN;			break;
				case 6:		damage = damage * Config.HDAMAGE_DARK_AVENGER;		break;
				case 7:		damage = damage * Config.HDAMAGE_ROGUE;				break;
				case 8:		damage = damage * Config.HDAMAGE_TREASURE_HUNTER;	break;
				case 9:		damage = damage * Config.HDAMAGE_HAWKEYE;			break;
				
				case 88:	damage = damage * Config.HDAMAGE_DUELIST;			break;
				case 89:	damage = damage * Config.HDAMAGE_DREADNOUGHT;		break;
				case 90:	damage = damage * Config.HDAMAGE_PHOENIX_KNIGHT;	break;
				case 91:	damage = damage * Config.HDAMAGE_HELL_KNIGHT;		break;
				case 92:	damage = damage * Config.HDAMAGE_SAGITTARIUS;		break;
				case 93:	damage = damage * Config.HDAMAGE_ADVENTURER;		break;
				
				//Elf
				//Fight
				case 18:	damage = damage * Config.EDamage_Fighter;			break;
				case 19:	damage = damage * Config.EDamage_Knight;			break;
				case 20:	damage = damage * Config.EDamage_Scout;				break;
				case 21:	damage = damage * Config.EDamage_TempleKnight;		break;
				case 22:	damage = damage * Config.EDamage_SwordSinger;		break;
				case 23:	damage = damage * Config.EDamage_PlainsWalker;		break;
				case 24:	damage = damage * Config.EDamage_SilverRanger;		break;
				//3 proff
				case 99:	damage = damage * Config.EDAMAGE_EVATEMPLAR;		break;
				case 100:	damage = damage * Config.EDAMAGE_SWORDMUSE;			break;
				case 101:	damage = damage * Config.EDAMAGE_WINDRIDER;			break;
				case 102:	damage = damage * Config.EDAMAGE_MOONLIGHT_SENTINEL;break;
				
				//Dark Elf
				//Fight
				case 31:	damage = damage * Config.DEDamage_Fighter;		break;
				case 32:	damage = damage * Config.DEDamage_PaulusKnight;		break;
				case 33:	damage = damage * Config.DEDamage_ShillienKnight;		break;
				case 34:	damage = damage * Config.DEDamage_BladeDancer;		break;
				case 35:	damage = damage * Config.DEDamage_Assassin;		break;
				case 36:	damage = damage * Config.DEDamage_AbyssWalker;		break;
				case 37:	damage = damage * Config.DEDamage_PhantomRanger;		break;
			
				case 106:	damage = damage * Config.DEDAMAGE_SHILLEN_TEMPLAR;		break;
				case 107:	damage = damage * Config.DEDAMAGE_SPECTRAL_DANCER;		break;
				case 108:	damage = damage * Config.DEDAMAGE_GHOST_HUNTER;		break;
				case 109:	damage = damage * Config.DEDAMAGE_GHOSTSENTINEL;		break;
				
				//Ork
				case 44:	damage = damage * Config.ODamage_Fighter;		break;
				case 45:	damage = damage * Config.ODamage_Raider;		break;
				case 46:	damage = damage * Config.ODamage_Destroyer;		break;
				case 47:	damage = damage * Config.ODamage_Monk;		break;
				case 48:	damage = damage * Config.ODamage_Tyrant;		break;
			
				case 113:	damage = damage * Config.ODAMAGE_TITAN;		break;
				case 114:	damage = damage * Config.ODAMAGE_GRAND_KHAUATARI;		break;
				
				//Dwarve
				case 53:	damage = damage * Config.DDamage_Fighter;		break;
				case 54:	damage = damage * Config.DDamage_Scavenger;		break;
				case 55:	damage = damage * Config.DDamage_BountyHunter;		break;
				case 56:	damage = damage * Config.DDamage_Artisan;		break;
				case 57:	damage = damage * Config.DDamage_Warsmith;		break;
				
				case 117:	damage = damage * Config.DDAMAGE_FORTUNESEEKER;		break;
				case 118:	damage = damage * Config.DDAMAGE_MAESTRO;		break;
				
				//Elf
				//Mage
				case 25:	damage = damage * Config.EDamage_Mage;				break;
				case 26:	damage = damage * Config.EDamage_Wizard;			break;
				case 27:	damage = damage * Config.EDamage_SpellSinger;		break;
				case 28:	damage = damage * Config.EDamage_ElementalSummoner;	break;
				case 29:	damage = damage * Config.EDamage_Oracle;			break;
				case 30:	damage = damage * Config.EDamage_Elder;				break;
				case 103:	damage = damage * Config.EDAMAGE_MYSTIC_MUSE;		break;
				case 104:	damage = damage * Config.EDAMAGE_ELEMENTAL_MASTER;	break;
				case 105:	damage = damage * Config.EDAMAGE_EVA_SEINT;			break;
				
				//Ork
				//Mage
				case 49:	damage = damage * Config.ODamage_Mage;		break;
				case 50:	damage = damage * Config.ODamage_Shaman;		break;
				case 51:	damage = damage * Config.ODamage_Overlord;		break;
				case 52:	damage = damage * Config.ODamage_Warcryer;		break;
				
				
				case 116:	damage = damage * Config.ODAMAGE_DOOMCRAYER;		break;
			  case 115:	damage = damage * Config.ODAMAGE_DOMINATOR;		break;					
				//DarkElf
				//Mage
				case 38:	damage = damage * Config.DEDamage_Mage;		break;
				case 39:	damage = damage * Config.DEDamage_DarkWizard;		break;
				case 40:	damage = damage * Config.DEDamage_Spellhowler;		break;
				case 41:	damage = damage * Config.DEDamage_PhantomSummoner;		break;
				case 42:	damage = damage * Config.DEDamage_ShillienOracle;		break;
				case 43:	damage = damage * Config.DEDamage_ShillienElder;		break;
				case 110:	damage = damage * Config.DEDAMAGE_STORMSCREAMER;		break;
				case 111:	damage = damage * Config.DEDAMAGE_SPECTRALMASTER;		break;
				case 112:	damage = damage * Config.DEDAMAGE_SHILLENSEINT;		break;

				
			}
					
				
		}

		return damage;
	}

	public final double calcMagicDam(L2Character attacker, L2Character target, L2Skill skill,
										boolean ss, boolean bss, boolean mcrit)
	{
		if (attacker instanceof L2PcInstance)
		{
			L2PcInstance pcInst = (L2PcInstance)attacker;
			if (pcInst.isGM() && pcInst.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)
				return 0;
		}
		
		double mAtk = attacker.getMAtk(target, skill);
		double mDef = target.getMDef(attacker, skill);
		if (bss) mAtk *= 4;
		else if (ss) mAtk *= 2;

		double damage = 91 * Math.sqrt(mAtk) / mDef * skill.getPower(attacker) * calcSkillVulnerability(target, skill);
		
		// In C5 summons make 10 % less dmg in PvP.
		if(attacker instanceof L2Summon && target instanceof L2PcInstance) damage *= 0.9;
		
		//		if(attacker instanceof L2PcInstance && target instanceof L2PcInstance) damage *= 0.9; // PvP modifier (-10%)

		// Failure calculation
		if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill))
		{
			if (attacker instanceof L2PcInstance)
			{
				int altDiffMagic = 9;
				if(Config.ALT_DIFF_MAGIC) altDiffMagic = 30;
				if (calcMagicSuccess(attacker, target, skill)
					&& (target.getLevel() - attacker.getLevel()) <= altDiffMagic)
				{
					if (skill.getSkillType() == SkillType.DRAIN)
						attacker.sendPacket(new SystemMessage(SystemMessageId.DRAIN_HALF_SUCCESFUL));
					else
						attacker.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));

					damage /= 2;
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
					sm.addString(target.getName());
					sm.addSkillName(skill.getId());
					attacker.sendPacket(sm);

					damage = 1;
				}
			}

			if (target instanceof L2PcInstance)
			{
				if (skill.getSkillType() == SkillType.DRAIN)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.RESISTED_S1_DRAIN);
					sm.addString(attacker.getName());
					target.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.RESISTED_S1_MAGIC);
					sm.addString(attacker.getName());
					target.sendPacket(sm);
				}
			}
		}
		else if (mcrit) damage *= Config.M_CRIT_DAMAGE;

		// Pvp bonusses for dmg
		if((attacker instanceof L2PcInstance || attacker instanceof L2Summon)
				&& (target instanceof L2PcInstance || target instanceof L2Summon))
		{
			if(skill.isMagic() && (!mcrit))
				damage *= attacker.calcStat(Stats.PVP_MAGICAL_DMG, 1, null, null);
			else
				damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
		}

		if (attacker instanceof L2PcInstance){
           if (((L2PcInstance) attacker).getClassId().isMage())
            damage = damage*Config.ALT_MAGES_MAGICAL_DAMAGE_MULTI;
           else damage = damage*Config.ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI;}
        else if (attacker instanceof L2Summon)
           damage = damage*Config.ALT_PETS_MAGICAL_DAMAGE_MULTI;
        else if (attacker instanceof L2NpcInstance)
            damage = damage*Config.ALT_NPC_MAGICAL_DAMAGE_MULTI;

		return damage;
	}

	/** Returns true in case of critical hit */
	public final boolean calcCrit(double rate)
	{
		return rate > Rnd.get(1000);
	}
	/** Calcul value of blow success */
	public final boolean calcBlow(L2Character activeChar, L2Character target, int chance)
	{
		return activeChar.calcStat(Stats.BLOW_RATE, chance*(1.0+(activeChar.getDEX()-20)/100), target, null)>Rnd.get(100);
	}
	/** Calcul value of lethal chance */
	public final double calcLethal(L2Character activeChar, L2Character target, int baseLethal)
	{
		return activeChar.calcStat(Stats.LETHAL_RATE, (baseLethal*((double)activeChar.getLevel()/target.getLevel())*0.5), target, null);
	}
	public final boolean calcLethalHit(L2Character activeChar, L2Character target, L2Skill skill)
    {
        if (!target.isRaid()
                && !(target instanceof L2DoorInstance)
                && !(target instanceof L2NpcInstance && ((L2NpcInstance) target).getNpcId() == 35062))
        {
        	if (Rnd.chance(target.calcStat(Stats.P_SKILL_EVASION, 0.0D, null, skill)))
            {
              return false;
            }
        	
            int chance = Rnd.get(100);
            // 2nd lethal effect activate (cp,hp to 1 or if target is npc then hp to 1)
            if (skill.getLethalChance2() > 0 && chance < calcLethal(activeChar, target, skill.getLethalChance2()))
            {
                if (target instanceof L2NpcInstance)
                    target.reduceCurrentHp(target.getCurrentHp() - 1, activeChar);
                else if (target instanceof L2PcInstance) // If is a active player set his HP and CP to 1
                {
                    L2PcInstance player = (L2PcInstance) target;
                    if (!player.isInvul())
                    {
                        player.setCurrentHp(1);
                        player.setCurrentCp(1);
                    }
                }
                activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
            }
            else if (skill.getLethalChance1() > 0 && chance < calcLethal(activeChar, target, skill.getLethalChance1()))
            {
                if (target instanceof L2PcInstance)
                {
                    L2PcInstance player = (L2PcInstance) target;
                    if (!player.isInvul())
                        player.setCurrentCp(1); // Set CP to 1
                }
                else if (target instanceof L2NpcInstance) // If is a monster remove first damage and after 50% of current hp
                    target.reduceCurrentHp(target.getCurrentHp() / 2, activeChar);
                activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
            }
            else
                return false;
        }
        else
            return false;
        
        return true;
    }
	public final boolean calcMCrit(double mRate)
	{
		return mRate > Rnd.get(1000);
	}

	/** Returns true in case when ATTACK is canceled due to hit */
	public final boolean calcAtkBreak(L2Character target, double dmg)
	{
        double init = 0;

		if (Config.ALT_GAME_CANCEL_CAST && target.isCastingNow()) init = 50;
		if (Config.ALT_GAME_CANCEL_BOW && target.isAttackingNow())
		{
			L2Weapon wpn = target.getActiveWeaponItem();
			if (wpn != null && wpn.getItemType() == L2WeaponType.BOW) init = 15;
		}

        if (init <= 0) return false; // No attack break

        // Chance of break is higher with higher dmg
        init += Math.sqrt(13*dmg);  

        // Chance is affected by target MEN
        init -= (MENbonus[target.getMEN()] * 100 - 100);

        // Calculate all modifiers for ATTACK_CANCEL
        double rate = target.calcStat(Stats.ATTACK_CANCEL, init, null, null); 

        // Adjust the rate to be between 1 and 99
        if (rate > 99) rate = 99;
        else if (rate < 1) rate = 1;

        return Rnd.get(100) < rate;
	}

	/** Calculate delay (in milliseconds) before next ATTACK */
	public final int calcPAtkSpd(L2Character attacker, L2Character target, double rate)
	{
		// measured Oct 2006 by Tank6585, formula by Sami
		// attack speed 312 equals 1500 ms delay... (or 300 + 40 ms delay?)
		if(rate < 2) return 2700;
	    else return (int)(470000/rate);
	}

	/** Calculate delay (in milliseconds) for skills cast */
	public final int calcMAtkSpd(L2Character attacker, L2Character target, L2Skill skill, double skillTime)
	{
		if (skill.isMagic()) return (int) (skillTime * 333 / attacker.getMAtkSpd());
		return (int) (skillTime * 333 / attacker.getPAtkSpd());
	}

	/** Calculate delay (in milliseconds) for skills cast */
	public final int calcMAtkSpd(L2Character attacker, L2Skill skill, double skillTime)
	{
		if (skill.isMagic()) return (int) (skillTime * 333 / attacker.getMAtkSpd());
		return (int) (skillTime * 333 / attacker.getPAtkSpd());
	}

	/** Returns true if hit missed (taget evaded) */
	public boolean calcHitMiss(L2Character attacker, L2Character target)
	{
		// accuracy+dexterity => probability to hit in percents
		int acc_attacker;
		int evas_target;
		acc_attacker = attacker.getAccuracy();
		evas_target = target.getEvasionRate(attacker);
		int d = 85 + acc_attacker - evas_target;
		return d < Rnd.get(100);
	}

	public boolean calcShldUse(L2Character attacker, L2Character target)
	{
		L2Weapon at_weapon = attacker.getActiveWeaponItem();
		double shldRate = target.calcStat(Stats.SHIELD_RATE, 0, attacker, null)
			* DEXbonus[target.getDEX()];
		if (shldRate == 0.0) return false;
		if (target.getKnownSkill(316) == null && target.getFirstEffect(318) == null)
			if (!target.isFront(attacker)) return false;
		if (at_weapon != null && at_weapon.getItemType() == L2WeaponType.BOW)
			shldRate *= 1.3;
		return shldRate > Rnd.get(100);
	}

	public boolean calcMagicAffected(L2Character actor, L2Character target, L2Skill skill)
	{
		SkillType type = skill.getSkillType();
		double defence = 0;
		// TODO: CHECK/FIX THIS FORMULA UP!!
		if (skill.isActive() && skill.isOffensive() && !skill.isNeutral())
			defence = target.getMDef(actor, skill);
		
		double attack = 2 * actor.getMAtk(target, skill) * calcSkillVulnerability(target, skill);
		double d = (attack - defence)/(attack + defence);
		if (target.isRaid())
		{
			switch (type)
			{
				case CONFUSION:
				case MUTE:
				case PARALYZE:
				case ROOT:
				case FEAR:
				case SLEEP:
				case STUN:
				case DEBUFF:
				case AGGDEBUFF:
				case HEAL:
				case HEAL_PERCENT:
					if ( d > 0 && Rnd.get(1000) == 1)
						return true;
					else
						return false;
			}
		}
		
		d += 0.5 * Rnd.nextGaussian();
		return d > 0;		
	}
			
	public double calcSkillVulnerability(L2Character target, L2Skill skill)
	{
		double multiplier = 1;	// initialize...

		// Get the skill type to calculate its effect in function of base stats
		// of the L2Character target
		if (skill != null)
		{
			// first, get the natural template vulnerability values for the target
			Stats stat = skill.getStat();
			if (stat != null)
			{
				switch (stat)
				{
				case AGGRESSION:
					multiplier *= target.getTemplate().baseAggressionVuln;
					break;
				case BLEED:
					multiplier *= target.getTemplate().baseBleedVuln;
					break;
				case POISON:
					multiplier *= target.getTemplate().basePoisonVuln;
					break;
				case STUN:
					multiplier *= target.getTemplate().baseStunVuln;
					break;
				case ROOT:
					multiplier *= target.getTemplate().baseRootVuln;
					break;
				case MOVEMENT:
					multiplier *= target.getTemplate().baseMovementVuln;
					break;
				case CONFUSION:
					multiplier *= target.getTemplate().baseConfusionVuln;
					break;
				case SLEEP:
					multiplier *= target.getTemplate().baseSleepVuln;
					break;
				case FIRE:
					multiplier *= target.getTemplate().baseFireVuln;
					break;
				case WIND:
					multiplier *= target.getTemplate().baseWindVuln;
					break;
				case WATER:
					multiplier *= target.getTemplate().baseWaterVuln;
					break;
				case EARTH:
					multiplier *= target.getTemplate().baseEarthVuln;
					break;
				case HOLY:
					multiplier *= target.getTemplate().baseHolyVuln;
					break;
				case DARK:
					multiplier *= target.getTemplate().baseDarkVuln;
					break;
				}
			}

			// Next, calculate the elemental vulnerabilities
			switch (skill.getElement())
			{
			case L2Skill.ELEMENT_EARTH:
				multiplier = target.calcStat(Stats.EARTH_VULN, multiplier, target, skill);
				break;
			case L2Skill.ELEMENT_FIRE:
				multiplier = target.calcStat(Stats.FIRE_VULN, multiplier, target, skill);
				break;
			case L2Skill.ELEMENT_WATER:
				multiplier = target.calcStat(Stats.WATER_VULN, multiplier, target, skill);
				break;
			case L2Skill.ELEMENT_WIND:
				multiplier = target.calcStat(Stats.WIND_VULN, multiplier, target, skill);
				break;
			case L2Skill.ELEMENT_HOLY:
				multiplier = target.calcStat(Stats.HOLY_VULN, multiplier, target, skill);
				break;
			case L2Skill.ELEMENT_DARK:
				multiplier = target.calcStat(Stats.DARK_VULN, multiplier, target, skill);
				break;
			}
			
			// Finally, calculate skilltype vulnerabilities
			SkillType type = skill.getSkillType();
			
			// For additional effects on PDAM and MDAM skills (like STUN, SHOCK, PARALYZE...)
			if (type != null && (type == SkillType.PDAM || type == SkillType.MDAM))
				type = skill.getEffectType();

			if (type != null)
			{
				switch (type)
				{
					case BLEED:
						multiplier = target.calcStat(Stats.BLEED_VULN, multiplier, target, null);
						break;
					case POISON:
						multiplier = target.calcStat(Stats.POISON_VULN, multiplier, target, null);
						break;
					case STUN:
						multiplier = target.calcStat(Stats.STUN_VULN, multiplier, target, null);
						break;
					case PARALYZE:
						multiplier = target.calcStat(Stats.PARALYZE_VULN, multiplier, target, null);
						break;
					case ROOT:
						multiplier = target.calcStat(Stats.ROOT_VULN, multiplier, target, null);
						break;
					case SLEEP:
						multiplier = target.calcStat(Stats.SLEEP_VULN, multiplier, target, null);
						break;
					case MUTE:
					case FEAR:
					case BETRAY:
					case AGGREDUCE_CHAR:
						multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
						break;
					case CONFUSION:
						multiplier = target.calcStat(Stats.CONFUSION_VULN, multiplier, target, null);
						break;
					case DEBUFF:
					case WEAKNESS:
						multiplier = target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null);
						break;
					default:
						;
				}
			}
			
		}
		return multiplier;
	}

	public double calcSkillStatModifier(SkillType type, L2Character target)
	{
		double multiplier = 1;
		if (type == null) return multiplier;
		switch (type)
		{
			case STUN:
			case BLEED:
				multiplier = 2 - Math.sqrt(CONbonus[target.getCON()]);
				break;
			case POISON:
			case SLEEP:
			case DEBUFF:
			case WEAKNESS:
			case ERASE:
			case ROOT:
			case MUTE:
			case FEAR:
			case BETRAY:
			case CONFUSION:
			case AGGREDUCE_CHAR:
			case PARALYZE:
				multiplier = 2 - Math.sqrt(MENbonus[target.getMEN()]);
				break;
			default:
				return multiplier;
		}
		if (multiplier < 0)
			multiplier = 0;
		return multiplier;
	}

	public boolean calcSkillSuccess(L2Character attacker, L2Character target, L2Skill skill, boolean ss,
									boolean sps, boolean bss)
	{
		SkillType type = skill.getSkillType();

		if (target.isRaid())
		{
			switch (type)
			{
			case CONFUSION:
			case MUTE:
			case PARALYZE:
			case ROOT:
			case FEAR:
			case SLEEP:
			case STUN:
			case DEBUFF:
			case AGGDEBUFF:
			case HEAL:
			case HEAL_PERCENT:
				return false; // these skills should not work on RaidBoss
			}
		}

		int value = (int) skill.getPower();
		int lvlDepend = skill.getLevelDepend();

		if (type == SkillType.PDAM || type == SkillType.MDAM) // For additional effects on PDAM skills (like STUN, SHOCK,...)
		{
			value = skill.getEffectPower();
			type = skill.getEffectType();
		}
		// TODO: Temporary fix for skills with EffectPower = 0 or EffectType not set
		if (value == 0 || type == null)
		{
			if (skill.getSkillType() == SkillType.PDAM)
			{
				value = 50;
				type = SkillType.STUN;
			}
			if (skill.getSkillType() == SkillType.MDAM)
			{
				value = 30;
				type = SkillType.PARALYZE;
			}
		}

		// TODO: Temporary fix for skills with Power = 0 or LevelDepend not set
		if (value == 0) value = (type == SkillType.PARALYZE) ? 50 : (type == SkillType.FEAR)? 40 : 80;
		if (lvlDepend == 0) lvlDepend = (type == SkillType.PARALYZE || type == SkillType.FEAR) ? 1 : 2;

		// TODO: Temporary fix for NPC skills with MagicLevel not set
		// int lvlmodifier = (skill.getMagicLevel() - target.getLevel()) * lvlDepend;
		int lvlmodifier = ((skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel()) - target.getLevel())
			* lvlDepend;
		double statmodifier = calcSkillStatModifier(type, target);
		double resmodifier = calcSkillVulnerability(target, skill);

		int ssmodifier = 100;
		if (bss) ssmodifier = 200;
		else if (sps) ssmodifier = 150;
		else if (ss) ssmodifier = 150;

		int rate = (int) ((value * statmodifier + lvlmodifier) * resmodifier);
		if (skill.isMagic())
			rate = (int) (rate * Math.pow((double) attacker.getMAtk(target, skill)
				/ target.getMDef(attacker, skill), 0.2));

		if (rate > 99) rate = 99;
		else if (rate < 1) rate = 1;

		if (ssmodifier != 100)
		{
			if (rate > 10000 / (100 + ssmodifier)) rate = 100 - (100 - rate) * 100 / ssmodifier;
			else rate = rate * ssmodifier / 100;
		}

		if (Config.DEVELOPER)
			System.out.println(skill.getName()
				+ ": "
				+ value
				+ ", "
				+ statmodifier
				+ ", "
				+ lvlmodifier
				+ ", "
				+ resmodifier
				+ ", "
				+ ((int) (Math.pow((double) attacker.getMAtk(target, skill)
					/ target.getMDef(attacker, skill), 0.2) * 100) - 100) + ", " + ssmodifier + " ==> "
				+ rate);
		//if ((Config.VIEW_SKILL_CHANCE) && (skill.getSkillType() != L2Skill.SkillType.MDAM) && (skill.getSkillType() != L2Skill.SkillType.PDAM) && (skill.getSkillType() != L2Skill.SkillType.DRAIN))
		    //  attacker.sendPacket(SystemMessage.sendString(skill.getName() + " шанс: " + rate + " %"));
		if (menu._vsc)
	    {
	      attacker.sendPacket(SystemMessage.sendString(skill.getName() + " шанс: " + rate + "%"));
	    }
		if ((!skill.isMagic()) && (Rnd.chance(target.calcStat(Stats.P_SKILL_EVASION, 0.0D, null, skill))))
	    {
	      return false;
	    }
		return (Rnd.get(100) < rate);
	}

	public boolean calcMagicSuccess(L2Character attacker, L2Character target, L2Skill skill)
	{
		double lvlDifference = (target.getLevel() - (skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel()));
		int rate = Math.round((float)(Math.pow(1.3, lvlDifference) * 100));

		if(Config.ALT_DIFF_MAGIC) rate /= 10;

		//int chans;
		//chans = rate;
		//if (rate > 100) chans = 100;
		//if (rate < 1) chans = 0;
		//if(Config.VIEW_SKILL_CHANCE && skill.getSkillType() != SkillType.MDAM && skill.getSkillType() != SkillType.PDAM && skill.getSkillType() != SkillType.DRAIN)
        //attacker.sendPacket(SystemMessage.sendString((new StringBuilder()).append(skill.getName()).append(" шанс: ").append(chans).append(" процентов").toString()));
		if (menu._vsc)
	    {
	      attacker.sendPacket(SystemMessage.sendString(skill.getName() + " шанс: " + rate + "%"));
	    }
		
		
		return (Rnd.get(10000) > rate);
	}

	public boolean calculateUnlockChance(L2Skill skill)
	{
		int level = skill.getLevel();
		int chance = 0;
		switch (level)
		{
			case 1:
				chance = 30;
				break;

			case 2:
				chance = 50;
				break;

			case 3:
				chance = 75;
				break;

			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
				chance = 100;
				break;
		}
		if (Rnd.get(120) > chance)		
			return false;
		return true;
	}
	
    public double calcManaDam(L2Character attacker, L2Character target, L2Skill skill,
			boolean ss, boolean bss)
    {
    	//Mana Burnt = (SQR(M.Atk)*Power*(Target Max MP/97))/M.Def
    	double mAtk = attacker.getMAtk(target, skill);
		double mDef = target.getMDef(attacker, skill);
		double mp = target.getMaxMp();
		if (bss) mAtk *= 4;
		else if (ss) mAtk *= 2;

		double damage = (Math.sqrt(mAtk) * skill.getPower(attacker) * (mp/97)) / mDef;
		damage *= calcSkillVulnerability(target, skill);		
    	return damage;
    }
    
    public double calculateSkillResurrectRestorePercent(double baseRestorePercent, int casterWIT)
	{
		double restorePercent = baseRestorePercent;
		double modifier = WITbonus[casterWIT];

		if(restorePercent != 100 && restorePercent != 0) {
						
			restorePercent = baseRestorePercent * modifier;
						
			if(restorePercent - baseRestorePercent > 20.0) 
				restorePercent = baseRestorePercent + 20.0;
		}
				
		if(restorePercent > 100) 
			restorePercent = 100;
		if(restorePercent < baseRestorePercent) 
			restorePercent = baseRestorePercent;
					
		return restorePercent;
	}
    
	public final double calcMagicDam(L2CubicInstance attacker, L2Character target, L2Skill skill, boolean mcrit)
	{
		if (target.isInvul()) return 0;
		
		double mAtk = attacker.getMAtk();
		double mDef = target.getMDef(attacker.getOwner(), skill);
		
		double damage = 91 * Math.sqrt(mAtk) / mDef * skill.getPower() * calcSkillVulnerability(target, skill);
		L2PcInstance owner = attacker.getOwner();
		
		// Failure calculation
		if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(owner, target, skill))
		{			
			if (calcMagicSuccess(owner, target, skill) && (target.getLevel() - skill.getMagicLevel()) <= 9){
				if (skill.getSkillType() == SkillType.DRAIN)
					owner.sendPacket(new SystemMessage(SystemMessageId.DRAIN_HALF_SUCCESFUL));
				else
					owner.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
	
				damage /= 2;
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
				sm.addString(target.getName());
				sm.addSkillName(skill.getId());
				owner.sendPacket(sm);
	
				damage = 1;
			}
		
			if (target instanceof L2PcInstance)
			{
				if (skill.getSkillType() == SkillType.DRAIN)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.RESISTED_S1_DRAIN);
					sm.addString(owner.getName());
					target.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.RESISTED_S1_MAGIC);
					sm.addString(owner.getName());
					target.sendPacket(sm);
				}
			}
		}
		else if (mcrit) damage *= 4;
		
		return damage;
	}

	public boolean calcCubicSkillSuccess(L2CubicInstance attacker, L2Character target, L2Skill skill)
	{
		SkillType type = skill.getSkillType();
		
		if (target.isRaid())
		{
			switch(type)
			{
				case CONFUSION:
				case MUTE:
				case PARALYZE:
				case ROOT:
				case FEAR:
				case SLEEP:
				case STUN:
				case DEBUFF:
				case AGGDEBUFF:
				case HEAL:
				case HEAL_PERCENT:
					return false; // these skills should not work on RaidBoss
			}
		}
		
		// if target reflect this skill then the effect will fail
		if (target.reflectSkill(skill)) return false;
		
		int value = (int) skill.getPower();
		int lvlDepend = skill.getLevelDepend();
		
		if (type == SkillType.PDAM || type == SkillType.MDAM) // For additional effects on PDAM skills (like STUN, SHOCK,...)
		{
			value = skill.getEffectPower();
			type = skill.getEffectType();
		}

		// TODO: Temporary fix for skills with Power = 0 or LevelDepend not set
		if (value == 0) value = (type == SkillType.PARALYZE) ? 50 : (type == SkillType.FEAR) ? 40 : 80;
		if (lvlDepend == 0) lvlDepend = (type == SkillType.PARALYZE || type == SkillType.FEAR) ? 1 : 2;

		// TODO: Temporary fix for NPC skills with MagicLevel not set
		// int lvlmodifier = (skill.getMagicLevel() - target.getLevel()) * lvlDepend;
		int lvlmodifier = ((skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getOwner().getLevel()) - target.getLevel())
		* lvlDepend;
		double statmodifier = calcSkillStatModifier(type, target);
		double resmodifier = calcSkillVulnerability(target, skill);
		
		int rate = (int) ((value * statmodifier + lvlmodifier) * resmodifier);
		if (skill.isMagic())
			rate += (int) (Math.pow((double) attacker.getMAtk() / target.getMDef(attacker.getOwner(), skill), 0.2) * 100) - 100;
		
		if (rate > 99) 
			rate = 99;
		else 
			if (rate < 1) 
				rate = 1;
		
		if (Config.DEVELOPER)
			_log.info(skill.getName()
				+ ": "
				+ value
				+ ", "
				+ statmodifier
				+ ", "
				+ lvlmodifier
				+ ", "
				+ resmodifier
				+ ", "
				+ ((int) (Math.pow((double) attacker.getMAtk()
					/ target.getMDef(attacker.getOwner(), skill), 0.2) * 100) - 100) + " ==> "
				+ rate);
		return (Rnd.get(100) < rate);
	}

    public double getSTRBonus(L2Character activeChar)
    {
    	return STRbonus[activeChar.getSTR()];
    }
	
	public boolean calcPhysicalSkillEvasion(L2Character target, L2Skill skill)
	{
		if (skill.isMagic())
			return false;
		
		return Rnd.get(100) < target.calcStat(Stats.P_SKILL_EVASION, 0, null, skill);
	}

	public boolean canCancelAttackerTarget(L2Character attacker, L2Character target)
    {
        if((double)Rnd.get(100) < target.calcStat(Stats.CANCEL_ATTACKER_TARGET, 0.0, null, null))
        {
			attacker.setTarget(null);
            return true;
        } else
        {
            return false;
        }
    }
	public boolean calcSkillMastery(L2Character actor, L2Skill sk)
	  {
	    if (actor == null) {
	      return false;
	    }
	    if (sk.getSkillType() == L2Skill.SkillType.FISHING) {
	      return false;
	    }
	    double val = actor.getStat().calcStat(Stats.SKILL_MASTERY, 0, null, null);

	    if ((actor instanceof L2PcInstance))
	    {
	      if (((L2PcInstance)actor).isMageClass())
	        val *= INTbonus[actor.getINT()];
	      else
	        val *= STRbonus[actor.getSTR()];
	    }
	    return Rnd.get(100) < val;
	  }
}
