package net.sf.l2j.gameserver.model;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.HeroSkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.conditions.Condition;
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
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSummon;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSummonNpc;
import net.sf.l2j.gameserver.skills.targets.TargetList;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.log.AbstractLogger;

public abstract class L2Skill
{
  protected static final Logger _log = AbstractLogger.getLogger(L2Skill.class.getName());
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
  public static final int STAT_MCRITRATE = 338;
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
  private int _displayId;
  private final String _name;
  private final SkillOpType _operateType;
  private final boolean _magic;
  private final int _mpConsume;
  private final int _mpInitialConsume;
  private final int _hpConsume;
  private final int _itemConsume;
  private final int _itemConsumeId;
  private final int _itemConsumeOT;
  private final int _itemConsumeIdOT;
  private final int _itemConsumeSteps;
  private final int _summonTotalLifeTime;
  private final int _summonTimeLostIdle;
  private final int _summonTimeLostActive;
  private final boolean _isCubic;
  private final int _itemConsumeTime;
  private final int _castRange;
  private final int _effectRange;
  private final int _hitTime;
  private final int _coolTime;
  private final int _reuseDelay;
  private final int _buffDuration;
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
  private final int _effectId;
  private final int _effectLvl;
  private final boolean _ispotion;
  private final int _element;
  private final int _savevs;
  private final int _initialEffectDelay;
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
  protected Condition _preCondition;
  protected Condition _itemPreCondition;
  protected FuncTemplate[] _funcTemplates;
  protected EffectTemplate[] _effectTemplates;
  protected EffectTemplate[] _effectTemplatesSelf;
  protected ChanceCondition _chanceCondition = null;
  private final int _chanceTriggeredId;
  private final int _chanceTriggeredLevel;
  private final boolean _fixedReuse;
  private final boolean _cancelProtected;
  private boolean _isHeroDebuff = false;
  private boolean _isForbiddenProfileSkill = false;
  private boolean _isProtected = false;
  private boolean _isNotAura = true;
  private boolean _isBattleForceSkill = false;
  private boolean _isSpellForceSkill = false;
  private boolean _isAugment = false;
  private boolean _isAOEpvp = false;
  private boolean _isBuff = false;
  private boolean _isSSBuff = false;
  private boolean _isMalariaBuff = false;
  private boolean _isNoShot = false;
  private boolean _isSkillTypeOffensive = false;
  private boolean _isPvpSkill = false;
  private boolean _useSoulShot = false;
  private boolean _useFishShot = false;
  private final boolean _isActive;
  private final boolean _isPassive;
  private final boolean _isToggle;
  private final boolean _isChance;
  private final boolean _isDeathLink;
  private boolean _isDebuff = false;
  private boolean _isMagicalSlow = false;
  private boolean _isPhysicalSlow = false;
  private boolean _isMiscDebuff = false;
  private boolean _isMiscDebuffPhys = false;
  private boolean _isNobleSkill = false;
  private boolean _isHerosSkill = false;
  private boolean _isClanSkill = false;
  private boolean _isSiegeSkill = false;
  private boolean _isFishingSkill = false;
  private boolean _isDwarvenSkill = false;
  private boolean _isMiscSkill = false;
  private boolean _canTargetSelf = false;
  private boolean _continueAttack = false;
  private boolean _unlockSkill = false;
  private boolean _useAltFormula = false;
  private boolean _isSupportBuff = false;
  private boolean _isSupportTarget = false;
  private boolean _isSignedTarget = false;
  private boolean _isAuraSignedTarget = false;
  private boolean _isUseSppritShot = false;
  private boolean _isNotForCursed = false;
  private double _baseLandRate;
  private SkillType _baseSkillType;
  private TargetList _targetList = null;

  protected L2Skill(StatsSet set) {
    _id = set.getInteger("skill_id");
    _level = set.getInteger("level");

    _displayId = set.getInteger("displayId", _id);
    _name = set.getString("name");
    _operateType = ((SkillOpType)set.getEnum("operateType", SkillOpType.class));
    _magic = set.getBool("isMagic", false);
    _ispotion = set.getBool("isPotion", false);
    _mpConsume = set.getInteger("mpConsume", 0);
    _mpInitialConsume = set.getInteger("mpInitialConsume", 0);
    _hpConsume = set.getInteger("hpConsume", 0);
    _itemConsume = set.getInteger("itemConsumeCount", 0);
    _itemConsumeId = set.getInteger("itemConsumeId", 0);
    _itemConsumeOT = set.getInteger("itemConsumeCountOT", 0);
    _itemConsumeIdOT = set.getInteger("itemConsumeIdOT", 0);
    _itemConsumeTime = set.getInteger("itemConsumeTime", 0);
    _itemConsumeSteps = set.getInteger("itemConsumeSteps", 0);
    _summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 1200000);
    _summonTimeLostIdle = set.getInteger("summonTimeLostIdle", 0);
    _summonTimeLostActive = set.getInteger("summonTimeLostActive", 0);

    _isCubic = set.getBool("isCubic", false);

    _castRange = set.getInteger("castRange", 0);
    _effectRange = set.getInteger("effectRange", -1);

    _hitTime = set.getInteger("hitTime", 0);
    _coolTime = set.getInteger("coolTime", 0);
    _initialEffectDelay = set.getInteger("initialEffectDelay", 0);

