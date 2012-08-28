//L2DDT
package net.sf.l2j.gameserver.skills;

import java.util.NoSuchElementException;

public enum Stats
{
	//
	// Base stats, for each in Calculator a slot is allocated
	//

	// HP & MP
	MAX_HP 				("maxHp"),
	MAX_MP 				("maxMp"),
    MAX_CP 				("maxCp"),
	REGENERATE_HP_RATE 	("regHp"),
    REGENERATE_CP_RATE	("regCp"),
	REGENERATE_MP_RATE	("regMp"),
    RECHARGE_MP_RATE	("gainMp"),
	HEAL_EFFECTIVNESS 	("gainHp"),

	// Atk & Def
	POWER_DEFENCE 		("pDef"),
	MAGIC_DEFENCE		("mDef"),
	POWER_ATTACK		("pAtk"),
	MAGIC_ATTACK 		("mAtk"),
	POWER_ATTACK_SPEED 	("pAtkSpd"),
	MAGIC_ATTACK_SPEED 	("mAtkSpd"), // how fast a spell is casted (including animation)
	MAGIC_REUSE_RATE 	("mReuse"), // how fast spells becomes ready to reuse
	P_REUSE             ("pReuse"),
	SHIELD_DEFENCE 		("sDef"),
	CRITICAL_DAMAGE 	("cAtk"),
	PVP_PHYSICAL_DMG    ("pvpPhysDmg"),
	PVP_MAGICAL_DMG     ("pvpMagicalDmg"),
	PVP_PHYS_SKILL_DMG  ("pvpPhysSkillsDmg"),

	REFLECT_DAMAGE_PHYSIC    ("DDT_PhysDmgReflect"),
	REFLECT_DAMAGE_MAGIC    ("DDT_MagicDmgReflect"),

	REFLECT_REVENGE_SKILL    ("DDT_ShieldRevenge"),

	// Atk & Def rates
	EVASION_RATE 		("rEvas"),
	P_SKILL_EVASION     ("pSkillEvas"),
	REFLECT_SKILL_MELEE_PHYSIC ("reflectSkillMeleePhysic"),
	SHIELD_RATE 		("rShld"),
	CRITICAL_RATE 		("rCrit" ),
	BLOW_RATE 			("blowRate" ),
	LETHAL_RATE 		("lethalRate" ),
	MCRITICAL_RATE 		("mCritRate"),
    EXPSP_RATE 			("rExp"),
	ATTACK_CANCEL		("cancel"),
	CANCEL_ATTACKER_TARGET ("cancelAttackerTarget"),

	// Accuracy and range
	ACCURACY_COMBAT 	("accCombat"),
	POWER_ATTACK_RANGE 	("pAtkRange"),
	MAGIC_ATTACK_RANGE 	("mAtkRange"),
    POWER_ATTACK_ANGLE 	("pAtkAngle"),
    ATTACK_COUNT_MAX    ("atkCountMax"),
	// Run speed,
	// walk & escape speed are calculated proportionally,
	// magic speed is a buff
	RUN_SPEED 			("runSpd"),
	WALK_SPEED			("walkSpd"),

	//
	// Player-only stats
	//
	STAT_STR	("STR"),
	STAT_CON 	("CON"),
	STAT_DEX 	("DEX"),
	STAT_INT 	("INT"),
	STAT_WIT 	("WIT"),
	STAT_MEN 	("MEN"),

	//
	// Special stats, share one slot in Calculator
	//

	// stats of various abilities
	BREATH 		("breath"),
	//
	AGGRESSION 	("aggression"), // locks a mob on tank caster
	BLEED 		("bleed"), // by daggers, like poison
	POISON 		("poison"), // by magic, hp dmg over time
	STUN 		("stun"), // disable move/ATTACK for a period of time
	ROOT 		("root"), // disable movement, but not ATTACK
	MOVEMENT 	("movement"), // slowdown movement, debuff
	CONFUSION 	("confusion"), // mob changes target, opposite to aggression/hate
	SLEEP 		("sleep"), // sleep (don't move/ATTACK) until attacked
	FIRE 		("fire"),
	WIND 		("wind"),
	WATER 		("water"),
	EARTH 		("earth"),
	HOLY 		("holy"),
	DARK 		("dark"),
	//
	AGGRESSION_VULN ("aggressionVuln"),
	BLEED_VULN 		("bleedVuln"),
	POISON_VULN		("poisonVuln"),
	STUN_VULN 		("stunVuln"),
	PARALYZE_VULN 	("paralyzeVuln"),
	ROOT_VULN 		("rootVuln"),
	SLEEP_VULN 		("sleepVuln"),
	CONFUSION_VULN 	("confusionVuln"),
	MOVEMENT_VULN 	("movementVuln"),
	FIRE_VULN 		("fireVuln"),
	WIND_VULN		("windVuln"),
	WATER_VULN 		("waterVuln"),
	EARTH_VULN 		("earthVuln"),
	HOLY_VULN 		("holyVuln"),
	DARK_VULN		("darkVuln"),
	CANCEL_VULN     ("cancelVuln"), // Resistance for cancel type skills
	DERANGEMENT_VULN("derangementVuln"),
	DEBUFF_VULN		("debuffVuln"),
	FALL_VULN		("fallVuln"), //Dobavil dlia skillov tipa Acrobatics(augument)

