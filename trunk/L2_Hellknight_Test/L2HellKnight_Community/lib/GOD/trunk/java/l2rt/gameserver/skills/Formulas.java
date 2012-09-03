package l2rt.gameserver.skills;

import l2rt.Config;
import l2rt.config.ConfigSystem;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.CastleManager;
import l2rt.gameserver.instancemanager.ClanHallManager;
import l2rt.gameserver.instancemanager.FortressManager;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.L2Skill.Element;
import l2rt.gameserver.model.L2Skill.SkillType;
import l2rt.gameserver.model.base.Race;
import l2rt.gameserver.model.entity.residence.*;
import l2rt.gameserver.model.instances.L2ReflectionBossInstance;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.conditions.ConditionPlayerState;
import l2rt.gameserver.skills.conditions.ConditionPlayerState.CheckPlayerState;
import l2rt.gameserver.skills.funcs.Func;
import l2rt.gameserver.templates.L2PlayerTemplate;
import l2rt.gameserver.templates.L2Weapon;
import l2rt.gameserver.templates.L2Weapon.WeaponType;
import l2rt.util.Rnd;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

public class Formulas
{
	protected static final Logger _log = Logger.getLogger(L2Character.class.getName());

	public static int MAX_STAT_VALUE = 120;

	public static final double[] WITbonus = new double[MAX_STAT_VALUE];
	public static final double[] MENbonus = new double[MAX_STAT_VALUE];
	public static final double[] INTbonus = new double[MAX_STAT_VALUE];
	public static final double[] STRbonus = new double[MAX_STAT_VALUE];
	public static final double[] DEXbonus = new double[MAX_STAT_VALUE];
	public static final double[] CONbonus = new double[MAX_STAT_VALUE];

	private static final double[] STRCompute = new double[] { 1.0229, 82.1999};			//{1.036, 34.845}; for GE	// Уже по оффу ?
	private static final double[] INTCompute = new double[] { 1.0189, 66.6880 };		//{1.020, 31.375}; for GE	// TODO
	private static final double[] DEXCompute = new double[] { 1.0059, 19.4000 };		//{1.009, 19.360}; for GE	// Уже по оффу
	private static final double[] WITCompute = new double[] { 1.0139, 77.6999 };		//{1.050, 20.000}; for GE	// Уже по оффу !
	private static final double[] CONCompute = new double[] { 1.0119, 36.7000 };		//{1.030, 27.632}; for GE	// Уже по оффу
	private static final double[] MENCompute = new double[] { 1.0090, -0.0300 };		//{1.010, -0.060}; for GE 	// TODO
	
	public static void GenerateBaseStats()
	{
		for (int i = 0; i < STRbonus.length; i++)
			STRbonus[i] = Math.floor(Math.pow(STRCompute[0], i - STRCompute[1]) * 100 + .5d) / 100;
		for (int i = 0; i < INTbonus.length; i++)
			INTbonus[i] = Math.floor(Math.pow(INTCompute[0], i - INTCompute[1]) * 100 + .5d) / 100;
		for (int i = 0; i < DEXbonus.length; i++)
			DEXbonus[i] = Math.floor(Math.pow(DEXCompute[0], i - DEXCompute[1]) * 100 + .5d) / 100;
		for (int i = 0; i < WITbonus.length; i++)
			WITbonus[i] = Math.floor(Math.pow(WITCompute[0], i - WITCompute[1]) * 100 + .5d) / 100;
		for (int i = 0; i < CONbonus.length; i++)
			CONbonus[i] = Math.floor(Math.pow(CONCompute[0], i - CONCompute[1]) * 100 + .5d) / 100;
		for (int i = 0; i < MENbonus.length; i++)
			MENbonus[i] = Math.floor(Math.pow(MENCompute[0], i - MENCompute[1]) * 100 + .5d) / 100;
	}

	private static class FuncMultRegenResting extends Func
	{
		static final FuncMultRegenResting[] func = new FuncMultRegenResting[Stats.NUM_STATS];

		static Func getFunc(Stats stat)
		{
			int pos = stat.ordinal();
			if(func[pos] == null)
				func[pos] = new FuncMultRegenResting(stat);
			return func[pos];
		}

		private FuncMultRegenResting(Stats stat)
		{
			super(stat, 0x30, null);
			setCondition(new ConditionPlayerState(CheckPlayerState.RESTING, true));
		}

		@Override
		public void calc(Env env)
		{
			if(env.character.isPlayer() && env.character.getLevel() <= 40 && ((L2Player) env.character).getClassId().getLevel() < 3 && _stat == Stats.REGENERATE_HP_RATE)
				env.value *= 6; // TODO: переделать красивее
			else
				env.value *= 1.5;
		}
	}

	private static class FuncMultRegenStanding extends Func
	{
		static final FuncMultRegenStanding[] func = new FuncMultRegenStanding[Stats.NUM_STATS];

		static Func getFunc(Stats stat)
		{
			int pos = stat.ordinal();
			if(func[pos] == null)
				func[pos] = new FuncMultRegenStanding(stat);
			return func[pos];
		}

		private FuncMultRegenStanding(Stats stat)
		{
			super(stat, 0x30, null);
			setCondition(new ConditionPlayerState(CheckPlayerState.STANDING, true));
		}

		@Override
		public void calc(Env env)
		{
			env.value *= 1.1;
		}
	}

	private static class FuncMultRegenRunning extends Func
	{
		static final FuncMultRegenRunning[] func = new FuncMultRegenRunning[Stats.NUM_STATS];

		static Func getFunc(Stats stat)
		{
			int pos = stat.ordinal();
			if(func[pos] == null)
				func[pos] = new FuncMultRegenRunning(stat);
			return func[pos];
		}

		private FuncMultRegenRunning(Stats stat)
		{
			super(stat, 0x30, null);
			setCondition(new ConditionPlayerState(CheckPlayerState.RUNNING, true));
		}

		@Override
		public void calc(Env env)
		{
			env.value *= 0.7;
		}
	}

	private static class FuncPAtkMul extends Func
	{
		static final FuncPAtkMul func = new FuncPAtkMul();

		private FuncPAtkMul()
		{
			super(Stats.POWER_ATTACK, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= STRbonus[env.character.getSTR()] * env.character.getLevelMod();
		}
	}

	private static class FuncMAtkMul extends Func
	{
		static final FuncMAtkMul func = new FuncMAtkMul();

		private FuncMAtkMul()
		{
			super(Stats.MAGIC_ATTACK, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			//{Wpn*(lvlbn^2)*[(1+INTbn)^2]+Msty}
			double ib = INTbonus[env.character.getINT()];
			double lvlb = env.character.getLevelMod();
			env.value *= lvlb * lvlb * ib * ib;
		}
	}

	private static class FuncPDefMul extends Func
	{
		static final FuncPDefMul func = new FuncPDefMul();

		private FuncPDefMul()
		{
			super(Stats.POWER_DEFENCE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= env.character.getLevelMod();
		}
	}

	private static class FuncMDefMul extends Func
	{
		static final FuncMDefMul func = new FuncMDefMul();

		private FuncMDefMul()
		{
			super(Stats.MAGIC_DEFENCE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= MENbonus[env.character.getMEN()] * env.character.getLevelMod();
		}
	}

	private static class FuncAttackRange extends Func
	{
		static final FuncAttackRange func = new FuncAttackRange();

		private FuncAttackRange()
		{
			super(Stats.POWER_ATTACK_RANGE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Weapon weapon = env.character.getActiveWeaponItem();
			if(weapon != null)
				env.value += weapon.getAttackRange();
		}
	}

	private static class FuncAccuracyAdd extends Func
	{
		static final FuncAccuracyAdd func = new FuncAccuracyAdd();

