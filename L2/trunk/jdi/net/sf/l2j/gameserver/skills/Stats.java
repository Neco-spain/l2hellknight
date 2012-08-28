package net.sf.l2j.gameserver.skills;

import java.util.NoSuchElementException;

public enum Stats
{
  MAX_HP("maxHp"), 
  MAX_MP("maxMp"), 
  MAX_CP("maxCp"), 
  REGENERATE_HP_RATE("regHp"), 
  REGENERATE_CP_RATE("regCp"), 
  REGENERATE_MP_RATE("regMp"), 
  RECHARGE_MP_RATE("gainMp"), 
  HEAL_EFFECTIVNESS("gainHp"), 

  POWER_DEFENCE("pDef"), 
  MAGIC_DEFENCE("mDef"), 
  POWER_ATTACK("pAtk"), 
  MAGIC_ATTACK("mAtk"), 
  POWER_ATTACK_SPEED("pAtkSpd"), 
  MAGIC_ATTACK_SPEED("mAtkSpd"), 
  MAGIC_REUSE_RATE("mReuse"), 
  P_REUSE("pReuse"), 
  SHIELD_DEFENCE("sDef"), 
  CRITICAL_DAMAGE("cAtk"), 
  PVP_PHYSICAL_DMG("pvpPhysDmg"), 
  PVP_MAGICAL_DMG("pvpMagicalDmg"), 
  PVP_PHYS_SKILL_DMG("pvpPhysSkillsDmg"), 

  REFLECT_DAMAGE_PHYSIC("DDT_PhysDmgReflect"), 
  REFLECT_DAMAGE_MAGIC("DDT_MagicDmgReflect"), 

  REFLECT_REVENGE_SKILL("DDT_ShieldRevenge"), 

  EVASION_RATE("rEvas"), 
  P_SKILL_EVASION("pSkillEvas"), 
  REFLECT_SKILL_MELEE_PHYSIC("reflectSkillMeleePhysic"), 
  SHIELD_RATE("rShld"), 
  CRITICAL_RATE("rCrit"), 
  BLOW_RATE("blowRate"), 
  LETHAL_RATE("lethalRate"), 
  MCRITICAL_RATE("mCritRate"), 
  EXPSP_RATE("rExp"), 
  ATTACK_CANCEL("cancel"), 
  CANCEL_ATTACKER_TARGET("cancelAttackerTarget"), 

  ACCURACY_COMBAT("accCombat"), 
  POWER_ATTACK_RANGE("pAtkRange"), 
  MAGIC_ATTACK_RANGE("mAtkRange"), 
  POWER_ATTACK_ANGLE("pAtkAngle"), 
  ATTACK_COUNT_MAX("atkCountMax"), 

  RUN_SPEED("runSpd"), 
  WALK_SPEED("walkSpd"), 

  STAT_STR("STR"), 
  STAT_CON("CON"), 
  STAT_DEX("DEX"), 
  STAT_INT("INT"), 
  STAT_WIT("WIT"), 
  STAT_MEN("MEN"), 

  BREATH("breath"), 

  AGGRESSION("aggression"), 
  BLEED("bleed"), 
  POISON("poison"), 
  STUN("stun"), 
  ROOT("root"), 
  MOVEMENT("movement"), 
  CONFUSION("confusion"), 
  SLEEP("sleep"), 
  FIRE("fire"), 
  WIND("wind"), 
  WATER("water"), 
  EARTH("earth"), 
  HOLY("holy"), 
  DARK("dark"), 

  AGGRESSION_VULN("aggressionVuln"), 
  BLEED_VULN("bleedVuln"), 
  POISON_VULN("poisonVuln"), 
  STUN_VULN("stunVuln"), 
  PARALYZE_VULN("paralyzeVuln"), 
  ROOT_VULN("rootVuln"), 
  SLEEP_VULN("sleepVuln"), 
  CONFUSION_VULN("confusionVuln"), 
  MOVEMENT_VULN("movementVuln"), 
  FIRE_VULN("fireVuln"), 
  WIND_VULN("windVuln"), 
  WATER_VULN("waterVuln"), 
  EARTH_VULN("earthVuln"), 
  HOLY_VULN("holyVuln"), 
  DARK_VULN("darkVuln"), 
  CANCEL_VULN("cancelVuln"), 
  DERANGEMENT_VULN("derangementVuln"), 
  DEBUFF_VULN("debuffVuln"), 
  FALL_VULN("fallVuln"), 

