package l2rt.gameserver.skills;

import l2rt.Config;
import l2rt.config.ConfigSystem;
import l2rt.gameserver.skills.inits.InitConst;
import l2rt.gameserver.skills.inits.InitFunc;
import l2rt.gameserver.skills.inits.Init_rShld;

import java.util.NoSuchElementException;

public enum Stats
{
	// Внимание, для значений из конфига использовать Integer а не int! Это нужно для изменения значений на лету.
	MAX_HP("maxHp", 1, ConfigSystem.getInt("MaxHP"), true),
	MAX_MP("maxMp", 1, ConfigSystem.getInt("MaxMP"), true),
	MAX_CP("maxCp", 1, ConfigSystem.getInt("MaxCP"), true),

	// Для эффектов типа Seal of Limit
	HP_LIMIT("hpLimit", 1, 100, false, new InitConst(100.)),
	MP_LIMIT("mpLimit", 1, 100, false, new InitConst(100.)),
	CP_LIMIT("cpLimit", 1, 100, false, new InitConst(100.)),

	REGENERATE_HP_RATE("regHp", null, null, false),
	REGENERATE_CP_RATE("regCp", null, null, false),
	REGENERATE_MP_RATE("regMp", null, null, false),

	RUN_SPEED("runSpd", 0, ConfigSystem.getInt("LimitMove"), true),

	POWER_DEFENCE("pDef", 0, ConfigSystem.getInt("LimitPDef"), true),
	MAGIC_DEFENCE("mDef", 0, ConfigSystem.getInt("LimitMDef"), true),
	POWER_ATTACK("pAtk", 0, ConfigSystem.getInt("LimitPatk"), true),
	MAGIC_ATTACK("mAtk", 0, ConfigSystem.getInt("LimitMAtk"), true),
	POWER_ATTACK_SPEED("pAtkSpd", 0, ConfigSystem.getInt("LimitPatkSpd"), false),
	MAGIC_ATTACK_SPEED("mAtkSpd", 0, ConfigSystem.getInt("LimitMatkSpd"), false),

	MAGIC_REUSE_RATE("mReuse", null, null, false),
	PHYSIC_REUSE_RATE("pReuse", null, null, false),
	ATK_REUSE("atkReuse", null, null, false),
	ATK_BASE("atkBaseSpeed", null, null, false),

	CRITICAL_DAMAGE("cAtk", 0, ConfigSystem.getInt("LimitCriticalDamage") / 2, false, new InitConst(100)),
	CRITICAL_DAMAGE_STATIC("cAtkStatic", null, null, false, new InitConst(0)),
	EVASION_RATE("rEvas", 0, ConfigSystem.getInt("LimitEvasion"), false),
	MEVASION_RATE("mrEvas", 0, ConfigSystem.getInt("LimitEvasion"), false),
	ACCURACY_COMBAT("accCombat", 0, ConfigSystem.getInt("LimitAccuracy"), false),
	MACCURACY_COMBAT("maccCombat", 0, ConfigSystem.getInt("LimitAccuracy"), false),
	CRITICAL_BASE("baseCrit", 0, ConfigSystem.getInt("LimitCritical"), false, new InitConst(100)),
	CRITICAL_RATE("rCrit", 0, null, false, new InitConst(100)),
	MCRITICAL_RATE("mCritRate", 0, ConfigSystem.getInt("LimitMCritical"), false, new InitConst(10.)),
	MCRITICAL_DAMAGE("mCritDamage", 0, 10, false, new InitConst(2.5)),

	PHYSICAL_DAMAGE("physDamage", null, null, false, null),
	MAGIC_DAMAGE("magicDamage", null, null, false, null),

	CAST_INTERRUPT("concentration", 0, 100, false, null),

	SHIELD_DEFENCE("sDef", null, null, false),
	SHIELD_RATE("rShld", 0, 90, false, new Init_rShld()),
	SHIELD_ANGLE("shldAngle", null, null, false, new InitConst(60)),

	POWER_ATTACK_RANGE("pAtkRange", 0, 1500, false),
	MAGIC_ATTACK_RANGE("mAtkRange", 0, 1500, false),
	POLE_ATTACK_ANGLE("poleAngle", 0, 180, false),
	POLE_TARGERT_COUNT("poleTargetCount", null, null, false),

	STAT_STR("STR", 1, 120, false),
	STAT_CON("CON", 1, 120, false),
	STAT_DEX("DEX", 1, 120, false),
	STAT_INT("INT", 1, 120, false),
	STAT_WIT("WIT", 1, 120, false),
	STAT_MEN("MEN", 1, 120, false),

	BREATH("breath", null, null, false),
	FALL("fall", null, null, false),
	EXP_LOST("expLost", null, null, false),