		private FuncAccuracyAdd()
		{
			super(Stats.ACCURACY_COMBAT, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.character.isPet())
				return;

			//[Square(DEX)] * 5 + lvl + weapon hitbonus;
			env.value += Math.sqrt(env.character.getDEX()) * 5 + env.character.getLevel();

			if(env.character.isSummon())
				env.value += env.character.getLevel() < 60 ? 4 : 5;

			if(env.character.getLevel() > 77)
				env.value += env.character.getLevel() - 77;
			if(env.character.getLevel() > 69)
				env.value += env.character.getLevel() - 69;
		}
	}

	private static class FuncEvasionAdd extends Func
	{
		static final FuncEvasionAdd func = new FuncEvasionAdd();

		private FuncEvasionAdd()
		{
			super(Stats.EVASION_RATE, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value += Math.sqrt(env.character.getDEX()) * 5 + env.character.getLevel();

			if(env.character.getLevel() > 77)
				env.value += env.character.getLevel() - 77;
			if(env.character.getLevel() > 69)
				env.value += env.character.getLevel() - 69;
		}
	}

	private static class FuncMAccuracyAdd extends Func
	{
		static final FuncMAccuracyAdd func = new FuncMAccuracyAdd();

		private FuncMAccuracyAdd()
		{
			super(Stats.MACCURACY_COMBAT, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.character.isPet())
				return;

			//val = sqrt(WIT) * 3 + player_lvl * 2
			env.value += (Math.sqrt(env.character.getWIT()) * 3) + (env.character.getLevel() * 2);

			if(env.character.isSummon())
				env.value += env.character.getLevel() < 60 ? 4 : 5;

			if(env.character.getLevel() > 77)
				env.value += env.character.getLevel() - 77;
			if(env.character.getLevel() > 69)
				env.value += env.character.getLevel() - 69;
		}
	}

	private static class FuncMEvasionAdd extends Func
	{
		static final FuncMEvasionAdd func = new FuncMEvasionAdd();

		private FuncMEvasionAdd()
		{
			super(Stats.MEVASION_RATE, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			//val = sqrt(WIT) * 3 + player_lvl * 2
			env.value += (Math.sqrt(env.character.getWIT()) * 3) + (env.character.getLevel() * 2);

			if(env.character.getLevel() > 77)
				env.value += env.character.getLevel() - 77;
			if(env.character.getLevel() > 69)
				env.value += env.character.getLevel() - 69;
		}
	}

	private static class FuncMCriticalRateMul extends Func
	{
		static final FuncMCriticalRateMul func = new FuncMCriticalRateMul();

		private FuncMCriticalRateMul()
		{
			super(Stats.MCRITICAL_RATE, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= WITbonus[env.character.getWIT()];
		}
	}

	private static class FuncPCriticalRateMul extends Func
	{
		static final FuncPCriticalRateMul func = new FuncPCriticalRateMul();

		private FuncPCriticalRateMul()
		{
			super(Stats.CRITICAL_BASE, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			if(!(env.character instanceof L2Summon))
				env.value *= DEXbonus[env.character.getDEX()];
			env.value *= 0.01 * env.character.calcStat(Stats.CRITICAL_RATE, env.target, env.skill);
		}
	}

	private static class FuncPAtkSpeedMul extends Func
	{
		static final FuncPAtkSpeedMul func = new FuncPAtkSpeedMul();

		private FuncPAtkSpeedMul()
		{
			super(Stats.POWER_ATTACK_SPEED, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= DEXbonus[env.character.getDEX()];
		}
	}

	private static class FuncMAtkSpeedMul extends Func
	{
		static final FuncMAtkSpeedMul func = new FuncMAtkSpeedMul();

		private FuncMAtkSpeedMul()
		{
			super(Stats.MAGIC_ATTACK_SPEED, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= WITbonus[env.character.getWIT()];
		}
	}

	private static class FuncHennaSTR extends Func
	{
		static final FuncHennaSTR func = new FuncHennaSTR();

		private FuncHennaSTR()
		{
			super(Stats.STAT_STR, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Player pc = (L2Player) env.character;
			if(pc != null)
				env.value = Math.max(1, env.value + pc.getHennaStatSTR());
		}
	}

	private static class FuncHennaDEX extends Func
	{
		static final FuncHennaDEX func = new FuncHennaDEX();

		private FuncHennaDEX()
		{
			super(Stats.STAT_DEX, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Player pc = (L2Player) env.character;
			if(pc != null)
				env.value = Math.max(1, env.value + pc.getHennaStatDEX());
		}
	}

	private static class FuncHennaINT extends Func
	{
		static final FuncHennaINT func = new FuncHennaINT();

		private FuncHennaINT()
		{
			super(Stats.STAT_INT, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Player pc = (L2Player) env.character;
			if(pc != null)
				env.value = Math.max(1, env.value + pc.getHennaStatINT());
		}
	}

	private static class FuncHennaMEN extends Func
	{
		static final FuncHennaMEN func = new FuncHennaMEN();

		private FuncHennaMEN()
		{
			super(Stats.STAT_MEN, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Player pc = (L2Player) env.character;
			if(pc != null)
				env.value = Math.max(1, env.value + pc.getHennaStatMEN());
		}
	}

	private static class FuncHennaCON extends Func
	{
		static final FuncHennaCON func = new FuncHennaCON();

		private FuncHennaCON()
		{
			super(Stats.STAT_CON, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Player pc = (L2Player) env.character;
			if(pc != null)
				env.value = Math.max(1, env.value + pc.getHennaStatCON());
		}
	}

	private static class FuncHennaWIT extends Func
	{
		static final FuncHennaWIT func = new FuncHennaWIT();

		private FuncHennaWIT()
		{
			super(Stats.STAT_WIT, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Player pc = (L2Player) env.character;
			if(pc != null)
				env.value = Math.max(1, env.value + pc.getHennaStatWIT());
		}
	}

	private static class FuncMaxHpAdd extends Func
	{
		static final FuncMaxHpAdd func = new FuncMaxHpAdd();

		private FuncMaxHpAdd()
		{
			super(Stats.MAX_HP, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PlayerTemplate t = (L2PlayerTemplate) env.character.getTemplate();
			int lvl = Math.max(0, env.character.getLevel() - t.classBaseLevel);
			double hpmod = t.lvlHpMod * lvl;
			double hpmax = (t.lvlHpAdd + hpmod) * lvl;
			double hpmin = t.lvlHpAdd * lvl + hpmod;
			env.value += (hpmax + hpmin) / 2;
		}
	}

	private static class FuncMaxHpMul extends Func
	{
		static final FuncMaxHpMul func = new FuncMaxHpMul();

		private FuncMaxHpMul()
		{
			super(Stats.MAX_HP, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= CONbonus[env.character.getCON()];
		}
	}

	private static class FuncMaxCpAdd extends Func
	{
		static final FuncMaxCpAdd func = new FuncMaxCpAdd();

		private FuncMaxCpAdd()
		{
			super(Stats.MAX_CP, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PlayerTemplate t = (L2PlayerTemplate) env.character.getTemplate();
			int lvl = Math.max(0, env.character.getLevel() - t.classBaseLevel);
			double cpmod = t.lvlCpMod * lvl;
			double cpmax = (t.lvlCpAdd + cpmod) * lvl;
			double cpmin = t.lvlCpAdd * lvl + cpmod;
			env.value += (cpmax + cpmin) / 2;
		}
	}

	private static class FuncMaxCpMul extends Func
	{
		static final FuncMaxCpMul func = new FuncMaxCpMul();

		private FuncMaxCpMul()
		{
			super(Stats.MAX_CP, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= CONbonus[env.character.getCON()];
		}
	}

	private static class FuncMaxMpAdd extends Func
	{
		static final FuncMaxMpAdd func = new FuncMaxMpAdd();

