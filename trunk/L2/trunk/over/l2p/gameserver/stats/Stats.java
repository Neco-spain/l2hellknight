package l2p.gameserver.stats;

import java.util.NoSuchElementException;

public enum Stats
{
  MAX_HP("maxHp", 0.0D, (1.0D / 0.0D), 1.0D), 
  MAX_MP("maxMp", 0.0D, (1.0D / 0.0D), 1.0D), 
  MAX_CP("maxCp", 0.0D, (1.0D / 0.0D), 1.0D), 

  REGENERATE_HP_RATE("regHp"), 
  REGENERATE_CP_RATE("regCp"), 
  REGENERATE_MP_RATE("regMp"), 

  HP_LIMIT("hpLimit", 1.0D, 100.0D, 100.0D), 
  MP_LIMIT("mpLimit", 1.0D, 100.0D, 100.0D), 
  CP_LIMIT("cpLimit", 1.0D, 100.0D, 100.0D), 

  RUN_SPEED("runSpd"), 

  POWER_DEFENCE("pDef"), 
  MAGIC_DEFENCE("mDef"), 
  POWER_ATTACK("pAtk"), 
  MAGIC_ATTACK("mAtk"), 
  POWER_ATTACK_SPEED("pAtkSpd"), 
  MAGIC_ATTACK_SPEED("mAtkSpd"), 

  MAGIC_REUSE_RATE("mReuse"), 
  PHYSIC_REUSE_RATE("pReuse"), 
  MUSIC_REUSE_RATE("musicReuse"), 
  ATK_REUSE("atkReuse"), 
  ATK_BASE("atkBaseSpeed"), 

  CRITICAL_DAMAGE("cAtk", 0.0D, (1.0D / 0.0D), 100.0D), 
  CRITICAL_DAMAGE_STATIC("cAtkStatic"), 
  EVASION_RATE("rEvas"), 
  ACCURACY_COMBAT("accCombat"), 
  CRITICAL_BASE("baseCrit", 0.0D, (1.0D / 0.0D), 100.0D), 
  CRITICAL_RATE("rCrit", 0.0D, (1.0D / 0.0D), 100.0D), 
  MCRITICAL_RATE("mCritRate", 0.0D, (1.0D / 0.0D), 10.0D), 
  MCRITICAL_DAMAGE("mCritDamage", 0.0D, 10.0D, 2.5D), 

  PHYSICAL_DAMAGE("physDamage"), 
  MAGIC_DAMAGE("magicDamage"), 

  CAST_INTERRUPT("concentration", 0.0D, 100.0D), 
  SHIELD_DEFENCE("sDef"), 
  SHIELD_RATE("rShld", 0.0D, 90.0D), 
  SHIELD_ANGLE("shldAngle", 0.0D, 180.0D, 60.0D), 

  POWER_ATTACK_RANGE("pAtkRange", 0.0D, 1500.0D), 
  MAGIC_ATTACK_RANGE("mAtkRange", 0.0D, 1500.0D), 
  POLE_ATTACK_ANGLE("poleAngle", 0.0D, 180.0D), 
  POLE_TARGET_COUNT("poleTargetCount"), 

  STAT_STR("STR", 1.0D, 99.0D), 
  STAT_CON("CON", 1.0D, 99.0D), 
  STAT_DEX("DEX", 1.0D, 99.0D), 
  STAT_INT("INT", 1.0D, 99.0D), 
  STAT_WIT("WIT", 1.0D, 99.0D), 
  STAT_MEN("MEN", 1.0D, 99.0D), 

  BREATH("breath"), 
  FALL("fall"), 
  EXP_LOST("expLost"), 

  BLEED_RESIST("bleedResist", (-1.0D / 0.0D), (1.0D / 0.0D)), 
  POISON_RESIST("poisonResist", (-1.0D / 0.0D), (1.0D / 0.0D)), 
  STUN_RESIST("stunResist", (-1.0D / 0.0D), (1.0D / 0.0D)), 
  ROOT_RESIST("rootResist", (-1.0D / 0.0D), (1.0D / 0.0D)), 
  MENTAL_RESIST("mentalResist", (-1.0D / 0.0D), (1.0D / 0.0D)), 
  SLEEP_RESIST("sleepResist", (-1.0D / 0.0D), (1.0D / 0.0D)), 
  PARALYZE_RESIST("paralyzeResist", (-1.0D / 0.0D), (1.0D / 0.0D)), 
  CANCEL_RESIST("cancelResist", -200.0D, 300.0D), 
  DEBUFF_RESIST("debuffResist", (-1.0D / 0.0D), (1.0D / 0.0D)), 
  MAGIC_RESIST("magicResist", -200.0D, 300.0D), 