	BLEED_RECEPTIVE("bleedRcpt", -200, 300, false, new InitConst(0)),
	POISON_RECEPTIVE("poisonRcpt", -200, 300, false, new InitConst(0)),
	STUN_RECEPTIVE("stunRcpt", -200, 300, false, new InitConst(0)),
	ROOT_RECEPTIVE("rootRcpt", -200, 300, false, new InitConst(0)),
	MENTAL_RECEPTIVE("mentalRcpt", -200, 300, false, new InitConst(0)),
	SLEEP_RECEPTIVE("sleepRcpt", -200, 300, false, new InitConst(0)),
	PARALYZE_RECEPTIVE("paralyzeRcpt", -200, 300, false, new InitConst(0)),
	CANCEL_RECEPTIVE("cancelRcpt", -200, 300, false, new InitConst(0)),
	DEBUFF_RECEPTIVE("debuffRcpt", -200, 300, false, new InitConst(0)),
	MAGIC_RECEPTIVE("magicRcpt", -200, 300, false, new InitConst(0)),

	BLEED_POWER("bleedPower", -200, 200, false, new InitConst(0)),
	POISON_POWER("poisonPower", -200, 200, false, new InitConst(0)),
	STUN_POWER("stunPower", -200, 200, false, new InitConst(0)),
	ROOT_POWER("rootPower", -200, 200, false, new InitConst(0)),
	MENTAL_POWER("mentalPower", -200, 200, false, new InitConst(0)),
	SLEEP_POWER("sleepPower", -200, 200, false, new InitConst(0)),
	PARALYZE_POWER("paralyzePower", -200, 200, false, new InitConst(0)),
	CANCEL_POWER("cancelPower", -200, 200, false, new InitConst(0)),
	DEBUFF_POWER("debuffPower", -200, 200, false, new InitConst(0)),
	MAGIC_POWER("magicPower", -200, 200, false, new InitConst(0)),

	SKILL_CRIT_CHANCE_MOD("SkillCritChanceMod", 10, 190, false, new InitConst(100)),
	FATALBLOW_RATE("blowRate", 0, 10, false, new InitConst(1.)),
	DEATH_RECEPTIVE("deathRcpt", 10, 190, false, new InitConst(100)),

	FIRE_RECEPTIVE("fireRcpt", null, null, false),
	WIND_RECEPTIVE("windRcpt", null, null, false),
	WATER_RECEPTIVE("waterRcpt", null, null, false),
	EARTH_RECEPTIVE("earthRcpt", null, null, false),
	UNHOLY_RECEPTIVE("unholyRcpt", null, null, false),
	SACRED_RECEPTIVE("sacredRcpt", null, null, false),

	CRIT_DAMAGE_RECEPTIVE("critDamRcpt", 50, 200, false, new InitConst(100)),
	CRIT_CHANCE_RECEPTIVE("critChanceRcpt", 10, 190, false, new InitConst(100)),

	ATTACK_ELEMENT_FIRE("attackFire", 0, 500, false),
	ATTACK_ELEMENT_WATER("attackWater", 0, 500, false),
	ATTACK_ELEMENT_WIND("attackWind", 0, 500, false),
	ATTACK_ELEMENT_EARTH("attackEarth", 0, 500, false),
	ATTACK_ELEMENT_SACRED("attackSacred", 0, 500, false),
	ATTACK_ELEMENT_UNHOLY("attackUnholy", 0, 500, false),

	SWORD_WPN_RECEPTIVE("swordWpnRcpt", 10, 200, false, new InitConst(100)),
	DUAL_WPN_RECEPTIVE("dualWpnRcpt", 10, 200, false, new InitConst(100)),
	BLUNT_WPN_RECEPTIVE("bluntWpnRcpt", 10, 200, false, new InitConst(100)),
	DAGGER_WPN_RECEPTIVE("daggerWpnRcpt", 10, 200, false, new InitConst(100)),
	BOW_WPN_RECEPTIVE("bowWpnRcpt", 10, 200, false, new InitConst(100)),
	CROSSBOW_WPN_RECEPTIVE("crossbowWpnRcpt", 10, 200, false, new InitConst(100)),
	POLE_WPN_RECEPTIVE("poleWpnRcpt", 10, 200, false, new InitConst(100)),
	FIST_WPN_RECEPTIVE("fistWpnRcpt", 10, 200, false, new InitConst(100)),

	ABSORB_DAMAGE_PERCENT("absorbDam", 0, 100, false),
	ABSORB_DAMAGEMP_PERCENT("absorbDamMp", 0, 100, false),

	TRANSFER_PET_DAMAGE_PERCENT("transferPetDam", 0, 100, false),

	// Отражение урона с шансом. Урон получает только атакующий.
	REFLECT_AND_BLOCK_DAMAGE_CHANCE("reflectAndBlockDam", 0, 100, false), // Ближний урон без скиллов
	REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE("reflectAndBlockPSkillDam", 0, 100, false), // Ближний урон скиллами
	REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE("reflectAndBlockMSkillDam", 0, 100, false), // Любой урон магией

