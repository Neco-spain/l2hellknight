package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.skills.Calculator;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2CharTemplate;

public class CharStat
{
  private L2Character _activeChar;
  private long _exp = 0L;
  private int _sp = 0;
  private byte _level = 1;

  public CharStat(L2Character activeChar)
  {
    _activeChar = activeChar;
  }

  public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
  {
    if (_activeChar == null) {
      return init;
    }
    int id = stat.ordinal();

    Calculator c = _activeChar.getCalculators()[id];

    if ((c == null) || (c.size() == 0)) {
      return init;
    }

    Env env = new Env();
    env.player = _activeChar;
    env.target = target;
    env.skill = skill;
    env.value = init;

    c.calc(env);

    if ((env.value <= 0.0D) && ((stat == Stats.MAX_HP) || (stat == Stats.MAX_MP) || (stat == Stats.MAX_CP) || (stat == Stats.MAGIC_DEFENCE) || (stat == Stats.POWER_DEFENCE) || (stat == Stats.POWER_ATTACK) || (stat == Stats.MAGIC_ATTACK) || (stat == Stats.POWER_ATTACK_SPEED) || (stat == Stats.MAGIC_ATTACK_SPEED) || (stat == Stats.SHIELD_DEFENCE) || (stat == Stats.STAT_CON) || (stat == Stats.STAT_DEX) || (stat == Stats.STAT_INT) || (stat == Stats.STAT_MEN) || (stat == Stats.STAT_STR) || (stat == Stats.STAT_WIT)))
    {
      env.value = 1.0D;
    }

    return env.value;
  }

  public int getAccuracy()
  {
    if (_activeChar == null) {
      return 0;
    }
    return (int)(calcStat(Stats.ACCURACY_COMBAT, 0.0D, null, null) / _activeChar.getWeaponExpertisePenalty());
  }

  public L2Character getActiveChar()
  {
    return _activeChar;
  }

  public final float getAttackSpeedMultiplier()
  {
    if (_activeChar == null) {
      return 1.0F;
    }
    return (float)(1.1D * getPAtkSpd() / _activeChar.getTemplate().basePAtkSpd);
  }

  public final int getCON()
  {
    if (_activeChar == null) {
      return 1;
    }
    return (int)calcStat(Stats.STAT_CON, _activeChar.getTemplate().baseCON, null, null);
  }

  public final double getCriticalDmg(L2Character target, double init)
  {
    return calcStat(Stats.CRITICAL_DAMAGE, init, target, null);
  }

  public int getCriticalHit(L2Character target, L2Skill skill)
  {
    if (_activeChar == null) {
      return 1;
    }
    int criticalHit = (int)calcStat(Stats.CRITICAL_RATE, _activeChar.getTemplate().baseCritRate, target, skill);

    if (criticalHit > Config.ALT_PCRITICAL_CAP) {
      criticalHit = Config.ALT_PCRITICAL_CAP;
    }
    return criticalHit;
  }

  public final int getDEX()
  {
    if (_activeChar == null) {
      return 1;
    }
    return (int)calcStat(Stats.STAT_DEX, _activeChar.getTemplate().baseDEX, null, null);
  }

  public int getEvasionRate(L2Character target)
  {
    if (_activeChar == null) {
      return 1;
    }
    return (int)(calcStat(Stats.EVASION_RATE, 0.0D, target, null) / _activeChar.getArmourExpertisePenalty());
  }

  public long getExp()
  {
    return _exp;
  }

  public void setExp(long value)
  {
    _exp = value;
  }

  public int getINT()
  {
    if (_activeChar == null) {
      return 1;
    }
    return (int)calcStat(Stats.STAT_INT, _activeChar.getTemplate().baseINT, null, null);
  }

  public byte getLevel()
  {
    return _level;
  }

  public void setLevel(byte value)
  {
    _level = value;
  }

  public final int getMagicalAttackRange(L2Skill skill)
  {
    if (skill != null) {
      return (int)calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
    }
    if (_activeChar == null) {
      return 1;
    }
    return _activeChar.getTemplate().baseAtkRange;
  }

  public int getMaxCp()
  {
    if (_activeChar == null) {
      return 1;
    }
    return (int)calcStat(Stats.MAX_CP, _activeChar.getTemplate().baseCpMax, null, null);
  }