		private FuncMaxMpAdd()
		{
			super(Stats.MAX_MP, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PlayerTemplate t = (L2PlayerTemplate) env.character.getTemplate();
			int lvl = Math.max(0, env.character.getLevel() - t.classBaseLevel);
			double mpmod = t.lvlMpMod * lvl;
			double mpmax = (t.lvlMpAdd + mpmod) * lvl;
			double mpmin = t.lvlMpAdd * lvl + mpmod;
			env.value += (mpmax + mpmin) / 2;
		}
	}

	private static class FuncMaxMpMul extends Func
	{
		static final FuncMaxMpMul func = new FuncMaxMpMul();

		private FuncMaxMpMul()
		{
			super(Stats.MAX_MP, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= MENbonus[env.character.getMEN()];
		}
	}

	private static class FuncPDamageResists extends Func
	{
		static final FuncPDamageResists func = new FuncPDamageResists();

		private FuncPDamageResists()
		{
			super(Stats.PHYSICAL_DAMAGE, 0x30, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.target.isRaid() && env.character.getLevel() - env.target.getLevel() > Config.RAID_MAX_LEVEL_DIFF)
			{
				env.value = 1;
				return;
			}

			// TODO переделать на ту же систему, что у эффектов
			L2Weapon weapon = env.character.getActiveWeaponItem();
			if(weapon == null)
				env.value *= 0.01 * env.target.calcStat(Stats.FIST_WPN_RECEPTIVE, env.character, env.skill);
			else if(weapon.getItemType().getDefence() != null)
				env.value *= 0.01 * env.target.calcStat(weapon.getItemType().getDefence(), env.character, env.skill);

			env.value = calcDamageResists(env.skill, env.character, env.target, env.value);
		}
	}

	private static class FuncMDamageResists extends Func
	{
		static final FuncMDamageResists func = new FuncMDamageResists();

		private FuncMDamageResists()
		{
			super(Stats.MAGIC_DAMAGE, 0x30, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.target.isRaid() && Math.abs(env.character.getLevel() - env.target.getLevel()) > Config.RAID_MAX_LEVEL_DIFF)
			{
				env.value = 1;
				return;
			}
			env.value = calcDamageResists(env.skill, env.character, env.target, env.value);
		}
	}

	private static class FuncInventory extends Func
	{
		static final FuncInventory func = new FuncInventory();

		private FuncInventory()
		{
			super(Stats.INVENTORY_LIMIT, 0x01, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Player player = (L2Player) env.character;
			if(player.isGM())
				env.value = Config.INVENTORY_MAXIMUM_GM;
			else if(player.getTemplate().race == Race.dwarf)
				env.value = Config.INVENTORY_MAXIMUM_DWARF;
			else
				env.value = Config.INVENTORY_MAXIMUM_NO_DWARF;
			env.value += player.getExpandInventory();
		}
	}

	private static class FuncWarehouse extends Func
	{
		static final FuncWarehouse func = new FuncWarehouse();

		private FuncWarehouse()
		{
			super(Stats.STORAGE_LIMIT, 0x01, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Player player = (L2Player) env.character;
			if(player.getTemplate().race == Race.dwarf)
				env.value = Config.WAREHOUSE_SLOTS_DWARF;
			else
				env.value = Config.WAREHOUSE_SLOTS_NO_DWARF;
			env.value += player.getExpandWarehouse();
		}
	}

	private static class FuncTradeLimit extends Func
	{
		static final FuncTradeLimit func = new FuncTradeLimit();

		private FuncTradeLimit()
		{
			super(Stats.TRADE_LIMIT, 0x01, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Player _cha = (L2Player) env.character;
			if(_cha.getRace() == Race.dwarf)
				env.value = Config.MAX_PVTSTORE_SLOTS_DWARF;
			else
				env.value = Config.MAX_PVTSTORE_SLOTS_OTHER;
		}
	}

	private static class FuncSDefAll extends Func
	{
		static final FuncSDefAll func = new FuncSDefAll();

		private FuncSDefAll()
		{
			super(Stats.SHIELD_RATE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.value == 0)
				return;

			L2Character target = env.target;
			if(target != null)
			{
				L2Weapon weapon = target.getActiveWeaponItem();
				if(weapon != null)
					switch(weapon.getItemType())
					{
						case BOW:
						case CROSSBOW:
							env.value += 30.;
							break;
						case DAGGER:
						case DUALDAGGER:
							env.value += 12.;
							break;
					}
			}
		}
	}

	private static class FuncSDefPlayers extends Func
	{
		static final FuncSDefPlayers func = new FuncSDefPlayers();

		private FuncSDefPlayers()
		{
			super(Stats.SHIELD_RATE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.value == 0)
				return;

			L2Character cha = env.character;
			L2ItemInstance shld = ((L2Player) cha).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
			if(shld == null || shld.getItemType() != WeaponType.NONE)
				return;
			env.value *= CONbonus[cha.getCON()];
		}
	}

	public static void addFuncsToNewCharacter(L2Character cha)
	{
		if(cha.isPlayer())
		{
			cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_CP_RATE));
			cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_CP_RATE));
			cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_CP_RATE));
			cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_MP_RATE));

			cha.addStatFunc(FuncMaxCpAdd.func);
			cha.addStatFunc(FuncMaxHpAdd.func);
			cha.addStatFunc(FuncMaxMpAdd.func);

			cha.addStatFunc(FuncMaxCpMul.func);
			cha.addStatFunc(FuncMaxHpMul.func);
			cha.addStatFunc(FuncMaxMpMul.func);

			cha.addStatFunc(FuncAttackRange.func);

			cha.addStatFunc(FuncHennaSTR.func);
			cha.addStatFunc(FuncHennaDEX.func);
			cha.addStatFunc(FuncHennaINT.func);
			cha.addStatFunc(FuncHennaMEN.func);
			cha.addStatFunc(FuncHennaCON.func);
			cha.addStatFunc(FuncHennaWIT.func);

			cha.addStatFunc(FuncInventory.func);
			cha.addStatFunc(FuncWarehouse.func);
			cha.addStatFunc(FuncTradeLimit.func);

			cha.addStatFunc(FuncSDefPlayers.func);
		}

		if(cha.isPlayer() || cha.isPet())
		{
			cha.addStatFunc(FuncPAtkMul.func);
			cha.addStatFunc(FuncMAtkMul.func);
			cha.addStatFunc(FuncPDefMul.func);
			cha.addStatFunc(FuncMDefMul.func);
		}

		if(!cha.isPet())
		{
			cha.addStatFunc(FuncAccuracyAdd.func);
			cha.addStatFunc(FuncEvasionAdd.func);
			cha.addStatFunc(FuncMAccuracyAdd.func);
			cha.addStatFunc(FuncMEvasionAdd.func);
		}

		if(!cha.isPet() && !cha.isSummon())
		{
			cha.addStatFunc(FuncPAtkSpeedMul.func);
			cha.addStatFunc(FuncMAtkSpeedMul.func);
			cha.addStatFunc(FuncSDefAll.func);
		}

		cha.addStatFunc(FuncMCriticalRateMul.func);
		cha.addStatFunc(FuncPCriticalRateMul.func);
		cha.addStatFunc(FuncPDamageResists.func);
		cha.addStatFunc(FuncMDamageResists.func);
	}

