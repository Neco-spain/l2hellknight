package net.sf.l2j.gameserver.templates;

public class L2CharTemplate
{
  public final int baseSTR;
  public final int baseCON;
  public final int baseDEX;
  public final int baseINT;
  public final int baseWIT;
  public final int baseMEN;
  public final float baseHpMax;
  public final float baseCpMax;
  public final float baseMpMax;
  public final float baseHpReg;
  public final float baseMpReg;
  public final int basePAtk;
  public final int baseMAtk;
  public final int basePDef;
  public final int baseMDef;
  public final int basePAtkSpd;
  public final int baseMAtkSpd;
  public final float baseMReuseRate;
  public final int baseShldDef;
  public final int baseAtkRange;
  public final int baseShldRate;
  public final int baseCritRate;
  public final int baseMCritRate;
  public final int baseWalkSpd;
  public final int baseRunSpd;
  public final int baseBreath;
  public final int baseAggression;
  public final int baseBleed;
  public final int basePoison;
  public final int baseStun;
  public final int baseRoot;
  public final int baseMovement;
  public final int baseConfusion;
  public final int baseSleep;
  public final int baseFire;
  public final int baseWind;
  public final int baseWater;
  public final int baseEarth;
  public final int baseHoly;
  public final int baseDark;
  public final double baseAggressionVuln;
  public final double baseBleedVuln;
  public final double basePoisonVuln;
  public final double baseStunVuln;
  public final double baseRootVuln;
  public final double baseMovementVuln;
  public final double baseConfusionVuln;
  public final double baseSleepVuln;
  public final double baseFireVuln;
  public final double baseWindVuln;
  public final double baseWaterVuln;
  public final double baseEarthVuln;
  public final double baseHolyVuln;
  public final double baseDarkVuln;
  public final boolean isUndead;
  public final int baseMpConsumeRate;
  public final int baseHpConsumeRate;
  public final int collisionRadius;
  public final int collisionHeight;

  public L2CharTemplate(StatsSet set)
  {
    baseSTR = set.getInteger("baseSTR");
    baseCON = set.getInteger("baseCON");
    baseDEX = set.getInteger("baseDEX");
    baseINT = set.getInteger("baseINT");
    baseWIT = set.getInteger("baseWIT");
    baseMEN = set.getInteger("baseMEN");
    baseHpMax = set.getFloat("baseHpMax");
    baseCpMax = set.getFloat("baseCpMax");
    baseMpMax = set.getFloat("baseMpMax");
    baseHpReg = set.getFloat("baseHpReg");
    baseMpReg = set.getFloat("baseMpReg");
    basePAtk = set.getInteger("basePAtk");
    baseMAtk = set.getInteger("baseMAtk");
    basePDef = set.getInteger("basePDef");
    baseMDef = set.getInteger("baseMDef");
    basePAtkSpd = set.getInteger("basePAtkSpd");
    baseMAtkSpd = set.getInteger("baseMAtkSpd");
    baseMReuseRate = set.getFloat("baseMReuseDelay", 1.0F);
    baseShldDef = set.getInteger("baseShldDef");
    baseAtkRange = set.getInteger("baseAtkRange");
    baseShldRate = set.getInteger("baseShldRate");
    baseCritRate = set.getInteger("baseCritRate");
    baseMCritRate = set.getInteger("baseMCritRate", 5);
    baseWalkSpd = set.getInteger("baseWalkSpd");
    baseRunSpd = set.getInteger("baseRunSpd");

    baseBreath = set.getInteger("baseBreath", 100);
    baseAggression = set.getInteger("baseAggression", 0);
    baseBleed = set.getInteger("baseBleed", 0);
    basePoison = set.getInteger("basePoison", 0);
    baseStun = set.getInteger("baseStun", 0);
    baseRoot = set.getInteger("baseRoot", 0);
    baseMovement = set.getInteger("baseMovement", 0);
    baseConfusion = set.getInteger("baseConfusion", 0);
    baseSleep = set.getInteger("baseSleep", 0);
    baseFire = set.getInteger("baseFire", 0);
    baseWind = set.getInteger("baseWind", 0);
    baseWater = set.getInteger("baseWater", 0);
    baseEarth = set.getInteger("baseEarth", 0);
    baseHoly = set.getInteger("baseHoly", 0);
    baseDark = set.getInteger("baseDark", 0);
    baseAggressionVuln = set.getInteger("baseAaggressionVuln", 1);
    baseBleedVuln = set.getInteger("baseBleedVuln", 1);
    basePoisonVuln = set.getInteger("basePoisonVuln", 1);
    baseStunVuln = set.getInteger("baseStunVuln", 1);
    baseRootVuln = set.getInteger("baseRootVuln", 1);
    baseMovementVuln = set.getInteger("baseMovementVuln", 1);
    baseConfusionVuln = set.getInteger("baseConfusionVuln", 1);
    baseSleepVuln = set.getInteger("baseSleepVuln", 1);
    baseFireVuln = set.getInteger("baseFireVuln", 1);
    baseWindVuln = set.getInteger("baseWindVuln", 1);
    baseWaterVuln = set.getInteger("baseWaterVuln", 1);
    baseEarthVuln = set.getInteger("baseEarthVuln", 1);
    baseHolyVuln = set.getInteger("baseHolyVuln", 1);
    baseDarkVuln = set.getInteger("baseDarkVuln", 1);

    isUndead = (set.getInteger("isUndead", 0) == 1);

    baseMpConsumeRate = set.getInteger("baseMpConsumeRate", 0);
    baseHpConsumeRate = set.getInteger("baseHpConsumeRate", 0);

    collisionRadius = set.getInteger("collision_radius");
    collisionHeight = set.getInteger("collision_height");
  }

  public int getCollisionHeight()
  {
    return collisionHeight;
  }

  public int getCollisionRadius()
  {
    return collisionRadius;
  }
}