  public int getMaxHp()
  {
    if (_activeChar == null) {
      return 1;
    }
    return (int)calcStat(Stats.MAX_HP, _activeChar.getTemplate().baseHpMax, null, null);
  }

  public int getMaxMp()
  {
    if (_activeChar == null) {
      return 1;
    }
    return (int)calcStat(Stats.MAX_MP, _activeChar.getTemplate().baseMpMax, null, null);
  }

  public int getMAtk(L2Character target, L2Skill skill)
  {
    if (_activeChar == null)
      return 1;
    float bonusAtk = 1.0F;
    if ((Config.CHAMPION_ENABLE) && (_activeChar.isChampion()))
      bonusAtk = Config.CHAMPION_ATK;
    double attack = _activeChar.getTemplate().baseMAtk * bonusAtk;

    Stats stat = skill == null ? null : skill.getStat();

    if (stat != null)
    {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$skills$Stats[stat.ordinal()])
      {
      case 1:
        attack += _activeChar.getTemplate().baseAggression;
        break;
      case 2:
        attack += _activeChar.getTemplate().baseBleed;
        break;
      case 3:
        attack += _activeChar.getTemplate().basePoison;
        break;
      case 4:
        attack += _activeChar.getTemplate().baseStun;
        break;
      case 5:
        attack += _activeChar.getTemplate().baseRoot;
        break;
      case 6:
        attack += _activeChar.getTemplate().baseMovement;
        break;
      case 7:
        attack += _activeChar.getTemplate().baseConfusion;
        break;
      case 8:
        attack += _activeChar.getTemplate().baseSleep;
        break;
      case 9:
        attack += _activeChar.getTemplate().baseFire;
        break;
      case 10:
        attack += _activeChar.getTemplate().baseWind;
        break;
      case 11:
        attack += _activeChar.getTemplate().baseWater;
        break;
      case 12:
        attack += _activeChar.getTemplate().baseEarth;
        break;
      case 13:
        attack += _activeChar.getTemplate().baseHoly;
        break;
      case 14:
        attack += _activeChar.getTemplate().baseDark;
      }

    }

    if (skill != null) {
      attack += skill.getPower();
    }