	public static double calcHpRegen(final L2Character cha)
	{
		double init;
		if(cha.isPlayer())
			init = (cha.getLevel() <= 10 ? 1.95 + cha.getLevel() / 20. : 1.4 + cha.getLevel() / 10.) * cha.getLevelMod() * CONbonus[cha.getCON()];
		else
			init = cha.getTemplate().baseHpReg;

		if(cha instanceof L2Playable)
		{
			final L2Player player = cha.getPlayer();
			if(player != null && player.getClan() != null && player.getInResidence() != ResidenceType.None)
				switch(player.getInResidence())
				{
					case Clanhall:
						final int clanHallIndex = player.getClan().getHasHideout();
						if(clanHallIndex > 0)
						{
							final ClanHall clansHall = ClanHallManager.getInstance().getClanHall(clanHallIndex);
							if(clansHall != null)
								if(clansHall.isFunctionActive(ResidenceFunction.RESTORE_HP))
									init *= 1. + clansHall.getFunction(ResidenceFunction.RESTORE_HP).getLevel() / 100.;
						}
						break;
					case Castle:
						final int caslteIndex = player.getClan().getHasCastle();
						if(caslteIndex > 0)
						{
							final Castle castle = CastleManager.getInstance().getCastleByIndex(caslteIndex);
							if(castle != null)
								if(castle.isFunctionActive(ResidenceFunction.RESTORE_HP))
									init *= 1. + castle.getFunction(ResidenceFunction.RESTORE_HP).getLevel() / 100.;
						}
						break;
					case Fortress:
						final int fortIndex = player.getClan().getHasCastle();
						if(fortIndex > 0)
						{
							final Fortress fort = FortressManager.getInstance().getFortressByIndex(fortIndex);
							if(fort != null)
								if(fort.isFunctionActive(ResidenceFunction.RESTORE_HP))
									init *= 1. + fort.getFunction(ResidenceFunction.RESTORE_HP).getLevel() / 100.;
						}
						break;
				}
		}

		return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null);
	}

	public static double calcMpRegen(L2Character cha)
	{
		double init;
		if(cha.isPlayer())
			init = (.87 + cha.getLevel() * .03) * cha.getLevelMod();
		else
			init = cha.getTemplate().baseMpReg;

		if(cha.isPlayable())
		{
			init *= MENbonus[cha.getMEN()];
			if(cha.isSummon())
				init *= 2;
		}
		else if(cha.isRaid())
			init *= 3;

		if(cha.isPlayable())
		{
			L2Player player = cha.getPlayer();
			if(player != null)
			{
				L2Clan clan = player.getClan();
				if(clan != null)
					switch(player.getInResidence())
					{
						case Clanhall:
							int clanHallIndex = clan.getHasHideout();
							if(clanHallIndex > 0)
							{
								ClanHall clansHall = ClanHallManager.getInstance().getClanHall(clanHallIndex);
								if(clansHall != null)
									if(clansHall.isFunctionActive(ResidenceFunction.RESTORE_MP))
										init *= 1. + clansHall.getFunction(ResidenceFunction.RESTORE_MP).getLevel() / 100.;
							}
							break;
						case Castle:
							int caslteIndex = clan.getHasCastle();
							if(caslteIndex > 0)
							{
								Castle castle = CastleManager.getInstance().getCastleByIndex(caslteIndex);
								if(castle != null)
									if(castle.isFunctionActive(ResidenceFunction.RESTORE_MP))
										init *= 1. + castle.getFunction(ResidenceFunction.RESTORE_MP).getLevel() / 100.;
							}
							break;
						case Fortress:
							int fortIndex = clan.getHasCastle();
							if(fortIndex > 0)
							{
								Fortress fort = FortressManager.getInstance().getFortressByIndex(fortIndex);
								if(fort != null)
									if(fort.isFunctionActive(ResidenceFunction.RESTORE_MP))
										init *= 1. + fort.getFunction(ResidenceFunction.RESTORE_MP).getLevel() / 100.;
							}
							break;
					}
			}
		}

		return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null);
	}

	public static double calcCpRegen(L2Character cha)
	{
		double init = (1.5 + cha.getLevel() / 10) * cha.getLevelMod() * CONbonus[cha.getCON()];
		return cha.calcStat(Stats.REGENERATE_CP_RATE, init, null, null);
	}

	public static class AttackInfo
	{
		public double damage;
		public double defence;
		public double crit_rcpt;
		public double crit_static;
		public double death_rcpt;
		public double lethal1;
		public double lethal2;
		public boolean crit;
		public boolean shld;
		public boolean lethal;
		public boolean miss;
	}

	/**
	 * Для простых ударов
	 * patk = patk
	 * При крите простым ударом:
	 * patk = patk * (1 + crit_damage_rcpt) * crit_damage_mod + crit_damage_static
	 * Для blow скиллов
	 * TODO
	 * Для скилловых критов, повреждения просто удваиваются, бафы не влияют (кроме blow, для них выше)
	 * patk = (1 + crit_damage_rcpt) * (patk + skill_power)
	 * Для обычных атак
	 * damage = patk * ss_bonus * 70 / pdef
	 */
	public static AttackInfo calcPhysDam(L2Character attacker, L2Character target, L2Skill skill, boolean dual, boolean blow, boolean ss, boolean onCrit)
	{
		target.doCounterAttack(skill, attacker);

		AttackInfo info = new AttackInfo();

		info.damage = attacker.getPAtk(target);
		info.defence = target.getPDef(attacker);
		info.crit_rcpt = 0.01 * target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, attacker, skill);
		info.crit_static = attacker.calcStat(Stats.CRITICAL_DAMAGE_STATIC, target, skill);
		info.death_rcpt = 0.01 * target.calcStat(Stats.DEATH_RECEPTIVE, attacker, skill);
		info.lethal1 = skill == null ? 0 : skill.getLethal1() * info.death_rcpt;
		info.lethal2 = skill == null ? 0 : skill.getLethal2() * info.death_rcpt;
		info.crit = Rnd.chance(calcCrit(attacker, target, skill, blow));
		info.shld = (skill == null || !skill.getShieldIgnore()) && Formulas.calcShldUse(attacker, target);
		info.lethal = false;
		info.miss = false;
		double nonBlowLethalDam = 0;

		if(info.shld)
			info.defence += target.getShldDef();

		info.defence = Math.max(info.defence, 1);

		if(skill != null)
		{
			if(!blow && !target.isLethalImmune()) // считаем леталы для не blow скиллов
				if(Rnd.chance(info.lethal1))
				{
					if(target.isPlayer())
					{
						nonBlowLethalDam = target.getCurrentCp();
						target.sendPacket(Msg.CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL);
						info.lethal = true;
					}
					else
						nonBlowLethalDam = target.getCurrentHp() / 2;
					attacker.sendPacket(Msg.HALF_KILL);
				}
				else if(Rnd.chance(info.lethal2))
				{
					if(target.isPlayer())
					{
						nonBlowLethalDam = target.getCurrentHp() + target.getCurrentCp() - 1;
						target.sendPacket(Msg.LETHAL_STRIKE);
						info.lethal = true;
					}
					else
						nonBlowLethalDam = target.getCurrentHp() - 1;
					attacker.sendPacket(Msg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
				}

			if(skill.getPower(target) == 0)
			{
				info.damage = nonBlowLethalDam; // если скилл не имеет своей силы дальше идти бесполезно, можно сразу вернуть дамаг от летала
				if(nonBlowLethalDam > 0)
					attacker.sendPacket(new SystemMessage(SystemMessage.C1_HAS_GIVEN_C2_DAMAGE_OF_S3).addName(attacker).addName(target).addNumber((long) info.damage));
				return info;
			}

			if(blow && !skill.isBehind() && ss) // Для обычных blow не влияет на power
				info.damage *= 2.04;

			info.damage += Math.max(0., skill.getPower(target));

			if(blow && skill.isBehind() && ss) // Для backstab влияет на power, но меньше множитель
				info.damage *= 1.5;

			info.damage *= 1 + (Rnd.get() * attacker.getRandomDamage() * 2 - attacker.getRandomDamage()) / 100;

			if(blow)
			{
				info.damage *= info.crit_rcpt;
				info.damage *= 0.01 * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill);
				info.damage += 6.1 * info.crit_static;
			}

			if(skill.isChargeBoost())
				info.damage *= 0.8 + 0.2 * attacker.getIncreasedForce();

			if(skill.getSkillType() == SkillType.CHARGE)
				info.damage *= 2;
			else if(skill.isSoulBoost())
				info.damage *= 1.0 + 0.06 * Math.min(attacker.getConsumedSouls(), 5);

			if(info.crit)
				info.damage = 2.0 * info.crit_rcpt * info.damage;

			// Gracia Physical Skill Damage Bonus
			info.damage *= 1.10113;
		}
		else
		{
			info.damage *= 1 + (Rnd.get() * attacker.getRandomDamage() * 2 - attacker.getRandomDamage()) / 100;

			if(dual)
				info.damage /= 2.;

			if(info.crit)
			{
				info.damage *= 2.0 * info.crit_rcpt;
				info.damage *= 0.01 * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill);
				info.damage += info.crit_static;
			}
		}