    _reuseDelay = set.getInteger("reuseDelay", 0);
    _buffDuration = set.getInteger("buffDuration", 0);

    _skillRadius = set.getInteger("skillRadius", 80);

    _targetType = ((SkillTargetType)set.getEnum("target", SkillTargetType.class));
    _power = set.getFloat("power", 0.0F);
    _effectPoints = set.getInteger("effectPoints", 0);
    _negateStats = set.getString("negateStats", "").split(" ");
    _negatePower = set.getFloat("negatePower", 0.0F);
    _negateId = set.getInteger("negateId", 0);
    _magicLevel = set.getInteger("magicLvl", SkillTreeTable.getInstance().getMinSkillLevel(_id, _level));
    _levelDepend = set.getInteger("lvlDepend", 0);
    _stat = ((Stats)set.getEnum("stat", Stats.class, null));

    _skillType = ((SkillType)set.getEnum("skillType", SkillType.class));
    _effectType = ((SkillType)set.getEnum("effectType", SkillType.class, null));
    _effectPower = set.getInteger("effectPower", 0);
    _effectId = set.getInteger("effectId", 0);
    _effectLvl = set.getInteger("effectLevel", 0);

    _element = set.getInteger("element", 0);
    _savevs = set.getInteger("save", 0);

    _condition = set.getInteger("condition", 0);
    _conditionValue = set.getInteger("conditionValue", 0);
    _overhit = set.getBool("overHit", false);
    _isSuicideAttack = set.getBool("isSuicideAttack", false);
    _weaponsAllowed = set.getInteger("weaponsAllowed", 0);
    _armorsAllowed = set.getInteger("armorsAllowed", 0);

    _addCrossLearn = set.getInteger("addCrossLearn", 1000);
    _mulCrossLearn = set.getFloat("mulCrossLearn", 2.0F);
    _mulCrossLearnRace = set.getFloat("mulCrossLearnRace", 2.0F);
    _mulCrossLearnProf = set.getFloat("mulCrossLearnProf", 3.0F);
    _minPledgeClass = set.getInteger("minPledgeClass", 0);
    _numCharges = set.getInteger("num_charges", getLevel());
    _forceId = set.getInteger("forceId", 0);

    if (_operateType == SkillOpType.OP_CHANCE) {
      _chanceCondition = ChanceCondition.parse(set);
    }

    _chanceTriggeredId = set.getInteger("chanceTriggeredId", 0);
    _chanceTriggeredLevel = set.getInteger("chanceTriggeredLevel", 0);

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
    if (canLearn == null) {
      _canLearn = null;
    } else {
      _canLearn = new FastList();
      StringTokenizer st = new StringTokenizer(canLearn, " \r\n\t,;");
      while (st.hasMoreTokens()) {
        String cls = st.nextToken();
        try {
          _canLearn.add(ClassId.valueOf(cls));
        } catch (Throwable t) {
          _log.log(Level.SEVERE, "Bad class " + cls + " to learn skill", t);
        }
      }
    }

    String teachers = set.getString("teachers", null);
    if (teachers == null) {
      _teachers = null;
    } else {
      _teachers = new FastList();
      StringTokenizer st = new StringTokenizer(teachers, " \r\n\t,;");
      while (st.hasMoreTokens()) {
        String npcid = st.nextToken();
        try {
          _teachers.add(Integer.valueOf(Integer.parseInt(npcid)));
        } catch (Throwable t) {
          _log.log(Level.SEVERE, "Bad teacher id " + npcid + " to teach skill", t);
        }
      }

    }

    _fixedReuse = Config.ALT_FIXED_REUSES.contains(Integer.valueOf(_id));
    _cancelProtected = Config.PROTECTED_BUFFS.contains(Integer.valueOf(_id));

    _isDeathLink = (_skillType == SkillType.DEATHLINK);

    _isActive = (_operateType == SkillOpType.OP_ACTIVE);
    _isPassive = (_operateType == SkillOpType.OP_PASSIVE);
    _isToggle = (_operateType == SkillOpType.OP_TOGGLE);
    _isChance = (_operateType == SkillOpType.OP_CHANCE);