    return (int)calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
  }

  public int getMAtkSpd()
  {
    if (_activeChar == null)
      return 1;
    float bonusSpdAtk = 1.0F;
    if ((Config.CHAMPION_ENABLE) && (_activeChar.isChampion()))
      bonusSpdAtk = Config.CHAMPION_SPD_ATK;
    double val = calcStat(Stats.MAGIC_ATTACK_SPEED, _activeChar.getTemplate().baseMAtkSpd * bonusSpdAtk, null, null);
    val /= _activeChar.getArmourExpertisePenalty();
    return (int)val;
  }

  public final int getMCriticalHit(L2Character target, L2Skill skill)
  {
    if (_activeChar == null)
      return 1;
    double baseMCritRate = 0.0D;
    int baseWIT = _activeChar.getWIT();

    if (baseWIT >= 31)
      baseMCritRate = 13.6D;
    else if (baseWIT == 30)
      baseMCritRate = 13.0D;
    else if (baseWIT == 29)
      baseMCritRate = 12.4D;
    else if (baseWIT == 28)
      baseMCritRate = 11.800000000000001D;
    else if (baseWIT == 27)
      baseMCritRate = 11.199999999999999D;
    else if (baseWIT == 26)
      baseMCritRate = 10.699999999999999D;
    else if (baseWIT == 25)
      baseMCritRate = 10.199999999999999D;
    else if (baseWIT == 24)
      baseMCritRate = 9.699999999999999D;
    else if (baseWIT == 23)
      baseMCritRate = 9.199999999999999D;
    else if (baseWIT == 22)
      baseMCritRate = 8.800000000000001D;
    else if (baseWIT == 21)
      baseMCritRate = 8.4D;
    else if (baseWIT == 20)
      baseMCritRate = 8.0D;
    else if (baseWIT == 19)
      baseMCritRate = 7.6D;
    else if (baseWIT == 18)
      baseMCritRate = 7.2D;
    else if (baseWIT == 17)
      baseMCritRate = 6.8D;
    else if (baseWIT == 16)
      baseMCritRate = 6.5D;
    else if (baseWIT == 15)
      baseMCritRate = 6.2D;
    else if (baseWIT == 14)
      baseMCritRate = 6.0D;
    else if (baseWIT == 13)
      baseMCritRate = 5.7D;
    else if (baseWIT == 12)
      baseMCritRate = 5.4D;
    else if (baseWIT == 11)
      baseMCritRate = 5.1D;
    else if (baseWIT == 10)
      baseMCritRate = 4.8D;
    else if (baseWIT == 9)
      baseMCritRate = 4.6D;
    else if (baseWIT == 8)
      baseMCritRate = 4.4D;
    else if (baseWIT == 7)
      baseMCritRate = 4.2D;
    else if (baseWIT == 6)
      baseMCritRate = 4.0D;
    else if (baseWIT == 5)
      baseMCritRate = 3.8D;
    else if (baseWIT == 4)
      baseMCritRate = 3.6D;
    else if (baseWIT == 3)
      baseMCritRate = 3.5D;
    else if (baseWIT == 2)
      baseMCritRate = 3.3D;
    else if (baseWIT == 1) {
      baseMCritRate = 3.2D;
    }

    double mrate = calcStat(Stats.MCRITICAL_RATE, baseMCritRate * Config.M_CRIT_CHANCE, target, skill);
    if (mrate > Config.ALT_MCRITICAL_CAP)
      mrate = Config.ALT_MCRITICAL_CAP;
    return (int)mrate;
  }

  public int getMDef(L2Character target, L2Skill skill)
  {
    if (_activeChar == null) {
      return 1;
    }

    double defence = _activeChar.getTemplate().baseMDef;

    if (_activeChar.isRaid()) {
      defence *= Config.RAID_DEFENCE_MULTIPLIER;
    }

    return (int)calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
  }

  public final int getMEN()
  {
    if (_activeChar == null) {
      return 1;
    }
    return (int)calcStat(Stats.STAT_MEN, _activeChar.getTemplate().baseMEN, null, null);
  }

  public final float getMovementSpeedMultiplier()
  {
    if (_activeChar == null) {
      return 1.0F;
    }
    return getRunSpeed() * 1.0F / _activeChar.getTemplate().baseRunSpd;
  }

  public final float getMoveSpeed()
  {
    if (_activeChar == null) {
      return 1.0F;
    }
    if (_activeChar.isRunning())
      return getRunSpeed();
    return getWalkSpeed();
  }

  public final double getMReuseRate(L2Skill skill)
  {
    if (_activeChar == null) {
      return 1.0D;
    }
    return calcStat(Stats.MAGIC_REUSE_RATE, _activeChar.getTemplate().baseMReuseRate, null, skill);
  }

  public final double getPReuseRate(L2Skill skill)
  {
    if (_activeChar == null) {
      return 1.0D;
    }
    return calcStat(Stats.P_REUSE, _activeChar.getTemplate().baseMReuseRate, null, skill);
  }

  public int getPAtk(L2Character target)
  {
    if (_activeChar == null)
      return 1;
    float bonusAtk = 1.0F;
    if ((Config.CHAMPION_ENABLE) && (_activeChar.isChampion()))
      bonusAtk = Config.CHAMPION_ATK;
    return (int)calcStat(Stats.POWER_ATTACK, _activeChar.getTemplate().basePAtk * bonusAtk, target, null);
  }

  public final double getPAtkAnimals(L2Character target)
  {
    return calcStat(Stats.PATK_ANIMALS, 1.0D, target, null);
  }

  public final double getPAtkDragons(L2Character target)
  {
    return calcStat(Stats.PATK_DRAGONS, 1.0D, target, null);
  }

  public final double getPAtkInsects(L2Character target)
  {
    return calcStat(Stats.PATK_INSECTS, 1.0D, target, null);
  }

  public final double getPAtkMonsters(L2Character target)
  {
    return calcStat(Stats.PATK_MONSTERS, 1.0D, target, null);
  }

  public final double getPAtkPlants(L2Character target)
  {
    return calcStat(Stats.PATK_PLANTS, 1.0D, target, null);
  }

  public int getPAtkSpd()
  {
    if (_activeChar == null)
      return 1;
    float bonusAtk = 1.0F;
    if ((Config.CHAMPION_ENABLE) && (_activeChar.isChampion()))
      bonusAtk = Config.CHAMPION_SPD_ATK;
    return (int)(calcStat(Stats.POWER_ATTACK_SPEED, _activeChar.getTemplate().basePAtkSpd * bonusAtk, null, null) / _activeChar.getArmourExpertisePenalty());
  }

  public final double getPAtkUndead(L2Character target)
  {
    return calcStat(Stats.PATK_UNDEAD, 1.0D, target, null);
  }

  public final double getPDefUndead(L2Character target)
  {
    return calcStat(Stats.PDEF_UNDEAD, 1.0D, target, null);
  }

  public final double getPDefAnimals(L2Character target)
  {
    return calcStat(Stats.PDEF_ANIMALS, 1.0D, target, null);
  }

  public final double getPDefDragons(L2Character target)
  {
    return calcStat(Stats.PDEF_DRAGONS, 1.0D, target, null);
  }

  public final double getPDefInsects(L2Character target)
  {
    return calcStat(Stats.PDEF_INSECTS, 1.0D, target, null);
  }

  public final double getPDefMonsters(L2Character target)
  {
    return calcStat(Stats.PDEF_MONSTERS, 1.0D, target, null);
  }

  public final double getPDefPlants(L2Character target)
  {
    return calcStat(Stats.PDEF_PLANTS, 1.0D, target, null);
  }

  public final double getPDefGiants(L2Character target)
  {
    return calcStat(Stats.PDEF_GIANTS, 1.0D, target, null);
  }

  public int getPDef(L2Character target)
  {
    if (_activeChar == null) {
      return 1;
    }
    return (int)calcStat(Stats.POWER_DEFENCE, _activeChar.getTemplate().basePDef, target, null);
  }

  public final int getPhysicalAttackRange()
  {
    if (_activeChar == null) {
      return 1;
    }
    return (int)calcStat(Stats.POWER_ATTACK_RANGE, _activeChar.getTemplate().baseAtkRange, null, null);
  }

  public final double getReuseModifier(L2Character target)
  {
    return calcStat(Stats.ATK_REUSE, 1.0D, target, null);
  }

  public int getRunSpeed()
  {
    if (_activeChar == null) {
      return 1;
    }

    int val = (int)calcStat(Stats.RUN_SPEED, _activeChar.getTemplate().baseRunSpd, null, null);

    if (_activeChar.isFlying())
    {
      val += Config.WYVERN_SPEED;
      return val;
    }
    if (_activeChar.isRiding())
    {
      val += Config.STRIDER_SPEED;
      return val;
    }

    if (((_activeChar instanceof L2PlayableInstance)) && (_activeChar.isInsideZone(128))) {
      val /= 2;
    }
    if (((_activeChar instanceof L2PlayableInstance)) && (_activeChar.isInsideZone(2048))) {
      val /= 2;
    }
    val = (int)(val / _activeChar.getArmourExpertisePenalty());
    return val;
  }

  public final int getShldDef()
  {
    return (int)calcStat(Stats.SHIELD_DEFENCE, 0.0D, null, null);
  }

  public int getSp()
  {
    return _sp;
  }

  public void setSp(int value)
  {
    _sp = value;
  }

  public final int getSTR()
  {
    if (_activeChar == null) {
      return 1;
    }
    return (int)calcStat(Stats.STAT_STR, _activeChar.getTemplate().baseSTR, null, null);
  }

  public final int getWalkSpeed()
  {
    if (_activeChar == null) {
      return 1;
    }
    if ((_activeChar instanceof L2PcInstance))
    {
      return getRunSpeed() * 70 / 100;
    }

    return (int)calcStat(Stats.WALK_SPEED, _activeChar.getTemplate().baseWalkSpd, null, null);
  }

  public final int getWIT()
  {
    if (_activeChar == null) {
      return 1;
    }
    return (int)calcStat(Stats.STAT_WIT, _activeChar.getTemplate().baseWIT, null, null);
  }

  public final int getMpConsume(L2Skill skill)
  {
    if (skill == null)
      return 1;
    int mpconsume = skill.getMpConsume();
    if ((skill.isDance()) && (_activeChar != null) && (_activeChar.getDanceCount() > 0))
      mpconsume += _activeChar.getDanceCount() * skill.getNextDanceMpCost();
    return (int)calcStat(Stats.MP_CONSUME, mpconsume, null, skill);
  }

  public final int getMpInitialConsume(L2Skill skill)
  {
    if (skill == null) {
      return 1;
    }
    return (int)calcStat(Stats.MP_CONSUME, skill.getMpInitialConsume(), null, skill);
  }
}