		if(info.crit)
		{
			// шанс абсорбации души (без анимации) при крите, если Soul Mastery 4го уровня или более
			int chance = attacker.getSkillLevel(L2Skill.SKILL_SOUL_MASTERY);
			if(chance > 0)
			{
				if(chance >= 21)
					chance = 30;
				else if(chance >= 15)
					chance = 25;
				else if(chance >= 9)
					chance = 20;
				else if(chance >= 4)
					chance = 15;
				if(Rnd.chance(chance))
					attacker.setConsumedSouls(attacker.getConsumedSouls() + 1, null);
			}
		}

		switch(attacker.getDirectionTo(target, true))
		{
			case BEHIND:
				info.damage *= 1.2;
				break;
			case SIDE:
				info.damage *= 1.1;
				break;
		}

		if(ss)
			info.damage *= blow ? 1.0 : 2.0;

		info.damage *= 70. / info.defence;
		info.damage = attacker.calcStat(Stats.PHYSICAL_DAMAGE, info.damage, target, skill);

		// In C5 summons make 10 % less dmg in PvP.
		if(attacker.isSummon() && target.isPlayer())
			info.damage *= 0.9;

		if(info.shld && Rnd.chance(5))
			info.damage = 1;

		info.damage += nonBlowLethalDam; // леталы не от даггер скиллов добавляются после защиты