  BLEED_POWER("bleedPower", -200.0D, 200.0D), 
  POISON_POWER("poisonPower", -200.0D, 200.0D), 
  STUN_POWER("stunPower", -200.0D, 200.0D), 
  ROOT_POWER("rootPower", -200.0D, 200.0D), 
  MENTAL_POWER("mentalPower", -200.0D, 200.0D), 
  SLEEP_POWER("sleepPower", -200.0D, 200.0D), 
  PARALYZE_POWER("paralyzePower", -200.0D, 200.0D), 
  CANCEL_POWER("cancelPower", -200.0D, 200.0D), 
  DEBUFF_POWER("debuffPower", -200.0D, 200.0D), 
  MAGIC_POWER("magicPower", -200.0D, 200.0D), 

  FATALBLOW_RATE("blowRate", 0.0D, 10.0D, 1.0D), 
  SKILL_CRIT_CHANCE_MOD("SkillCritChanceMod", 10.0D, 190.0D, 100.0D), 
  DEATH_VULNERABILITY("deathVuln", 10.0D, 190.0D, 100.0D), 

  CRIT_DAMAGE_RECEPTIVE("critDamRcpt", (-1.0D / 0.0D), (1.0D / 0.0D)), 
  CRIT_CHANCE_RECEPTIVE("critChanceRcpt", 10.0D, 190.0D, 100.0D), 

  DEFENCE_FIRE("defenceFire", (-1.0D / 0.0D), (1.0D / 0.0D)), 
  DEFENCE_WATER("defenceWater", (-1.0D / 0.0D), (1.0D / 0.0D)), 
  DEFENCE_WIND("defenceWind", (-1.0D / 0.0D), (1.0D / 0.0D)), 
  DEFENCE_EARTH("defenceEarth", (-1.0D / 0.0D), (1.0D / 0.0D)), 
  DEFENCE_HOLY("defenceHoly", (-1.0D / 0.0D), (1.0D / 0.0D)), 
  DEFENCE_UNHOLY("defenceUnholy", (-1.0D / 0.0D), (1.0D / 0.0D)), 

  ATTACK_FIRE("attackFire", 0.0D, (1.0D / 0.0D)), 
  ATTACK_WATER("attackWater", 0.0D, (1.0D / 0.0D)), 
  ATTACK_WIND("attackWind", 0.0D, (1.0D / 0.0D)), 
  ATTACK_EARTH("attackEarth", 0.0D, (1.0D / 0.0D)), 
  ATTACK_HOLY("attackHoly", 0.0D, (1.0D / 0.0D)), 
  ATTACK_UNHOLY("attackUnholy", 0.0D, (1.0D / 0.0D)), 

  SWORD_WPN_VULNERABILITY("swordWpnVuln", 10.0D, 200.0D, 100.0D), 
  DUAL_WPN_VULNERABILITY("dualWpnVuln", 10.0D, 200.0D, 100.0D), 
  BLUNT_WPN_VULNERABILITY("bluntWpnVuln", 10.0D, 200.0D, 100.0D), 
  DAGGER_WPN_VULNERABILITY("daggerWpnVuln", 10.0D, 200.0D, 100.0D), 
  BOW_WPN_VULNERABILITY("bowWpnVuln", 10.0D, 200.0D, 100.0D), 
  CROSSBOW_WPN_VULNERABILITY("crossbowWpnVuln", 10.0D, 200.0D, 100.0D), 
  POLE_WPN_VULNERABILITY("poleWpnVuln", 10.0D, 200.0D, 100.0D), 
  FIST_WPN_VULNERABILITY("fistWpnVuln", 10.0D, 200.0D, 100.0D), 

  ABSORB_DAMAGE_PERCENT("absorbDam", 0.0D, 100.0D), 
  ABSORB_DAMAGEMP_PERCENT("absorbDamMp", 0.0D, 100.0D), 

  TRANSFER_TO_SUMMON_DAMAGE_PERCENT("transferPetDam", 0.0D, 100.0D), 
  TRANSFER_TO_EFFECTOR_DAMAGE_PERCENT("transferToEffectorDam", 0.0D, 100.0D), 
  TRANSFER_TO_MP_DAMAGE_PERCENT("transferToMpDam", 0.0D, 100.0D), 

  REFLECT_AND_BLOCK_DAMAGE_CHANCE("reflectAndBlockDam", 0.0D, 100.0D), 
  REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE("reflectAndBlockPSkillDam", 0.0D, 100.0D), 
  REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE("reflectAndBlockMSkillDam", 0.0D, 100.0D), 