  NONE_WPN_VULN("noneWpnVuln"), 
  SWORD_WPN_VULN("swordWpnVuln"), 
  BLUNT_WPN_VULN("bluntWpnVuln"), 
  DAGGER_WPN_VULN("daggerWpnVuln"), 
  BOW_WPN_VULN("bowWpnVuln"), 
  POLE_WPN_VULN("poleWpnVuln"), 
  ETC_WPN_VULN("etcWpnVuln"), 
  FIST_WPN_VULN("fistWpnVuln"), 
  DUAL_WPN_VULN("dualWpnVuln"), 
  DUALFIST_WPN_VULN("dualFistWpnVuln"), 

  REFLECT_DAMAGE_PERCENT("reflectDam"), 
  REFLECT_SKILL_MAGIC("reflectSkillMagic"), 
  REFLECT_SKILL_PHYSIC("reflectSkillPhysic"), 
  REFLECT_MAGIC_DEBUFF("reflectMagicDebuff"), 
  REFLECT_PHYSIC_DEBUFF("reflectPhysicDebuff"), 
  ABSORB_DAMAGE_PERCENT("absorbDam"), 
  TRANSFER_DAMAGE_PERCENT("transDam"), 

  MAX_LOAD("maxLoad"), 

  PATK_PLANTS("pAtk-plants"), 
  PATK_INSECTS("pAtk-insects"), 
  PATK_ANIMALS("pAtk-animals"), 
  PATK_MONSTERS("pAtk-monsters"), 
  PATK_DRAGONS("pAtk-dragons"), 
  PATK_UNDEAD("pAtk-undead"), 

  PDEF_PLANTS("pDef-plants"), 
  PDEF_INSECTS("pDef-insects"), 
  PDEF_ANIMALS("pDef-animals"), 
  PDEF_MONSTERS("pDef-monsters"), 
  PDEF_DRAGONS("pDef-dragons"), 
  PDEF_UNDEAD("pDef-undead"), 
  PDEF_GIANTS("pDef-giants"), 

  ATK_REUSE("atkReuse"), 

  INV_LIM("inventoryLimit"), 
  WH_LIM("whLimit"), 
  FREIGHT_LIM("FreightLimit"), 
  P_SELL_LIM("PrivateSellLimit"), 
  P_BUY_LIM("PrivateBuyLimit"), 
  REC_D_LIM("DwarfRecipeLimit"), 
  REC_C_LIM("CommonRecipeLimit"), 

  PHYSICAL_MP_CONSUME_RATE("PhysicalMpConsumeRate"), 
  MAGICAL_MP_CONSUME_RATE("MagicalMpConsumeRate"), 
  DANCE_MP_CONSUME_RATE("DanceMpConsumeRate"), 
  HP_CONSUME_RATE("HpConsumeRate"), 
  MP_CONSUME("MpConsume"), 
  SOULSHOT_COUNT("soulShotCount"), 
  SHIELD_DEFENCE_ANGLE("shieldDefAngle"), 
  SKILL_MASTERY("skillMastery");

  public static final int NUM_STATS;
  private String _value;

  public String getValue()
  {
    return _value;
  }

  private Stats(String s) {
    _value = s;
  }

  public static Stats valueOfXml(String name) {
    name = name.intern();
    for (Stats s : values())
    {
      if (s.getValue().equals(name)) {
        return s;
      }
    }
    throw new NoSuchElementException("Unknown name '" + name + "' for enum BaseStats");
  }

  static
  {
    NUM_STATS = values().length;
  }
}