		// Тут проверяем только если skill != null, т.к. L2Character.onHitTimer не обсчитывает дамаг.
		if(skill != null)
		{
			if(info.shld)
				if(info.damage == 1)
					target.sendPacket(Msg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				else
					target.sendPacket(Msg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);

			if(Rnd.chance(target.calcStat(Stats.PSKILL_EVASION, 0, attacker, skill)))
			{
				attacker.sendPacket(new SystemMessage(SystemMessage.C1S_ATTACK_WENT_ASTRAY).addName(attacker));
				target.sendPacket(new SystemMessage(SystemMessage.C1_HAS_EVADED_C2S_ATTACK).addName(target).addName(attacker));
				info.damage = 1;
			}

			if(target.isMonster())
			{
				if(attacker.getActiveWeaponItem() != null)
				{			
					final L2Weapon weapon = attacker.getActiveWeaponItem();
					if(weapon.getItemType() == WeaponType.BOW || weapon.getItemType() == WeaponType.CROSSBOW)
						info.damage = 1.0 * attacker.calcStat(Stats.PVE_BOW_SKILL_DMG, info.damage, target, skill);
					else
						info.damage = 1.0 * attacker.calcStat(Stats.PVE_PHYS_SKILL_DMG, info.damage, target, skill);	
				}
 			}

			if(info.damage > 1 && skill.isDeathlink())
				info.damage *= 1.8 * (1.0 - attacker.getCurrentHpRatio());

			if(onCrit && !calcBlow(attacker, target, skill))
			{
				info.miss = true;
				info.damage = 0;
				attacker.sendPacket(new SystemMessage(SystemMessage.C1S_ATTACK_WENT_ASTRAY).addName(attacker));
			}

			if(blow)
				if(Rnd.chance(info.lethal1))
				{
					if(target.isPlayer())
					{
						info.damage = Math.max(info.damage, target.getCurrentCp());
						target.sendPacket(Msg.CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL);
						info.lethal = true;
					}
					else if(target.isLethalImmune())
						info.damage *= 2;
					else
						info.damage = Math.max(info.damage, target.getCurrentHp() / 2);
					attacker.sendPacket(Msg.HALF_KILL);
				}
				else if(Rnd.chance(info.lethal2))
				{
					if(target.isPlayer())
					{
						info.damage = Math.max(info.damage, target.getCurrentHp() + target.getCurrentCp() - 1);
						target.sendPacket(Msg.LETHAL_STRIKE);
						info.lethal = true;
					}
					else if(target.isLethalImmune())
						info.damage *= 3;
					else
						info.damage = Math.max(info.damage, target.getCurrentHp() - 1);
					attacker.sendPacket(Msg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
				}

			if(info.damage > 0)
				if(attacker instanceof L2Summon)
					((L2Summon) attacker).displayHitMessage(target, (int) info.damage, info.crit || blow, false);
				else if(attacker.isPlayer())
				{
					if(info.crit || blow)
						attacker.sendPacket(new SystemMessage(SystemMessage.C1_HAD_A_CRITICAL_HIT).addName(attacker));
					attacker.sendPacket(new SystemMessage(SystemMessage.C1_HAS_GIVEN_C2_DAMAGE_OF_S3).addName(attacker).addName(target).addNumber((long) info.damage));
				}

			if(target.isStunned() && calcStunBreak(info.crit))
			{
				target.getEffectList().stopEffects(EffectType.Stun);
				target.getEffectList().stopEffects(EffectType.Turner); // stun from bluff
			}

			if(calcCastBreak(target, info.crit))
				target.abortCast(false);
		}

		if (target.isMonster())
		{
			if(attacker.getActiveWeaponItem() != null)
			{
				final L2Weapon weapon = attacker.getActiveWeaponItem();
				if(weapon.getItemType() == WeaponType.BOW || weapon.getItemType() == WeaponType.CROSSBOW)
					info.damage = 1.0 * attacker.calcStat(Stats.PVE_BOW_DMG, info.damage, target, skill);
				else
					info.damage = 1.0 * attacker.calcStat(Stats.PVE_PHYSICAL_DMG, info.damage, target, skill);		
			}
		}

		info.damage = Math.max(1., info.damage);

		return info;
	}

	public static double calcMagicDam(L2Character attacker, L2Character target, L2Skill skill, int sps)
	{
		// Параметр ShieldIgnore для магических скиллов инвертирован
		boolean shield = skill.getShieldIgnore() && Formulas.calcShldUse(attacker, target);

		double mAtk = attacker.getMAtk(target, skill);

		if(sps == 2)
			mAtk *= 4;
		else if(sps == 1)
			mAtk *= 2;

		double mdef = target.getMDef(null, skill);

		if(shield)
			mdef += target.getShldDef();
		if(mdef == 0)
			mdef = 1;

		double power = skill.getPower(target);
		double lethalDamage = 0;

		if(Rnd.chance(skill.getLethal1()))
		{
			if(target.isPlayer())
			{
				lethalDamage = target.getCurrentCp();
				target.sendPacket(Msg.CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL);
			}
			else if(!target.isLethalImmune())
				lethalDamage = target.getCurrentHp() / 2;
			else
				power *= 2;
			attacker.sendPacket(Msg.HALF_KILL);
		}
		else if(Rnd.chance(skill.getLethal2()))
		{
			if(target.isPlayer())
			{
				lethalDamage = target.getCurrentHp() + target.getCurrentCp() - 1;
				target.sendPacket(Msg.LETHAL_STRIKE);
			}
			else if(!target.isLethalImmune())
				lethalDamage = target.getCurrentHp() - 1;
			else
				power *= 3;
			attacker.sendPacket(Msg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
		}

		if(power == 0)
		{
			if(lethalDamage > 0)
				attacker.sendPacket(new SystemMessage(SystemMessage.C1_HAS_GIVEN_C2_DAMAGE_OF_S3).addName(attacker).addName(target).addNumber((long) lethalDamage));
			return lethalDamage;
		}

		if(skill.isSoulBoost())
			power *= 1.0 + 0.06 * Math.min(attacker.getConsumedSouls(), 5);

		double damage = 91 * power * Math.sqrt(mAtk) / mdef;

		damage *= 1 + (Rnd.get() * attacker.getRandomDamage() * 2 - attacker.getRandomDamage()) / 100;

		boolean crit = calcMCrit(attacker.getMagicCriticalRate(target, skill));

		if(crit)
			damage *= attacker.calcStat(Stats.MCRITICAL_DAMAGE, attacker.isPlayable() && target.isPlayable() ? 2.5 : 3., target, skill);

		damage = attacker.calcStat(Stats.MAGIC_DAMAGE, damage, target, skill);
		
		if(target.isMonster())
			damage = attacker.calcStat(Stats.PVE_MAGICAL_DMG, damage, target, skill);

		if(shield)
		{
			if(Rnd.chance(5))
			{
				damage = 1;
				target.sendPacket(Msg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
			}
			else
				target.sendPacket(Msg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
			attacker.sendMessage("Spell deflected.");
		}

		int mLevel = skill.getMagicLevel() == 0 || !attacker.isPlayer() ? attacker.getLevel() : skill.getMagicLevel();
		int levelDiff = target.getLevel() - mLevel;

		if(levelDiff > -4) // Фейлы возможны даже на зеленых мобах
		{
			double magic_rcpt = target.calcStat(Stats.MAGIC_RECEPTIVE, attacker, skill) - attacker.calcStat(Stats.MAGIC_POWER, target, skill);
			double failChance = 5. * Math.max(1., levelDiff) * (1. + magic_rcpt / 100.);

			if(Rnd.chance(.1 * failChance))
			{
				damage = 1;
				SystemMessage msg = new SystemMessage(SystemMessage.C1_RESISTED_C2S_MAGIC).addName(target).addName(attacker);
				attacker.sendPacket(msg);
				target.sendPacket(msg);
			}
			else if(Rnd.chance(failChance))
			{
				damage /= 2;
				SystemMessage msg = new SystemMessage(SystemMessage.DAMAGE_IS_DECREASED_BECAUSE_C1_RESISTED_AGAINST_C2S_MAGIC).addName(target).addName(attacker);
				attacker.sendPacket(msg);
				target.sendPacket(msg);
			}
		}

		if(Rnd.chance(target.calcStat(Stats.MSKILL_EVASION, 0, attacker, skill)))
		{
			attacker.sendPacket(new SystemMessage(SystemMessage.C1S_ATTACK_WENT_ASTRAY).addName(attacker));
			target.sendPacket(new SystemMessage(SystemMessage.C1_HAS_EVADED_C2S_ATTACK).addName(target).addName(attacker));
			damage = 1;
		}

		if(damage > 1 && skill.isDeathlink())
			damage *= 1.8 * (1.0 - attacker.getCurrentHpRatio());

		if(damage > 1 && skill.isBasedOnTargetDebuff())
			damage *= 1 + 0.05 * target.getEffectList().getAllEffects().size();

		damage += lethalDamage;

		if(skill.getSkillType() == SkillType.MANADAM)
			damage = Math.max(1, damage / 2.);

		if(attacker instanceof L2Summon)
			((L2Summon) attacker).displayHitMessage(target, (int) damage, crit, false);
		else if(attacker.isPlayer())
		{
			if(crit)
				attacker.sendPacket(new SystemMessage(SystemMessage.MAGIC_CRITICAL_HIT).addName(attacker));
			attacker.sendPacket(new SystemMessage(SystemMessage.C1_HAS_GIVEN_C2_DAMAGE_OF_S3).addName(attacker).addName(target).addNumber((long) damage));
		}

		if(calcCastBreak(target, crit))
			target.abortCast(false);

		return damage;
	}

	public static boolean calcStunBreak(boolean crit)
	{
		return Rnd.chance(crit ? 75 : 10);
	}

	/** Returns true in case of fatal blow success */
	public static boolean calcBlow(L2Character activeChar, L2Character target, L2Skill skill)
	{
		L2Weapon weapon = activeChar.getActiveWeaponItem();

		double base_weapon_crit = weapon == null ? 4. : weapon.getCritical();
		double dex_bonus = DEXbonus[activeChar.getDEX()];
		double clamped_dz = Math.min(25, Math.max(-25, target.getZ() - activeChar.getZ()));
		double crit_height_bonus = 0.008 * clamped_dz + 1.1;
		double buffs_mult = activeChar.calcStat(Stats.FATALBLOW_RATE, target, skill);
		double skill_mod = skill.isBehind() ? 4 : 3;

		double chance = dex_bonus * base_weapon_crit * buffs_mult * crit_height_bonus * skill_mod;

		if(!target.isInCombat())
			chance *= 1.1;

		int head = activeChar.getHeadingTo(target, true);
		if(head <= 10430 || head >= 55105)
			chance *= 1.3;
		else if(skill.isBehind())
			chance = 3.0;
		else if(head <= 21000 || head >= 44500)
			chance *= 1.1;

		chance = Math.min(skill.isBehind() ? 100 : 80, chance);

		if(ConfigSystem.getBoolean("SkillsShowChance") && activeChar.isPlayer() && !((L2Player) activeChar).getVarB("SkillsHideChance")) // Выводим сообщение с шансом
			activeChar.sendMessage(new CustomMessage("l2rt.gameserver.skills.Formulas.Chance", activeChar).addString(skill.getName()).addNumber((long) chance));

		return Rnd.chance(chance);
	}

	/** Возвращает шанс крита в процентах */
	public static double calcCrit(L2Character attacker, L2Character target, L2Skill skill, boolean blow)
	{
		if(attacker.isPlayer() && attacker.getActiveWeaponItem() == null)
			return 0;
		if(skill != null)
			return skill.getCriticalRate() * (blow ? DEXbonus[attacker.getDEX()] : STRbonus[attacker.getSTR()]) * 0.01 * attacker.calcStat(Stats.SKILL_CRIT_CHANCE_MOD, target, skill);

		double rate = attacker.getCriticalHit(target, null) * 0.01 * target.calcStat(Stats.CRIT_CHANCE_RECEPTIVE, attacker, skill);

		switch(attacker.getDirectionTo(target, true))
		{
			case BEHIND:
				rate *= 1.4;
				break;
			case SIDE:
				rate *= 1.2;
				break;
		}

		return rate / 10;
	}

	public static boolean calcMCrit(double mRate)
	{
		// floating point random gives more accuracy calculation, because argument also floating point
		return Rnd.get() * 100 <= Math.min(ConfigSystem.getInt("LimitMCritical"), mRate);
	}

	public static boolean calcCastBreak(L2Character target, boolean crit)
	{
		if(target == null || target.isInvul() || target.isRaid() || !target.isCastingNow())
			return false;
		L2Skill skill = target.getCastingSkill();
		if(skill != null && (skill.getSkillType() == SkillType.TAKECASTLE || skill.getSkillType() == SkillType.TAKEFORTRESS || skill.getSkillType() == SkillType.TAKEFLAG))
			return false;
		return Rnd.chance(target.calcStat(Stats.CAST_INTERRUPT, crit ? 75 : 10, null, skill));
	}

	/** Calculate delay (in milliseconds) before next ATTACK */
	public static int calcPAtkSpd(double rate)
	{
		return (int) (500000 / rate); // в миллисекундах поэтому 500*1000
	}

	/** Calculate delay (in milliseconds) for skills cast */
	public static int calcMAtkSpd(L2Character attacker, L2Skill skill, double skillTime)
	{
		if(skill.isMagic())
			return (int) (skillTime * 333 / Math.max(attacker.getMAtkSpd(), 1));
		return (int) (skillTime * 333 / Math.max(attacker.getPAtkSpd(), 1));
	}

	/** Calculate reuse delay (in milliseconds) for skills */
	public static long calcSkillReuseDelay(L2Character actor, L2Skill skill)
	{
		long reuseDelay = skill.getReuseDelay();
		if(actor.isMonster())
			reuseDelay = skill.getReuseForMonsters();
		if(skill.isReuseDelayPermanent() || skill.isHandler() || skill.isItemSkill())
			return reuseDelay;
		if(actor.getSkillMastery(skill.getId()) == 1)
		{
			actor.removeSkillMastery(skill.getId());
			return 0;
		}
		if(skill.isMagic())
			return (long) actor.calcStat(Stats.MAGIC_REUSE_RATE, reuseDelay, null, skill);
		return (long) actor.calcStat(Stats.PHYSIC_REUSE_RATE, reuseDelay, null, skill);
	}

	/** Returns true if hit missed (target evaded) */
	public static boolean calcHitMiss(L2Character attacker, L2Character target)
	{
		int chanceToHit = 88 + 2 * (attacker.getAccuracy() - target.getEvasionRate(attacker));

		chanceToHit = Math.max(chanceToHit, 28);
		chanceToHit = Math.min(chanceToHit, 98);

		if(attacker.isBehindTarget(target))
			chanceToHit *= 1.2;
		if(attacker.isToSideOfTarget(target))
			chanceToHit *= 1.1;

		return !Rnd.chance(chanceToHit);
	}

	/** Returns true if shield defence successfull */
	public static boolean calcShldUse(L2Character attacker, L2Character target)
	{
		int angle = (int) target.calcStat(Stats.SHIELD_ANGLE, attacker, null);
		if(!target.isInFront(attacker, angle))
			return false;
		return Rnd.chance(target.calcStat(Stats.SHIELD_RATE, attacker, null));
	}

	public static double calcSavevsDependence(int save, L2Character cha)
	{
		try
		{
			/*
			switch(save)
			{
				case L2Skill.SAVEVS_INT:
					return INTbonus[cha.getINT()];
				case L2Skill.SAVEVS_WIT:
					return WITbonus[cha.getWIT()];
				case L2Skill.SAVEVS_MEN:
					return MENbonus[cha.getMEN()];
				case L2Skill.SAVEVS_CON:
					return CONbonus[cha.getCON()];
				case L2Skill.SAVEVS_DEX:
					return DEXbonus[cha.getDEX()];
				case L2Skill.SAVEVS_STR:
					return STRbonus[cha.getSTR()];
			}
			*/
			switch(save)
			{
				case L2Skill.SAVEVS_INT:
					return cha.getINT();
				case L2Skill.SAVEVS_WIT:
					return cha.getWIT();
				case L2Skill.SAVEVS_MEN:
					return cha.getMEN();
				case L2Skill.SAVEVS_CON:
					return cha.getCON();
				case L2Skill.SAVEVS_DEX:
					return cha.getDEX();
				case L2Skill.SAVEVS_STR:
					return cha.getSTR();
			}
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			_log.warning("Failed calc savevs on char " + cha + " with save-stat " + save);
			e.printStackTrace();
		}
		return 1.;
	}

	public static boolean calcSkillSuccess(Env env, Stats resistType, Stats attibuteType, int spiritshot)
	{
		if(env.value == -1)
			return true;

		L2Skill skill = env.skill;
		if(!skill.isOffensive())
			return Rnd.chance(env.value);

		L2Character character = env.character;
		L2Character target = env.target;

		env.value = Math.max(Math.min(env.value, 100), 1); // На всякий случай

		double base = env.value; // Запоминаем базовый шанс (нужен позже)

		double mLevel = skill.getMagicLevel() == 0 || !character.isPlayer() ? character.getLevel() : skill.getMagicLevel(); // Разница в уровнях

		mLevel = (mLevel - target.getLevel() + 3) * skill.getLevelModifier(); //Не пойму, зачем у них +3 стоит...

		env.value += mLevel >= 0 ? 0 : mLevel;

		boolean isGM = character.isPlayer() && ((L2Player) character).isGM();

		if(isGM)
		{
			character.sendMessage("magic level: " + mLevel);
			character.sendMessage("chance: " + env.value);
		}

		if(skill.getSavevs() > 0)
		{
			if(isGM)
			{
				character.sendMessage("skill: " + skill);
				character.sendMessage("chance: " + env.value);
				character.sendMessage("save type: " + skill.getSavevs());
			}

			env.value += 30 - calcSavevsDependence(skill.getSavevs(), target);
			// В принципе я считаю можно даже не лезть в формулу, а просто снизить множитель шанса прохождения в конфиге.

			if(isGM)
				character.sendMessage("chance w/savevs: " + env.value);
		}
		
		env.value = Math.max(env.value, 1);

		if(skill.isMagic()) // Этот блок только для магических скиллов
		{
			int mdef = Math.max(1, target.getMDef(target, skill)); // Вычисляем mDef цели
			double matk = character.getMAtk(target, skill);
			if(skill.isSSPossible() && spiritshot > 0) // Считаем бонус от шотов
				matk *= spiritshot * 2;
			env.value *= ConfigSystem.getDouble("SkillsChanceMod") * Math.pow(matk, ConfigSystem.getDouble("SkillsChancePow")) / mdef;
		}

		if(!skill.isIgnoreResists())
		{
			double res = 0;
			if(resistType != null)
				res += target.calcStat(resistType, character, skill);
			if(attibuteType != null)
				res -= character.calcStat(attibuteType, target, skill);

			res += target.calcStat(Stats.DEBUFF_RECEPTIVE, character, skill);

			if(res != 0)
			{
				double mod = Math.abs(0.02 * res) + 1;

				env.value = res > 0 ? env.value / mod : env.value * mod;

				if(isGM)
				{
					if(resistType != null)
					{
						character.sendMessage("resist: " + resistType);
						character.sendMessage("defense: " + (int) target.calcStat(resistType, character, skill));
					}

					if(attibuteType != null)
					{
						character.sendMessage("attack: " + (int) character.calcStat(attibuteType, target, skill));
						character.sendMessage("chance w/resist: " + env.value);
					}
				}
			}
		}

		env.value = character.calcStat(Stats.ACTIVATE_RATE, env.value, target, skill); // Учитываем общий бонус к шансам, если есть

		//if(skill.isSoulBoost()) // Бонус от душ камаелей
		//	env.value *= 0.85 + 0.06 * Math.min(character.getConsumedSouls(), 5);

		env.value = Math.max(env.value, Math.min(base, ConfigSystem.getDouble("SkillsChanceMin"))); // Если базовый шанс более Config.SKILLS_CHANCE_MIN, то при небольшой разнице в уровнях, делаем кап снизу.

		env.value = Math.max(Math.min(env.value, ConfigSystem.getDouble("SkillsChanceCap")), 1); // Применяем кап

		if(target.isPlayer())
		{
			L2Player player = (L2Player) target;
			if((ConfigSystem.getBoolean("SkillsShowChance") && character.isMonster() || player.isGM()) && player.getVarB("SkillsMobChance"))
				target.sendMessage(character.getName() + ": " + new CustomMessage("l2rt.gameserver.skills.Formulas.Chance", target).addString(skill.getName()).addNumber(Math.round(env.value)).toString());
		}

		if(ConfigSystem.getBoolean("SkillsShowChance") || isGM)
		{
			L2Player player = character.getPlayer();
			if(player != null && !player.getVarB("SkillsHideChance"))
				player.sendMessage(new CustomMessage("l2rt.gameserver.skills.Formulas.Chance", player).addString(skill.getName()).addNumber(Math.round(env.value)));
		}

		return Rnd.chance(env.value);
	}

	public static boolean calcSkillSuccess(L2Character player, L2Character target, L2Skill skill, int activateRate)
	{
		Env env = new Env();
		env.character = player;
		env.target = target;
		env.skill = skill;
		env.value = activateRate;
		switch(skill.getSkillType())
		{
			case CANCEL:
			case NEGATE_EFFECTS:
			case NEGATE_STATS:
			case STEAL_BUFF:
				return calcSkillSuccess(env, Stats.CANCEL_RECEPTIVE, Stats.CANCEL_POWER, player.getChargedSpiritShot());
			case DESTROY_SUMMON:
				return calcSkillSuccess(env, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, player.getChargedSpiritShot());
			default:
				return calcSkillSuccess(env, null, null, player.getChargedSpiritShot());
		}
	}

	public static void calcSkillMastery(L2Skill skill, L2Character activeChar)
	{
		if(skill.isHandler())
			return;

		//Skill id 330 for fighters, 331 for mages
		//Actually only GM can have 2 skill masteries, so let's make them more lucky ^^
		if(activeChar.getSkillLevel(331) > 0 && activeChar.calcStat(Stats.SKILL_MASTERY, activeChar.getINT(), null, skill) >= Rnd.get(1000) || activeChar.getSkillLevel(330) > 0 && activeChar.calcStat(Stats.SKILL_MASTERY, activeChar.getSTR(), null, skill) >= Rnd.get(1000))
		{
			//byte mastery level, 0 = no skill mastery, 1 = no reuseTime, 2 = buff duration*2, 3 = power*3
			byte masteryLevel;
			L2Skill.SkillType type = skill.getSkillType();
			if(skill.isMusic() || type == L2Skill.SkillType.BUFF || type == L2Skill.SkillType.HOT || type == L2Skill.SkillType.HEAL_PERCENT) //Hope i didn't forget skills to multiply their time
				masteryLevel = 2;
			else if(type == L2Skill.SkillType.HEAL)
				masteryLevel = 3;
			else
				masteryLevel = 1;
			if(masteryLevel > 0)
				activeChar.setSkillMastery(skill.getId(), masteryLevel);
		}
	}

	public static double calcDamageResists(L2Skill skill, L2Character attacker, L2Character defender, double value)
	{
		if(attacker == defender) // это дамаг от местности вроде ожога в лаве, наносится от своего имени
			return value; // TODO: по хорошему надо учитывать защиту, но поскольку эти скиллы немагические то надо делать отдельный механизм

		if(attacker.isBoss())
			value *= ConfigSystem.getDouble("RateEpicAttack");
		else if(attacker.isRaid() || attacker instanceof L2ReflectionBossInstance)
			value *= ConfigSystem.getDouble("RateRaidAttack");

		if(defender.isBoss())
			value /= ConfigSystem.getDouble("RateEpicDefense");
		else if(defender.isRaid() || defender instanceof L2ReflectionBossInstance)
			value /= ConfigSystem.getDouble("RateRaidDefense");

		L2Player pAttacker = attacker.getPlayer();

		// если уровень игрока ниже чем на 2 и более уровней моба 78+, то его урон по мобу снижается
		int diff = defender.getLevel() - (pAttacker != null ? pAttacker.getLevel() : attacker.getLevel());
		if(attacker.isPlayable() && defender.isMonster() && defender.getLevel() >= 78 && diff > 2)
			value *= .7 / Math.pow(diff - 2, .25);

		if(skill != null)
		{
			if(pAttacker != null && pAttacker.isGM())
			{
				attacker.sendMessage("skill element: " + skill.getElement());
				attacker.sendMessage("skill element power: " + skill.getElement().getAttack());
			}
			if(skill.getElement() == Element.NONE)
				return value;
			return applyDefense(attacker, defender, -defender.calcStat(skill.getElement().getDefence(), 0), attacker.calcStat(skill.getElement().getAttack(), skill.getElementPower()), value, skill);
		}

		TreeMap<Double, Stats> sort_attibutes = new TreeMap<Double, Stats>();
		for(Element e : Element.values())
			if(e != Element.NONE)
				sort_attibutes.put(attacker.calcStat(e.getAttack(), 0), e.getDefence());

		int attack = sort_attibutes.lastEntry().getKey().intValue();
		if(attack <= 0)
			return value;

		return applyDefense(attacker, defender, -defender.calcStat(sort_attibutes.lastEntry().getValue(), 0), attack, value, null);
	}

	public static double applyDefense(L2Character attacker, L2Character defender, double defense, double attack, double value, L2Skill skill)
	{
		if(skill == null || !skill.isMagic())
		{
			if(attacker.isPlayer() && ((L2Player) attacker).isGM() || ConfigSystem.getBoolean("AttribShowCalc"))
			{
				double mod = getElementMod(defense, attack, skill != null);
				attacker.sendMessage("--- element calc ---");
				attacker.sendMessage("defense: " + (int) defense);
				attacker.sendMessage("attack: " + (int) attack);
				attacker.sendMessage("skill: " + (skill == null ? "null" : skill));
				attacker.sendMessage("old value: " + (int) value);
				attacker.sendMessage("new value: " + (int) (value * mod));
				attacker.sendMessage("--------------------");
			}
			return value * getElementMod(defense, attack, skill != null);
		}

		double defenseFirst60 = Math.min(60, defense);

		value *= 1 + (attack - defenseFirst60) / 400.;

		if(defense <= defenseFirst60)
			return value;

		defense -= defenseFirst60;

		if(defense > 0 && Rnd.chance(defense / 3.))
		{
			value /= 2.;
			attacker.sendPacket(new SystemMessage(SystemMessage.DAMAGE_IS_DECREASED_BECAUSE_C1_RESISTED_AGAINST_C2S_MAGIC).addName(defender).addName(attacker));
		}

		return value;
	}

	/**
	 * Возвращает множитель для атаки из значений атакующего и защитного элемента. Только для простых атак и немагических скиллов.
	 * <br /><br />
	 * Для простых атак диапазон от 1.0 до 1.7
	 * <br /><br />
	 * Для скиллов от 1.0 до 2.0
	 * <br /><br />
	 * @param defense значение защиты
	 * @param attack значение атаки
	 * @param skill флаг использования скилла
	 * @return множитель
	 */
	private static double getElementMod(double defense, double attack, boolean skill)
	{
		double diff = attack - defense;
		if(diff <= 0)
			return 1.0;
		else if(!skill)
			return 1.0 + 0.007 * Math.min(Math.max(diff, -20), 100);
		else if(diff < 75)
			return 1.0 + diff * 0.0052;
		else if(diff < 150)
			return 1.4;
		else if(diff < 290)
			return 1.7;
		else if(diff < 300)
			return 1.8;
		else
			return 2.0;
	}

	/**
	 * Используется только для отображения в окне информации
	 */
	public static int[] calcAttackElement(L2Character attacker)
	{
		TreeMap<Double, Integer> sort_attibutes = new TreeMap<Double, Integer>();
		for(Element e : Element.values())
			if(e != Element.NONE)
				sort_attibutes.put(attacker.calcStat(e.getAttack(), 0), e.getId());

		Entry<Double, Integer> element = sort_attibutes.lastEntry();
		if(element.getKey().intValue() <= 0)
			return null;

		return new int[] { element.getValue(), element.getKey().intValue() };
	}
}