  REFLECT_DAMAGE_PERCENT("reflectDam", 0.0D, 100.0D), 
  REFLECT_PSKILL_DAMAGE_PERCENT("reflectPSkillDam", 0.0D, 100.0D), 
  REFLECT_MSKILL_DAMAGE_PERCENT("reflectMSkillDam", 0.0D, 100.0D), 

  REFLECT_PHYSIC_SKILL("reflectPhysicSkill", 0.0D, 100.0D), 
  REFLECT_MAGIC_SKILL("reflectMagicSkill", 0.0D, 100.0D), 

  REFLECT_PHYSIC_DEBUFF("reflectPhysicDebuff", 0.0D, 100.0D), 
  REFLECT_MAGIC_DEBUFF("reflectMagicDebuff", 0.0D, 100.0D), 

  PSKILL_EVASION("pSkillEvasion", 0.0D, 100.0D), 
  COUNTER_ATTACK("counterAttack", 0.0D, 100.0D), 

  SKILL_POWER("skillPower"), 
  PVP_PHYS_DMG_BONUS("pvpPhysDmgBonus"), 
  PVP_PHYS_SKILL_DMG_BONUS("pvpPhysSkillDmgBonus"), 
  PVP_MAGIC_SKILL_DMG_BONUS("pvpMagicSkillDmgBonus"), 

  PVP_PHYS_DEFENCE_BONUS("pvpPhysDefenceBonus"), 
  PVP_PHYS_SKILL_DEFENCE_BONUS("pvpPhysSkillDefenceBonus"), 
  PVP_MAGIC_SKILL_DEFENCE_BONUS("pvpMagicSkillDefenceBonus"), 

  HEAL_EFFECTIVNESS("hpEff", 0.0D, 1000.0D), 
  MANAHEAL_EFFECTIVNESS("mpEff", 0.0D, 1000.0D), 
  CPHEAL_EFFECTIVNESS("cpEff", 0.0D, 1000.0D), 
  HEAL_POWER("healPower"), 
  MP_MAGIC_SKILL_CONSUME("mpConsum"), 
  MP_PHYSICAL_SKILL_CONSUME("mpConsumePhysical"), 
  MP_DANCE_SKILL_CONSUME("mpDanceConsume"), 
  MP_USE_BOW("cheapShot"), 
  MP_USE_BOW_CHANCE("cheapShotChance"), 
  SS_USE_BOW("miser"), 
  SS_USE_BOW_CHANCE("miserChance"), 
  SKILL_MASTERY("skillMastery"), 

  MAX_LOAD("maxLoad"), 
  MAX_NO_PENALTY_LOAD("maxNoPenaltyLoad"), 
  INVENTORY_LIMIT("inventoryLimit"), 
  STORAGE_LIMIT("storageLimit"), 
  TRADE_LIMIT("tradeLimit"), 
  COMMON_RECIPE_LIMIT("CommonRecipeLimit"), 
  DWARVEN_RECIPE_LIMIT("DwarvenRecipeLimit"), 
  BUFF_LIMIT("buffLimit"), 
  SOULS_LIMIT("soulsLimit"), 
  SOULS_CONSUME_EXP("soulsExp"), 
  TALISMANS_LIMIT("talismansLimit", 0.0D, 6.0D), 
  CUBICS_LIMIT("cubicsLimit", 0.0D, 3.0D, 1.0D), 
  CLOAK_SLOT("openCloakSlot", 0.0D, 1.0D), 

  GRADE_EXPERTISE_LEVEL("gradeExpertiseLevel"), 
  EXP("ExpMultiplier"), 
  SP("SpMultiplier"), 
  REWARD_MULTIPLIER("DropMultiplier");

  public static final int NUM_STATS;
  private final String _value;
  private double _min;
  private double _max;
  private double _init;

  public String getValue() { return _value;
  }

  public double getInit()
  {
    return _init;
  }

  private Stats(String s)
  {
    this(s, 0.0D, (1.0D / 0.0D), 0.0D);
  }

  private Stats(String s, double min, double max)
  {
    this(s, min, max, 0.0D);
  }

  private Stats(String s, double min, double max, double init)
  {
    _value = s;
    _min = min;
    _max = max;
    _init = init;
  }

  public double validate(double val)
  {
    if (val < _min)
      return _min;
    if (val > _max)
      return _max;
    return val;
  }

  public static Stats valueOfXml(String name)
  {
    for (Stats s : values()) {
      if (s.getValue().equals(name))
        return s;
    }
    throw new NoSuchElementException("Unknown name '" + name + "' for enum BaseStats");
  }

  public String toString()
  {
    return _value;
  }

  static
  {
    NUM_STATS = values().length;
  }
}