	NONE_WPN_VULN 	("noneWpnVuln"), // Shields!!!
	SWORD_WPN_VULN 	("swordWpnVuln"),
	BLUNT_WPN_VULN 	("bluntWpnVuln"),
	DAGGER_WPN_VULN	("daggerWpnVuln"),
	BOW_WPN_VULN 	("bowWpnVuln"),
	POLE_WPN_VULN 	("poleWpnVuln"),
	ETC_WPN_VULN 	("etcWpnVuln"),
	FIST_WPN_VULN 	("fistWpnVuln"),
	DUAL_WPN_VULN 	("dualWpnVuln"),
	DUALFIST_WPN_VULN("dualFistWpnVuln"),

	REFLECT_DAMAGE_PERCENT 	("reflectDam"),
	REFLECT_SKILL_MAGIC     ("reflectSkillMagic"),
	REFLECT_SKILL_PHYSIC    ("reflectSkillPhysic"),
	REFLECT_MAGIC_DEBUFF("reflectMagicDebuff"), 
	REFLECT_PHYSIC_DEBUFF("reflectPhysicDebuff"), 
	ABSORB_DAMAGE_PERCENT 	("absorbDam"),
    TRANSFER_DAMAGE_PERCENT ("transDam"),

	MAX_LOAD 		("maxLoad"),

	PATK_PLANTS 	("pAtk-plants"),
	PATK_INSECTS	("pAtk-insects"),
	PATK_ANIMALS 	("pAtk-animals"),
	PATK_MONSTERS 	("pAtk-monsters"),
	PATK_DRAGONS 	("pAtk-dragons"),
	PATK_UNDEAD 	("pAtk-undead"),

	PDEF_PLANTS 	("pDef-plants"),
	PDEF_INSECTS	("pDef-insects"),
	PDEF_ANIMALS 	("pDef-animals"),
	PDEF_MONSTERS 	("pDef-monsters"),
	PDEF_DRAGONS 	("pDef-dragons"),
	PDEF_UNDEAD 	("pDef-undead"),
	PDEF_GIANTS    ("pDef-giants"),


	ATK_REUSE 		("atkReuse"),

	//ExSkill :)
	INV_LIM 		("inventoryLimit"),
	WH_LIM 			("whLimit"),
	FREIGHT_LIM 	("FreightLimit"),
	P_SELL_LIM 		("PrivateSellLimit"),
	P_BUY_LIM 		("PrivateBuyLimit"),
	REC_D_LIM 		("DwarfRecipeLimit"),
	REC_C_LIM 		("CommonRecipeLimit"),

    //C4 Stats
 	PHYSICAL_MP_CONSUME_RATE  ("PhysicalMpConsumeRate"), 
	MAGICAL_MP_CONSUME_RATE   ("MagicalMpConsumeRate"),
	DANCE_MP_CONSUME_RATE     ("DanceMpConsumeRate"),
    HP_CONSUME_RATE		("HpConsumeRate"),
    MP_CONSUME 			("MpConsume"),
    SOULSHOT_COUNT 		("soulShotCount"),
	SHIELD_DEFENCE_ANGLE	("shieldDefAngle"),
	SKILL_MASTERY			("skillMastery");

	
	public static final int NUM_STATS = values().length;

	private String _value;

	public String getValue()
	{
		return _value;
	}
	private Stats(String s)
	{
		_value = s;
	}
	public static Stats valueOfXml(String name)
	{
		name = name.intern();
		for(Stats s: values())
		{
			if(s.getValue().equals(name))
				return s;
		}

		throw new NoSuchElementException("Unknown name '"+name+"' for enum BaseStats");
	}
}