	// Отражение урона в процентах. Урон получает и атакующий и цель
	REFLECT_DAMAGE_PERCENT("reflectDam", 0, 100, false), // Ближний урон без скиллов
	REFLECT_PSKILL_DAMAGE_PERCENT("reflectPSkillDam", 0, 100, false), // Ближний урон скиллами
	REFLECT_MSKILL_DAMAGE_PERCENT("reflectMSkillDam", 0, 100, false), // Любой урон магией
	REFLECT_DAMAGE_DAMAGE_PERCENT("reflectSkillDam", 0, 100, false), // урон скилами и дальние удары

	REFLECT_PHYSIC_SKILL("reflectPhysicSkill", 0, 100, false),
	REFLECT_MAGIC_SKILL("reflectMagicSkill", 0, 100, false),

	REFLECT_PHYSIC_DEBUFF("reflectPhysicDebuff", 0, 100, false),
	REFLECT_MAGIC_DEBUFF("reflectMagicDebuff", 0, 100, false),

	PSKILL_EVASION("pSkillEvasion", 0, 100, false),
	MSKILL_EVASION("mSkillEvasion", 0, 100, false),

	COUNTER_ATTACK("counterAttack", 0, 100, false),

	CANCEL_TARGET("cancelTarget", 0, 100, false),

	HEAL_EFFECTIVNESS("hpEff", 0, 1000, false),
	MANAHEAL_EFFECTIVNESS("mpEff", 0, null, false),
	CPHEAL_EFFECTIVNESS("cpEff", 0, 1000, false),
	HEAL_POWER("healPower", null, null, false),
	MP_MAGIC_SKILL_CONSUME("mpConsum", null, null, false),
	MP_PHYSICAL_SKILL_CONSUME("mpConsumePhysical", null, null, false),
	MP_DANCE_SKILL_CONSUME("mpDanceConsume", null, null, false),
	MP_USE_BOW("cheapShot", null, null, false),
	MP_USE_BOW_CHANCE("cheapShotChance", null, null, false),
	SS_USE_BOW("miser", null, null, false),
	SS_USE_BOW_CHANCE("miserChance", null, null, false),
	ACTIVATE_RATE("activateRate", null, null, false),
	SKILL_MASTERY("skillMastery", 0, null, false),

	MAX_LOAD("maxLoad", null, null, false),
	MAX_NO_PENALTY_LOAD("maxNoPenaltyLoad", null, null, false),
	INVENTORY_LIMIT("inventoryLimit", null, Config.SERVICES_EXPAND_INVENTORY_MAX, false),
	STORAGE_LIMIT("storageLimit", null, null, false),
	TRADE_LIMIT("tradeLimit", null, null, false),
	COMMON_RECIPE_LIMIT("CommonRecipeLimit", null, null, false),
	DWARVEN_RECIPE_LIMIT("DwarvenRecipeLimit", null, null, false),
	BUFF_LIMIT("buffLimit", null, null, false),
	SONG_LIMIT("songLimit", null, null, false),
	SOULS_LIMIT("soulsLimit", null, null, false),
	SOULS_CONSUME_EXP("soulsExp", null, null, false),
	TALISMANS_LIMIT("talismansLimit", 0, 6, false),
	
	PVE_PHYSICAL_DMG("pvePhysDmg", null, null, false, null), //
	PVE_PHYS_SKILL_DMG("pvePhysSkillsDmg", null, null, false, null),
	PVE_BOW_DMG("pveBowDmg", null, null, false, null),
	PVE_BOW_SKILL_DMG("pveBowSkillsDmg", null, null, false, null),
	PVE_MAGICAL_DMG("pveMagicalDmg", null, null, false, null),

	GRADE_EXPERTISE_LEVEL("gradeExpertiseLevel", null, null, false),
	EXP("ExpMultiplier", 0, null, false),
	SP("SpMultiplier", 0, null, false),
	DROP("DropMultiplier", null, null, false);

	public static final int NUM_STATS = values().length;

	private String _value;
	public final Integer _min;
	public final Integer _max;
	private boolean _limitOnlyPlayable;
	private InitFunc _init;

	public String getValue()
	{
		return _value;
	}

	public boolean isLimitOnlyPlayable()
	{
		return _limitOnlyPlayable;
	}

	public InitFunc getInit()
	{
		return _init;
	}

	private Stats(String s, Integer min, Integer max, boolean limitOnlyPlayable)
	{
		_value = s;
		_min = min;
		_max = max;
		_limitOnlyPlayable = limitOnlyPlayable;
		_init = null;
	}

	private Stats(String s, Integer min, Integer max, boolean limitOnlyPlayable, InitFunc init)
	{
		_value = s;
		_min = min;
		_max = max;
		_limitOnlyPlayable = limitOnlyPlayable;
		_init = init;
	}

	public static Stats valueOfXml(String name)
	{
		for(Stats s : values())
			if(s.getValue().equals(name))
				return s;

		throw new NoSuchElementException("Unknown name '" + name + "' for enum BaseStats");
	}

	@Override
	public String toString()
	{
		return _value;
	}
}