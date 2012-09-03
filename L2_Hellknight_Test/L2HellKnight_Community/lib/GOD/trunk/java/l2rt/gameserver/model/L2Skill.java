package l2rt.gameserver.model;

import l2rt.config.ConfigSystem;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.base.ClassId;
import l2rt.gameserver.model.entity.Duel;
import l2rt.gameserver.model.entity.vehicle.L2AirShip;
import l2rt.gameserver.model.entity.vehicle.L2Ship;
import l2rt.gameserver.model.entity.vehicle.L2Vehicle;
import l2rt.gameserver.model.instances.*;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.FlyToLocation.FlyType;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.EffectType;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.Formulas;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.skills.conditions.Condition;
import l2rt.gameserver.skills.effects.EffectTemplate;
import l2rt.gameserver.skills.funcs.Func;
import l2rt.gameserver.skills.funcs.FuncTemplate;
import l2rt.gameserver.skills.skillclasses.*;
import l2rt.gameserver.skills.skillclasses.DeathPenalty;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2Weapon.WeaponType;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.ArrayMap;
import l2rt.util.GArray;
import l2rt.util.Rnd;
import l2rt.util.Util;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class L2Skill implements Cloneable
{
	public static class AddedSkill
	{
		public int id;
		public int level;

		public AddedSkill(int id, int level)
		{
			this.id = id;
			this.level = level;
		}

		public L2Skill getSkill()
		{
			return SkillTable.getInstance().getInfo(id, level);
		}
	}

	public static enum Element
	{
		FIRE(0, Stats.ATTACK_ELEMENT_FIRE, Stats.FIRE_RECEPTIVE),
		WATER(1, Stats.ATTACK_ELEMENT_WATER, Stats.WATER_RECEPTIVE),
		WIND(2, Stats.ATTACK_ELEMENT_WIND, Stats.WIND_RECEPTIVE),
		EARTH(3, Stats.ATTACK_ELEMENT_EARTH, Stats.EARTH_RECEPTIVE),
		SACRED(4, Stats.ATTACK_ELEMENT_SACRED, Stats.SACRED_RECEPTIVE),
		UNHOLY(5, Stats.ATTACK_ELEMENT_UNHOLY, Stats.UNHOLY_RECEPTIVE),
		NONE(6, null, null);

		private final int id;
		private final Stats attack;
		private final Stats defence;

		private Element(int id, Stats attack, Stats defence)
		{
			this.id = id;
			this.attack = attack;
			this.defence = defence;
		}

		public int getId()
		{
			return id;
		}

		public Stats getAttack()
		{
			return attack;
		}

		public Stats getDefence()
		{
			return defence;
		}

		public static Element getElementById(int id)
		{
			for(Element e : values())
				if(e.getId() == id)
					return e;
			return NONE;
		}
	}

	public static enum NextAction
	{
		ATTACK,
		CAST,
		DEFAULT,
		MOVE,
		NONE
	}

	public static enum SkillOpType
	{
		OP_ACTIVE,
		OP_PASSIVE,
		OP_TOGGLE,
		OP_ON_ACTION;
	}

	public static enum TriggerActionType
	{
		ADD, // скилл срабатывает при добавлении в лист
		ATTACK, // OP_ON_ATTACK
		CRIT, // OP_ON_CRIT
		//SKILL_USE,
		//MAGIC_SKILL_USE,
		OFFENSIVE_PHYSICAL_SKILL_USE,
		OFFENSIVE_MAGICAL_SKILL_USE, // OP_ON_MAGIC_ATTACK
		SUPPORT_MAGICAL_SKILL_USE, // OP_ON_MAGIC_SUPPORT
		UNDER_ATTACK, // OP_ON_UNDER_ATTACK
		UNDER_MISSED_ATTACK, // OP_ON_EVASION
		UNDER_SKILL_ATTACK, // OP_ON_UNDER_SKILL_ATTACK
		//UNDER_MAGIC_SKILL_ATTACK,
		//UNDER_OFFENSIVE_SKILL_ATTACK,
		//UNDER_MAGIC_SUPPORT,
		DIE,
	}

	public static enum SkillTargetType
	{
		TARGET_ALLY,
		TARGET_AREA,
		TARGET_AREA_AIM_CORPSE,
		TARGET_AURA,
		TARGET_PET_AURA,
		TARGET_CHEST,
		TARGET_CLAN,
		TARGET_CLAN_ONLY,
		TARGET_CORPSE,
		TARGET_CORPSE_PLAYER,
		TARGET_ENEMY_PET,
		TARGET_ENEMY_SUMMON,
		TARGET_ENEMY_SERVITOR,
		TARGET_FLAGPOLE,
		TARGET_HOLY,
		TARGET_ITEM,
		TARGET_MULTIFACE,
		TARGET_MULTIFACE_AURA,
		TARGET_TUNNEL,
		TARGET_NONE,
		TARGET_ONE,
		TARGET_OWNER,
		TARGET_PARTY,
		TARGET_PARTY_ONE,
		TARGET_PET,
		TARGET_SELF,
		TARGET_SIEGE,
		TARGET_UNLOCKABLE
	}

	public static enum SkillType
	{
		AGGRESSION(Aggression.class),
		AIEFFECTS(AIeffects.class),
		BALANCE(Balance.class),
		BEAST_FEED(BeastFeed.class),
		BLEED(Continuous.class),
		BUFF(Continuous.class),
		BUFF_CHARGER(BuffCharger.class),
		CALL(Call.class),
		CANCEL(Cancel.class),
		CHARGE(Charge.class),
		CHARGE_SOUL(ChargeSoul.class),
		COMBATPOINTHEAL(CombatPointHeal.class),
		CONT(Toggle.class),
		CPDAM(CPDam.class),
		CPHOT(Continuous.class),
		CRAFT(Craft.class),
		DEATH_PENALTY(DeathPenalty.class),
		DEBUFF(Continuous.class),
		DELETE_HATE(DeleteHate.class),
		DELETE_HATE_OF_ME(DeleteHateOfMe.class),
		DESTROY_SUMMON(DestroySummon.class),
		DEFUSE_TRAP(DefuseTrap.class),
		DETECT_TRAP(DetectTrap.class),
		DISCORD(Continuous.class),
		DOT(Continuous.class),
		ITEM_R(ItemR.class),
		DRAIN(Drain.class),
		DRAIN_SOUL(DrainSoul.class),
		EFFECT(Effect.class),
		EFFECTS_FROM_SKILLS(EffectsFromSkills.class),
		ENCHANT_ARMOR,
		ENCHANT_WEAPON,
		EXTRACT_STONE(ExtractStone.class),
		FEED_PET,
		FISHING(Fishing.class),
		HARDCODED(Effect.class),
		HARVESTING(Harvesting.class),
		HEAL(Heal.class),
		HEAL_PERCENT(HealPercent.class),
		HOT(Continuous.class),
		KAMAEL_WEAPON_EXCHANGE(KamaelWeaponExchange.class),
		LETHAL_SHOT(LethalShot.class),
		LUCK,
		MANADAM(ManaDam.class),
		MANAHEAL(ManaHeal.class),
		MANAHEAL_PERCENT(ManaHealPercent.class),
		MDAM(MDam.class),
		MDOT(Continuous.class),
		MPHOT(Continuous.class),
		MUTE(Disablers.class),
		NEGATE_EFFECTS(NegateEffects.class),
		NEGATE_STATS(NegateStats.class),
		NOTDONE,
		NOTUSED,
		OUTPOST(Outpost.class),
		PARALYZE(Disablers.class),
		PASSIVE,
		PDAM(PDam.class),
		POISON(Continuous.class),
		PUMPING(ReelingPumping.class),
		RECALL(Recall.class),
		REELING(ReelingPumping.class),
		REFILL(Refill.class),
		RESURRECT(Resurrect.class),
		RIDE(Ride.class),
		ROOT(Disablers.class),
		SHIFT_AGGRESSION(ShiftAggression.class),
		SIEGEFLAG(SiegeFlag.class),
		SLEEP(Disablers.class),
		SOULSHOT,
		SOWING(Sowing.class),
		SPHEAL(SPHeal.class),
		SPIRITSHOT,
		SPOIL(Spoil.class),
		STEAL_BUFF(StealBuff.class),
		STUN(Disablers.class),
		SUMMON(Summon.class),
		SUMMON_ITEM(SummonItem.class),
		SWEEP(Sweep.class),
		TAKECASTLE(TakeCastle.class),
		TAKEFORTRESS(TakeFortress.class),
		TAKEFLAG(TakeFlag.class),
		TELEPORT_NPC(TeleportNpc.class),
		TRANSFORMATION(Transformation.class),
		UNLOCK(Unlock.class),
		WATCHER_GAZE(Continuous.class);

		private final Class<? extends L2Skill> clazz;

		private SkillType()
		{
			clazz = Default.class;
		}

		private SkillType(Class<? extends L2Skill> clazz)
		{
			this.clazz = clazz;
		}

		public L2Skill makeSkill(StatsSet set)
		{
			try
			{
				Constructor<? extends L2Skill> c = clazz.getConstructor(StatsSet.class);
				return c.newInstance(set);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	protected static Logger _log = Logger.getLogger(L2Skill.class.getName());

	protected static final TreeMap<TriggerActionType, Double> EMPTY_ACTIONS = new TreeMap<TriggerActionType, Double>();
	protected static final AddedSkill[] _emptyAddedSkills = new AddedSkill[0];
	protected static final Func[] _emptyFunctionSet = new Func[0];

	protected static final HashMap<Integer, GArray<Integer>> _reuseGroups = new HashMap<Integer, GArray<Integer>>();

	protected EffectTemplate[] _effectTemplates;
	protected FuncTemplate[] _funcTemplates;

	protected TreeMap<TriggerActionType, Double> _triggerActions;
	protected GArray<Integer> _teachers; // which NPC teaches
	protected GArray<ClassId> _canLearn; // which classes can learn

	protected AddedSkill[] _addedSkills;

	protected final int[] _itemConsume;
	protected final int[] _itemConsumeId;

	public static final int SKILL_CUBIC_MASTERY = 143;
	public static final int SKILL_CRAFTING = 172;
	public static final int SKILL_POLEARM_MASTERY = 216;
	public static final int SKILL_CRYSTALLIZE = 248;
	public static final int SKILL_WEAPON_MAGIC_MASTERY1 = 249;
	public static final int SKILL_WEAPON_MAGIC_MASTERY2 = 250;
	public static final int SKILL_BLINDING_BLOW = 321;
	public static final int SKILL_STRIDER_ASSAULT = 325;
	public static final int SKILL_WYVERN_AEGIS = 327;
	public static final int SKILL_BLUFF = 358;
	public static final int SKILL_HEROIC_MIRACLE = 395;
	public static final int SKILL_HEROIC_BERSERKER = 396;
	public static final int SKILL_SOUL_MASTERY = 467;
	public static final int SKILL_TRANSFOR_DISPELL = 619;
	public static final int SKILL_FINAL_FLYING_FORM = 840;
	public static final int SKILL_AURA_BIRD_FALCON = 841;
	public static final int SKILL_AURA_BIRD_OWL = 842;
	public static final int SKILL_DETECTION = 933;
	public static final int SKILL_RECHARGE = 1013;
	public static final int SKILL_TRANSFER_PAIN = 1262;
	public static final int SKILL_FISHING_MASTERY = 1315;
	public static final int SKILL_NOBLESSE_BLESSING = 1323;
	public static final int SKILL_SUMMON_CP_POTION = 1324;
	public static final int SKILL_FORTUNE_OF_NOBLESSE = 1325;
	public static final int SKILL_HARMONY_OF_NOBLESSE = 1326;
	public static final int SKILL_SYMPHONY_OF_NOBLESSE = 1327;
	public static final int SKILL_HEROIC_VALOR = 1374;
	public static final int SKILL_HEROIC_GRANDEUR = 1375;
	public static final int SKILL_HEROIC_DREAD = 1376;
	public static final int SKILL_MYSTIC_IMMUNITY = 1411;
	public static final int SKILL_RAID_BLESSING = 2168;
	public static final int SKILL_DISMISS_AGATHION = 3267;
	public static final int SKILL_HINDER_STRIDER = 4258;
	public static final int SKILL_WYVERN_BREATH = 4289;
	public static final int SKILL_RAID_CURSE = 4515;
	public static final int SKILL_CHARM_OF_COURAGE = 5041;
	public static final int SKILL_EVENT_TIMER = 5239;
	public static final int SKILL_BATTLEFIELD_DEATH_SYNDROME = 5660;

	public static final int[] SKILLS_S80_AND_S84_SETS = {
			3416,
			8210,
			3354,
			8211,
			3355,
			8212,
			3356,
			8213,
			3357,
			8214,
			3412,
			8202,
			3348,
			8203,
			3349,
			8204,
			3350,
			8205,
			3351,
			8206,
			3413,
			8207,
			3352,
			8208,
			3353,
			8209,
			3414,
			8215,
			3415,
			8216,
			3420,
			8217,
			3420,
			8218,
			3645,
			8229,
			3646,
			8230,
			3647,
			8231,
			3648,
			8232,
			3636,
			8219,
			3637,
			8220,
			3638,
			8221,
			3639,
			8222,
			3640,
			8223,
			3641,
			8224,
			3642,
			8225,
			3643,
			8226,
			3644,
			8227,
			3805,
			8228,
			8283,
			8285,
			8287,
			8284,
			8286,
			8288,
			8301,
			8303,
			8305,
			8302,
			8304,
			8306,
			8403,
			8404,
			8405,
			8412,
			8413,
			8414,
			8400,
			8401,
			8402,
			8409,
			8410,
			8411,
			8397,
			8398,
			8399,
			8406,
			8407,
			8408,
			13075,
			13074,
			13085,
			13084,
			13065,
			13066,
			13077,
			13087,
			13067,
			13076,
			13086,
			13068,
			13079,
			13089,
			13069,
			13078,
			13088,
			13070,
			13081,
			13091,
			13071,
			13080,
			13090 };

	public final static int SAVEVS_CON = 4;
	public final static int SAVEVS_DEX = 5;
	public final static int SAVEVS_INT = 1;
	public final static int SAVEVS_MEN = 3;
	public final static int SAVEVS_STR = 6;
	public final static int SAVEVS_WIT = 2;

	protected boolean _isAltUse;
	protected boolean _isBehind;
	protected boolean _isCancelable;
	protected boolean _isCorpse;
	protected boolean _isCommon;
	protected boolean _isItemHandler;
	protected Boolean _isOffensive;
	protected Boolean _isPvpSkill;
	protected Boolean _isPvm;
	protected boolean _isForceUse;
	protected boolean _isMagic;
	protected boolean _isSaveable;
	protected boolean _isSkillTimePermanent;
	protected boolean _isReuseDelayPermanent;
	protected boolean _isSuicideAttack;
	protected boolean _isShieldignore;
	protected boolean _isUndeadOnly;
	protected Boolean _isUseSS;
	protected boolean _isOverhit;
	protected boolean _isSoulBoost;
	protected boolean _isChargeBoost;
	protected boolean _isUsingWhileCasting;
	protected boolean _skillInterrupt;
	protected boolean _deathlink;
	protected boolean _basedOnTargetDebuff;
	protected boolean _isNotUsedByAI;
	protected boolean _isIgnoreResists;
	protected boolean _isMusic;
	protected boolean _isNotAffectedByMute;
	protected boolean _flyingTransformUsage;
	protected boolean _isOlympiadEnabled;

	protected SkillType _skillType;
	protected SkillOpType _operateType;
	protected SkillTargetType _targetType;
	protected NextAction _nextAction;
	protected Element _element;
	protected FlyType _flyType;
	protected boolean _flyToBack;
	protected Condition[] _preCondition;

	protected Integer _id;
	protected Short _level;
	protected Short _baseLevel;
	protected Integer _displayId;
	protected Short _displayLevel;

	protected int _activateRate;
	protected int _castRange;
	protected int _cancelTarget;
	protected int _condCharges;
	protected int _coolTime;
	protected int _delayedEffect;
	protected int _effectPoint;
	protected int _elementPower;
	protected int _flyRadius;
	protected int _forceId;
	protected int _hitTime;
	protected int _hpConsume;
	protected int _levelModifier;
	protected int _magicLevel;
	protected int _matak;
	protected int _minPledgeClass;
	protected int _minRank;
	protected int _negatePower;
	protected int _negateSkill;
	protected int _npcId;
	protected int _numCharges;
	protected int _reuseGroupId;
	protected int _savevs;
	protected int _skillInterruptTime;
	protected int _skillRadius;
	protected int _soulsConsume;
	protected int _symbolId;
	protected int _weaponsAllowed;
	protected int _castCount;
	protected int _enchantLevelCount;
	protected int _criticalRate;

	protected long _reuseDelay;

	protected double _power;
	protected double _powerPvP;
	protected double _powerPvE;
	protected double _mpConsume1;
	protected double _mpConsume2;
	protected double _lethal1;
	protected double _lethal2;
	protected double _absorbPart;

	protected String _name;
	protected String _baseValues;

	public boolean _isStandart = false;

	// Жрет много памяти, включить только если будет необходимость
	//protected StatsSet _set;

	/**
	 * Внимание!!! У наследников вручную надо поменять тип на public
	 * @param set парамерты скилла
	 */
	protected L2Skill(StatsSet set)
	{
		//_set = set;
		_id = set.getInteger("skill_id");
		_level = set.getShort("level");
		_displayId = set.getInteger("displayId", _id);
		_displayLevel = set.getShort("displayLevel", _level);
		_name = set.getString("name");
		_operateType = set.getEnum("operateType", SkillOpType.class);
		_isMagic = set.getBool("isMagic", false);
		_isAltUse = set.getBool("altUse", false);
		_mpConsume1 = set.getInteger("mpConsume1", 0);
		_mpConsume2 = set.getInteger("mpConsume2", 0);
		_hpConsume = set.getInteger("hpConsume", 0);
		_soulsConsume = set.getInteger("soulsConsume", 0);
		_isSoulBoost = set.getBool("soulBoost", false);
		_isChargeBoost = set.getBool("chargeBoost", false);
		_isUsingWhileCasting = set.getBool("isUsingWhileCasting", false);
		_matak = set.getInteger("mAtk", 0);
		_isUseSS = set.getBool("useSS", null);
		_forceId = set.getInteger("forceId", 0);
		_magicLevel = set.getInteger("magicLevel", 0);
		_castCount = set.getInteger("castCount", 0);

		_baseValues = set.getString("baseValues", null);

		if(_operateType == SkillOpType.OP_ON_ACTION)
		{
			StringTokenizer st = new StringTokenizer(set.getString("triggerActions", ""), ";");
			if(st.hasMoreTokens())
			{
				_triggerActions = new TreeMap<TriggerActionType, Double>();
				while(st.hasMoreTokens())
				{
					TriggerActionType tat = Enum.valueOf(TriggerActionType.class, st.nextToken());
					Double chance = Double.valueOf(st.nextToken());
					_triggerActions.put(tat, chance);
				}
			}
		}

		if(_triggerActions == null)
			_triggerActions = EMPTY_ACTIONS;

		String s1 = set.getString("itemConsumeCount", "");
		String s2 = set.getString("itemConsumeId", "");

		if(s1.length() == 0)
			_itemConsume = new int[] { 0 };
		else
		{
			String[] s = s1.split(" ");
			_itemConsume = new int[s.length];
			for(int i = 0; i < s.length; i++)
				_itemConsume[i] = Integer.parseInt(s[i]);
		}

		if(s2.length() == 0)
			_itemConsumeId = new int[] { 0 };
		else
		{
			String[] s = s2.split(" ");
			_itemConsumeId = new int[s.length];
			for(int i = 0; i < s.length; i++)
				_itemConsumeId[i] = Integer.parseInt(s[i]);
		}

		_isItemHandler = set.getBool("isHandler", false);
		_reuseGroupId = set.getInteger("reuseGroup", 0);
		if(_reuseGroupId > 0)
		{
			if(_reuseGroups.get(_reuseGroupId) == null)
				_reuseGroups.put(_reuseGroupId, new GArray<Integer>());
			if(!_reuseGroups.get(_reuseGroupId).contains(_id))
				_reuseGroups.get(_reuseGroupId).add(_id);
		}

		_isCommon = set.getBool("isCommon", false);
		_isSaveable = set.getBool("isSaveable", true);
		_coolTime = set.getInteger("coolTime", 0);
		_skillInterruptTime = set.getInteger("skillInterruptTime", 0);
		
		_reuseDelay = set.getLong("reuseDelay", 0);
		
		if (ConfigSystem.getBoolean("EnableModifySkillReuse") && ConfigSystem.SKILL_REUSE_LIST.containsKey(_id))
			_reuseDelay = ConfigSystem.SKILL_REUSE_LIST.get(_id);
		else
			_reuseDelay = set.getLong("reuseDelay", 0);
		
		_skillRadius = set.getInteger("skillRadius", 80);
		_targetType = set.getEnum("target", SkillTargetType.class);
		_isUndeadOnly = set.getBool("undeadOnly", false);
		_isCorpse = set.getBool("corpse", false);
		_power = set.getDouble("power", 0.);
		_powerPvP = set.getDouble("powerPvP", 0.);
		_powerPvE = set.getDouble("powerPvE", 0.);
		_effectPoint = set.getInteger("effectPoint", 0);
		_nextAction = NextAction.valueOf(set.getString("nextAction", "DEFAULT").toUpperCase());
		_skillType = set.getEnum("skillType", SkillType.class);
		_isSuicideAttack = set.getBool("isSuicideAttack", false);
		_isSkillTimePermanent = set.getBool("isSkillTimePermanent", false);
		_isReuseDelayPermanent = set.getBool("isReuseDelayPermanent", false);
		_deathlink = set.getBool("deathlink", false);
		_basedOnTargetDebuff = set.getBool("basedOnTargetDebuff", false);
		_isNotUsedByAI = set.getBool("isNotUsedByAI", false);
		_isIgnoreResists = set.getBool("isIgnoreResists", false);
		_isMusic = set.getBool("isMusic", false);
		_isNotAffectedByMute = set.getBool("isNotAffectedByMute", false);
		_flyingTransformUsage = set.getBool("flyingTransformUsage", false);
		_isOlympiadEnabled = set.getBool("isOlympiadEnabled", false);

		if(Util.isNumber(set.getString("element", "NONE")))
			_element = Element.getElementById(set.getInteger("element", 6));
		else
			_element = Element.valueOf(set.getString("element", "NONE").toUpperCase());

		_elementPower = set.getInteger("elementPower", 0);

		if(Util.isNumber(set.getString("save", "0")))
			_savevs = set.getInteger("save", 0);
		else
			try
			{
				_savevs = L2Skill.class.getField("SAVEVS_" + set.getString("save").toUpperCase()).getInt(null);
			}
			catch(Exception e)
			{
				_log.warning("Invalid savevs value: " + set.getString("save"));
				e.printStackTrace();
			}

		_activateRate = set.getInteger("activateRate", -1);
		_levelModifier = set.getInteger("levelModifier", 1);
		_isCancelable = set.getBool("cancelable", true);
		_isShieldignore = set.getBool("shieldignore", false);
		_criticalRate = set.getInteger("criticalRate", 0);
		_isOverhit = set.getBool("overHit", false);
		_weaponsAllowed = set.getInteger("weaponsAllowed", 0);
		_minPledgeClass = set.getInteger("minPledgeClass", 0);
		_minRank = set.getInteger("minRank", 0);
		_isOffensive = set.getBool("isOffensive", null);
		_isPvpSkill = set.getBool("isPvpSkill", null);
		_isPvm = set.getBool("isPvm", null);
		_isForceUse = set.getBool("isForceUse", false);
		_isBehind = set.getBool("behind", false);
		_symbolId = set.getInteger("symbolId", 0);
		_npcId = set.getInteger("npcId", 0);
		_flyType = FlyType.valueOf(set.getString("flyType", "NONE").toUpperCase());
		_flyToBack = set.getBool("flyToBack", false);
		_flyRadius = set.getInteger("flyRadius", 200);
		_negateSkill = set.getInteger("negateSkill", 0);
		_negatePower = set.getInteger("negatePower", Integer.MAX_VALUE);
		_numCharges = set.getInteger("num_charges", 0);
		_condCharges = set.getInteger("cond_charges", 0);
		_delayedEffect = set.getInteger("delayedEffect", 0);
		_cancelTarget = set.getInteger("cancelTarget", 0);
		_skillInterrupt = set.getBool("skillInterrupt", false);
		_lethal1 = set.getDouble("lethal1", 0);
		_lethal2 = set.getDouble("lethal2", 0);
		_absorbPart = set.getFloat("absorbPart", 0.f);

		StringTokenizer st = new StringTokenizer(set.getString("addSkills", ""), ";");
		while(st.hasMoreTokens())
		{
			int id = Integer.valueOf(st.nextToken());
			int level = Integer.valueOf(st.nextToken());
			if(level == -1)
				level = _level;
			_addedSkills = (AddedSkill[]) Util.addElementToArray(_addedSkills, new AddedSkill(id, level), AddedSkill.class);
		}

		if(_nextAction == NextAction.DEFAULT)
			switch(_skillType)
			{
				case PDAM:
				case CPDAM:
				case LETHAL_SHOT:
				case SPOIL:
				case SOWING:
				case STUN:
				case DRAIN_SOUL:
					_nextAction = NextAction.ATTACK;
					break;
				default:
					_nextAction = NextAction.NONE;
			}

		if(_savevs == 0)
			switch(_skillType)
			{
				case BLEED:
				case DOT:
				case MDOT:
				case LETHAL_SHOT:
				case PDAM:
				case CPDAM:
				case POISON:
				case STUN:
					_savevs = SAVEVS_CON;
					break;
				case CANCEL:
				case MANADAM:
				case DEBUFF:
				case MDAM:
				case MUTE:
				case PARALYZE:
				case ROOT:
				case SLEEP:
					_savevs = SAVEVS_MEN;
					break;
			}

		String canLearn = set.getString("canLearn", null);
		if(canLearn == null)
			_canLearn = null;
		else
		{
			_canLearn = new GArray<ClassId>();
			st = new StringTokenizer(canLearn, " \r\n\t,;");
			while(st.hasMoreTokens())
			{
				String cls = st.nextToken();
				try
				{
					_canLearn.add(ClassId.valueOf(cls));
				}
				catch(Throwable t)
				{
					_log.log(Level.SEVERE, "Bad class " + cls + " to learn skill", t);
				}
			}
		}

		String teachers = set.getString("teachers", null);
		if(teachers == null)
			_teachers = null;
		else
		{
			_teachers = new GArray<Integer>();
			st = new StringTokenizer(teachers, " \r\n\t,;");
			while(st.hasMoreTokens())
			{
				String npcid = st.nextToken();
				try
				{
					_teachers.add(Integer.parseInt(npcid));
				}
				catch(Throwable t)
				{
					_log.log(Level.SEVERE, "Bad teacher id " + npcid + " to teach skill", t);
				}
			}
		}
	}

	public final boolean getWeaponDependancy(L2Character activeChar)
	{
		if(_weaponsAllowed == 0)
			return true;

		if(activeChar.getActiveWeaponInstance() != null && activeChar.getActiveWeaponItem() != null)
			if((activeChar.getActiveWeaponItem().getItemType().mask() & _weaponsAllowed) != 0)
				return true;

		if(activeChar.getSecondaryWeaponInstance() != null && activeChar.getSecondaryWeaponItem() != null)
			if((activeChar.getSecondaryWeaponItem().getItemType().mask() & _weaponsAllowed) != 0)
				return true;

		if(isActive())
		{
			StringBuffer skillmsg = new StringBuffer();
			skillmsg.append(_name);
			skillmsg.append(" can only be used with weapons of type ");
			for(WeaponType wt : WeaponType.values())
				if((wt.mask() & _weaponsAllowed) != 0)
					skillmsg.append(wt).append('/');
			skillmsg.setCharAt(skillmsg.length() - 1, '.');
			activeChar.sendMessage(skillmsg.toString());
		}

		return false;
	}

	public boolean checkCondition(L2Character activeChar, L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		L2Player player = activeChar.getPlayer();

		if(activeChar.isDead())
			return false;

		if(target != null && (activeChar.getReflection() != target.getReflection() || target != activeChar && target.isInvisible() && getId() != SKILL_DETECTION))
		{
			activeChar.sendPacket(Msg.CANNOT_SEE_TARGET);
			return false;
		}

		if(!getWeaponDependancy(activeChar))
			return false;

		if(first && activeChar.isSkillDisabled(_id))
		{
			activeChar.sendReuseMessage(this);
			return false;
		}

		if(first && activeChar.getCurrentMp() < (isMagic() ? activeChar.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, _mpConsume1 + _mpConsume2, target, this) : activeChar.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, _mpConsume1 + _mpConsume2, target, this)))
		{
			activeChar.sendPacket(Msg.NOT_ENOUGH_MP);
			return false;
		}

		if(activeChar.getCurrentHp() < _hpConsume + 1)
		{
			activeChar.sendPacket(Msg.NOT_ENOUGH_HP);
			return false;
		}

		if(!(_isItemHandler || _isAltUse) && activeChar.isMuted(this))
			return false;

		if(_soulsConsume > activeChar.getConsumedSouls())
		{
			activeChar.sendPacket(Msg.THERE_IS_NOT_ENOUGHT_SOUL);
			return false;
		}

		// TODO перенести потребление из формул сюда
		if(activeChar.getIncreasedForce() < _condCharges || activeChar.getIncreasedForce() < _numCharges)
		{
			activeChar.sendPacket(Msg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_);
			return false;
		}

		if(player != null)
		{
			if(!isOlympiadEnabled() && player.isInOlympiadMode())
			{
				player.sendPacket(Msg.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
				return false;
			}

			if(player.isInFlyingTransform() && _isItemHandler && !flyingTransformUsage())
			{
				player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(getItemConsumeId()[0]));
				return false;
			}

			if(player.isInVehicle())
			{
				L2Vehicle vehicle = player.getVehicle();
				// На воздушных кораблях можно использовать скилы-хэндлеры всем кроме капитана
				if(vehicle.isAirShip() && (!_isItemHandler || ((L2AirShip) vehicle).getDriver() == player))
					return false;

				// С морских кораблей можно ловить рыбу
				if(vehicle instanceof L2Ship && !(this instanceof Fishing || this instanceof ReelingPumping))
					return false;
			}

			if(player.inObserverMode())
			{
				activeChar.sendPacket(Msg.OBSERVERS_CANNOT_PARTICIPATE);
				return false;
			}

			if(first && _itemConsume[0] > 0)
				for(int i = 0; i < _itemConsume.length; i++)
				{
					Inventory inv = ((L2Playable) activeChar).getInventory();
					if(inv == null)
						inv = player.getInventory();
					L2ItemInstance requiredItems = inv.getItemByItemId(_itemConsumeId[i]);
					if(requiredItems == null || requiredItems.getCount() < _itemConsume[i])
					{
						if(activeChar == player)
							player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
						return false;
					}
				}

			if(player.isFishing() && _id != 1312 && _id != 1313 && _id != 1314 && !altUse())
			{
				if(activeChar == player)
					player.sendPacket(Msg.ONLY_FISHING_SKILLS_ARE_AVAILABLE);
				return false;
			}
		}

		if(getFlyType() == FlyType.CHARGE && (activeChar.isImobilised() || activeChar.isRooted()) || getFlyType() == FlyType.THROW_HORIZONTAL && (activeChar.isImobilised() || activeChar.isRooted()) || getFlyType() == FlyType.THROW_UP && (activeChar.isImobilised() || activeChar.isRooted()))
		{
			activeChar.getPlayer().sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}

		// Fly скиллы нельзя использовать слишком близко
		if(first && target != null && getFlyType() == FlyType.CHARGE && activeChar.isInRange(target.getLoc(), Math.min(150, getFlyRadius())))
		{
			activeChar.getPlayer().sendPacket(Msg.THERE_IS_NOT_ENOUGH_SPACE_TO_MOVE_THE_SKILL_CANNOT_BE_USED);
			return false;
		}

		SystemMessage msg = checkTarget(activeChar, target, target, forceUse, first);
		if(msg != null && activeChar.getPlayer() != null)
		{
			activeChar.getPlayer().sendPacket(msg);
			return false;
		}

		if(_preCondition == null || _preCondition.length == 0)
			return true;

		Env env = new Env();
		env.character = activeChar;
		env.skill = this;
		env.target = target;

		if(first)
			for(Condition с : _preCondition)
				if(с != null && !с.test(env))
				{
					String cond_msg = с.getMessage();
					if(cond_msg != null)
						activeChar.sendMessage(cond_msg);
					return false;
				}

		return true;
	}

	public SystemMessage checkTarget(L2Character activeChar, L2Character target, L2Character aimingTarget, boolean forceUse, boolean first)
	{
		if(target == activeChar && isNotTargetAoE() || target == activeChar.getPet() && _targetType == SkillTargetType.TARGET_PET_AURA)
			return null;
		if(target == null || isOffensive() && target == activeChar)
			return Msg.THAT_IS_THE_INCORRECT_TARGET;
		if(activeChar.getReflection() != target.getReflection()) // Массовые атаки должны попадать по дагерам в Hide. Если потребуется убрать - раскомментировать. || target != activeChar && target.isInvisible() && getId() != SKILL_DETECTION)
			return Msg.CANNOT_SEE_TARGET;
		// Попадает ли цель в радиус действия в конце каста
		if(!first && target != activeChar && target == aimingTarget && getCastRange() > 0 && getCastRange() != 32767 && !activeChar.isInRange(target.getLoc(), getCastRange() + (getCastRange() < 200 ? 400 : 500)))
			return Msg.YOUR_TARGET_IS_OUT_OF_RANGE;
		// Для этих скиллов дальнейшие проверки не нужны
		if(_skillType == SkillType.TAKECASTLE || _skillType == SkillType.TAKEFORTRESS || _skillType == SkillType.TAKEFLAG)
			return null;
		// Конусообразные скиллы
		if(!first && target != activeChar && (_targetType == SkillTargetType.TARGET_MULTIFACE || _targetType == SkillTargetType.TARGET_MULTIFACE_AURA || _targetType == SkillTargetType.TARGET_TUNNEL) && (_isBehind ? activeChar.isInFront(target, 120) : !activeChar.isInFront(target, 60)))
			return Msg.YOUR_TARGET_IS_OUT_OF_RANGE;
		// Проверка на каст по трупу
		if(target.isDead() != _isCorpse && _targetType != SkillTargetType.TARGET_AREA_AIM_CORPSE || _isUndeadOnly && !target.isUndead())
			return Msg.INVALID_TARGET;
		if(target.isMonster() && ((L2MonsterInstance) target).isDying())
			return Msg.INVALID_TARGET;
		// Нельзя юзать дебафы и масс-скиллы на НПЦ и гвардов (но не осадных).
		if((target instanceof L2NpcInstance && !target.isMonster()) && !target.isSiegeGuard() && (_skillType == SkillType.DEBUFF || isAoE()))
			return Msg.INVALID_TARGET;
		if(_targetType != SkillTargetType.TARGET_UNLOCKABLE && target.isDoor() && !((L2DoorInstance) target).isAttackable(activeChar))
			return Msg.INVALID_TARGET;
		// Для различных бутылок, и для скилла кормления, дальнейшие проверки не нужны
		if(_isAltUse || _skillType == SkillType.BEAST_FEED || _targetType == SkillTargetType.TARGET_UNLOCKABLE || _targetType == SkillTargetType.TARGET_CHEST)
			return null;
		if(activeChar.isPlayable())
		{
			L2Player player = activeChar.getPlayer();
			if(player == null)
				return Msg.THAT_IS_THE_INCORRECT_TARGET;
			// Запрет на атаку мирных NPC в осадной зоне на TW. Иначе таким способом набивают очки.
			if(player.getTerritorySiege() > -1 && target.isNpc() && !(target instanceof L2TerritoryFlagInstance) && !(target.getAI() instanceof DefaultAI) && player.isInZone(ZoneType.Siege))
				return Msg.INVALID_TARGET;
			if(target.isPlayable())
			{
				if(isPvM())
					return Msg.THAT_IS_THE_INCORRECT_TARGET;

				L2Player pcTarget = target.getPlayer();
				if(pcTarget == null)
					return Msg.THAT_IS_THE_INCORRECT_TARGET;

				if(player.isInZone(ZoneType.epic) != pcTarget.isInZone(ZoneType.epic))
					return Msg.THAT_IS_THE_INCORRECT_TARGET;

				if(pcTarget.isInOlympiadMode() && (!player.isInOlympiadMode() || player.getOlympiadGameId() != pcTarget.getOlympiadGameId())) // На всякий случай
					return Msg.THAT_IS_THE_INCORRECT_TARGET;

				if(player.getTeam() > 0 && player.isChecksForTeam() && pcTarget.getTeam() == 0) // Запрет на атаку/баф участником эвента незарегистрированного игрока
					return Msg.THAT_IS_THE_INCORRECT_TARGET;
				if(pcTarget.getTeam() > 0 && pcTarget.isChecksForTeam() && player.getTeam() == 0) // Запрет на атаку/баф участника эвента незарегистрированным игроком
					return Msg.THAT_IS_THE_INCORRECT_TARGET;

				if(isOffensive())
				{
					if(player.isInOlympiadMode() && !player.isOlympiadCompStart()) // Бой еще не начался
						return Msg.INVALID_TARGET;
					if(player.isInOlympiadMode() && player.isOlympiadCompStart() && player.getOlympiadSide() == pcTarget.getOlympiadSide()) // Свою команду атаковать нельзя
						return Msg.THAT_IS_THE_INCORRECT_TARGET;
					if(player.getTeam() > 0 && player.isChecksForTeam() && pcTarget.getTeam() > 0 && pcTarget.isChecksForTeam() && player.getTeam() == pcTarget.getTeam()) // Свою команду атаковать нельзя
						return Msg.THAT_IS_THE_INCORRECT_TARGET;
					if(isAoE() && getCastRange() < Integer.MAX_VALUE && !GeoEngine.canSeeTarget(activeChar, target, activeChar.isFlying()))
						return Msg.CANNOT_SEE_TARGET;
					if(activeChar.isInZoneBattle() != target.isInZoneBattle() && !player.getPlayerAccess().PeaceAttack)
						return Msg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE;
					if((activeChar.isInZonePeace() || target.isInZonePeace()) && !player.getPlayerAccess().PeaceAttack)
						return Msg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE;

					if(activeChar.isInZoneBattle())
					{
						if(!forceUse && !isForceUse() && player.getParty() != null && player.getParty() == pcTarget.getParty())
							return Msg.INVALID_TARGET;
						return null; // Остальные условия на аренах и на олимпиаде проверять не требуется
					}

					// Только враг и только если он еше не проиграл.
					Duel duel1 = player.getDuel();
					Duel duel2 = pcTarget.getDuel();
					if(player != pcTarget && duel1 != null && duel1 == duel2)
					{
						if(duel1.getTeamForPlayer(pcTarget) == duel1.getTeamForPlayer(player))
							return Msg.INVALID_TARGET;
						if(duel1.getDuelState(player.getStoredId()) != Duel.DuelState.Fighting)
							return Msg.INVALID_TARGET;
						if(duel1.getDuelState(pcTarget.getStoredId()) != Duel.DuelState.Fighting)
							return Msg.INVALID_TARGET;
						return null;
					}

					if(isPvpSkill() || !forceUse || isAoE())
					{
						if(player == pcTarget)
							return Msg.INVALID_TARGET;
						if(player.getParty() != null && player.getParty() == pcTarget.getParty())
							return Msg.INVALID_TARGET;
						if(player.getClanId() != 0 && player.getClanId() == pcTarget.getClanId())
							return Msg.INVALID_TARGET;
						if(player.getDuel() != null && pcTarget.getDuel() != player.getDuel())
							return Msg.INVALID_TARGET;
					}

					if(activeChar.isInZone(ZoneType.Siege) && target.isInZone(ZoneType.Siege))
					{
						if(player.getTerritorySiege() > -1 && player.getTerritorySiege() == pcTarget.getTerritorySiege())
							return Msg.INVALID_TARGET;
						L2Clan clan1 = player.getClan();
						L2Clan clan2 = pcTarget.getClan();
						if(clan1 == null || clan2 == null)
							return null;
						if(clan1.getSiege() == null || clan2.getSiege() == null)
							return null;
						if(clan1.getSiege() != clan2.getSiege())
							return null;
						if(clan1.isDefender() && clan2.isDefender())
							return Msg.INVALID_TARGET;
						if(clan1.getSiege().isMidVictory())
							return null;
						if(clan1.isAttacker() && clan2.isAttacker())
							return Msg.INVALID_TARGET;
						return null;
					}

					if(player.atMutualWarWith(pcTarget))
						return null;
					if(isForceUse())
						return null;
					// Защита от развода на флаг с копьем
					if(!forceUse && player.getPvpFlag() == 0 && pcTarget.getPvpFlag() != 0 && aimingTarget != target)
						return Msg.INVALID_TARGET;
					if(pcTarget.getPvpFlag() != 0)
						return null;
					if(pcTarget.getKarma() < 0)
						return null;
					if(forceUse && !isPvpSkill() && (!isAoE() || aimingTarget == target))
						return null;

					return Msg.INVALID_TARGET;
				}

				if(pcTarget == player)
					return null;

				if(player.isInOlympiadMode() && player.getOlympiadSide() != pcTarget.getOlympiadSide()) // Чужой команде помогать нельзя
					return Msg.THAT_IS_THE_INCORRECT_TARGET;
				if(player.getTeam() > 0 && player.isChecksForTeam() && pcTarget.getTeam() > 0 && pcTarget.isChecksForTeam() && player.getTeam() != pcTarget.getTeam()) // Чужой команде помогать нельзя
					return Msg.THAT_IS_THE_INCORRECT_TARGET;

				if(!activeChar.isInZoneBattle() && target.isInZoneBattle())
					return Msg.INVALID_TARGET;
				if(activeChar.isInZonePeace() && !target.isInZonePeace())
					return Msg.INVALID_TARGET;

				if(forceUse || isForceUse())
					return null;

				if(player.getDuel() != null && pcTarget.getDuel() != player.getDuel())
					return Msg.INVALID_TARGET;
				if(player != pcTarget && player.getDuel() != null && pcTarget.getDuel() != null && pcTarget.getDuel() == pcTarget.getDuel())
					return Msg.INVALID_TARGET;

				if(player.getParty() != null && player.getParty() == pcTarget.getParty())
					return null;
				if(player.getClanId() != 0 && player.getClanId() == pcTarget.getClanId())
					return null;

				if(player.atMutualWarWith(pcTarget))
					return Msg.INVALID_TARGET;
				if(pcTarget.getPvpFlag() != 0)
					return Msg.INVALID_TARGET;
				if(pcTarget.getKarma() < 0)
					return Msg.INVALID_TARGET;

				return null;
			}
		}

		if(isAoE() && isOffensive() && getCastRange() < Integer.MAX_VALUE && !GeoEngine.canSeeTarget(activeChar, target, activeChar.isFlying()))
			return Msg.CANNOT_SEE_TARGET;
		if(!forceUse && !isForceUse() && !isOffensive() && target.isAutoAttackable(activeChar))
			return Msg.INVALID_TARGET;
		if(!forceUse && !isForceUse() && isOffensive() && !target.isAutoAttackable(activeChar))
			return Msg.INVALID_TARGET;
		if(!target.isAttackable(activeChar))
			return Msg.INVALID_TARGET;

		return null;
	}

	public final L2Character getAimingTarget(L2Character activeChar, L2Object obj)
	{
		L2Character target = obj == null || !obj.isCharacter() ? null : (L2Character) obj;
		switch(_targetType)
		{
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_PARTY:
			case TARGET_CLAN_ONLY:
			case TARGET_SELF:
				return activeChar;
			case TARGET_AURA:
			case TARGET_MULTIFACE_AURA:
				return activeChar;
			case TARGET_HOLY:
				return target != null && activeChar.isPlayer() && target.isArtefact() ? target : null;
			case TARGET_FLAGPOLE:
				return activeChar.isPlayer() && target instanceof L2StaticObjectInstance && ((L2StaticObjectInstance) target).getType() == 3 ? target : null;
			case TARGET_UNLOCKABLE:
				return target != null && target.isDoor() || target instanceof L2ChestInstance ? target : null;
			case TARGET_CHEST:
				return target instanceof L2ChestInstance ? target : null;
			case TARGET_PET:
			case TARGET_PET_AURA:
				target = activeChar.getPet();
				return target != null && target.isDead() == _isCorpse ? target : null;
			case TARGET_OWNER:
				if(activeChar.isSummon())
					target = activeChar.getPlayer();
				else
					return null;
				return target != null && target.isDead() == _isCorpse ? target : null;
			case TARGET_ENEMY_PET:
				if(target == null || target == activeChar.getPet() || !target.isPet())
					return null;
				return target;
			case TARGET_ENEMY_SUMMON:
				if(target == null || target == activeChar.getPet() || !target.isSummon())
					return null;
				return target;
			case TARGET_ENEMY_SERVITOR:
				if(target == null || target == activeChar.getPet() || !(target instanceof L2Summon))
					return null;
				return target;
			case TARGET_ONE:
				return target != null && target.isDead() == _isCorpse && !(target == activeChar && isOffensive()) && (!_isUndeadOnly || target.isUndead()) ? target : null;
			case TARGET_PARTY_ONE:
				if(target == null)
					return null;
				L2Player player = activeChar.getPlayer();
				L2Player ptarget = target.getPlayer();
				// self or self pet.
				if(ptarget != null && ptarget.equals(activeChar))
					return target;
				// olympiad party member or olympiad party member pet.
				if(player != null && player.isInOlympiadMode() && ptarget != null && player.getOlympiadSide() == ptarget.getOlympiadSide() && player.getOlympiadGameId() == ptarget.getOlympiadGameId() && target.isDead() == _isCorpse && !(target == activeChar && isOffensive()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				// party member or party member pet.
				if(ptarget != null && player != null && player.getParty() != null && player.getParty().containsMember(ptarget) && target.isDead() == _isCorpse && !(target == activeChar && isOffensive()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				return null;
			case TARGET_AREA:
			case TARGET_MULTIFACE:
			case TARGET_TUNNEL:
				return target != null && target.isDead() == _isCorpse && !(target == activeChar && isOffensive()) && (!_isUndeadOnly || target.isUndead()) ? target : null;
			case TARGET_AREA_AIM_CORPSE:
				return target != null && target.isDead() ? target : null;
			case TARGET_CORPSE:
				return target != null && target.isNpc() && target.isDead() ? target : null;
			case TARGET_CORPSE_PLAYER:
				return target != null && target.isPlayable() && target.isDead() ? target : null;
			case TARGET_SIEGE:
				return target != null && !target.isDead() && (target.isDoor() || target instanceof L2ControlTowerInstance) ? target : null;
			default:
				activeChar.sendMessage("Target type of skill is not currently handled");
				return null;
		}
	}

	public GArray<L2Character> getTargets(L2Character activeChar, L2Character aimingTarget, boolean forceUse)
	{
		GArray<L2Character> targets = new GArray<L2Character>();
		if(oneTarget())
		{
			targets.add(aimingTarget);
			return targets;
		}

		switch(_targetType)
		{
			case TARGET_AREA_AIM_CORPSE:
			case TARGET_AREA:
			case TARGET_MULTIFACE:
			case TARGET_TUNNEL:
			{
				if(aimingTarget.isDead() == _isCorpse && (!_isUndeadOnly || aimingTarget.isUndead()))
					targets.add(aimingTarget);
				addTargetsToList(targets, aimingTarget, activeChar, forceUse);
				break;
			}
			case TARGET_AURA:
			case TARGET_MULTIFACE_AURA:
			{
				addTargetsToList(targets, activeChar, activeChar, forceUse);
				break;
			}
			case TARGET_PET_AURA:
			{
				if(activeChar.getPet() == null)
					break;
				addTargetsToList(targets, activeChar.getPet(), activeChar, forceUse);
				break;
			}
			case TARGET_PARTY:
			case TARGET_CLAN:
			case TARGET_CLAN_ONLY:
			case TARGET_ALLY:
			{
				if(activeChar.isMonster() || activeChar.isSiegeGuard())
				{
					targets.add(activeChar);
					for(L2Character c : L2World.getAroundCharacters(activeChar, _skillRadius, 128))
						if(!c.isDead() && (c.isMonster() || c.isSiegeGuard()) /*&& ((L2MonsterInstance) c).getFactionId().equals(mob.getFactionId())*/)
							targets.add(c);
					break;
				}
				L2Player player = activeChar.getPlayer();
				if(player == null)
				{
					if(activeChar.isPet() || activeChar.isSummon())
						break;
					System.out.println("L2Skill.getTargets | player = null | activeChar = " + activeChar + "[" + activeChar.getNpcId() + "] | SkillID: " + getId());
					Thread.dumpStack();
					break;
				}
				if(player.isInOlympiadMode())
				{
					addOlympiadTargetsToList(targets, player);
					addTargetAndPetToList(targets, player, player);
					break;
				}
				for(L2Player target : L2World.getAroundPlayers(player, _skillRadius, 128))
				{
					boolean check = false;
					switch(_targetType)
					{
						case TARGET_PARTY:
							check = player.getParty() != null && player.getParty() == target.getParty();
							break;
						case TARGET_CLAN:
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId() || player.getParty() != null && target.getParty() == player.getParty();
							break;
						case TARGET_CLAN_ONLY:
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId();
							break;
						case TARGET_ALLY:
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId() || player.getAllyId() != 0 && target.getAllyId() == player.getAllyId();
							break;
					}
					if(!check)
						continue;
					if(checkTarget(player, target, aimingTarget, forceUse, false) != null)
						continue;
					addTargetAndPetToList(targets, player, target);
				}
				addTargetAndPetToList(targets, player, player);
				break;
			}
		}
		return targets;
	}

	private void addTargetAndPetToList(GArray<L2Character> targets, L2Player actor, L2Player target)
	{
		if((actor == target || actor.isInRange(target, _skillRadius)) && target.isDead() == _isCorpse)
			targets.add(target);
		L2Summon pet = target.getPet();
		if(pet != null && actor.isInRange(pet, _skillRadius) && pet.isDead() == _isCorpse)
			targets.add(pet);
	}

	private void addOlympiadTargetsToList(GArray<L2Character> targets, L2Player player)
	{
		if(!_isCorpse)
			targets.add(player);
		L2Summon pet = player.getPet();
		if(pet != null && pet.isDead() == _isCorpse)
			targets.add(pet);
		for(L2Player target : L2World.getAroundPlayers(player, _skillRadius, 128))
		{
			if(player.getOlympiadSide() != target.getOlympiadSide()) // Чужой команде помогать нельзя
				continue;
			if(player.getOlympiadGameId() != target.getOlympiadGameId()) // Команде на чужой арене помогать нельзя
				continue;
			addTargetAndPetToList(targets, player, target);
		}
	}

	private void addTargetsToList(GArray<L2Character> targets, L2Character aimingTarget, L2Character activeChar, boolean forceUse)
	{
		int count = 0;
		L2Territory terr = null;
		if(_targetType == SkillTargetType.TARGET_TUNNEL)
		{
			// Создаем параллелепипед ("косой" по вертикали)

			int radius = 100;
			int zmin1 = activeChar.getZ() - 200;
			int zmax1 = activeChar.getZ() + 200;
			int zmin2 = aimingTarget.getZ() - 200;
			int zmax2 = aimingTarget.getZ() + 200;

			double angle = Util.convertHeadingToDegree(activeChar.getHeading());
			double radian1 = Math.toRadians(angle - 90);
			double radian2 = Math.toRadians(angle + 90);

			terr = new L2Territory(0);
			terr.add(activeChar.getX() + (int) (Math.cos(radian1) * radius), activeChar.getY() + (int) (Math.sin(radian1) * radius), zmin1, zmax1);
			terr.add(activeChar.getX() + (int) (Math.cos(radian2) * radius), activeChar.getY() + (int) (Math.sin(radian2) * radius), zmin1, zmax1);
			terr.add(aimingTarget.getX() + (int) (Math.cos(radian2) * radius), aimingTarget.getY() + (int) (Math.sin(radian2) * radius), zmin2, zmax2);
			terr.add(aimingTarget.getX() + (int) (Math.cos(radian1) * radius), aimingTarget.getY() + (int) (Math.sin(radian1) * radius), zmin2, zmax2);

			if(activeChar.isPlayer() && ((L2Player) activeChar).isGM())
			{
				activeChar.sendPacket(Functions.Points2Trace(terr.getCoords(), 50, true, false));
				activeChar.sendPacket(Functions.Points2Trace(terr.getCoords(), 50, true, true));
			}
		}
		for(L2Character target : aimingTarget.getAroundCharacters(_skillRadius, 128))
		{
			if(terr != null && !terr.isInside(target))
				continue;
			if(target == null || activeChar == target || activeChar.getPlayer() != null && activeChar.getPlayer() == target.getPlayer())
				continue;
			if(getId() == SKILL_DETECTION && target.isInvisible() && target.getEffectList().getEffectByType(EffectType.Invisible) != null)
				target.getEffectList().stopEffects(EffectType.Invisible);
			if(checkTarget(activeChar, target, aimingTarget, forceUse, false) != null)
				continue;
			if(!(activeChar instanceof L2DecoyInstance) && activeChar.isNpc() && target.isNpc())
				continue;
			targets.add(target);
			count++;
			if(count >= 20 && !activeChar.isRaid())
				break;
		}
	}

	/**
	 * Создает и применяет эффекты скилла. Выполняется в отдельном потоке.
	 */
	public final void getEffects(final L2Character effector, final L2Character effected, final boolean calcChance, final boolean applyOnCaster)
	{
		if(isPassive() || _effectTemplates == null || _effectTemplates.length == 0 || effector == null || effected == null)
			return;

		// Mystic Immunity Makes a target temporarily immune to buffs/debuffs
		if(effected.isEffectImmune())
		{
			//effector.sendMessage(new CustomMessage("l2rt.gameserver.skills.Formulas.NoChance", effector).addString(effected.getName()).addString(getName()));
			effector.sendPacket(new SystemMessage(SystemMessage.C1_HAS_RESISTED_YOUR_S2).addString(effected.getName()).addSkillName(_displayId, _displayLevel));
			return;
		}

		// No effect on invulnerable characters unless they cast it themselves.
		if(effector != effected && isOffensive() && effected.isInvul())
		{
			if(effector.isPlayer())
				effector.sendPacket(new SystemMessage(SystemMessage.C1_HAS_RESISTED_YOUR_S2).addString(effected.getName()).addSkillName(_displayId, _displayLevel));
			return;
		}

		// No effect on doors/walls
		if(effected.isDoor())
			return;

		final int sps = effector.getChargedSpiritShot();

		ThreadPoolManager.getInstance().executeGeneral(new Runnable(){
			@Override
			public void run()
			{
				final int mastery = effector.getSkillMastery(getId());
				if(mastery == 2 && !applyOnCaster)
					effector.removeSkillMastery(getId());

				boolean success = false;

				loop: for(EffectTemplate et : _effectTemplates)
				{
					if(applyOnCaster != et._applyOnCaster || et._counter == 0)
						continue;

					L2Character target = et._applyOnCaster ? effector : effected;

					if(et._stackOrder == -1)
						if(et._stackType != EffectTemplate.NO_STACK)
						{
							for(L2Effect e : target.getEffectList().getAllEffects())
								if(e.getStackType().equalsIgnoreCase(et._stackType))
									continue loop;
						}
						else if(target.getEffectList().getEffectsBySkillId(getId()) != null)
							continue;

					if(target.isRaid() && et.getEffectType().isRaidImmune())
					{
						effector.sendPacket(new SystemMessage(SystemMessage.C1_HAS_RESISTED_YOUR_S2).addString(effected.getName()).addSkillName(_displayId, _displayLevel));
						continue;
					}

					if(isBlockedByChar(target, et))
						continue;

					Env env = new Env(effector, target, L2Skill.this);

					int chance = et.getParam().getInteger("activateRate", getActivateRate());
					if(calcChance && !et._applyOnCaster)
					{
						env.value = chance;
						if(!Formulas.calcSkillSuccess(env, et.getEffectType().getResistType(), et.getEffectType().getAttibuteType(), sps))
						{
							effector.sendPacket(new SystemMessage(SystemMessage.C1_HAS_RESISTED_YOUR_S2).addString(effected.getName()).addSkillName(_displayId, _displayLevel));
							continue;
						}
					}

					if(target != effector && isOffensive() && !effector.isTrap())
						if(Rnd.chance(target.calcStat(isMagic() ? Stats.REFLECT_MAGIC_DEBUFF : Stats.REFLECT_PHYSIC_DEBUFF, 0, effector, L2Skill.this)))
						{
							target.sendPacket(new SystemMessage(SystemMessage.YOU_COUNTERED_C1S_ATTACK).addName(effector));
							effector.sendPacket(new SystemMessage(SystemMessage.C1_DODGES_THE_ATTACK).addName(target));
							target = effector;
							env.target = target;
						}

					if(success)
						env.arraymap = ArrayMap.set(env.arraymap, Env.FirstEffectSuccess, Integer.MAX_VALUE);

					if(mastery != 0)
						env.arraymap = ArrayMap.set(env.arraymap, Env.SkillMastery, mastery);

					final L2Effect e = et.getEffect(env);
					if(e != null)
					{
						if(chance > 0)
							success = true;
						if(e._count == 1 && e.getPeriod() == 0)
						{
							// Эффекты однократного действия не шедулятся, а применяются немедленно
							// Как правило это побочные эффекты для скиллов моментального действия
							e.onStart();
							e.onActionTime();
							e.onExit();
						}
						else
							e.getEffected().getEffectList().addEffect(e);
					}
					L2Summon pet = effected.getPet(); 
					if (pet != null) 
					{ 
						for (EffectTemplate et1 : getEffectTemplates()) 
						{ 
							if(getSkillType() == SkillType.BUFF) 
							{ 
								Env env1 = new Env(effector, pet, L2Skill.this); 
								L2Effect effect = et1.getEffect(env1); 
								effect.setPeriod(effect.getPeriod()); 
								pet.getEffectList().addEffect(effect); 
								pet.updateEffectIcons(); 
							} 
						}                
					}   
				}

				if(calcChance)
					if(success)
						effector.sendPacket(new SystemMessage(SystemMessage.S1_HAS_SUCCEEDED).addSkillName(_displayId, _displayLevel));
					else
						effector.sendPacket(new SystemMessage(SystemMessage.S1_HAS_FAILED).addSkillName(_displayId, _displayLevel));
			}
		});
	}

	public final void attach(EffectTemplate effect)
	{
		if(_effectTemplates == null)
			_effectTemplates = new EffectTemplate[] { effect };
		else
		{
			int len = _effectTemplates.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplates = tmp;
		}
	}

	public final void attach(FuncTemplate f)
	{
		if(_funcTemplates == null)
			_funcTemplates = new FuncTemplate[] { f };
		else
		{
			int len = _funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}

	public final Func[] getStatFuncs()
	{
		if(_funcTemplates == null)
			return _emptyFunctionSet;
		GArray<Func> funcs = new GArray<Func>();
		for(FuncTemplate t : _funcTemplates)
		{
			Func f = t.getFunc(this); // skill is owner
			if(f != null)
				funcs.add(f);
		}
		if(funcs.size() == 0)
			return _emptyFunctionSet;
		return funcs.toArray(new Func[funcs.size()]);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		final L2Skill other = (L2Skill) obj;
		if(_displayId == null)
		{
			if(other._displayId != null)
				return false;
		}
		else if(!_displayId.equals(other._displayId))
			return false;
		if(_displayLevel == null)
		{
			if(other._displayLevel != null)
				return false;
		}
		else if(!_displayLevel.equals(other._displayLevel))
			return false;
		if(_id == null)
		{
			if(other._id != null)
				return false;
		}
		else if(!_id.equals(other._id))
			return false;
		if(_level == null)
		{
			if(other._level != null)
				return false;
		}
		else if(!_level.equals(other._level))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (_displayId == null ? 0 : _displayId.hashCode());
		result = prime * result + (_displayLevel == null ? 0 : _displayLevel.hashCode());
		result = prime * result + (_id == null ? 0 : _id.hashCode());
		result = prime * result + (_level == null ? 0 : _level.hashCode());
		return result;
	}

	public final void attach(Condition c)
	{
		_preCondition = (Condition[]) Util.addElementToArray(_preCondition, c, Condition.class);
	}

	public final boolean altUse()
	{
		return _isAltUse;
	}

	public final boolean canTeachBy(int npcId)
	{
		return _teachers == null || _teachers.contains(npcId);
	}

	public final int getActivateRate()
	{
		return _activateRate;
	}

	public AddedSkill[] getAddedSkills()
	{
		return _addedSkills == null ? _emptyAddedSkills : _addedSkills;
	}

	public final boolean getCanLearn(ClassId cls)
	{
		return _canLearn == null || _canLearn.contains(cls);
	}

	/**
	 * @return Returns the castRange.
	 */
	public final int getCastRange()
	{
		return _castRange;
	}

	public final int getAOECastRange()
	{
		return Math.max(_castRange, _skillRadius);
	}

	public int getCondCharges()
	{
		return _condCharges;
	}

	public final int getCoolTime()
	{
		return _coolTime;
	}

	public boolean getCorpse()
	{
		return _isCorpse;
	}

	public int getDelayedEffect()
	{
		return _delayedEffect;
	}

	public final int getDisplayId()
	{
		return _displayId;
	}

	public short getDisplayLevel()
	{
		return _displayLevel;
	}

	public int getEffectPoint()
	{
		return _effectPoint;
	}

	public EffectTemplate[] getEffectTemplates()
	{
		return _effectTemplates;
	}

	public L2Effect getSameByStackType(ConcurrentLinkedQueue<L2Effect> ef_list)
	{
		if(_effectTemplates == null)
			return null;
		L2Effect ret;
		for(EffectTemplate et : _effectTemplates)
			if(et != null && (ret = et.getSameByStackType(ef_list)) != null)
				return ret;
		return null;
	}

	public L2Effect getSameByStackType(EffectList ef_list)
	{
		return getSameByStackType(ef_list.getAllEffects());
	}

	public L2Effect getSameByStackType(L2Character actor)
	{
		return getSameByStackType(actor.getEffectList().getAllEffects());
	}

	public final Element getElement()
	{
		return _element;
	}

	public final int getElementPower()
	{
		return _elementPower;
	}

	public L2Skill getFirstAddedSkill()
	{
		if(_addedSkills == null)
			return null;
		return _addedSkills[0].getSkill();
	}

	public int getFlyRadius()
	{
		return _flyRadius;
	}

	public FlyType getFlyType()
	{
		return _flyType;
	}

	public boolean isFlyToBack()
	{
		return _flyToBack;
	}

	public int getForceId()
	{
		return _forceId;
	}

	public final int getHitTime()
	{
		return _hitTime;
	}

	/**
	 * @return Returns the hpConsume.
	 */
	public final int getHpConsume()
	{
		return _hpConsume;
	}

	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return _id;
	}

	public void setId(int id)
	{
		_id = id;
	}

	/**
	 * @return Returns the itemConsume.
	 */
	public final int[] getItemConsume()
	{
		return _itemConsume;
	}

	/**
	 * @return Returns the itemConsumeId.
	 */
	public final int[] getItemConsumeId()
	{
		return _itemConsumeId;
	}

	/**
	 * @return Returns the level.
	 */
	public final short getLevel()
	{
		return _level;
	}

	public final short getBaseLevel()
	{
		return _baseLevel;
	}

	public final void setBaseLevel(short baseLevel)
	{
		_baseLevel = baseLevel;
	}

	public final int getLevelModifier()
	{
		return _levelModifier;
	}

	public final int getMagicLevel()
	{
		return _magicLevel;
	}

	public int getMatak()
	{
		return _matak;
	}

	public int getMinPledgeClass()
	{
		return _minPledgeClass;
	}

	public int getMinRank()
	{
		return _minRank;
	}

	/**
	 * @return Returns the mpConsume as _mpConsume1 + _mpConsume2.
	 */
	public final double getMpConsume()
	{
		return _mpConsume1 + _mpConsume2;
	}

	/**
	 * @return Returns the mpConsume1.
	 */
	public final double getMpConsume1()
	{
		return _mpConsume1;
	}

	/**
	 * @return Returns the mpConsume2.
	 */
	public final double getMpConsume2()
	{
		return _mpConsume2;
	}

	/**
	 * @return Returns the name.
	 */
	public final String getName()
	{
		return _name;
	}

	public int getNegatePower()
	{
		return _negatePower;
	}

	public int getNegateSkill()
	{
		return _negateSkill;
	}

	public NextAction getNextAction()
	{
		return _nextAction;
	}

	public int getNpcId()
	{
		return _npcId;
	}

	public int getNumCharges()
	{
		return _numCharges;
	}

	public final double getPower(L2Character target)
	{
		if(target != null)
		{
			if(target.isPlayable())
				return getPowerPvP();
			if(target.isMonster())
				return getPowerPvE();
		}
		return getPower();
	}

	public final double getPower()
	{
		return _power;
	}

	public final double getPowerPvP()
	{
		return _powerPvP != 0 ? _powerPvP : _power;
	}

	public final double getPowerPvE()
	{
		return _powerPvE != 0 ? _powerPvE : _power;
	}

	public final long getReuseDelay()
	{
		return _reuseDelay;
	}

	/**
	 * для изменения времени отката из скриптов
	 */
	public final void setReuseDelay(long newReuseDelay)
	{
		_reuseDelay = newReuseDelay;
	}

	public final GArray<Integer> getReuseGroup()
	{
		return _reuseGroups.get(_reuseGroupId);
	}

	public final int getReuseGroupId()
	{
		return _reuseGroupId;
	}

	public final int getSavevs()
	{
		return _savevs;
	}

	public final boolean getShieldIgnore()
	{
		return _isShieldignore;
	}

	public final int getSkillInterruptTime()
	{
		return _skillInterruptTime;
	}

	public final int getSkillRadius()
	{
		return _skillRadius;
	}

	public final SkillType getSkillType()
	{
		return _skillType;
	}

	public int getSoulsConsume()
	{
		return _soulsConsume;
	}

	public int getSymbolId()
	{
		return _symbolId;
	}

	public final SkillTargetType getTargetType()
	{
		return _targetType;
	}

	public final int getWeaponsAllowed()
	{
		return _weaponsAllowed;
	}

	public double getLethal1()
	{
		return _lethal1;
	}

	public double getLethal2()
	{
		return _lethal2;
	}

	public String getBaseValues()
	{
		return _baseValues;
	}

	public boolean isBlockedByChar(L2Character effected, EffectTemplate et)
	{
		if(et._funcTemplates == null)
			return false;
		for(FuncTemplate func : et._funcTemplates)
			if(func != null && effected.checkBlockedStat(func._stat))
				return true;
		return false;
	}

	public final boolean isCancelable()
	{
		return _isCancelable && getSkillType() != SkillType.TRANSFORMATION && !isToggle();
	}

	/**
	 * Является ли скилл общим
	 */
	public final boolean isCommon()
	{
		return _isCommon;
	}

	public final int getCriticalRate()
	{
		return _criticalRate;
	}

	public final boolean isHandler()
	{
		return _isItemHandler;
	}

	public final boolean isMagic()
	{
		return _isMagic;
	}

	public void setOperateType(SkillOpType type)
	{
		_operateType = type;
	}

	public double getChanceForAction(TriggerActionType action)
	{
		return _triggerActions.get(action);
	}

	public TreeMap<TriggerActionType, Double> getTriggerActions()
	{
		return _triggerActions;
	}

	public final boolean isOnAction()
	{
		return _operateType == SkillOpType.OP_ON_ACTION;
	}

	public final boolean isOverhit()
	{
		return _isOverhit;
	}

	public final boolean isActive()
	{
		return _operateType == SkillOpType.OP_ACTIVE;
	}

	public final boolean isPassive()
	{
		return _operateType == SkillOpType.OP_PASSIVE;
	}

	public final boolean isLikePassive()
	{
		return _operateType == SkillOpType.OP_PASSIVE || _operateType == SkillOpType.OP_ON_ACTION;
	}

	public boolean isSaveable()
	{
		if(!ConfigSystem.getBoolean("AltSaveUnsaveable") && (isMusic() || _name.startsWith("Herb of")))
			return false;
		return _isSaveable;
	}

	/**
	 * На некоторые скиллы и хендлеры предметов скорости каста/атаки не влияет
	 */
	public final boolean isSkillTimePermanent()
	{
		return _isSkillTimePermanent || _isItemHandler || _name.contains("Talisman");
	}

	public final boolean isReuseDelayPermanent()
	{
		return _isReuseDelayPermanent || _isItemHandler;
	}

	public boolean isDeathlink()
	{
		return _deathlink;
	}

	public boolean isBasedOnTargetDebuff()
	{
		return _basedOnTargetDebuff;
	}

	public boolean isSoulBoost()
	{
		return _isSoulBoost;
	}

	public boolean isChargeBoost()
	{
		return _isChargeBoost;
	}

	public boolean isUsingWhileCasting()
	{
		return _isUsingWhileCasting;
	}

	public boolean isBehind()
	{
		return _isBehind;
	}

	/**
	 * Может ли скилл тратить шоты, для хендлеров всегда false
	 */
	public boolean isSSPossible()
	{
		return Boolean.TRUE.equals(_isUseSS) || (_isUseSS == null && !_isItemHandler && !isMusic() && isActive() && !(getTargetType() == SkillTargetType.TARGET_SELF && !isMagic()));
	}

	public final boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}

	public final boolean isToggle()
	{
		return _operateType == SkillOpType.OP_TOGGLE;
	}

	public void setCastRange(int castRange)
	{
		_castRange = castRange;
	}

	public void setDisplayLevel(Short lvl)
	{
		_displayLevel = lvl;
	}

	public void setHitTime(int hitTime)
	{
		_hitTime = hitTime;
	}

	public void setHpConsume(int hpConsume)
	{
		_hpConsume = hpConsume;
	}

	public void setIsMagic(boolean isMagic)
	{
		_isMagic = isMagic;
	}

	public final void setMagicLevel(int newlevel)
	{
		_magicLevel = newlevel;
	}

	public void setMpConsume1(double mpConsume1)
	{
		_mpConsume1 = mpConsume1;
	}

	public void setMpConsume2(double mpConsume2)
	{
		_mpConsume2 = mpConsume2;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public void setOverhit(final boolean isOverhit)
	{
		_isOverhit = isOverhit;
	}

	public final void setPower(double power)
	{
		_power = power;
	}

	public void setSkillInterruptTime(int skillInterruptTime)
	{
		_skillInterruptTime = skillInterruptTime;
	}

	public boolean isItemSkill()
	{
		return _name.contains("Item Skill") || _name.contains("Talisman");
	}

	@Override
	public String toString()
	{
		return _name + "[id=" + _id + ",lvl=" + _level + "]";
	}

	public abstract void useSkill(L2Character activeChar, GArray<L2Character> targets);

	/**
	 * Такие скиллы не аггрят цель, и не флагают чара, но являются "плохими"
	 */
	public boolean isAI()
	{
		switch(_skillType)
		{
			case AGGRESSION:
			case AIEFFECTS:
			case SOWING:
			case DELETE_HATE:
			case DELETE_HATE_OF_ME:
				return true;
			default:
				return false;
		}
	}

	public boolean isAoE()
	{
		switch(_targetType)
		{
			case TARGET_AREA:
			case TARGET_AREA_AIM_CORPSE:
			case TARGET_AURA:
			case TARGET_PET_AURA:
			case TARGET_MULTIFACE:
			case TARGET_MULTIFACE_AURA:
			case TARGET_TUNNEL:
				return true;
			default:
				return false;
		}
	}

	public boolean isNotTargetAoE()
	{
		switch(_targetType)
		{
			case TARGET_AURA:
			case TARGET_MULTIFACE_AURA:
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_CLAN_ONLY:
			case TARGET_PARTY:
				return true;
			default:
				return false;
		}
	}

	public boolean isOffensive()
	{
		if(_isOffensive != null)
			return _isOffensive;

		switch(_skillType)
		{
			case AGGRESSION:
			case AIEFFECTS:
			case BLEED:
			case CANCEL:
			case DEBUFF:
			case DOT:
			case DRAIN:
			case DRAIN_SOUL:
			case LETHAL_SHOT:
			case MANADAM:
			case MDAM:
			case MDOT:
			case MUTE:
			case PARALYZE:
			case PDAM:
			case CPDAM:
			case POISON:
			case ROOT:
			case SLEEP:
			case SOULSHOT:
			case SPIRITSHOT:
			case SPOIL:
			case STUN:
			case SWEEP:
			case HARVESTING:
			case TELEPORT_NPC:
			case SOWING:
			case DELETE_HATE:
			case DELETE_HATE_OF_ME:
			case DESTROY_SUMMON:
			case STEAL_BUFF:
			case DISCORD:
				return true;
			default:
				return false;
		}
	}

	public final boolean isForceUse()
	{
		return _isForceUse;
	}

	/**
	 * Работают только против npc
	 */
	public boolean isPvM()
	{
		if(_isPvm != null)
			return _isPvm;

		switch(_skillType)
		{
			case DISCORD:
				return true;
			default:
				return false;
		}
	}

	public final boolean isPvpSkill()
	{
		if(_isPvpSkill != null)
			return _isPvpSkill;

		switch(_skillType)
		{
			case BLEED:
			case CANCEL:
			case AGGRESSION:
			case DEBUFF:
			case DOT:
			case MDOT:
			case MUTE:
			case PARALYZE:
			case POISON:
			case ROOT:
			case SLEEP:
			case MANADAM:
			case DESTROY_SUMMON:
			case NEGATE_STATS:
			case NEGATE_EFFECTS:
			case STEAL_BUFF:
			case DELETE_HATE:
			case DELETE_HATE_OF_ME:
				return true;
			default:
				return false;
		}
	}

	public boolean isMusic()
	{
		return _isMusic;
	}

	public boolean oneTarget()
	{
		switch(_targetType)
		{
			case TARGET_CORPSE:
			case TARGET_CORPSE_PLAYER:
			case TARGET_HOLY:
			case TARGET_FLAGPOLE:
			case TARGET_ITEM:
			case TARGET_NONE:
			case TARGET_ONE:
			case TARGET_PARTY_ONE:
			case TARGET_PET:
			case TARGET_OWNER:
			case TARGET_ENEMY_PET:
			case TARGET_ENEMY_SUMMON:
			case TARGET_ENEMY_SERVITOR:
			case TARGET_SELF:
			case TARGET_UNLOCKABLE:
			case TARGET_CHEST:
			case TARGET_SIEGE:
				return true;
			default:
				return false;
		}
	}

	public int getCancelTarget()
	{
		return _cancelTarget;
	}

	public boolean isSkillInterrupt()
	{
		return _skillInterrupt;
	}

	public boolean isNotUsedByAI()
	{
		return _isNotUsedByAI;
	}

	/**
	 * Игнорирование резистов
	 */
	public boolean isIgnoreResists()
	{
		return _isIgnoreResists;
	}

	public boolean isNotAffectedByMute()
	{
		return _isNotAffectedByMute;
	}

	public boolean flyingTransformUsage()
	{
		return _flyingTransformUsage;
	}

	public int getCastCount()
	{
		return _castCount;
	}

	public int getEnchantLevelCount()
	{
		return _enchantLevelCount;
	}

	public void setEnchantLevelCount(int count)
	{
		_enchantLevelCount = count;
	}

	public boolean isClanSkill()
	{
		return _id >= 370 && _id <= 391 || _id >= 611 && _id <= 616;
	}

	public double getSimpleDamage(L2Character attacker, L2Character target)
	{
		if(isMagic())
		{
			// магический урон
			double mAtk = attacker.getMAtk(target, this);
			double mdef = target.getMDef(null, this);
			double power = getPower();
			int sps = attacker.getChargedSpiritShot() > 0 && isSSPossible() ? attacker.getChargedSpiritShot() * 2 : 1;
			return 91 * power * Math.sqrt(sps * mAtk) / mdef;
		}
		// физический урон
		double pAtk = attacker.getPAtk(target);
		double pdef = target.getPDef(attacker);
		double power = getPower();
		int ss = attacker.getChargedSoulShot() && isSSPossible() ? 2 : 1;
		return ss * (pAtk + power) * 70. / pdef;
	}

	public long getReuseForMonsters()
	{
		long min = 1000;
		switch(_skillType)
		{
			case PARALYZE:
			case DEBUFF:
			case CANCEL:
			case NEGATE_EFFECTS:
			case NEGATE_STATS:
			case STEAL_BUFF:
				min = 10000;
				break;
			case MUTE:
			case ROOT:
			case SLEEP:
			case STUN:
				min = 5000;
				break;
		}
		return Math.max(Math.max(_hitTime + _coolTime, _reuseDelay), min);
	}

	public double getAbsorbPart()
	{
		return _absorbPart;
	}

	public static void broadcastUseAnimation(L2Skill skill, L2Character user, GArray<L2Character> targets)
	{
		int displayId = 0, displayLevel = 0;

		if(skill.getEffectTemplates() != null)
		{
			displayId = skill.getEffectTemplates()[0]._displayId;
			displayLevel = skill.getEffectTemplates()[0]._displayLevel;
		}

		if(displayId == 0)
			displayId = skill.getDisplayId();
		if(displayLevel == 0)
			displayLevel = skill.getDisplayLevel();

		for(L2Character cha : targets)
			user.broadcastPacket(new MagicSkillUse(user, cha, displayId, displayLevel, 0, 0));
	}

	public boolean isOlympiadEnabled()
	{
		return _isOlympiadEnabled || !_isItemHandler && !_isAltUse;
	}
	
   public boolean isFishing()
	{
        switch(getId())
		{
			// Рыбацкие скилы
			case 1312:
			case 1313:
			case 1314:
			case 1315:
			case 1368:
			case 1369:
			case 1370:
			case 1371:
			case 1372:
				return true;
		}

		return false;
	}

	
    public boolean isAwakingSkill()
	{
		if(getId() > 10000 && getId() < 12000)
			return true;
		return false;
	}
	
	/** Жрет много памяти (_set), включить только если будет необходимость
	public L2Skill clone()
	{
		L2Skill skill = getSkillType().makeSkill(_set);
		// Поля, перечисленные ниже, могут не совпадать с _set, поэтому обновляются отдельно
		// Необходимо сверять этот список с SkillTable.loadSqlSkills()
		skill.setPower(_power);
		skill.setBaseLevel(_baseLevel);
		skill.setMagicLevel(_magicLevel);
		skill.setCastRange(_castRange);
		skill.setName(_name);
		skill.setHitTime(_hitTime);
		skill.setSkillInterruptTime(_skillInterruptTime);
		skill.setIsMagic(_isMagic);
		skill.setOverhit(_isOverhit);
		skill.setHpConsume(_hpConsume);
		skill.setMpConsume1(_mpConsume1);
		skill.setMpConsume2(_mpConsume2);
		return skill;
	}
	*/
}