    switch (_skillType) {
    case PUMPING:
    case REELING:
      _useFishShot = true;
    }

    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[_skillType.ordinal()]) {
    case 3:
    case 4:
    case 5:
    case 6:
      _useSoulShot = true;
    }

    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[_skillType.ordinal()]) {
    case 4:
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
      _isPvpSkill = true;
    case 5:
    case 6:
    }

    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[_skillType.ordinal()]) {
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
    case 44:
    case 45:
    case 46:
      _isSkillTypeOffensive = true;
    case 26:
    }

    switch (_id) {
    case 1375:
    case 1376:
      _isHeroDebuff = true;
    }

    if ((Config.F_PROFILE_BUFFS.contains(Integer.valueOf(_id))) || ((_id >= 5123) && (_id <= 5129)))
    {
      _isForbiddenProfileSkill = true;
    }

    switch (_skillType) {
    case PDAM:
    case DOT:
    case MDAM:
    case CPDAM:
      _isProtected = true;
    }

    switch (_id) {
    case 1231:
    case 1275:
    case 1417:
      _isNotAura = false;
    }

    switch (_id) {
    case 454:
    case 455:
    case 456:
    case 457:
    case 458:
    case 459:
    case 460:
      _isBattleForceSkill = true;
    }

    switch (_id) {
    case 1419:
    case 1420:
    case 1421:
    case 1422:
    case 1423:
    case 1424:
    case 1425:
    case 1426:
    case 1427:
    case 1428:
      _isSpellForceSkill = true;
    }

    if ((_id >= 3100) && (_id <= 3299)) {
      _isAugment = true;
    }

    if ((_id > 4360) && (_id < 4367)) {
      _isSSBuff = true;
    }

    if ((_id >= 4552) && (_id <= 4554)) {
      _isMalariaBuff = true;
    }

    if ((_skillType == SkillType.FAKE_DEATH) || (_id == 11) || (_id == 12)) {
      _isNoShot = true;
    }

    switch (_id) {
    case 36:
    case 48:
    case 320:
    case 361:
    case 452:
    case 1417:
      _isAOEpvp = true;
    }

    switch (_skillType) {
    case DEBUFF:
    case BUFF:
    case REFLECT:
    case HEAL_PERCENT:
    case MANAHEAL_PERCENT:
      _isBuff = true;
    }

    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[_skillType.ordinal()]) {
    case 3:
    case 4:
    case 9:
    case 11:
    case 12:
    case 13:
    case 14:
    case 15:
    case 18:
    case 20:
    case 22:
    case 23:
    case 24:
    case 29:
    case 51:
      _isDebuff = true;
    case 5:
    case 6:
    case 7:
    case 8:
    case 10:
    case 16:
    case 17:
    case 19:
    case 21:
    case 25:
    case 26:
    case 27:
    case 28:
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
    case 44:
    case 45:
    case 46:
    case 47:
    case 48:
    case 49:
    case 50: } switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[_skillType.ordinal()]) {
    case 23:
    case 24:
    case 25:
    case 47:
    case 48:
    case 49:
    case 51:
    case 52:
    case 53:
    case 54:
    case 55:
    case 56:
    case 57:
    case 58:
    case 59:
    case 60:
    case 61:
      _canTargetSelf = true;
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
    case 44:
    case 45:
    case 46:
    case 50: } switch (_skillType) {
    case PDAM:
    case CHARGEDAM:
    case BLOW:
    case SPOIL:
    case DRAIN_SOUL:
    case SOW:
      _continueAttack = true;
    }

    switch (_skillType) {
    case DELUXE_KEY_UNLOCK:
    case UNLOCK:
      _unlockSkill = true;
    }

    switch (_id) {
    case 102:
    case 105:
    case 127:
    case 1099:
    case 1160:
    case 1184:
    case 1236:
    case 1298:
      _isMagicalSlow = true;
    }

    switch (_id) {
    case 95:
    case 354:
      _isPhysicalSlow = true;
    }

    switch (_id) {
    case 65:
    case 115:
    case 122:
    case 279:
    case 405:
    case 450:
    case 1042:
    case 1049:
    case 1056:
    case 1064:
    case 1092:
    case 1096:
    case 1104:
    case 1164:
    case 1169:
    case 1170:
    case 1206:
    case 1222:
    case 1246:
    case 1247:
    case 1248:
    case 1263:
    case 1269:
    case 1272:
    case 1336:
    case 1337:
    case 1338:
    case 1339:
    case 1340:
    case 1342:
    case 1343:
    case 1358:
    case 1359:
    case 1360:
    case 1361:
    case 1366:
    case 1367:
    case 1376:
    case 1381:
    case 1382:
    case 1386:
    case 1396:
    case 3194:
    case 4108:
    case 4689:
    case 5092:
    case 5220:
      _isMiscDebuff = true;
    }

    switch (_id) {
    case 97:
    case 106:
    case 116:
    case 342:
    case 353:
    case 367:
    case 400:
    case 407:
    case 408:
    case 412:
      _isMiscDebuffPhys = true;
    }

    if (((_id >= 325) && (_id <= 327)) || ((_id >= 1323) && (_id <= 1327))) {
      _isNobleSkill = true;
    }

    if (((_id >= 1374) && (_id <= 1376)) || ((_id >= 395) && (_id <= 396))) {
      _isHerosSkill = true;
    }

    if ((_id >= 370) && (_id <= 391)) {
      _isClanSkill = true;
    }

    if ((_id >= 246) && (_id <= 247)) {
      _isSiegeSkill = true;
    }

    if ((_id >= 1312) && (_id <= 1322)) {
      _isFishingSkill = true;
    }

    if ((_id >= 1368) && (_id <= 1373)) {
      _isDwarvenSkill = true;
    }

    if ((_id >= 3000) && (_id < 10000)) {
      _isMiscSkill = true;
    }

    _isOffensive = set.getBool("offensive", isSkillTypeOffensive());

    _targetList = TargetList.create(_targetType);

    _baseLandRate = _power;
    _baseSkillType = _skillType;
    switch (_skillType) {
    case PDAM:
    case MDAM:
      _baseSkillType = getEffectType();
      if (_baseSkillType != null) {
        _baseLandRate = getEffectPower();
      } else {
        switch (_id) {
        case 279:
          _baseLandRate = 20.0D;
          break;
        case 352:
          _baseLandRate = 80.0D;
          break;
        case 400:
          _baseLandRate = 60.0D;
          break;
        default:
          _baseLandRate = 60.0D;
        }

        _baseSkillType = SkillType.ROOT;
        if (_baseSkillType != SkillType.PDAM) break;
        _baseSkillType = SkillType.STUN;
      }

    }

    if (_isMiscDebuff) {
      _baseSkillType = SkillType.ROOT;
    } else if (_isMiscDebuffPhys) {
      if (_id == 279) {
        _baseLandRate = 40.0D;
      }

      _baseSkillType = SkillType.DEBUFF;
    }

    switch (_id) {
    case 115:
    case 122:
    case 403:
    case 1056:
    case 1071:
    case 1074:
    case 1083:
    case 1223:
    case 1224:
    case 1263:
    case 1339:
    case 1340:
    case 1341:
    case 1342:
    case 1358:
    case 1359:
    case 1360:
    case 1361:
    case 1383:
    case 1384:
    case 1385:
      _useAltFormula = true;
    }

    switch (_id) {
    case 48:
      _baseLandRate += 20.0D;
      break;
    case 84:
      _baseLandRate += 20.0D;
      break;
    case 367:
      _baseLandRate += 20.0D;
      break;
    case 452:
      _baseLandRate += 20.0D;
      break;
    case 403:
      _baseLandRate += 20.0D;
      break;
    case 1263:
      _baseLandRate += 20.0D;
      break;
    case 1339:
      _baseLandRate -= 20.0D;
      break;
    case 1340:
      _baseLandRate -= 20.0D;
      break;
    case 1341:
      _baseLandRate -= 20.0D;
      break;
    case 1342:
      _baseLandRate -= 20.0D;
      break;
    case 1343:
      _baseLandRate += 40.0D;
    }

    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[_skillType.ordinal()]) {
    case 47:
    case 48:
    case 49:
    case 50:
    case 52:
    case 53:
    case 55:
    case 56:
    case 57:
    case 59:
      _isSupportBuff = true;
    case 51:
    case 54:
    case 58:
    }
    switch (_skillType) {
    case DOT:
    case BUFF:
    case MANAHEAL:
    case RESURRECT:
    case RECALL:
      _isUseSppritShot = true;
    }

    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillTargetType[_targetType.ordinal()]) {
    case 1:
    case 2:
    case 3:
    case 4:
    case 5:
      _isSupportTarget = true;
    }

    switch (_targetType) {
    case TARGET_SIGNET_GROUND:
    case TARGET_SIGNET:
      _isSignedTarget = true;
    }

    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillTargetType[_targetType.ordinal()]) {
    case 1:
    case 3:
    case 4:
    case 5:
    case 6:
    case 7:
    case 8:
      _isAuraSignedTarget = true;
    case 2:
    }

    if (Config.FORB_CURSED_SKILLS.contains(Integer.valueOf(_id)))
      _isNotForCursed = true;
  }

  public boolean isSupportSkill()
  {
    return _isSupportBuff;
  }

  public boolean isSupportTargetType() {
    return _isSupportTarget;
  }

  public boolean isSignedTargetType() {
    return _isSignedTarget;
  }

  public boolean isAuraSignedTargetType() {
    return _isAuraSignedTarget;
  }

  public boolean isUseSppritShot() {
    return _isUseSppritShot;
  }
  public abstract void useSkill(L2Character paramL2Character, FastList<L2Object> paramFastList);

  public final boolean isPotion() {
    return _ispotion;
  }

  public final int getArmorsAllowed() {
    return _armorsAllowed;
  }

  public final int getConditionValue() {
    return _conditionValue;
  }

  public final SkillType getSkillType() {
    return _skillType;
  }

  public final int getSavevs() {
    return _savevs;
  }

  public final int getElement() {
    return _element;
  }

  public final SkillTargetType getTargetType()
  {
    return _targetType;
  }

  public final int getCondition() {
    return _condition;
  }

  public final boolean isOverhit() {
    return _overhit;
  }

  public final boolean isSuicideAttack() {
    return _isSuicideAttack;
  }

  public final double getPower(L2Character activeChar)
  {
    if ((_isDeathLink) && (activeChar != null)) {
      return _power * Math.pow(1.7165D - activeChar.getCurrentHp() / activeChar.getMaxHp(), 2.0D) * 0.577D;
    }

    return _power;
  }

  public final double getPower() {
    return _power;
  }

  public final int getEffectPoints() {
    return _effectPoints;
  }

  public final String[] getNegateStats() {
    return _negateStats;
  }

  public final float getNegatePower() {
    return _negatePower;
  }

  public final int getNegateId() {
    return _negateId;
  }

  public final int getMagicLevel() {
    return _magicLevel;
  }

  public final int getLevelDepend() {
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

  public final int getId()
  {
    return _id;
  }

  public int getDisplayId() {
    return _displayId;
  }

  public void setDisplayId(int id) {
    _displayId = id;
  }

  public int getForceId() {
    return _forceId;
  }

  public final int getInitialEffectDelay()
  {
    return _initialEffectDelay;
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

  public final boolean isCubic() {
    return _isCubic;
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
  @Deprecated
  public final int getSkillTime() {
    return _hitTime;
  }

  public final int getHitTime() {
    return _hitTime;
  }

  public final int getCoolTime()
  {
    return _coolTime;
  }

  public final int getSkillRadius() {
    return _skillRadius;
  }

  public final boolean isActive() {
    return _isActive;
  }

  public final boolean isPassive() {
    return _isPassive;
  }

  public final boolean isToggle() {
    return _isToggle;
  }

  public final boolean isChance() {
    return _isChance;
  }

  public ChanceCondition getChanceCondition() {
    return _chanceCondition;
  }

  public final int getChanceTriggeredId() {
    return _chanceTriggeredId;
  }

  public final int getChanceTriggeredLevel() {
    return _chanceTriggeredLevel;
  }

  public final boolean isDance() {
    return _isDance;
  }

  public final int getNextDanceMpCost() {
    return _nextDanceCost;
  }

  public final float getSSBoost() {
    return _sSBoost;
  }

  public final int getAggroPoints() {
    return _aggroPoints;
  }

  public final boolean useSoulShot() {
    return _useSoulShot;
  }

  public final boolean useSpiritShot() {
    return isMagic();
  }

  public final boolean useFishShot() {
    return _useFishShot;
  }

  public final int getWeaponsAllowed() {
    return _weaponsAllowed;
  }

  public final int getCrossLearnAdd() {
    return _addCrossLearn;
  }

  public final float getCrossLearnMul() {
    return _mulCrossLearn;
  }

  public final float getCrossLearnRace() {
    return _mulCrossLearnRace;
  }

  public final float getCrossLearnProf() {
    return _mulCrossLearnProf;
  }

  public final boolean getCanLearn(ClassId cls) {
    return (_canLearn == null) || (_canLearn.contains(cls));
  }

  public final boolean canTeachBy(int npcId) {
    return (_teachers == null) || (_teachers.contains(Integer.valueOf(npcId)));
  }

  public int getMinPledgeClass() {
    return _minPledgeClass;
  }

  public final boolean isPvpSkill() {
    return _isPvpSkill;
  }

  public final boolean isOffensive() {
    return _isOffensive;
  }

  public final boolean isHeroSkill() {
    return _isHeroSkill;
  }

  public final int getNumCharges() {
    return _numCharges;
  }

  public final int getBaseCritRate() {
    return _baseCritRate;
  }

  public final int getLethalChance1() {
    return _lethalEffect1;
  }

  public final int getLethalChance2() {
    return _lethalEffect2;
  }

  public final boolean getDmgDirectlyToHP() {
    return _directHpDmg;
  }

  public final boolean isSkillTypeOffensive() {
    return _isSkillTypeOffensive;
  }

  public final boolean getWeaponDependancy(L2Character activeChar)
  {
    if (getWeaponDependancy(activeChar, false)) {
      return true;
    }

    activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(this));
    return false;
  }

  public final boolean getWeaponDependancy(L2Character activeChar, boolean chance) {
    int weaponsAllowed = getWeaponsAllowed();

    if (weaponsAllowed == 0) {
      return true;
    }
    if (activeChar.getActiveWeaponItem() != null)
    {
      L2WeaponType playerWeapon = activeChar.getActiveWeaponItem().getItemType();
      int mask = playerWeapon.mask();
      if ((mask & weaponsAllowed) != 0) {
        return true;
      }

      if (activeChar.getSecondaryWeaponItem() != null) {
        playerWeapon = activeChar.getSecondaryWeaponItem().getItemType();
        mask = playerWeapon.mask();
        if ((mask & weaponsAllowed) != 0) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean checkCondition(L2Character activeChar, L2Object target, boolean itemOrWeapon)
  {
    Condition preCondition = _preCondition;
    if (itemOrWeapon) {
      preCondition = _itemPreCondition;
    }
    if (preCondition == null) {
      return true;
    }

    Env env = new Env();
    env.cha = activeChar;
    if (target.isL2Character())
    {
      env.target = ((L2Character)target);
    }
    env.skill = this;

    if (!preCondition.test(env)) {
      String msg = preCondition.getMessage();
      if (msg != null) {
        activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_S2).addString(msg));
      }

      return false;
    }
    return true;
  }

  public boolean checkForceCondition(L2PcInstance activeChar, int id) {
    if (Config.DISABLE_FORCES) {
      return true;
    }

    int forceId = 0;
    boolean isBattle = isBattleForceSkill();

    if (isBattle)
      forceId = 5104;
    else {
      forceId = 5105;
    }

    L2Effect force = activeChar.getFirstEffect(forceId);
    if (force != null)
    {
      return force.getLevel() >= getForceLvlFor(id);
    }
    return false;
  }

  public int getForceLvlFor(int id) {
    int Level = 5;
    switch (_id) {
    case 454:
    case 455:
    case 456:
    case 457:
    case 458:
    case 459:
    case 460:
    case 1424:
    case 1425:
    case 1426:
    case 1427:
      Level = 2;
      break;
    case 1419:
    case 1420:
    case 1421:
    case 1422:
    case 1423:
    case 1428:
      Level = 3;
    }

    return Level;
  }

  public final FastList<L2Object> getTargetList(L2Character activeChar, boolean onlyFirst)
  {
    L2Character target = null;

    L2Object objTarget = activeChar.getTarget();

    if ((objTarget != null) && (objTarget.isL2Character())) {
      target = (L2Character)objTarget;
    }

    return getTargetList(activeChar, onlyFirst, target);
  }

  public final FastList<L2Object> getTargetList(L2Character activeChar, boolean onlyFirst, L2Character target)
  {
    return _targetList.getTargetList(new FastList(), activeChar, onlyFirst, target, this);
  }

  public final FastList<L2Object> getTargetList(L2Character activeChar) {
    return getTargetList(activeChar, false);
  }

  public final L2Object getFirstOfTargetList(L2Character activeChar) {
    FastList ftargets = getTargetList(activeChar, true);
    if ((ftargets == null) || (ftargets.isEmpty())) {
      return null;
    }

    return (L2Object)ftargets.getFirst();
  }

  public final Func[] getStatFuncs(L2Effect effect, L2Character player) {
    if ((!player.isPlayer()) && (!player.isL2Attackable()) && (!player.isL2Summon()))
    {
      return _emptyFunctionSet;
    }
    if (_funcTemplates == null) {
      return _emptyFunctionSet;
    }
    List funcs = new FastList();
    for (FuncTemplate t : _funcTemplates) {
      Env env = new Env();
      env.cha = player;
      env.skill = this;
      Func f = t.getFunc(env, this);
      if (f != null) {
        funcs.add(f);
      }
    }
    if (funcs.isEmpty()) {
      return _emptyFunctionSet;
    }
    return (Func[])funcs.toArray(new Func[funcs.size()]);
  }

  public boolean hasEffects() {
    return (_effectTemplates != null) && (_effectTemplates.length > 0);
  }

  public final L2Effect[] getEffects(L2Character effector, L2Character effected) {
    if ((isPassive()) || (_effectTemplates == null)) {
      return _emptyEffectSet;
    }

    if ((isPvpSkill()) && ((effected.isRaid()) || (effected.isDebuffProtected()))) {
      return _emptyEffectSet;
    }

    if ((effector != effected) && (effected.isInvul())) {
      return _emptyEffectSet;
    }

    List effects = new FastList();

    boolean skillMastery = false;

    if ((!isToggle()) && (Formulas.calcSkillMastery(effector))) {
      skillMastery = true;
    }

    for (EffectTemplate et : _effectTemplates) {
      Env env = new Env();
      env.cha = effector;
      env.target = effected;
      env.skill = this;
      env.skillMastery = skillMastery;
      L2Effect e = et.getEffect(env);
      if (e != null) {
        effects.add(e);
      }
    }

    if (effects.isEmpty()) {
      return _emptyEffectSet;
    }

    return (L2Effect[])effects.toArray(new L2Effect[effects.size()]);
  }

  public final L2Effect[] getEffectsSelf(L2Character effector) {
    if (isPassive()) {
      return _emptyEffectSet;
    }

    if (_effectTemplatesSelf == null) {
      return _emptyEffectSet;
    }

    List effects = new FastList();

    for (EffectTemplate et : _effectTemplatesSelf) {
      Env env = new Env();
      env.cha = effector;
      env.target = effector;
      env.skill = this;
      L2Effect e = et.getEffect(env);
      if (e != null) {
        effects.add(e);
      }
    }
    if (effects.isEmpty()) {
      return _emptyEffectSet;
    }

    return (L2Effect[])effects.toArray(new L2Effect[effects.size()]);
  }

  public final void attach(FuncTemplate f) {
    if (_funcTemplates == null) {
      _funcTemplates = new FuncTemplate[] { f };
    } else {
      int len = _funcTemplates.length;
      FuncTemplate[] tmp = new FuncTemplate[len + 1];
      System.arraycopy(_funcTemplates, 0, tmp, 0, len);
      tmp[len] = f;
      _funcTemplates = tmp;
    }
  }

  public final void attach(EffectTemplate effect) {
    if (_effectTemplates == null) {
      _effectTemplates = new EffectTemplate[] { effect };
    } else {
      int len = _effectTemplates.length;
      EffectTemplate[] tmp = new EffectTemplate[len + 1];
      System.arraycopy(_effectTemplates, 0, tmp, 0, len);
      tmp[len] = effect;
      _effectTemplates = tmp;
    }
  }

  public final void attachSelf(EffectTemplate effect)
  {
    if (_effectTemplatesSelf == null) {
      _effectTemplatesSelf = new EffectTemplate[] { effect };
    } else {
      int len = _effectTemplatesSelf.length;
      EffectTemplate[] tmp = new EffectTemplate[len + 1];
      System.arraycopy(_effectTemplatesSelf, 0, tmp, 0, len);
      tmp[len] = effect;
      _effectTemplatesSelf = tmp;
    }
  }

  public final void attach(Condition c, boolean itemOrWeapon) {
    if (itemOrWeapon)
      _itemPreCondition = c;
    else
      _preCondition = c;
  }

  public String toString()
  {
    return "" + _name + "[id=" + _id + ",lvl=" + _level + "]";
  }

  public final boolean isHeroDebuff()
  {
    return _isHeroDebuff;
  }

  public final boolean isForbiddenProfileSkill()
  {
    return _isForbiddenProfileSkill;
  }

  public final boolean isProtected()
  {
    return _isProtected;
  }

  public final boolean isNotAura()
  {
    return _isNotAura;
  }

  public final boolean isBattleForceSkill()
  {
    return _isBattleForceSkill;
  }

  public final boolean isSpellForceSkill() {
    return _isSpellForceSkill;
  }

  public final boolean isAugment() {
    return _isAugment;
  }

  public final boolean isAOEpvp() {
    return _isAOEpvp;
  }

  public final boolean isBuff() {
    return _isBuff;
  }

  public final boolean isSSBuff() {
    return _isSSBuff;
  }

  public final boolean isMalariaBuff() {
    return _isMalariaBuff;
  }

  public final boolean isNoShot() {
    return _isNoShot;
  }

  public final boolean isFixedReuse() {
    return _fixedReuse;
  }

  public final boolean isCancelProtected() {
    return _cancelProtected;
  }

  public final boolean isDebuff() {
    return _isDebuff;
  }

  public final boolean isMagicalSlow() {
    return _isMagicalSlow;
  }

  public final boolean isPhysicalSlow() {
    return _isPhysicalSlow;
  }

  public final boolean isMiscDebuff() {
    return _isMiscDebuff;
  }

  public final boolean isMiscDebuffPhys() {
    return _isMiscDebuffPhys;
  }

  public final boolean canTargetSelf() {
    return _canTargetSelf;
  }

  public final boolean isNobleSkill()
  {
    return _isNobleSkill;
  }

  public final boolean isHerosSkill() {
    return _isHerosSkill;
  }

  public final boolean isClanSkill() {
    return _isClanSkill;
  }

  public final boolean isSiegeSkill() {
    return _isSiegeSkill;
  }

  public final boolean isFishingSkill() {
    return _isFishingSkill;
  }

  public final boolean isDwarvenSkill() {
    return _isDwarvenSkill;
  }

  public final boolean isMiscSkill() {
    return _isMiscSkill;
  }

  public final boolean isContinueAttack() {
    return _continueAttack;
  }

  public final boolean isNotUnlock() {
    return !_unlockSkill;
  }

  public boolean isNotForCursed() {
    return _isNotForCursed;
  }

  public boolean useAltFormula(L2Character attacker) {
    if ((_id != 1097) && (attacker.isOverlord())) {
      return true;
    }

    return _useAltFormula;
  }

  public double getBaseLandRate()
  {
    return _baseLandRate;
  }

  public SkillType getBaseSkillType() {
    return _baseSkillType;
  }

  private boolean useTempHook(int _id)
  {
    switch (_id) {
    case 1358:
    case 1359:
    case 1360:
    case 1361:
      return true;
    }
    return false;
  }

  private double caclMatkMod(double ss, double mAtk, double mDef, double mAtkModifier)
  {
    if (isMagic()) {
      mAtkModifier = 14.0D * Math.sqrt(ss * mAtk) / mDef;
    }
    return mAtkModifier;
  }

  private double calcStatMod(double statmodifier, double mAtkModifier) {
    return _baseLandRate / statmodifier * mAtkModifier;
  }

  private double calcResMod(L2Character target, double rate) {
    double resMod = 1.0D;
    double res = target.calcSkillResistans(this, _baseSkillType, _skillType);
    if (res < 0.0D)
      resMod = 1.0D / (1.0D - 0.075D * res);
    else if (res >= 0.0D) {
      resMod = 1.0D + 0.02D * res;
    }
    rate *= resMod;
    return rate;
  }

  private double calcLevelMod(double levelmod, double delta) {
    double deltamod = delta / 5.0D * 5.0D;
    if (deltamod != delta) {
      if (delta < 0.0D)
        levelmod = deltamod - 5.0D;
      else if (delta >= 0.0D)
        levelmod = deltamod + 5.0D;
    }
    else if (deltamod == delta) {
      levelmod = deltamod;
    }
    return levelmod;
  }

  private double calcAltVuln(L2Character target, double rate)
  {
    switch (_id) {
    case 403:
      rate *= target.calcStat(Stats.ROOT_VULN, 1.0D, target, null);
      break;
    case 1056:
      rate = Math.min(rate, _baseLandRate * 1.5D);
      rate *= target.calcStat(Stats.CANCEL_VULN, 1.0D, target, null);
    }

    return rate;
  }

  private double caclAttr(L2Character target) {
    switch (_id) {
    case 106:
    case 353:
      return target.calcStat(Stats.DERANGEMENT_VULN, 1.0D, target, null);
    case 95:
    case 367:
      return target.calcStat(Stats.EARTH_VULN, 1.0D, target, this);
    case 400:
      return target.calcStat(Stats.HOLY_VULN, 1.0D, target, this);
    }
    return 1.0D;
  }

  public double calcAltActivateRate(double mAtk, double mDef, L2Character target, double ss)
  {
    double mAtkModifier = caclMatkMod(ss, mAtk, mDef, 1.0D);
    if (useTempHook(_id)) {
      int min = 60;
      if (target.getFirstEffect(1354) != null) {
        min = (int)(_baseLandRate / 2.0D);
      }

      return Math.min(_baseLandRate, min) * mAtkModifier;
    }

    double rate = calcStatMod(isMagic() ? target.calcMENModifier() : target.calcCONModifier(), mAtkModifier);
    rate = calcResMod(target, rate);
    rate = calcAltVuln(target, rate);

    return rate + calcLevelMod(0.0D, _magicLevel - target.getLevel());
  }

  public double calcActivateRate(double mAtk, double mDef, L2Character target, boolean alt) {
    mDef = Math.min(mDef, Config.MAX_MDEF_CALC);
    if (alt) {
      mAtk = Math.min(mAtk, Config.MAX_MATK_CALC);
      return calcAltActivateRate(mAtk, mDef, target, 4.0D);
    }
    mAtk = Math.min(mAtk / 2.0D, Config.MAX_MATK_CALC);

    double mAtkModifier = mAtk / mDef;

    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[_baseSkillType.ordinal()]) {
    case 4:
    case 8:
      if (isMagic()) {
        return _baseLandRate / target.calcMENModifier() * target.calcSkillVulnerability(this) * (_magicLevel / target.getLevel()) * mAtkModifier;
      }
      return _baseLandRate / target.calcCONModifier() * target.calcSkillResistans(this, _baseSkillType, _skillType) * (_magicLevel / target.getLevel());
    case 13:
      return _baseLandRate * target.calcMENModifier() * target.calcSkillResistans(this, _baseSkillType, _skillType) * (_magicLevel / target.getLevel()) * mAtkModifier;
    case 10:
    case 15:
    case 20:
      return _baseLandRate * target.calcMENModifier() * target.calcSkillResistans(this, _baseSkillType, _skillType) * (_magicLevel / target.getLevel()) * mAtkModifier;
    case 11:
      if (isMagicalSlow())
      {
        return _baseLandRate / target.calcMENModifier() * target.calcStat(Stats.EARTH_VULN, 1.0D, target, this) * (_magicLevel / target.getLevel()) * mAtkModifier;
      }if ((isPhysicalSlow()) || (isMiscDebuffPhys()))
      {
        return _baseLandRate / target.calcMENModifier() * caclAttr(target) * (_magicLevel / target.getLevel());
      }
      return _baseLandRate / target.calcMENModifier() * target.calcSkillResistans(this, _baseSkillType, _skillType) * (_magicLevel / target.getLevel()) * mAtkModifier;
    case 5:
    case 6:
    case 7:
    case 9:
    case 12:
    case 14:
    case 16:
    case 17:
    case 18:
    case 19: } return Math.min(_baseLandRate, 60.0D);
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
    ERASE, 

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

    COMMON_CRAFT, 
    DWARVEN_CRAFT, 
    CREATE_ITEM(L2SkillCreateItem.class), 
    SUMMON_TREASURE_KEY, 

    SUMMON(L2SkillSummon.class), 
    FEED_PET, 
    DEATHLINK_PET, 
    STRSIEGEASSAULT, 
    BLUFF, 
    BETRAY, 

    CANCEL, 
    MAGE_BANE, 
    WARRIOR_BANE, 
    NEGATE, 
    BUFF, 
    DEBUFF, 
    PASSIVE, 
    CONT, 
    RESURRECT, 
    CHARGE(L2SkillCharge.class), 
    CHARGE_EFFECT(L2SkillChargeEffect.class), 
    CHARGEDAM(L2SkillChargeDmg.class), 
    MHOT, 
    DETECT_WEAKNESS, 
    LUCK, 
    RECALL, 
    WEDDINGTP, 
    SUMMON_FRIEND, 
    CLAN_GATE, 
    GATE_CHANT, 
    ZAKENTPPLAYER, 
    ZAKENTPSELF, 
    CUSTOM_TELEPORT, 
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
    SUMMON_NPC(L2SkillSummonNpc.class), 
    HOLD_UNDEAD, 
    TURN_UNDEAD, 

    NOTDONE;

    private final Class<? extends L2Skill> _class;

    public L2Skill makeSkill(StatsSet set) { try { Constructor c = _class.getConstructor(new Class[] { StatsSet.class });

        return (L2Skill)c.newInstance(new Object[] { set }); } catch (Exception e) {
      }
      throw new RuntimeException(e);
    }

    private SkillType()
    {
      _class = L2SkillDefault.class;
    }

    private SkillType(Class<? extends L2Skill> classType) {
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
    TARGET_MULTIFACE, 
    TARGET_CORPSE_ALLY, 
    TARGET_CORPSE_CLAN, 
    TARGET_CORPSE_PLAYER, 
    TARGET_CORPSE_PET, 
    TARGET_ITEM, 
    TARGET_MOB, 
    TARGET_AREA_CORPSE_MOB, 
    TARGET_CORPSE_MOB, 
    TARGET_UNLOCKABLE, 
    TARGET_HOLY, 
    TARGET_PARTY_MEMBER, 
    TARGET_PARTY_OTHER, 
    TARGET_ENEMY_SUMMON, 
    TARGET_OWNER_PET, 
    TARGET_SIGNET_GROUND, 
    TARGET_SIGNET, 
    TARGET_GROUND, 
    TARGET_TYRANNOSAURUS;
  }

  public static enum SkillOpType
  {
    OP_PASSIVE, OP_ACTIVE, OP_TOGGLE, OP_CHANCE;
  }
}