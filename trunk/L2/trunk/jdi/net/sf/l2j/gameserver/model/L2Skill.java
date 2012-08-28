package net.sf.l2j.gameserver.model;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.HeroSkillTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.model.actor.instance.L2ArtefactInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2ChestInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2CubicInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2TyrannosaurusInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.conditions.Condition;
import net.sf.l2j.gameserver.skills.effects.EffectCharge;
import net.sf.l2j.gameserver.skills.effects.EffectTemplate;
import net.sf.l2j.gameserver.skills.funcs.Func;
import net.sf.l2j.gameserver.skills.funcs.FuncTemplate;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillCharge;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillChargeDmg;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillChargeEffect;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillCreateItem;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillDefault;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillDrain;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSeed;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSignet;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSignetCasttime;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSummon;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public abstract class L2Skill
{
  protected static final Logger _log = Logger.getLogger(L2Skill.class.getName());
  public static final int SKILL_CUBIC_MASTERY = 143;
  public static final int SKILL_LUCKY = 194;
  public static final int SKILL_CREATE_COMMON = 1320;
  public static final int SKILL_CREATE_DWARVEN = 172;
  public static final int SKILL_CRYSTALLIZE = 248;
  public static final int SKILL_FAKE_INT = 9001;
  public static final int SKILL_FAKE_WIT = 9002;
  public static final int SKILL_FAKE_MEN = 9003;
  public static final int SKILL_FAKE_CON = 9004;
  public static final int SKILL_FAKE_DEX = 9005;
  public static final int SKILL_FAKE_STR = 9006;
  public static final int ELEMENT_WIND = 1;
  public static final int ELEMENT_FIRE = 2;
  public static final int ELEMENT_WATER = 3;
  public static final int ELEMENT_EARTH = 4;
  public static final int ELEMENT_HOLY = 5;
  public static final int ELEMENT_DARK = 6;
  public static final int SAVEVS_INT = 1;
  public static final int SAVEVS_WIT = 2;
  public static final int SAVEVS_MEN = 3;
  public static final int SAVEVS_CON = 4;
  public static final int SAVEVS_DEX = 5;
  public static final int SAVEVS_STR = 6;
  public static final int STAT_PATK = 301;
  public static final int STAT_PDEF = 302;
  public static final int STAT_MATK = 303;
  public static final int STAT_MDEF = 304;
  public static final int STAT_MAXHP = 305;
  public static final int STAT_MAXMP = 306;
  public static final int STAT_CURHP = 307;
  public static final int STAT_CURMP = 308;
  public static final int STAT_HPREGEN = 309;
  public static final int STAT_MPREGEN = 310;
  public static final int STAT_CASTINGSPEED = 311;
  public static final int STAT_ATKSPD = 312;
  public static final int STAT_CRITDAM = 313;
  public static final int STAT_CRITRATE = 314;
  public static final int STAT_FIRERES = 315;
  public static final int STAT_WINDRES = 316;
  public static final int STAT_WATERRES = 317;
  public static final int STAT_EARTHRES = 318;
  public static final int STAT_HOLYRES = 336;
  public static final int STAT_DARKRES = 337;
  public static final int STAT_ROOTRES = 319;
  public static final int STAT_SLEEPRES = 320;
  public static final int STAT_CONFUSIONRES = 321;
  public static final int STAT_BREATH = 322;
  public static final int STAT_AGGRESSION = 323;
  public static final int STAT_BLEED = 324;
  public static final int STAT_POISON = 325;
  public static final int STAT_STUN = 326;
  public static final int STAT_ROOT = 327;
  public static final int STAT_MOVEMENT = 328;
  public static final int STAT_EVASION = 329;
  public static final int STAT_ACCURACY = 330;
  public static final int STAT_COMBAT_STRENGTH = 331;
  public static final int STAT_COMBAT_WEAKNESS = 332;
  public static final int STAT_ATTACK_RANGE = 333;
  public static final int STAT_NOAGG = 334;
  public static final int STAT_SHIELDDEF = 335;
  public static final int STAT_MP_CONSUME_RATE = 336;
  public static final int STAT_HP_CONSUME_RATE = 337;
  public static final int COMBAT_MOD_ANIMAL = 200;
  public static final int COMBAT_MOD_BEAST = 201;
  public static final int COMBAT_MOD_BUG = 202;
  public static final int COMBAT_MOD_DRAGON = 203;
  public static final int COMBAT_MOD_MONSTER = 204;
  public static final int COMBAT_MOD_PLANT = 205;
  public static final int COMBAT_MOD_HOLY = 206;
  public static final int COMBAT_MOD_UNHOLY = 207;
  public static final int COMBAT_MOD_BOW = 208;
  public static final int COMBAT_MOD_BLUNT = 209;
  public static final int COMBAT_MOD_DAGGER = 210;
  public static final int COMBAT_MOD_FIST = 211;
  public static final int COMBAT_MOD_DUAL = 212;
  public static final int COMBAT_MOD_SWORD = 213;
  public static final int COMBAT_MOD_POISON = 214;
  public static final int COMBAT_MOD_BLEED = 215;
  public static final int COMBAT_MOD_FIRE = 216;
  public static final int COMBAT_MOD_WATER = 217;
  public static final int COMBAT_MOD_EARTH = 218;
  public static final int COMBAT_MOD_WIND = 219;
  public static final int COMBAT_MOD_ROOT = 220;
  public static final int COMBAT_MOD_STUN = 221;
  public static final int COMBAT_MOD_CONFUSION = 222;
  public static final int COMBAT_MOD_DARK = 223;
  public static final int COND_RUNNING = 1;
  public static final int COND_WALKING = 2;
  public static final int COND_SIT = 4;
  public static final int COND_BEHIND = 8;
  public static final int COND_CRIT = 16;
  public static final int COND_LOWHP = 32;
  public static final int COND_ROBES = 64;
  public static final int COND_CHARGES = 128;
  public static final int COND_SHIELD = 256;
  public static final int COND_GRADEA = 65536;
  public static final int COND_GRADEB = 131072;
  public static final int COND_GRADEC = 262144;
  public static final int COND_GRADED = 524288;
  public static final int COND_GRADES = 1048576;
  private static final Func[] _emptyFunctionSet = new Func[0];
  private static final L2Effect[] _emptyEffectSet = new L2Effect[0];
  private final int _id;
  private final int _level;
  private final int _activationtime;
  private final int _activationchance;
  private int _displayId;
  private final String _name;
  private final SkillOpType _operateType;
  private final boolean _magic;
  private final int _mpConsume;
  private final int _mpInitialConsume;
  private final int _hpConsume;
  private final int _targetConsume;
  private final int _targetConsumeId;
  private final int _itemConsume;
  private final int _itemConsumeId;
  private final int _itemConsumeOT;
  private final int _itemConsumeIdOT;
  private final int _itemConsumeSteps;
  private final int _summonTotalLifeTime;
  private final int _summonTimeLostIdle;
  private final int _summonTimeLostActive;
  private final int _itemConsumeTime;
  private final int _castRange;
  private final int _effectRange;
  private final int _hitTime;
  private final int _coolTime;
  private final int _reuseDelay;
  private final boolean _staticReuse;
  private final int _buffDuration;
  private final boolean _standartCritFormulas;
  private final boolean _staticHitTime;
  private final SkillTargetType _targetType;
  private final double _power;
  private final int _effectPoints;
  private final int _magicLevel;
  private final String[] _negateStats;
  private final float _negatePower;
  private final int _negateId;
  private final int _levelDepend;
  private final int _skillRadius;
  private final SkillType _skillType;
  private final SkillType _effectType;
  private final int _effectPower;
  private final int _effectLvl;
  private final int _effectId;
  private final boolean _ispotion;
  private final boolean _noAquire;
  private final boolean _isNeutral;
  private final int _element;
  private final int _savevs;
  private final boolean _isSuicideAttack;
  private final Stats _stat;
  private final int _condition;
  private final int _conditionValue;
  private final boolean _overhit;
  private final int _weaponsAllowed;
  private final int _armorsAllowed;
  private final int _addCrossLearn;
  private final float _mulCrossLearn;
  private final float _mulCrossLearnRace;
  private final float _mulCrossLearnProf;
  private final List<ClassId> _canLearn;
  private final List<Integer> _teachers;
  private final int _minPledgeClass;
  private final boolean _isOffensive;
  private final int _numCharges;
  private final int _forceId;
  private final boolean _isHeroSkill;
  private final int _baseCritRate;
  private final int _lethalEffect1;
  private final int _lethalEffect2;
  private final boolean _directHpDmg;
  private final boolean _isDance;
  private final int _nextDanceCost;
  private final float _sSBoost;
  private final int _aggroPoints;
  private final int _maxNegatedEffect;
  protected final int _reuseGroupId;
  static final HashMap<Integer, ArrayList<L2Skill>> _reuseGroups = new HashMap();
  protected Condition _preCondition;
  protected Condition _itemPreCondition;
  protected FuncTemplate[] _funcTemplates;
  protected EffectTemplate[] _effectTemplates;
  protected EffectTemplate[] _effectTemplatesSelf;
  protected ChanceCondition _chanceCondition = null;

  protected L2Skill(StatsSet set)
  {
    _id = set.getInteger("skill_id");
    _level = set.getInteger("level");

    _displayId = set.getInteger("displayId", _id);
    _name = set.getString("name");
    _operateType = ((SkillOpType)set.getEnum("operateType", SkillOpType.class));
    _magic = set.getBool("isMagic", false);
    _ispotion = set.getBool("isPotion", false);
    _noAquire = set.getBool("noAquire", false);
    _isNeutral = set.getBool("isNeutral", false);
    _mpConsume = set.getInteger("mpConsume", 0);
    _mpInitialConsume = set.getInteger("mpInitialConsume", 0);
    _hpConsume = set.getInteger("hpConsume", 0);
    _targetConsume = set.getInteger("targetConsumeCount", 0);
    _targetConsumeId = set.getInteger("targetConsumeId", 0);
    _itemConsume = set.getInteger("itemConsumeCount", 0);
    _itemConsumeId = set.getInteger("itemConsumeId", 0);
    _itemConsumeOT = set.getInteger("itemConsumeCountOT", 0);
    _itemConsumeIdOT = set.getInteger("itemConsumeIdOT", 0);
    _itemConsumeTime = set.getInteger("itemConsumeTime", 0);
    _itemConsumeSteps = set.getInteger("itemConsumeSteps", 0);
    _summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 1200000);
    _summonTimeLostIdle = set.getInteger("summonTimeLostIdle", 0);
    _summonTimeLostActive = set.getInteger("summonTimeLostActive", 0);
    _staticHitTime = set.getBool("staticHitTime", false);

    _activationtime = set.getInteger("activationtime", 8);
    _activationchance = set.getInteger("activationchance", 30);

    _castRange = set.getInteger("castRange", 0);
    _effectRange = set.getInteger("effectRange", -1);

    _reuseGroupId = set.getInteger("reuseGroup", 0);
    if (_reuseGroupId > 0)
    {
      if (_reuseGroups.get(Integer.valueOf(_reuseGroupId)) == null)
        _reuseGroups.put(Integer.valueOf(_reuseGroupId), new ArrayList());
      ((ArrayList)_reuseGroups.get(Integer.valueOf(_reuseGroupId))).add(this);
    }

    _hitTime = set.getInteger("hitTime", 0);
    _coolTime = set.getInteger("coolTime", 0);
    _reuseDelay = set.getInteger("reuseDelay", 0);
    _staticReuse = set.getBool("staticReuse", false);

    _buffDuration = set.getInteger("buffDuration", 0);
    _standartCritFormulas = set.getBool("standartCritFormulas", false);

    _skillRadius = set.getInteger("skillRadius", 80);

    _targetType = ((SkillTargetType)set.getEnum("target", SkillTargetType.class));
    _power = set.getFloat("power", 0.0F);
    _effectPoints = set.getInteger("effectPoints", 0);
    _negateStats = set.getString("negateStats", "").split(" ");
    _negatePower = set.getFloat("negatePower", 0.0F);
    _negateId = set.getInteger("negateId", 0);
    _maxNegatedEffect = set.getInteger("maxNegated", 0);
    _magicLevel = set.getInteger("magicLvl", SkillTreeTable.getInstance().getMinSkillLevel(_id, _level));
    _levelDepend = set.getInteger("lvlDepend", 0);
    _stat = ((Stats)set.getEnum("stat", Stats.class, null));

    _skillType = ((SkillType)set.getEnum("skillType", SkillType.class));
    _effectType = ((SkillType)set.getEnum("effectType", SkillType.class, null));
    _effectPower = set.getInteger("effectPower", 0);
    _effectLvl = set.getInteger("effectLevel", 0);
    _effectId = set.getInteger("effectId", 0);
    _element = set.getInteger("element", 0);
    _savevs = set.getInteger("save", 0);

    _condition = set.getInteger("condition", 0);
    _conditionValue = set.getInteger("conditionValue", 0);
    _overhit = set.getBool("overHit", false);
    _isSuicideAttack = set.getBool("isSuicideAttack", false);
    _weaponsAllowed = set.getInteger("weaponsAllowed", 0);
    _armorsAllowed = set.getInteger("armorsAllowed", 0);

    if (_operateType == SkillOpType.OP_CHANCE) {
      _chanceCondition = ChanceCondition.parse(set);
    }
    _addCrossLearn = set.getInteger("addCrossLearn", 1000);
    _mulCrossLearn = set.getFloat("mulCrossLearn", 2.0F);
    _mulCrossLearnRace = set.getFloat("mulCrossLearnRace", 2.0F);
    _mulCrossLearnProf = set.getFloat("mulCrossLearnProf", 3.0F);
    _minPledgeClass = set.getInteger("minPledgeClass", 0);
    _isOffensive = set.getBool("offensive", isSkillTypeOffensive());
    _numCharges = set.getInteger("num_charges", getLevel());
    _forceId = set.getInteger("forceId", 0);

    _isHeroSkill = HeroSkillTable.isHeroSkill(_id);

    _baseCritRate = set.getInteger("baseCritRate", (_skillType == SkillType.PDAM) || (_skillType == SkillType.BLOW) ? 0 : -1);
    _lethalEffect1 = set.getInteger("lethal1", 0);
    _lethalEffect2 = set.getInteger("lethal2", 0);

    _directHpDmg = set.getBool("dmgDirectlyToHp", false);
    _isDance = set.getBool("isDance", false);
    _nextDanceCost = set.getInteger("nextDanceCost", 0);
    _sSBoost = set.getFloat("SSBoost", 0.0F);
    _aggroPoints = set.getInteger("aggroPoints", 0);

    String canLearn = set.getString("canLearn", null);
    if (canLearn == null)
    {
      _canLearn = null;
    }
    else
    {
      _canLearn = new FastList();
      StringTokenizer st = new StringTokenizer(canLearn, " \r\n\t,;");
      while (st.hasMoreTokens())
      {
        String cls = st.nextToken();
        try
        {
          _canLearn.add(ClassId.valueOf(cls));
        }
        catch (Throwable t)
        {
          _log.log(Level.SEVERE, "Bad class " + cls + " to learn skill", t);
        }
      }
    }

    String teachers = set.getString("teachers", null);
    if (teachers == null)
    {
      _teachers = null;
    }
    else
    {
      _teachers = new FastList();
      StringTokenizer st = new StringTokenizer(teachers, " \r\n\t,;");
      while (st.hasMoreTokens())
      {
        String npcid = st.nextToken();
        try
        {
          _teachers.add(Integer.valueOf(Integer.parseInt(npcid)));
        }
        catch (Throwable t)
        {
          _log.log(Level.SEVERE, "Bad teacher id " + npcid + " to teach skill", t);
        }
      }
    }
  }

  public abstract void useSkill(L2Character paramL2Character, L2Object[] paramArrayOfL2Object);

  public final boolean isPotion() {
    return _ispotion;
  }

  public final boolean isStaticHitTime()
  {
    return _staticHitTime;
  }

  public final boolean noAquire()
  {
    return _noAquire;
  }

  public final boolean isNeutral()
  {
    return _isNeutral;
  }

  public final int getArmorsAllowed()
  {
    return _armorsAllowed;
  }

  public final int getConditionValue()
  {
    return _conditionValue;
  }

  public final SkillType getSkillType()
  {
    return _skillType;
  }

  public final int getSavevs()
  {
    return _savevs;
  }

  public final int getElement()
  {
    return _element;
  }

  public final SkillTargetType getTargetType()
  {
    return _targetType;
  }

  public final int getCondition()
  {
    return _condition;
  }

  public final int getReuseGroupId()
  {
    return _reuseGroupId;
  }

  public final ArrayList<L2Skill> getReuseGroup()
  {
    return (ArrayList)_reuseGroups.get(Integer.valueOf(_reuseGroupId));
  }

  public final boolean isOverhit()
  {
    return _overhit;
  }

  public final boolean isSuicideAttack()
  {
    return _isSuicideAttack;
  }

  public final double getPower(L2Character activeChar)
  {
    if ((_skillType == SkillType.DEATHLINK) && (activeChar != null)) return _power * Math.pow(1.7165D - activeChar.getCurrentHp() / activeChar.getMaxHp(), 2.0D) * 0.577D;

    return _power;
  }

  public final double getPower()
  {
    return _power;
  }

  public final int getEffectPoints()
  {
    return _effectPoints;
  }

  public final String[] getNegateStats()
  {
    return _negateStats;
  }

  public final float getNegatePower()
  {
    return _negatePower;
  }

  public final int getNegateId()
  {
    return _negateId;
  }

  public final int getMagicLevel()
  {
    return _magicLevel;
  }

  public final int getLevelDepend()
  {
    return _levelDepend;
  }

  public final int getEffectPower()
  {
    return _effectPower;
  }

  public final int getEffectId() {
    return _effectId;
  }

  public final int getEffectLvl()
  {
    return _effectLvl;
  }

  public final SkillType getEffectType()
  {
    return _effectType;
  }

  public final int getBuffDuration()
  {
    return _buffDuration;
  }

  public final boolean getStandartCritFormulas()
  {
    return _standartCritFormulas;
  }

  public final int getCastRange()
  {
    return _castRange;
  }

  public final int getEffectRange()
  {
    return _effectRange;
  }

  public final int getHpConsume()
  {
    return _hpConsume;
  }

  public final int getTargetConsumeId()
  {
    return _targetConsumeId;
  }

  public final int getTargetConsume()
  {
    return _targetConsume;
  }

  public final int getId()
  {
    return _id;
  }

  public int getDisplayId()
  {
    return _displayId;
  }

  public void setDisplayId(int id)
  {
    _displayId = id;
  }

  public int getForceId()
  {
    return _forceId;
  }

  public final Stats getStat()
  {
    return _stat;
  }

  public final int getItemConsume()
  {
    return _itemConsume;
  }

  public final int getItemConsumeId()
  {
    return _itemConsumeId;
  }

  public final int getItemConsumeOT()
  {
    return _itemConsumeOT;
  }

  public final int getItemConsumeIdOT()
  {
    return _itemConsumeIdOT;
  }

  public final int getItemConsumeSteps()
  {
    return _itemConsumeSteps;
  }

  public final int getTotalLifeTime()
  {
    return _summonTotalLifeTime;
  }

  public final int getTimeLostIdle()
  {
    return _summonTimeLostIdle;
  }

  public final int getTimeLostActive()
  {
    return _summonTimeLostActive;
  }

  public final int getItemConsumeTime()
  {
    return _itemConsumeTime;
  }

  public final int getLevel()
  {
    return _level;
  }

  public final boolean isMagic()
  {
    return _magic;
  }

  public final int getMpConsume()
  {
    return _mpConsume;
  }

  public final int getMpInitialConsume()
  {
    return _mpInitialConsume;
  }

  public final String getName()
  {
    return _name;
  }

  public final int getReuseDelay()
  {
    return _reuseDelay;
  }

  public final boolean isStaticReuse()
  {
    return _staticReuse;
  }

  @Deprecated
  public final int getSkillTime() {
    return _hitTime;
  }

  public final int getHitTime()
  {
    return _hitTime;
  }

  public final int getCoolTime()
  {
    return _coolTime;
  }

  public final int getSkillRadius()
  {
    return _skillRadius;
  }

  public final boolean isActive()
  {
    return _operateType == SkillOpType.OP_ACTIVE;
  }

  public final boolean isPassive()
  {
    return _operateType == SkillOpType.OP_PASSIVE;
  }

  public final boolean isToggle()
  {
    return _operateType == SkillOpType.OP_TOGGLE;
  }

  public final boolean isChance()
  {
    return _operateType == SkillOpType.OP_CHANCE;
  }

  public ChanceCondition getChanceCondition()
  {
    return _chanceCondition;
  }

  public final boolean isDance()
  {
    return _isDance;
  }

  public final int getNextDanceMpCost()
  {
    return _nextDanceCost;
  }

  public final float getSSBoost()
  {
    return _sSBoost;
  }

  public final int getAggroPoints()
  {
    return _aggroPoints;
  }

  public final boolean useSoulShot()
  {
    return (getSkillType() == SkillType.PDAM) || (getSkillType() == SkillType.STUN) || (getSkillType() == SkillType.CHARGEDAM);
  }

  public final boolean useSpiritShot()
  {
    return isMagic();
  }

  public final boolean useFishShot() {
    return (getSkillType() == SkillType.PUMPING) || (getSkillType() == SkillType.REELING);
  }

  public final int getWeaponsAllowed() {
    return _weaponsAllowed;
  }

  public final int getCrossLearnAdd()
  {
    return _addCrossLearn;
  }

  public final float getCrossLearnMul()
  {
    return _mulCrossLearn;
  }

  public final float getCrossLearnRace()
  {
    return _mulCrossLearnRace;
  }

  public final float getCrossLearnProf()
  {
    return _mulCrossLearnProf;
  }

  public final boolean getCanLearn(ClassId cls)
  {
    return (_canLearn == null) || (_canLearn.contains(cls));
  }

  public final boolean canTeachBy(int npcId)
  {
    return (_teachers == null) || (_teachers.contains(Integer.valueOf(npcId)));
  }
  public int getMinPledgeClass() {
    return _minPledgeClass;
  }

  public final boolean isPvpSkill() {
    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[_skillType.ordinal()])
    {
    case 1:
    case 2:
    case 3:
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
    case 15:
    case 16:
    case 17:
    case 18:
    case 19:
    case 20:
    case 21:
      return true;
    }
    return false;
  }

  public final boolean isOffensive()
  {
    return _isOffensive;
  }

  public final boolean isHeroSkill()
  {
    return _isHeroSkill;
  }

  public final int getNumCharges()
  {
    return _numCharges;
  }

  public final int getBaseCritRate()
  {
    return _baseCritRate;
  }

  public final int getLethalChance1()
  {
    return _lethalEffect1;
  }

  public final int getLethalChance2() {
    return _lethalEffect2;
  }

  public final boolean getDmgDirectlyToHP() {
    return _directHpDmg;
  }

  public final boolean isSkillTypeOffensive() {
    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[_skillType.ordinal()])
    {
    case 1:
    case 2:
    case 3:
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
    case 15:
    case 16:
    case 17:
    case 18:
    case 19:
    case 20:
    case 21:
    case 22:
    case 23:
    case 24:
    case 25:
    case 26:
    case 27:
    case 28:
    case 29:
    case 30:
    case 31:
    case 32:
    case 33:
    case 34:
    case 35:
    case 36:
    case 37:
    case 38:
    case 39:
    case 40:
    case 41:
    case 42:
    case 43:
      return true;
    }
    return false;
  }

  public final boolean getWeaponDependancy(L2Character activeChar)
  {
    int weaponsAllowed = getWeaponsAllowed();

    if (weaponsAllowed == 0) return true;
    if (activeChar.getActiveWeaponItem() != null)
    {
      L2WeaponType playerWeapon = activeChar.getActiveWeaponItem().getItemType();
      int mask = playerWeapon.mask();
      if ((mask & weaponsAllowed) != 0) return true;

      if (activeChar.getSecondaryWeaponItem() != null)
      {
        playerWeapon = activeChar.getSecondaryWeaponItem().getItemType();
        mask = playerWeapon.mask();
        if ((mask & weaponsAllowed) != 0) return true;
      }
    }
    TextBuilder skillmsg = new TextBuilder();
    skillmsg.append(getName());
    skillmsg.append(" can only be used with weapons of type ");
    for (L2WeaponType wt : L2WeaponType.values())
    {
      if ((wt.mask() & weaponsAllowed) == 0) continue; skillmsg.append(wt).append('/');
    }
    skillmsg.setCharAt(skillmsg.length() - 1, '.');
    SystemMessage message = new SystemMessage(SystemMessageId.S1_S2);
    message.addString(skillmsg.toString());
    activeChar.sendPacket(message);

    return false;
  }

  public boolean checkCondition(L2Character activeChar, L2Object target, boolean itemOrWeapon)
  {
    if ((getCondition() & 0x100) != 0);
    Condition preCondition = _preCondition;
    if (itemOrWeapon) preCondition = _itemPreCondition;
    if (preCondition == null) return true;

    Env env = new Env();
    env.player = activeChar;
    if ((target instanceof L2Character))
      env.target = ((L2Character)target);
    env.skill = this;

    if (!preCondition.test(env))
    {
      String msg = preCondition.getMessage();
      if (msg != null)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
        sm.addString(msg);
        activeChar.sendPacket(sm);
      }
      return false;
    }
    return true;
  }

  public final L2Object[] getTargetList(L2Character activeChar, boolean onlyFirst)
  {
    L2Character target = null;

    L2Object objTarget = activeChar.getTarget();

    if ((objTarget instanceof L2Character))
    {
      target = (L2Character)objTarget;
    }

    return getTargetList(activeChar, onlyFirst, target);
  }

  public final L2Object[] getTargetList(L2Character activeChar, boolean onlyFirst, L2Character target)
  {
    List targetList = new FastList();

    SkillTargetType targetType = getTargetType();

    L2Object objTarget = activeChar.getTarget();

    SkillType skillType = getSkillType();

    if ((objTarget instanceof L2Character))
    {
      target = (L2Character)objTarget;
    }

    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillTargetType[targetType.ordinal()])
    {
    case 1:
      boolean canTargetSelf = false;
      switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[skillType.ordinal()]) { case 17:
      case 18:
      case 19:
      case 44:
      case 45:
      case 46:
      case 47:
      case 48:
      case 49:
      case 50:
      case 51:
      case 52:
      case 53:
      case 54:
      case 55:
      case 56:
      case 57:
        canTargetSelf = true;
      case 20:
      case 21:
      case 22:
      case 23:
      case 24:
      case 25:
      case 26:
      case 27:
      case 28:
      case 29:
      case 30:
      case 31:
      case 32:
      case 33:
      case 34:
      case 35:
      case 36:
      case 37:
      case 38:
      case 39:
      case 40:
      case 41:
      case 42:
      case 43: } if ((target == null) || (target.isDead()) || ((target == activeChar) && (!canTargetSelf)))
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
        return null;
      }

      if (((target instanceof L2PcInstance)) && ((activeChar instanceof L2PcInstance)) && (!target.isInsideZone(1)) && (!activeChar.isInsideZone(1)))
      {
        if (((activeChar.getParty() != null) && (target.getParty() != null) && (activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID())) || ((((L2PcInstance)activeChar).getClanId() != 0) && (((L2PcInstance)target).getClanId() != 0) && (((L2PcInstance)activeChar).getClanId() == ((L2PcInstance)target).getClanId())))
        {
          if (isPvpSkill())
          {
            activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
            return null;
          }
        }
      }
      if (!GeoData.getInstance().canSeeTarget(activeChar, target)) {
        return null;
      }
      return new L2Character[] { target };
    case 2:
    case 3:
      return new L2Character[] { activeChar };
    case 4:
      if ((activeChar instanceof L2PcInstance))
      {
        if ((activeChar.getTarget() instanceof L2TyrannosaurusInstance)) {
          return new L2Character[] { (L2TyrannosaurusInstance)activeChar.getTarget() };
        }
      }
      return null;
    case 5:
      if ((activeChar instanceof L2PcInstance))
      {
        if ((activeChar.getTarget() instanceof L2ArtefactInstance)) {
          return new L2Character[] { (L2ArtefactInstance)activeChar.getTarget() };
        }
      }
      return null;
    case 6:
      target = activeChar.getPet();
      if ((target != null) && (!target.isDead())) return new L2Character[] { target };

      return null;
    case 7:
      if ((activeChar instanceof L2Summon))
      {
        target = ((L2Summon)activeChar).getOwner();
        if ((target != null) && (!target.isDead())) {
          return new L2Character[] { target };
        }
      }
      return null;
    case 8:
      if ((activeChar instanceof L2PcInstance))
      {
        target = activeChar.getPet();
        if ((target != null) && (target.isDead())) {
          return new L2Character[] { target };
        }
      }
      return null;
    case 9:
      int radius = getSkillRadius();
      boolean srcInArena = (activeChar.isInsideZone(1)) && (!activeChar.isInsideZone(4));

      L2PcInstance src = null;
      if ((activeChar instanceof L2PcInstance)) src = (L2PcInstance)activeChar;
      if ((activeChar instanceof L2Summon)) src = ((L2Summon)activeChar).getOwner();

      for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
      {
        if (((obj instanceof L2Attackable)) || ((obj instanceof L2PlayableInstance)))
        {
          if ((obj != activeChar) && (obj != src) && (!((L2Character)obj).isDead())) {
            if (src != null)
            {
              if ((!GeoData.getInstance().canSeeTarget(activeChar, obj)) || (
                ((obj instanceof L2PcInstance)) && (
                (!src.checkPvpSkill(obj, this)) || 
                ((src.getParty() != null) && (((L2PcInstance)obj).getParty() != null) && (src.getParty().getPartyLeaderOID() == ((L2PcInstance)obj).getParty().getPartyLeaderOID())) || (
                (!srcInArena) && ((!((L2Character)obj).isInsideZone(1)) || (((L2Character)obj).isInsideZone(4))) && (
                ((src.getAllyId() == ((L2PcInstance)obj).getAllyId()) && (src.getAllyId() != 0)) || (
                (src.getClanId() != 0) && (src.getClanId() == ((L2PcInstance)obj).getClanId())))))))
              {
                continue;
              }
              if ((obj instanceof L2Summon))
              {
                L2PcInstance trg = ((L2Summon)obj).getOwner();
                if ((trg == src) || 
                  (!src.checkPvpSkill(trg, this)) || 
                  ((src.getParty() != null) && (trg.getParty() != null) && (src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())) || (
                  (!srcInArena) && ((!((L2Character)obj).isInsideZone(1)) || (((L2Character)obj).isInsideZone(4))) && (
                  ((src.getAllyId() == trg.getAllyId()) && (src.getAllyId() != 0)) || (
                  (src.getClanId() != 0) && (src.getClanId() == trg.getClanId()))))) {
                  continue;
                }
              }
            }
            else
            {
              if ((!(obj instanceof L2PlayableInstance)) && (!activeChar.isConfused())) {
                continue;
              }
            }
            if (!Util.checkIfInRange(radius, activeChar, obj, true))
              continue;
            if (!onlyFirst) targetList.add((L2Character)obj); else
              return new L2Character[] { (L2Character)obj }; 
          }
        }
      }
      return (L2Object[])targetList.toArray(new L2Character[targetList.size()]);
    case 10:
      if (((!(target instanceof L2Attackable)) && (!(target instanceof L2PlayableInstance))) || ((getCastRange() >= 0) && ((target == null) || (target == activeChar) || (target.isAlikeDead()))))
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
        return null;
      }
      L2Character cha;
      if (getCastRange() >= 0)
      {
        L2Character cha = target;

        if (!onlyFirst) targetList.add(cha); else
          return new L2Character[] { cha };
      } else {
        cha = activeChar;
      }
      boolean effectOriginIsL2PlayableInstance = cha instanceof L2PlayableInstance;

      L2PcInstance src = null;
      if ((activeChar instanceof L2PcInstance)) src = (L2PcInstance)activeChar;
      else if ((activeChar instanceof L2Summon)) src = ((L2Summon)activeChar).getOwner();

      int radius = getSkillRadius();

      boolean srcInArena = (activeChar.isInsideZone(1)) && (!activeChar.isInsideZone(4));

      for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
      {
        if ((obj == null) || 
          ((!(obj instanceof L2Attackable)) && (!(obj instanceof L2PlayableInstance))) || 
          (obj == cha)) continue;
        target = (L2Character)obj;

        if (!GeoData.getInstance().canSeeTarget(activeChar, target)) {
          continue;
        }
        if ((!target.isAlikeDead()) && (target != activeChar))
        {
          if (!Util.checkIfInRange(radius, obj, cha, true)) {
            continue;
          }
          if (src != null)
          {
            if ((obj instanceof L2PcInstance))
            {
              L2PcInstance trg = (L2PcInstance)obj;
              if ((trg == src) || 
                ((src.getParty() != null) && (trg.getParty() != null) && (src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())) || 
                (trg.isInsideZone(2)) || (
                (!srcInArena) && ((!trg.isInsideZone(1)) || (trg.isInsideZone(4))) && (
                ((src.getAllyId() == trg.getAllyId()) && (src.getAllyId() != 0)) || 
                ((src.getClan() != null) && (trg.getClan() != null) && 
                (src.getClan().getClanId() == trg.getClan().getClanId())) || 
                (!src.checkPvpSkill(obj, this))))) {
                continue;
              }
            }
            if ((obj instanceof L2Summon))
            {
              L2PcInstance trg = ((L2Summon)obj).getOwner();
              if ((trg == src) || 
                ((src.getParty() != null) && (trg.getParty() != null) && (src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())) || 
                ((!srcInArena) && ((!trg.isInsideZone(1)) || (trg.isInsideZone(4))) && (
                ((src.getAllyId() == trg.getAllyId()) && (src.getAllyId() != 0)) || 
                ((src.getClan() != null) && (trg.getClan() != null) && 
                (src.getClan().getClanId() == trg.getClan().getClanId())) || 
                (!src.checkPvpSkill(trg, this)))) || 
                (((L2Summon)obj).isInsideZone(2)))
                continue;
            }
          }
          else
          {
            if ((effectOriginIsL2PlayableInstance) && (!(obj instanceof L2PlayableInstance)))
            {
              continue;
            }
          }
          targetList.add((L2Character)obj);
        }
      }

      if (targetList.size() == 0) {
        return null;
      }
      return (L2Object[])targetList.toArray(new L2Character[targetList.size()]);
    case 11:
      if (onlyFirst) {
        return new L2Character[] { activeChar };
      }
      targetList.add(activeChar);

      L2PcInstance player = null;

      if ((activeChar instanceof L2Summon))
      {
        player = ((L2Summon)activeChar).getOwner();
        targetList.add(player);
      }
      else if ((activeChar instanceof L2PcInstance))
      {
        player = (L2PcInstance)activeChar;
        if (activeChar.getPet() != null) {
          targetList.add(activeChar.getPet());
        }
      }
      if (activeChar.getParty() != null)
      {
        List partyList = activeChar.getParty().getPartyMembers();

        for (L2PcInstance partyMember : partyList)
        {
          if ((partyMember == null) || 
            (partyMember == player))
            continue;
          if ((!partyMember.isDead()) && (Util.checkIfInRange(getSkillRadius(), activeChar, partyMember, true)))
          {
            targetList.add(partyMember);

            if ((partyMember.getPet() != null) && (!partyMember.getPet().isDead()))
            {
              targetList.add(partyMember.getPet());
            }
          }
        }
      }
      return (L2Object[])targetList.toArray(new L2Character[targetList.size()]);
    case 12:
      if (((target != null) && (target == activeChar)) || ((target != null) && (activeChar.getParty() != null) && (target.getParty() != null) && (activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID())) || ((target != null) && ((activeChar instanceof L2PcInstance)) && ((target instanceof L2Summon)) && (activeChar.getPet() == target)) || ((target != null) && ((activeChar instanceof L2Summon)) && ((target instanceof L2PcInstance)) && (activeChar == target.getPet())))
      {
        if (!target.isDead())
        {
          return new L2Character[] { target };
        }

        return null;
      }

      activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
      return null;
    case 13:
      if ((target != null) && (target != activeChar) && (activeChar.getParty() != null) && (target.getParty() != null) && (activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID()))
      {
        if (!target.isDead())
        {
          return new L2Character[] { target };
        }

        return null;
      }

      activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
      return null;
    case 14:
    case 15:
      int radius;
      L2PcInstance player;
      if ((activeChar instanceof L2PcInstance))
      {
        radius = getSkillRadius();
        player = (L2PcInstance)activeChar;
        L2Clan clan = player.getClan();

        if (player.isInOlympiadMode()) {
          return new L2Character[] { player };
        }
        if (targetType != SkillTargetType.TARGET_CORPSE_ALLY)
        {
          if (!onlyFirst) targetList.add(player); else {
            return new L2Character[] { player };
          }
        }
        if (clan != null)
        {
          for (L2Object newTarget : activeChar.getKnownList().getKnownObjects().values())
          {
            if ((newTarget == null) || (!(newTarget instanceof L2PcInstance)) || (
              ((((L2PcInstance)newTarget).getAllyId() == 0) || (((L2PcInstance)newTarget).getAllyId() != player.getAllyId())) && ((((L2PcInstance)newTarget).getClan() == null) || (((L2PcInstance)newTarget).getClanId() != player.getClanId()) || 
              ((player.isInDuel()) && ((player.getDuelId() != ((L2PcInstance)newTarget).getDuelId()) || ((player.getParty() != null) && (!player.getParty().getPartyMembers().contains(newTarget))))) || 
              ((targetType == SkillTargetType.TARGET_ALLY) && ((((L2PcInstance)newTarget).isDead()) || (((L2PcInstance)newTarget).getCurrentHp() == 0.0D))) || 
              ((targetType == SkillTargetType.TARGET_CORPSE_ALLY) && (
              (!((L2PcInstance)newTarget).isDead()) || (
              (getSkillType() == SkillType.RESURRECT) && 
              (((L2PcInstance)newTarget).isInsideZone(4))))) || 
              (!Util.checkIfInRange(radius, activeChar, newTarget, true)) || 
              (!player.checkPvpSkill(newTarget, this)))))
              continue;
            if (!onlyFirst) targetList.add((L2Character)newTarget); else {
              return new L2Character[] { (L2Character)newTarget };
            }
          }
        }
      }
      return (L2Object[])targetList.toArray(new L2Character[targetList.size()]);
    case 16:
    case 17:
      if ((activeChar instanceof L2PcInstance))
      {
        int radius = getSkillRadius();
        L2PcInstance player = (L2PcInstance)activeChar;
        L2Clan clan = player.getClan();

        if (player.isInOlympiadMode()) {
          return new L2Character[] { player };
        }
        if (targetType != SkillTargetType.TARGET_CORPSE_CLAN)
        {
          if (!onlyFirst) targetList.add(player); else {
            return new L2Character[] { player };
          }
        }
        if (clan != null)
        {
          for (L2ClanMember member : clan.getMembers())
          {
            L2PcInstance newTarget = member.getPlayerInstance();

            if (newTarget == null)
              continue;
            if (targetType == SkillTargetType.TARGET_CORPSE_CLAN)
            {
              if (!newTarget.isDead())
                continue;
              if ((getSkillType() == SkillType.RESURRECT) && 
                (newTarget.isInsideZone(4))) {
                continue;
              }
            }
            if ((player.isInDuel()) && ((player.getDuelId() != newTarget.getDuelId()) || ((player.getParty() != null) && (!player.getParty().getPartyMembers().contains(newTarget)))))
            {
              continue;
            }

            if (!Util.checkIfInRange(radius, activeChar, newTarget, true)) {
              continue;
            }
            if (!player.checkPvpSkill(newTarget, this))
              continue;
            if (!onlyFirst) targetList.add(newTarget); else {
              return new L2Character[] { newTarget };
            }
          }
        }
      }

      return (L2Object[])targetList.toArray(new L2Character[targetList.size()]);
    case 18:
      if ((target != null) && (target.isDead()))
      {
        L2PcInstance player = null;

        if ((activeChar instanceof L2PcInstance)) player = (L2PcInstance)activeChar;
        L2PcInstance targetPlayer = null;

        if ((target instanceof L2PcInstance)) targetPlayer = (L2PcInstance)target;
        L2PetInstance targetPet = null;

        if ((target instanceof L2PetInstance)) targetPet = (L2PetInstance)target;

        if ((player != null) && ((targetPlayer != null) || (targetPet != null)))
        {
          boolean condGood = true;

          if (getSkillType() == SkillType.RESURRECT)
          {
            if (target.isInsideZone(4))
            {
              condGood = false;
              player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE));
            }

            if (targetPlayer != null)
            {
              if (targetPlayer.isReviveRequested())
              {
                if (targetPlayer.isRevivingPet())
                  player.sendPacket(new SystemMessage(SystemMessageId.MASTER_CANNOT_RES));
                else
                  player.sendPacket(new SystemMessage(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED));
                condGood = false;
              }
            }
            else if (targetPet != null)
            {
              if (targetPet.getOwner() != player)
              {
                condGood = false;
                player.sendMessage("You are not the owner of this pet");
              }
            }
          }

          if (condGood)
          {
            if (!onlyFirst)
            {
              targetList.add(target);
              return (L2Object[])targetList.toArray(new L2Object[targetList.size()]);
            }
            return new L2Character[] { target };
          }
        }
      }

      activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
      return null;
    case 19:
      if ((!(target instanceof L2Attackable)) || (!target.isDead()))
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
        return null;
      }

      if (!onlyFirst)
      {
        targetList.add(target);
        return (L2Object[])targetList.toArray(new L2Object[targetList.size()]);
      }
      return new L2Character[] { target };
    case 20:
      if ((!(target instanceof L2Attackable)) || (!target.isDead()))
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
        return null;
      }

      if (!onlyFirst) targetList.add(target); else {
        return new L2Character[] { target };
      }
      boolean srcInArena = (activeChar.isInsideZone(1)) && (!activeChar.isInsideZone(4));
      L2PcInstance src = null;
      if ((activeChar instanceof L2PcInstance))
        src = (L2PcInstance)activeChar;
      L2PcInstance trg = null;

      int radius = getSkillRadius();
      if (activeChar.getKnownList() != null) {
        for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
        {
          if ((obj == null) || 
            ((!(obj instanceof L2Attackable)) && (!(obj instanceof L2PlayableInstance))) || (((L2Character)obj).isDead()) || ((L2Character)obj == activeChar) || 
            (!Util.checkIfInRange(radius, target, obj, true)) || 
            (!GeoData.getInstance().canSeeTarget(activeChar, obj))) {
            continue;
          }
          if (((obj instanceof L2PcInstance)) && (src != null))
          {
            trg = (L2PcInstance)obj;

            if (((src.getParty() != null) && (trg.getParty() != null) && (src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())) || 
              (trg.isInsideZone(2)) || (
              (!srcInArena) && ((!trg.isInsideZone(1)) || (trg.isInsideZone(4))) && (
              ((src.getAllyId() == trg.getAllyId()) && (src.getAllyId() != 0)) || 
              ((src.getClan() != null) && (trg.getClan() != null) && 
              (src.getClan().getClanId() == trg.getClan().getClanId())) || 
              (!src.checkPvpSkill(obj, this))))) {
              continue;
            }
          }
          if (((obj instanceof L2Summon)) && (src != null))
          {
            trg = ((L2Summon)obj).getOwner();

            if (((src.getParty() != null) && (trg.getParty() != null) && (src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())) || 
              ((!srcInArena) && ((!trg.isInsideZone(1)) || (trg.isInsideZone(4))) && (
              ((src.getAllyId() == trg.getAllyId()) && (src.getAllyId() != 0)) || 
              ((src.getClan() != null) && (trg.getClan() != null) && 
              (src.getClan().getClanId() == trg.getClan().getClanId())) || 
              (!src.checkPvpSkill(trg, this)))) || 
              (((L2Summon)obj).isInsideZone(2)))
              continue;
          }
          targetList.add((L2Character)obj);
        }
      }
      if (targetList.size() == 0) return null;
      return (L2Object[])targetList.toArray(new L2Character[targetList.size()]);
    case 21:
      if ((!(target instanceof L2DoorInstance)) && (!(target instanceof L2ChestInstance)))
      {
        return null;
      }

      if (!onlyFirst)
      {
        targetList.add(target);
        return (L2Object[])targetList.toArray(new L2Object[targetList.size()]);
      }
      return new L2Character[] { target };
    case 22:
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
      sm.addString("Target type of skill is not currently handled");
      activeChar.sendPacket(sm);
      return null;
    case 23:
      if (((target instanceof L2NpcInstance)) || ((target instanceof L2SummonInstance)))
      {
        if ((!target.isUndead()) || (target.isDead()))
        {
          activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
          return null;
        }

        if (!onlyFirst) targetList.add(target); else {
          return new L2Character[] { target };
        }
        return (L2Object[])targetList.toArray(new L2Object[targetList.size()]);
      }

      activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
      return null;
    case 24:
      int radius = getSkillRadius();
      L2Character cha;
      if ((getCastRange() >= 0) && (((target instanceof L2NpcInstance)) || ((target instanceof L2SummonInstance))) && (target.isUndead()) && (!target.isAlikeDead()))
      {
        L2Character cha = target;

        if (!onlyFirst) targetList.add(cha); else
          return new L2Character[] { cha };
      }
      else {
        cha = activeChar;
      }
      if ((cha != null) && (cha.getKnownList() != null))
        for (L2Object obj : cha.getKnownList().getKnownObjects().values())
        {
          if (obj != null) {
            if ((obj instanceof L2NpcInstance))
              target = (L2NpcInstance)obj;
            else if ((obj instanceof L2SummonInstance)) {
              target = (L2SummonInstance)obj;
            }

            if (!GeoData.getInstance().canSeeTarget(activeChar, target)) {
              continue;
            }
            if (!target.isAlikeDead())
            {
              if ((!target.isUndead()) || 
                (!Util.checkIfInRange(radius, cha, obj, true))) {
                continue;
              }
              if (!onlyFirst) targetList.add((L2Character)obj); else
                return new L2Character[] { (L2Character)obj };
            }
          }
        }
      if (targetList.size() == 0) return null;
      return (L2Object[])targetList.toArray(new L2Character[targetList.size()]);
    case 25:
      if ((target != null) && ((target instanceof L2Summon)))
      {
        L2Summon targetSummon = (L2Summon)target;
        if ((((activeChar instanceof L2PcInstance)) && (activeChar.getPet() != targetSummon) && (!targetSummon.isDead()) && ((targetSummon.getOwner().getPvpFlag() != 0) || (targetSummon.getOwner().getKarma() > 0))) || ((targetSummon.getOwner().isInsideZone(1)) && (((L2PcInstance)activeChar).isInsideZone(1))))
        {
          return new L2Character[] { targetSummon };
        }
      }
      return null;
    }

    SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
    sm.addString("Target type of skill is not currently handled");
    activeChar.sendPacket(sm);
    return null;
  }

  public final L2Object[] getTargetList(L2Character activeChar)
  {
    return getTargetList(activeChar, false);
  }

  public final L2Object getFirstOfTargetList(L2Character activeChar)
  {
    L2Object[] targets = getTargetList(activeChar, true);

    if ((targets == null) || (targets.length == 0)) return null;
    return targets[0];
  }

  public final Func[] getStatFuncs(L2Effect effect, L2Character player)
  {
    if ((!(player instanceof L2PcInstance)) && (!(player instanceof L2Attackable)) && (!(player instanceof L2Summon)))
      return _emptyFunctionSet;
    if (_funcTemplates == null) return _emptyFunctionSet;
    List funcs = new FastList();
    for (FuncTemplate t : _funcTemplates)
    {
      Env env = new Env();
      env.player = player;
      env.skill = this;
      Func f = t.getFunc(env, this);
      if (f == null) continue; funcs.add(f);
    }
    if (funcs.size() == 0) return _emptyFunctionSet;
    return (Func[])funcs.toArray(new Func[funcs.size()]);
  }

  public boolean hasEffects()
  {
    return (_effectTemplates != null) && (_effectTemplates.length > 0);
  }

  public final int getMaxNegatedEffects()
  {
    return _maxNegatedEffect;
  }

  public final int getActivationTime()
  {
    return _activationtime;
  }

  public final int getActivationChance()
  {
    return _activationchance;
  }

  public final L2Effect[] getEffects(L2Character effector, L2Character effected)
  {
    if (isPassive()) return _emptyEffectSet;

    if (_effectTemplates == null) {
      return _emptyEffectSet;
    }
    if ((effector != effected) && (effected.isInvul())) {
      return _emptyEffectSet;
    }
    if ((effected != effector) && (!effector.isRaid()) && (isOffensive())) if (Rnd.chance(effected.calcStat(isMagic() ? Stats.REFLECT_MAGIC_DEBUFF : Stats.REFLECT_PHYSIC_DEBUFF, 0.0D, null, this)))
      {
        effected.sendPacket(new SystemMessage(SystemMessageId.COUNTERED_S1_ATTACK).addString(effector.getName()));
        effector.sendPacket(new SystemMessage(SystemMessageId.S1_IS_PERFORMING_A_COUNTER_ATTACK).addString(effected.getName()));
        effected = effector;
      }

    if (((effected instanceof L2NpcInstance)) && (((effector instanceof L2PcInstance)) || ((effector instanceof L2Summon))))
    {
      if (Config.INVUL_NPC_LIST.contains(Integer.valueOf(((L2NpcInstance)effected).getNpcId()))) {
        return _emptyEffectSet;
      }
    }

    List effects = new FastList();

    boolean skillMastery = false;
    if ((!isToggle()) && (Formulas.getInstance().calcSkillMastery(effector, this))) {
      skillMastery = true;
    }
    for (EffectTemplate et : _effectTemplates)
    {
      Env env = new Env();
      env.player = effector;
      env.target = effected;
      env.skill = this;
      env.skillMastery = skillMastery;
      L2Effect e = et.getEffect(env);
      if (e == null) continue; effects.add(e);
    }

    if (effects.size() == 0) return _emptyEffectSet;

    return (L2Effect[])effects.toArray(new L2Effect[effects.size()]);
  }

  public final L2Effect[] getEffects(L2CubicInstance effector, L2Character effected)
  {
    if (isPassive()) return _emptyEffectSet;

    if (_effectTemplates == null) {
      return _emptyEffectSet;
    }
    if ((!effector.equals(effected)) && (effected.isInvul())) {
      return _emptyEffectSet;
    }

    List effects = new FastList();

    for (EffectTemplate et : _effectTemplates)
    {
      Env env = new Env();
      env.player = effector.getOwner();
      env.cubic = effector;
      env.target = effected;
      env.skill = this;
      L2Effect e = et.getEffect(env);
      if (e == null) continue; effects.add(e);
    }

    if (effects.size() == 0) return _emptyEffectSet;

    return (L2Effect[])effects.toArray(new L2Effect[effects.size()]);
  }

  public final L2Effect[] getEffectsSelf(L2Character effector)
  {
    if (isPassive()) return _emptyEffectSet;

    if (_effectTemplatesSelf == null) return _emptyEffectSet;

    List effects = new FastList();

    for (EffectTemplate et : _effectTemplatesSelf)
    {
      Env env = new Env();
      env.player = effector;
      env.target = effector;
      env.skill = this;
      L2Effect e = et.getEffect(env);
      if (e == null) {
        continue;
      }
      if (e.getEffectType() == L2Effect.EffectType.CHARGE)
      {
        env.skill = SkillTable.getInstance().getInfo(8, effector.getSkillLevel(8));
        EffectCharge effect = (EffectCharge)env.target.getFirstEffect(L2Effect.EffectType.CHARGE);
        if (effect != null)
        {
          int effectcharge = effect.getLevel();
          if (effectcharge < _numCharges)
          {
            effectcharge++;
            effect.addNumCharges(effectcharge);
            if ((env.target instanceof L2PcInstance))
            {
              env.target.sendPacket(new EtcStatusUpdate((L2PcInstance)env.target));
              SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
              sm.addNumber(effectcharge);
              env.target.sendPacket(sm);
            }
          }
        } else {
          effects.add(e);
        }
      } else {
        effects.add(e);
      }
    }
    if (effects.size() == 0) return _emptyEffectSet;

    return (L2Effect[])effects.toArray(new L2Effect[effects.size()]);
  }

  public final void attach(FuncTemplate f)
  {
    if (_funcTemplates == null)
    {
      _funcTemplates = new FuncTemplate[] { f };
    }
    else
    {
      int len = _funcTemplates.length;
      FuncTemplate[] tmp = new FuncTemplate[len + 1];
      System.arraycopy(_funcTemplates, 0, tmp, 0, len);
      tmp[len] = f;
      _funcTemplates = tmp;
    }
  }

  public final void attach(EffectTemplate effect)
  {
    if (_effectTemplates == null)
    {
      _effectTemplates = new EffectTemplate[] { effect };
    }
    else
    {
      int len = _effectTemplates.length;
      EffectTemplate[] tmp = new EffectTemplate[len + 1];
      System.arraycopy(_effectTemplates, 0, tmp, 0, len);
      tmp[len] = effect;
      _effectTemplates = tmp;
    }
  }

  public final void attachSelf(EffectTemplate effect)
  {
    if (_effectTemplatesSelf == null)
    {
      _effectTemplatesSelf = new EffectTemplate[] { effect };
    }
    else
    {
      int len = _effectTemplatesSelf.length;
      EffectTemplate[] tmp = new EffectTemplate[len + 1];
      System.arraycopy(_effectTemplatesSelf, 0, tmp, 0, len);
      tmp[len] = effect;
      _effectTemplatesSelf = tmp;
    }
  }

  public final void attach(Condition c, boolean itemOrWeapon)
  {
    if (itemOrWeapon) _itemPreCondition = c; else
      _preCondition = c;
  }

  public String toString()
  {
    return "" + _name + "[id=" + _id + ",lvl=" + _level + "]";
  }

  public static enum SkillType
  {
    PDAM, 
    MDAM, 
    CPDAM, 
    MANADAM, 
    DOT, 
    MDOT, 
    DRAIN_SOUL, 
    DRAIN(L2SkillDrain.class), 
    DEATHLINK, 
    FATALCOUNTER, 
    BLOW, 

    BLEED, 
    POISON, 
    STUN, 
    ROOT, 
    CONFUSION, 
    FEAR, 
    SLEEP, 
    CONFUSE_MOB_ONLY, 
    MUTE, 
    PARALYZE, 
    WEAKNESS, 

    HEAL, 
    HOT, 
    BALANCE_LIFE, 
    HEAL_PERCENT, 
    HEAL_STATIC, 
    COMBATPOINTHEAL, 
    CPHOT, 
    MANAHEAL, 
    MANA_BY_LEVEL, 
    MANAHEAL_PERCENT, 
    MANARECHARGE, 
    MPHOT, 

    GIVE_SP, 

    AGGDAMAGE, 
    AGGREDUCE, 
    AGGREMOVE, 
    AGGREDUCE_CHAR, 
    AGGDEBUFF, 

    FISHING, 
    PUMPING, 
    REELING, 

    UNLOCK, 
    ENCHANT_ARMOR, 
    ENCHANT_WEAPON, 
    SOULSHOT, 
    SPIRITSHOT, 
    SIEGEFLAG, 
    TAKECASTLE, 
    WEAPON_SA, 
    DELUXE_KEY_UNLOCK, 
    SOW, 
    HARVEST, 
    GET_PLAYER, 
    CLAN_GATE, 

    COMMON_CRAFT, 
    DWARVEN_CRAFT, 
    CREATE_ITEM(L2SkillCreateItem.class), 
    SUMMON_TREASURE_KEY, 

    SUMMON(L2SkillSummon.class), 
    FEED_PET, 
    DEATHLINK_PET, 
    STRSIEGEASSAULT, 
    ERASE, 
    BETRAY, 

    CANCEL, 
    CANCEL_DEBUFF, 
    MAGE_BANE, 
    WARRIOR_BANE, 
    NEGATE, 

    BUFF, 
    DEBUFF, 
    PASSIVE, 
    CONT, 
    SIGNET(L2SkillSignet.class), 
    SIGNET_CASTTIME(L2SkillSignetCasttime.class), 

    RESURRECT, 
    CHARGE(L2SkillCharge.class), 
    CHARGE_EFFECT(L2SkillChargeEffect.class), 
    CHARGEDAM(L2SkillChargeDmg.class), 
    MHOT, 
    DETECT_WEAKNESS, 
    LUCK, 
    RECALL, 
    SUMMON_FRIEND, 
    REFLECT, 
    SPOIL, 
    SWEEP, 
    FAKE_DEATH, 
    UNBLEED, 
    UNPOISON, 
    UNDEAD_DEFENSE, 
    SEED(L2SkillSeed.class), 
    BEAST_FEED, 
    FORCE_BUFF, 
    CHAMELEON, 

    SUMMON_PET, 

    COREDONE, 

    NOTDONE;

    private final Class<? extends L2Skill> _class;

    public L2Skill makeSkill(StatsSet set)
    {
      try {
        Constructor c = _class.getConstructor(new Class[] { StatsSet.class });

        return (L2Skill)c.newInstance(new Object[] { set });
      }
      catch (Exception e) {
      }
      throw new RuntimeException(e);
    }

    private SkillType()
    {
      _class = L2SkillDefault.class;
    }

    private SkillType(Class<? extends L2Skill> classType)
    {
      _class = classType;
    }
  }

  public static enum SkillTargetType
  {
    TARGET_NONE, 
    TARGET_SELF, 
    TARGET_ONE, 
    TARGET_PARTY, 
    TARGET_ALLY, 
    TARGET_CLAN, 
    TARGET_PET, 
    TARGET_AREA, 
    TARGET_AURA, 
    TARGET_CORPSE, 
    TARGET_UNDEAD, 
    TARGET_AREA_UNDEAD, 
    TARGET_AREA_ANGEL, 
    TARGET_CORPSE_ALLY, 
    TARGET_CORPSE_CLAN, 
    TARGET_CORPSE_PLAYER, 
    TARGET_CORPSE_PET, 
    TARGET_ITEM, 
    TARGET_AREA_CORPSE_MOB, 
    TARGET_CORPSE_MOB, 
    TARGET_UNLOCKABLE, 
    TARGET_HOLY, 
    TARGET_PARTY_MEMBER, 
    TARGET_PARTY_OTHER, 
    TARGET_ENEMY_SUMMON, 
    TARGET_OWNER_PET, 
    TARGET_GROUND, 
    TARGET_TYRANNOSAURUS;
  }

  public static enum SkillOpType
  {
    OP_PASSIVE, OP_ACTIVE, OP_TOGGLE, OP_CHANCE;
  }
}