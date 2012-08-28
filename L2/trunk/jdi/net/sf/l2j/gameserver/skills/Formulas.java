package net.sf.l2j.gameserver.skills;

import java.io.PrintStream;
import java.util.List;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.menu;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.CustomZoneManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2CubicInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.model.actor.stat.CharStat;
import net.sf.l2j.gameserver.model.actor.status.CharStatus;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.ClanHall.ClanHallFunction;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.conditions.Condition;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerState;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerState.CheckPlayerState;
import net.sf.l2j.gameserver.skills.conditions.ConditionUsingItemType;
import net.sf.l2j.gameserver.skills.funcs.Func;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate.Race;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public final class Formulas
{
  protected static final Logger _log = Logger.getLogger(L2Character.class.getName());
  private static final int HP_REGENERATE_PERIOD = 3000;
  public static final int MAX_STAT_VALUE = 100;
  private static final double[] STRCompute = { 1.036D, 34.844999999999999D };
  private static final double[] INTCompute = { 1.02D, 31.375D };
  private static final double[] DEXCompute = { 1.009D, 19.359999999999999D };
  private static final double[] WITCompute = { 1.05D, 20.0D };
  private static final double[] CONCompute = { 1.03D, 27.632000000000001D };
  private static final double[] MENCompute = { 1.01D, -0.06D };

  protected static final double[] WITbonus = new double[100];
  protected static final double[] MENbonus = new double[100];
  protected static final double[] INTbonus = new double[100];
  protected static final double[] STRbonus = new double[100];
  protected static final double[] DEXbonus = new double[100];
  protected static final double[] CONbonus = new double[100];
  private static final Formulas _instance;

  public static Formulas getInstance()
  {
    return _instance;
  }

  public int getRegeneratePeriod(L2Character cha)
  {
    if ((cha instanceof L2DoorInstance)) return 300000;

    return 3000;
  }

  public Calculator[] getStdNPCCalculators()
  {
    Calculator[] std = new Calculator[Stats.NUM_STATS];

    std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
    std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());

    std[Stats.EVASION_RATE.ordinal()] = new Calculator();
    std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());

    return std;
  }

  public void addFuncsToNewCharacter(L2Character cha)
  {
    if ((cha instanceof L2PcInstance))
    {
      cha.addStatFunc(FuncMaxHpAdd.getInstance());
      cha.addStatFunc(FuncMaxHpMul.getInstance());
      cha.addStatFunc(FuncMaxCpAdd.getInstance());
      cha.addStatFunc(FuncMaxCpMul.getInstance());
      cha.addStatFunc(FuncMaxMpAdd.getInstance());
      cha.addStatFunc(FuncMaxMpMul.getInstance());

      cha.addStatFunc(FuncBowAtkRange.getInstance());

      cha.addStatFunc(FuncPAtkMod.getInstance());
      cha.addStatFunc(FuncMAtkMod.getInstance());
      cha.addStatFunc(FuncPDefMod.getInstance());
      cha.addStatFunc(FuncMDefMod.getInstance());
      cha.addStatFunc(FuncAtkCritical.getInstance());
      cha.addStatFunc(FuncAtkAccuracy.getInstance());
      cha.addStatFunc(FuncAtkEvasion.getInstance());
      cha.addStatFunc(FuncPAtkSpeed.getInstance());
      cha.addStatFunc(FuncMAtkSpeed.getInstance());
      cha.addStatFunc(FuncMoveSpeed.getInstance());

      cha.addStatFunc(FuncHennaSTR.getInstance());
      cha.addStatFunc(FuncHennaDEX.getInstance());
      cha.addStatFunc(FuncHennaINT.getInstance());
      cha.addStatFunc(FuncHennaMEN.getInstance());
      cha.addStatFunc(FuncHennaCON.getInstance());
      cha.addStatFunc(FuncHennaWIT.getInstance());
    }
    else if ((cha instanceof L2PetInstance))
    {
      cha.addStatFunc(FuncPAtkMod.getInstance());
      cha.addStatFunc(FuncMAtkMod.getInstance());
      cha.addStatFunc(FuncPDefMod.getInstance());
      cha.addStatFunc(FuncMDefMod.getInstance());
    }
    else if ((cha instanceof L2Summon))
    {
      cha.addStatFunc(FuncAtkCritical.getInstance());
      cha.addStatFunc(FuncAtkAccuracy.getInstance());
      cha.addStatFunc(FuncAtkEvasion.getInstance());
    }
  }

  public final double calcHpRegen(L2Character cha)
  {
    double init = cha.getTemplate().baseHpReg;
    double hpRegenMultiplier = cha.isRaid() ? Config.RAID_HP_REGEN_MULTIPLIER : Config.HP_REGEN_MULTIPLIER;
    double hpRegenBonus = 0.0D;

    if ((cha instanceof L2GrandBossInstance))
    {
      L2GrandBossInstance boss = (L2GrandBossInstance)cha;
      if ((boss.getNpcId() == 29022) && (CustomZoneManager.getInstance().checkIfInZone("SunlightRoom", boss))) {
        hpRegenMultiplier *= 0.75D;
      }
    }
    if ((Config.CHAMPION_ENABLE) && (cha.isChampion())) {
      hpRegenMultiplier *= Config.CHAMPION_HP_REGEN;
    }
    if ((cha instanceof L2PcInstance))
    {
      L2PcInstance player = (L2PcInstance)cha;

      init += (player.getLevel() > 10 ? (player.getLevel() - 1) / 10.0D : 0.5D);

      if ((SevenSignsFestival.getInstance().isFestivalInProgress()) && (player.isFestivalParticipant())) {
        hpRegenMultiplier *= calcFestivalRegenModifier(player);
      }
      else {
        double siegeModifier = calcSiegeRegenModifer(player);
        if (siegeModifier > 0.0D) hpRegenMultiplier *= siegeModifier;
      }

      if ((player.isInsideZone(16)) && (player.getClan() != null))
      {
        int clanHallIndex = player.getClan().getHasHideout();
        if (clanHallIndex > 0)
        {
          ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
          if ((clansHall != null) && 
            (clansHall.getFunction(3) != null)) {
            hpRegenMultiplier *= (1 + clansHall.getFunction(3).getLvl() / 100);
          }
        }
      }

      if (player.isInsideZone(8)) hpRegenBonus += 2.0D;

      if (player.isSitting()) {
        if (player.getLevel() <= 40) hpRegenMultiplier *= 6.0D;
        else if (player.isSitting()) hpRegenMultiplier *= 1.5D;
        else if (!player.isMoving()) hpRegenMultiplier *= 1.1D;
        else if (player.isRunning()) hpRegenMultiplier *= 0.7D;
      }

      init *= cha.getLevelMod() * CONbonus[cha.getCON()];
    }

    if (init < 1.0D) init = 1.0D;

    return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null) * hpRegenMultiplier + hpRegenBonus;
  }

  public final double calcMpRegen(L2Character cha)
  {
    double init = cha.getTemplate().baseMpReg;
    double mpRegenMultiplier = cha.isRaid() ? Config.RAID_MP_REGEN_MULTIPLIER : Config.MP_REGEN_MULTIPLIER;
    double mpRegenBonus = 0.0D;

    if ((cha instanceof L2PcInstance))
    {
      L2PcInstance player = (L2PcInstance)cha;

      init += 0.3D * ((player.getLevel() - 1) / 10.0D);

      if ((SevenSignsFestival.getInstance().isFestivalInProgress()) && (player.isFestivalParticipant())) {
        mpRegenMultiplier *= calcFestivalRegenModifier(player);
      }

      if (player.isInsideZone(8)) mpRegenBonus += 1.0D;

      if ((player.isInsideZone(16)) && (player.getClan() != null))
      {
        int clanHallIndex = player.getClan().getHasHideout();
        if (clanHallIndex > 0)
        {
          ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
          if ((clansHall != null) && 
            (clansHall.getFunction(4) != null)) {
            mpRegenMultiplier *= (1 + clansHall.getFunction(4).getLvl() / 100);
          }
        }
      }

      if (player.isSitting()) mpRegenMultiplier *= 1.5D;
      else if (!player.isMoving()) mpRegenMultiplier *= 1.1D;
      else if (player.isRunning()) mpRegenMultiplier *= 0.7D;

      init *= cha.getLevelMod() * MENbonus[cha.getMEN()];
    }

    if (init < 1.0D) init = 1.0D;

    return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null) * mpRegenMultiplier + mpRegenBonus;
  }

  public final double calcCpRegen(L2Character cha)
  {
    double init = cha.getTemplate().baseHpReg;
    double cpRegenMultiplier = Config.CP_REGEN_MULTIPLIER;
    double cpRegenBonus = 0.0D;

    if ((cha instanceof L2PcInstance))
    {
      L2PcInstance player = (L2PcInstance)cha;

      init += (player.getLevel() > 10 ? (player.getLevel() - 1) / 10.0D : 0.5D);

      if (player.isSitting()) cpRegenMultiplier *= 1.5D;
      else if (!player.isMoving()) cpRegenMultiplier *= 1.1D;
      else if (player.isRunning()) cpRegenMultiplier *= 0.7D;

    }
    else if (!cha.isMoving()) { cpRegenMultiplier *= 1.1D;
    } else if (cha.isRunning()) { cpRegenMultiplier *= 0.7D;
    }

    init *= cha.getLevelMod() * CONbonus[cha.getCON()];
    if (init < 1.0D) init = 1.0D;

    return cha.calcStat(Stats.REGENERATE_CP_RATE, init, null, null) * cpRegenMultiplier + cpRegenBonus;
  }

  public final double calcFestivalRegenModifier(L2PcInstance activeChar)
  {
    int[] festivalInfo = SevenSignsFestival.getInstance().getFestivalForPlayer(activeChar);
    int oracle = festivalInfo[0];
    int festivalId = festivalInfo[1];

    if (festivalId < 0) return 0.0D;
    int[] festivalCenter;
    int[] festivalCenter;
    if (oracle == 2) festivalCenter = SevenSignsFestival.FESTIVAL_DAWN_PLAYER_SPAWNS[festivalId]; else {
      festivalCenter = SevenSignsFestival.FESTIVAL_DUSK_PLAYER_SPAWNS[festivalId];
    }

    double distToCenter = activeChar.getDistance(festivalCenter[0], festivalCenter[1]);

    if (Config.DEBUG) {
      _log.info("Distance: " + distToCenter + ", RegenMulti: " + distToCenter * 2.5D / 50.0D);
    }
    return 1.0D - distToCenter * 0.0005D;
  }

  public final double calcSiegeRegenModifer(L2PcInstance activeChar)
  {
    if ((activeChar == null) || (activeChar.getClan() == null)) return 0.0D;

    Siege siege = SiegeManager.getInstance().getSiege(activeChar.getPosition().getX(), activeChar.getPosition().getY(), activeChar.getPosition().getZ());

    if ((siege == null) || (!siege.getIsInProgress())) return 0.0D;

    L2SiegeClan siegeClan = siege.getAttackerClan(activeChar.getClan().getClanId());
    if ((siegeClan == null) || (siegeClan.getFlag().size() == 0) || (!Util.checkIfInRange(200, activeChar, (L2Object)siegeClan.getFlag().get(0), true))) {
      return 0.0D;
    }
    return 1.5D;
  }

  public double calcBlowDamage(L2Character attacker, L2Character target, L2Skill skill, boolean shld, boolean ss)
  {
    if (Rnd.chance(target.calcStat(Stats.P_SKILL_EVASION, 0.0D, null, skill)))
    {
      return 0.0D;
    }

    double power = skill.getPower();
    double damage = attacker.getPAtk(target);
    double defence = target.getPDef(attacker);
    L2Effect vicious = attacker.getFirstEffect(312);
    if ((vicious != null) && (damage > 1.0D))
    {
      for (Func func : vicious.getStatFuncs())
      {
        Env env = new Env();
        env.player = attacker;
        env.target = target;
        env.skill = skill;
        env.value = damage;
        func.calc(env);
        damage = (int)env.value;
      }
    }
    if (ss)
      damage *= 2.0D;
    if (shld)
      defence += target.getShldDef();
    if ((ss) && (skill.getSSBoost() > 0.0F))
      power *= skill.getSSBoost();
    damage += attacker.calcStat(Stats.CRITICAL_DAMAGE, damage + power, target, skill);
    if (attacker.isBehindTarget()) damage = 1.5D * damage;
    if ((target instanceof L2NpcInstance))
    {
      damage *= ((L2NpcInstance)target).getTemplate().getVulnerability(Stats.DAGGER_WPN_VULN);
    }
    damage = target.calcStat(Stats.DAGGER_WPN_VULN, damage, target, null);
    damage *= 70.0D / defence;
    damage += Rnd.get() * attacker.getRandomDamage(target);
    if ((target instanceof L2PcInstance))
    {
      L2Armor armor = ((L2PcInstance)target).getActiveChestArmorItem();
      if (armor != null)
      {
        if (((L2PcInstance)target).isWearingHeavyArmor())
          damage /= Config.BLOW_DAMAGE_HEAVY;
        if (((L2PcInstance)target).isWearingLightArmor())
          damage /= Config.BLOW_DAMAGE_LIGHT;
        if (((L2PcInstance)target).isWearingMagicArmor())
          damage /= Config.BLOW_DAMAGE_ROBE;
      }
    }
    return damage < 1.0D ? 1.0D : damage;
  }

  public final double calcMagicDam(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean bss, boolean mcrit)
  {
    if ((attacker instanceof L2PcInstance))
    {
      L2PcInstance pcInst = (L2PcInstance)attacker;
      if ((pcInst.isGM()) && (pcInst.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)) {
        return 0.0D;
      }
    }
    double mAtk = attacker.getMAtk(target, skill);
    double mDef = target.getMDef(attacker, skill);
    if (bss) mAtk *= 4.0D;
    else if (ss) mAtk *= 2.0D;

    double damage = 91.0D * Math.sqrt(mAtk) / mDef * skill.getPower(attacker) * calcSkillVulnerability(target, skill);

    if (((attacker instanceof L2Summon)) && ((target instanceof L2PcInstance))) damage *= 0.9D;

    if ((Config.ALT_GAME_MAGICFAILURES) && (!calcMagicSuccess(attacker, target, skill)))
    {
      if ((attacker instanceof L2PcInstance))
      {
        int altDiffMagic = 9;
        if (Config.ALT_DIFF_MAGIC) altDiffMagic = 30;
        if ((calcMagicSuccess(attacker, target, skill)) && (target.getLevel() - attacker.getLevel() <= altDiffMagic))
        {
          if (skill.getSkillType() == L2Skill.SkillType.DRAIN)
            attacker.sendPacket(new SystemMessage(SystemMessageId.DRAIN_HALF_SUCCESFUL));
          else {
            attacker.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
          }
          damage /= 2.0D;
        }
        else
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
          sm.addString(target.getName());
          sm.addSkillName(skill.getId());
          attacker.sendPacket(sm);

          damage = 1.0D;
        }
      }

      if ((target instanceof L2PcInstance))
      {
        if (skill.getSkillType() == L2Skill.SkillType.DRAIN)
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.RESISTED_S1_DRAIN);
          sm.addString(attacker.getName());
          target.sendPacket(sm);
        }
        else
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.RESISTED_S1_MAGIC);
          sm.addString(attacker.getName());
          target.sendPacket(sm);
        }
      }
    }
    else if (mcrit) { damage *= Config.M_CRIT_DAMAGE;
    }

    if ((((attacker instanceof L2PcInstance)) || ((attacker instanceof L2Summon))) && (((target instanceof L2PcInstance)) || ((target instanceof L2Summon))))
    {
      if ((skill.isMagic()) && (!mcrit))
        damage *= attacker.calcStat(Stats.PVP_MAGICAL_DMG, 1.0D, null, null);
      else {
        damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1.0D, null, null);
      }
    }
    if ((attacker instanceof L2PcInstance)) {
      if (((L2PcInstance)attacker).getClassId().isMage())
        damage *= Config.ALT_MAGES_MAGICAL_DAMAGE_MULTI;
      else damage *= Config.ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI; 
    }
    else if ((attacker instanceof L2Summon))
      damage *= Config.ALT_PETS_MAGICAL_DAMAGE_MULTI;
    else if ((attacker instanceof L2NpcInstance)) {
      damage *= Config.ALT_NPC_MAGICAL_DAMAGE_MULTI;
    }
    return damage;
  }

  public final boolean calcCrit(double rate)
  {
    return rate > Rnd.get(1000);
  }

  public final boolean calcBlow(L2Character activeChar, L2Character target, int chance)
  {
    return activeChar.calcStat(Stats.BLOW_RATE, chance * (1.0D + (activeChar.getDEX() - 20) / 100), target, null) > Rnd.get(100);
  }

  public final double calcLethal(L2Character activeChar, L2Character target, int baseLethal)
  {
    return activeChar.calcStat(Stats.LETHAL_RATE, baseLethal * (activeChar.getLevel() / target.getLevel()) * 0.5D, target, null);
  }

  public final boolean calcLethalHit(L2Character activeChar, L2Character target, L2Skill skill) {
    if ((!target.isRaid()) && (!(target instanceof L2DoorInstance)) && ((!(target instanceof L2NpcInstance)) || (((L2NpcInstance)target).getNpcId() != 35062)))
    {
      if (Rnd.chance(target.calcStat(Stats.P_SKILL_EVASION, 0.0D, null, skill)))
      {
        return false;
      }

      int chance = Rnd.get(100);

      if ((skill.getLethalChance2() > 0) && (chance < calcLethal(activeChar, target, skill.getLethalChance2())))
      {
        if ((target instanceof L2NpcInstance)) {
          target.reduceCurrentHp(target.getCurrentHp() - 1.0D, activeChar);
        } else if ((target instanceof L2PcInstance))
        {
          L2PcInstance player = (L2PcInstance)target;
          if (!player.isInvul())
          {
            player.setCurrentHp(1.0D);
            player.setCurrentCp(1.0D);
          }
        }
        activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
      }
      else if ((skill.getLethalChance1() > 0) && (chance < calcLethal(activeChar, target, skill.getLethalChance1())))
      {
        if ((target instanceof L2PcInstance))
        {
          L2PcInstance player = (L2PcInstance)target;
          if (!player.isInvul())
            player.setCurrentCp(1.0D);
        }
        else if ((target instanceof L2NpcInstance)) {
          target.reduceCurrentHp(target.getCurrentHp() / 2.0D, activeChar);
        }activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
      }
      else {
        return false;
      }
    } else {
      return false;
    }
    return true;
  }

  public final boolean calcMCrit(double mRate) {
    return mRate > Rnd.get(1000);
  }

  public final boolean calcAtkBreak(L2Character target, double dmg)
  {
    double init = 0.0D;

    if ((Config.ALT_GAME_CANCEL_CAST) && (target.isCastingNow())) init = 50.0D;
    if ((Config.ALT_GAME_CANCEL_BOW) && (target.isAttackingNow()))
    {
      L2Weapon wpn = target.getActiveWeaponItem();
      if ((wpn != null) && (wpn.getItemType() == L2WeaponType.BOW)) init = 15.0D;
    }

    if (init <= 0.0D) return false;

    init += Math.sqrt(13.0D * dmg);

    init -= MENbonus[target.getMEN()] * 100.0D - 100.0D;

    double rate = target.calcStat(Stats.ATTACK_CANCEL, init, null, null);

    if (rate > 99.0D) rate = 99.0D;
    else if (rate < 1.0D) rate = 1.0D;

    return Rnd.get(100) < rate;
  }

  public final int calcPAtkSpd(L2Character attacker, L2Character target, double rate)
  {
    if (rate < 2.0D) return 2700;
    return (int)(470000.0D / rate);
  }

  public final int calcMAtkSpd(L2Character attacker, L2Character target, L2Skill skill, double skillTime)
  {
    if (skill.isMagic()) return (int)(skillTime * 333.0D / attacker.getMAtkSpd());
    return (int)(skillTime * 333.0D / attacker.getPAtkSpd());
  }

  public final int calcMAtkSpd(L2Character attacker, L2Skill skill, double skillTime)
  {
    if (skill.isMagic()) return (int)(skillTime * 333.0D / attacker.getMAtkSpd());
    return (int)(skillTime * 333.0D / attacker.getPAtkSpd());
  }

  public boolean calcHitMiss(L2Character attacker, L2Character target)
  {
    int acc_attacker = attacker.getAccuracy();
    int evas_target = target.getEvasionRate(attacker);
    int d = 85 + acc_attacker - evas_target;
    return d < Rnd.get(100);
  }

  public boolean calcShldUse(L2Character attacker, L2Character target)
  {
    L2Weapon at_weapon = attacker.getActiveWeaponItem();
    double shldRate = target.calcStat(Stats.SHIELD_RATE, 0.0D, attacker, null) * DEXbonus[target.getDEX()];

    if (shldRate == 0.0D) return false;
    if ((target.getKnownSkill(316) == null) && (target.getFirstEffect(318) == null) && 
      (!target.isFront(attacker))) return false;
    if ((at_weapon != null) && (at_weapon.getItemType() == L2WeaponType.BOW))
      shldRate *= 1.3D;
    return shldRate > Rnd.get(100);
  }

  public boolean calcMagicAffected(L2Character actor, L2Character target, L2Skill skill)
  {
    L2Skill.SkillType type = skill.getSkillType();
    double defence = 0.0D;

    if ((skill.isActive()) && (skill.isOffensive()) && (!skill.isNeutral())) {
      defence = target.getMDef(actor, skill);
    }
    double attack = 2 * actor.getMAtk(target, skill) * calcSkillVulnerability(target, skill);
    double d = (attack - defence) / (attack + defence);
    if (target.isRaid())
    {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[type.ordinal()])
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
        return (d > 0.0D) && (Rnd.get(1000) == 1);
      }

    }

    d += 0.5D * Rnd.nextGaussian();
    return d > 0.0D;
  }

  public double calcSkillVulnerability(L2Character target, L2Skill skill)
  {
    double multiplier = 1.0D;

    if (skill != null)
    {
      Stats stat = skill.getStat();
      if (stat != null)
      {
        switch (1.$SwitchMap$net$sf$l2j$gameserver$skills$Stats[stat.ordinal()])
        {
        case 1:
          multiplier *= target.getTemplate().baseAggressionVuln;
          break;
        case 2:
          multiplier *= target.getTemplate().baseBleedVuln;
          break;
        case 3:
          multiplier *= target.getTemplate().basePoisonVuln;
          break;
        case 4:
          multiplier *= target.getTemplate().baseStunVuln;
          break;
        case 5:
          multiplier *= target.getTemplate().baseRootVuln;
          break;
        case 6:
          multiplier *= target.getTemplate().baseMovementVuln;
          break;
        case 7:
          multiplier *= target.getTemplate().baseConfusionVuln;
          break;
        case 8:
          multiplier *= target.getTemplate().baseSleepVuln;
          break;
        case 9:
          multiplier *= target.getTemplate().baseFireVuln;
          break;
        case 10:
          multiplier *= target.getTemplate().baseWindVuln;
          break;
        case 11:
          multiplier *= target.getTemplate().baseWaterVuln;
          break;
        case 12:
          multiplier *= target.getTemplate().baseEarthVuln;
          break;
        case 13:
          multiplier *= target.getTemplate().baseHolyVuln;
          break;
        case 14:
          multiplier *= target.getTemplate().baseDarkVuln;
        }

      }

      switch (skill.getElement())
      {
      case 4:
        multiplier = target.calcStat(Stats.EARTH_VULN, multiplier, target, skill);
        break;
      case 2:
        multiplier = target.calcStat(Stats.FIRE_VULN, multiplier, target, skill);
        break;
      case 3:
        multiplier = target.calcStat(Stats.WATER_VULN, multiplier, target, skill);
        break;
      case 1:
        multiplier = target.calcStat(Stats.WIND_VULN, multiplier, target, skill);
        break;
      case 5:
        multiplier = target.calcStat(Stats.HOLY_VULN, multiplier, target, skill);
        break;
      case 6:
        multiplier = target.calcStat(Stats.DARK_VULN, multiplier, target, skill);
      }

      L2Skill.SkillType type = skill.getSkillType();

      if ((type != null) && ((type == L2Skill.SkillType.PDAM) || (type == L2Skill.SkillType.MDAM))) {
        type = skill.getEffectType();
      }
      if (type != null)
      {
        switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[type.ordinal()])
        {
        case 12:
          multiplier = target.calcStat(Stats.BLEED_VULN, multiplier, target, null);
          break;
        case 13:
          multiplier = target.calcStat(Stats.POISON_VULN, multiplier, target, null);
          break;
        case 7:
          multiplier = target.calcStat(Stats.STUN_VULN, multiplier, target, null);
          break;
        case 3:
          multiplier = target.calcStat(Stats.PARALYZE_VULN, multiplier, target, null);
          break;
        case 4:
          multiplier = target.calcStat(Stats.ROOT_VULN, multiplier, target, null);
          break;
        case 6:
          multiplier = target.calcStat(Stats.SLEEP_VULN, multiplier, target, null);
          break;
        case 2:
        case 5:
        case 14:
        case 15:
        case 16:
          multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
          break;
        case 1:
          multiplier = target.calcStat(Stats.CONFUSION_VULN, multiplier, target, null);
          break;
        case 8:
        case 17:
          multiplier = target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null);
          break;
        case 9:
        case 10:
        case 11:
        }
      }
    }
    return multiplier;
  }

  public double calcSkillStatModifier(L2Skill.SkillType type, L2Character target)
  {
    double multiplier = 1.0D;
    if (type == null) return multiplier;
    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[type.ordinal()])
    {
    case 7:
    case 12:
      multiplier = 2.0D - Math.sqrt(CONbonus[target.getCON()]);
      break;
    case 1:
    case 2:
    case 3:
    case 4:
    case 5:
    case 6:
    case 8:
    case 13:
    case 14:
    case 15:
    case 16:
    case 17:
      multiplier = 2.0D - Math.sqrt(MENbonus[target.getMEN()]);
      break;
    case 9:
    case 10:
    case 11:
    default:
      return multiplier;
    }
    if (multiplier < 0.0D)
      multiplier = 0.0D;
    return multiplier;
  }

  public boolean calcSkillSuccess(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean sps, boolean bss)
  {
    L2Skill.SkillType type = skill.getSkillType();

    if (target.isRaid())
    {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[type.ordinal()])
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
        return false;
      }
    }
    boolean NoImmun = false;
    L2Effect[] effects = target.getAllEffects();
    for (L2Effect e : effects)
    {
      if ((e.getSkill().getId() != 1411) && (e.getSkill().getId() != 396))
        continue;
      NoImmun = true;
      break;
    }

    int value = (int)skill.getPower();
    int lvlDepend = skill.getLevelDepend();

    if ((type == L2Skill.SkillType.PDAM) || (type == L2Skill.SkillType.MDAM))
    {
      value = skill.getEffectPower();
      type = skill.getEffectType();
    }

    if ((value == 0) || (type == null))
    {
      if (skill.getSkillType() == L2Skill.SkillType.PDAM)
      {
        value = 50;
        type = L2Skill.SkillType.STUN;
      }
      if (skill.getSkillType() == L2Skill.SkillType.MDAM)
      {
        value = 30;
        type = L2Skill.SkillType.PARALYZE;
      }

    }

    if (value == 0) value = type == L2Skill.SkillType.FEAR ? 40 : type == L2Skill.SkillType.PARALYZE ? 50 : 80;
    if (lvlDepend == 0) lvlDepend = (type == L2Skill.SkillType.PARALYZE) || (type == L2Skill.SkillType.FEAR) ? 1 : 2;

    int lvlmodifier = ((skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel()) - target.getLevel()) * lvlDepend;

    double statmodifier = calcSkillStatModifier(type, target);
    double resmodifier = calcSkillVulnerability(target, skill);

    int ssmodifier = 100;
    if (bss) ssmodifier = 200;
    else if (sps) ssmodifier = 150;
    else if (ss) ssmodifier = 150;

    int rate = (int)((value * statmodifier + lvlmodifier) * resmodifier);
    if (skill.isMagic()) {
      rate = (int)(rate * Math.pow(attacker.getMAtk(target, skill) / target.getMDef(attacker, skill), 0.2D));
    }

    if (rate > 99) rate = 99;
    else if ((rate < 1) && (NoImmun)) rate = 0;
    else if (rate < 1) rate = 1;
    if (ssmodifier != 100)
    {
      if (rate > 10000 / (100 + ssmodifier)) rate = 100 - (100 - rate) * 100 / ssmodifier; else {
        rate = rate * ssmodifier / 100;
      }
    }
    if (Config.DEVELOPER) {
      System.out.println(skill.getName() + ": " + value + ", " + statmodifier + ", " + lvlmodifier + ", " + resmodifier + ", " + ((int)(Math.pow(attacker.getMAtk(target, skill) / target.getMDef(attacker, skill), 0.2D) * 100.0D) - 100) + ", " + ssmodifier + " ==> " + rate);
    }

    if (menu._vsc)
    {
      attacker.sendPacket(SystemMessage.sendString(skill.getName() + " \u0448\u0430\u043D\u0441: " + rate + "%"));
    }

    if ((!skill.isMagic()) && (Rnd.chance(target.calcStat(Stats.P_SKILL_EVASION, 0.0D, null, skill))))
    {
      return false;
    }
    return Rnd.get(100) < rate;
  }

  public boolean calcMagicSuccess(L2Character attacker, L2Character target, L2Skill skill)
  {
    double lvlDifference = target.getLevel() - (skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel());
    int rate = Math.round((float)(Math.pow(1.3D, lvlDifference) * 100.0D));

    if (Config.ALT_DIFF_MAGIC) rate /= 10;

    if (menu._vsc)
    {
      attacker.sendPacket(SystemMessage.sendString(skill.getName() + " \u0448\u0430\u043D\u0441: " + rate + "%"));
    }

    return Rnd.get(10000) > rate;
  }

  public boolean calculateUnlockChance(L2Skill skill)
  {
    int level = skill.getLevel();
    int chance = 0;
    switch (level)
    {
    case 1:
      chance = 30;
      break;
    case 2:
      chance = 50;
      break;
    case 3:
      chance = 75;
      break;
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
      chance = 100;
    }

    return Rnd.get(120) <= chance;
  }

  public double calcManaDam(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean bss)
  {
    double mAtk = attacker.getMAtk(target, skill);
    double mDef = target.getMDef(attacker, skill);
    double mp = target.getMaxMp();
    if (bss) mAtk *= 4.0D;
    else if (ss) mAtk *= 2.0D;

    double damage = Math.sqrt(mAtk) * skill.getPower(attacker) * (mp / 97.0D) / mDef;
    damage *= calcSkillVulnerability(target, skill);
    return damage;
  }

  public double calculateSkillResurrectRestorePercent(double baseRestorePercent, int casterWIT)
  {
    double restorePercent = baseRestorePercent;
    double modifier = WITbonus[casterWIT];

    if ((restorePercent != 100.0D) && (restorePercent != 0.0D))
    {
      restorePercent = baseRestorePercent * modifier;

      if (restorePercent - baseRestorePercent > 20.0D) {
        restorePercent = baseRestorePercent + 20.0D;
      }
    }
    if (restorePercent > 100.0D)
      restorePercent = 100.0D;
    if (restorePercent < baseRestorePercent) {
      restorePercent = baseRestorePercent;
    }
    return restorePercent;
  }

  public final double calcMagicDam(L2CubicInstance attacker, L2Character target, L2Skill skill, boolean mcrit)
  {
    if (target.isInvul()) return 0.0D;

    double mAtk = attacker.getMAtk();
    double mDef = target.getMDef(attacker.getOwner(), skill);

    double damage = 91.0D * Math.sqrt(mAtk) / mDef * skill.getPower() * calcSkillVulnerability(target, skill);
    L2PcInstance owner = attacker.getOwner();

    if ((Config.ALT_GAME_MAGICFAILURES) && (!calcMagicSuccess(owner, target, skill)))
    {
      if ((calcMagicSuccess(owner, target, skill)) && (target.getLevel() - skill.getMagicLevel() <= 9)) {
        if (skill.getSkillType() == L2Skill.SkillType.DRAIN)
          owner.sendPacket(new SystemMessage(SystemMessageId.DRAIN_HALF_SUCCESFUL));
        else {
          owner.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
        }
        damage /= 2.0D;
      }
      else
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
        sm.addString(target.getName());
        sm.addSkillName(skill.getId());
        owner.sendPacket(sm);

        damage = 1.0D;
      }

      if ((target instanceof L2PcInstance))
      {
        if (skill.getSkillType() == L2Skill.SkillType.DRAIN)
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.RESISTED_S1_DRAIN);
          sm.addString(owner.getName());
          target.sendPacket(sm);
        }
        else
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.RESISTED_S1_MAGIC);
          sm.addString(owner.getName());
          target.sendPacket(sm);
        }
      }
    }
    else if (mcrit) { damage *= 4.0D;
    }
    return damage;
  }

  public boolean calcCubicSkillSuccess(L2CubicInstance attacker, L2Character target, L2Skill skill)
  {
    L2Skill.SkillType type = skill.getSkillType();

    if (target.isRaid())
    {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[type.ordinal()])
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
        return false;
      }

    }

    if (target.reflectSkill(skill)) return false;

    int value = (int)skill.getPower();
    int lvlDepend = skill.getLevelDepend();

    if ((type == L2Skill.SkillType.PDAM) || (type == L2Skill.SkillType.MDAM))
    {
      value = skill.getEffectPower();
      type = skill.getEffectType();
    }

    if (value == 0) value = type == L2Skill.SkillType.FEAR ? 40 : type == L2Skill.SkillType.PARALYZE ? 50 : 80;
    if (lvlDepend == 0) lvlDepend = (type == L2Skill.SkillType.PARALYZE) || (type == L2Skill.SkillType.FEAR) ? 1 : 2;

    int lvlmodifier = ((skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getOwner().getLevel()) - target.getLevel()) * lvlDepend;

    double statmodifier = calcSkillStatModifier(type, target);
    double resmodifier = calcSkillVulnerability(target, skill);

    int rate = (int)((value * statmodifier + lvlmodifier) * resmodifier);
    if (skill.isMagic()) {
      rate += (int)(Math.pow(attacker.getMAtk() / target.getMDef(attacker.getOwner(), skill), 0.2D) * 100.0D) - 100;
    }
    if (rate > 99) {
      rate = 99;
    }
    else if (rate < 1) {
      rate = 1;
    }
    if (Config.DEVELOPER) {
      _log.info(skill.getName() + ": " + value + ", " + statmodifier + ", " + lvlmodifier + ", " + resmodifier + ", " + ((int)(Math.pow(attacker.getMAtk() / target.getMDef(attacker.getOwner(), skill), 0.2D) * 100.0D) - 100) + " ==> " + rate);
    }

    return Rnd.get(100) < rate;
  }

  public double getSTRBonus(L2Character activeChar)
  {
    return STRbonus[activeChar.getSTR()];
  }

  public boolean calcPhysicalSkillEvasion(L2Character target, L2Skill skill)
  {
    if (skill.isMagic()) {
      return false;
    }
    return Rnd.get(100) < target.calcStat(Stats.P_SKILL_EVASION, 0.0D, null, skill);
  }

  public boolean canCancelAttackerTarget(L2Character attacker, L2Character target)
  {
    if (Rnd.get(100) < target.calcStat(Stats.CANCEL_ATTACKER_TARGET, 0.0D, null, null))
    {
      attacker.broadcastPacket(new MagicSkillUser(attacker, attacker, 5144, 1, 350, 150));
      attacker.setTarget(null);
      return true;
    }

    return false;
  }

  public boolean calcSkillMastery(L2Character actor, L2Skill sk)
  {
    if (actor == null) {
      return false;
    }
    if (sk.getSkillType() == L2Skill.SkillType.FISHING) {
      return false;
    }
    double val = actor.getStat().calcStat(Stats.SKILL_MASTERY, 0.0D, null, null);

    if ((actor instanceof L2PcInstance))
    {
      if (((L2PcInstance)actor).isMageClass())
        val *= INTbonus[actor.getINT()];
      else
        val *= STRbonus[actor.getSTR()];
    }
    return Rnd.get(100) < val;
  }

  public final double calcPhysDam(L2Character attacker, L2Character target, L2Skill skill, boolean shld, boolean crit, boolean dual, boolean ss)
  {
    if ((attacker instanceof L2PcInstance))
    {
      L2PcInstance pcInst = (L2PcInstance)attacker;
      if ((pcInst.isGM()) && (pcInst.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)) {
        return 0.0D;
      }
    }
    double damage = attacker.getPAtk(target);
    double defence = target.getPDef(attacker);
    if (ss) damage *= 2.0D;
    if (skill != null)
    {
      if (Rnd.chance(target.calcStat(Stats.P_SKILL_EVASION, 0.0D, null, skill)))
      {
        return 0.0D;
      }

      double skillpower = skill.getPower();
      float ssboost = skill.getSSBoost();
      if (ssboost <= 0.0F)
        damage += skillpower;
      else if (ssboost > 0.0F)
      {
        if (ss)
        {
          skillpower *= ssboost;
          damage += skillpower;
        }
        else {
          damage += skillpower;
        }
      }
    }
    if (((attacker instanceof L2Summon)) && ((target instanceof L2PcInstance))) damage *= 0.9D;

    L2Weapon weapon = attacker.getActiveWeaponItem();
    Stats stat = null;
    if (weapon != null)
    {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$templates$L2WeaponType[weapon.getItemType().ordinal()])
      {
      case 1:
        stat = Stats.BOW_WPN_VULN;
        break;
      case 2:
      case 3:
        stat = Stats.BLUNT_WPN_VULN;
        break;
      case 4:
        stat = Stats.DAGGER_WPN_VULN;
        break;
      case 5:
        stat = Stats.DUAL_WPN_VULN;
        break;
      case 6:
        stat = Stats.DUALFIST_WPN_VULN;
        break;
      case 7:
        stat = Stats.ETC_WPN_VULN;
        break;
      case 8:
        stat = Stats.FIST_WPN_VULN;
        break;
      case 9:
        stat = Stats.POLE_WPN_VULN;
        break;
      case 10:
        stat = Stats.SWORD_WPN_VULN;
        break;
      case 11:
        stat = Stats.SWORD_WPN_VULN;
      }

    }

    if (crit)
    {
      if ((skill != null) && (skill.getStandartCritFormulas()))
        damage += attacker.getCriticalDmg(target, damage);
      else
        damage = Config.ALT_CRIT_DAMAGE * (2.0D * attacker.getCriticalDmg(target, damage));
    }
    if (shld)
    {
      defence += target.getShldDef();
    }

    damage = 70.0D * damage / defence;

    if (stat != null)
    {
      damage = target.calcStat(stat, damage, target, null);
      if ((target instanceof L2NpcInstance))
      {
        damage *= ((L2NpcInstance)target).getTemplate().getVulnerability(stat);
      }
    }

    damage += Rnd.nextDouble() * damage / 10.0D;

    if ((attacker instanceof L2NpcInstance))
    {
      if (((L2NpcInstance)attacker).getTemplate().getRace() == L2NpcTemplate.Race.UNDEAD)
        damage /= attacker.getPDefUndead(target);
    }
    if ((target instanceof L2NpcInstance))
    {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$templates$L2NpcTemplate$Race[((L2NpcInstance)target).getTemplate().getRace().ordinal()])
      {
      case 1:
        damage *= attacker.getPAtkUndead(target);
        break;
      case 2:
        damage *= attacker.getPAtkMonsters(target);
        break;
      case 3:
        damage *= attacker.getPAtkAnimals(target);
        break;
      case 4:
        damage *= attacker.getPAtkPlants(target);
        break;
      case 5:
        damage *= attacker.getPAtkDragons(target);
        break;
      case 6:
        damage *= attacker.getPAtkInsects(target);
        break;
      }

    }

    if (skill != null)
    {
      if (skill.getSkillType() == L2Skill.SkillType.FATALCOUNTER) {
        damage *= (1.0D - attacker.getStatus().getCurrentHp() / attacker.getMaxHp()) * 3.0D;
      }
    }
    if (shld)
    {
      if (100 - Config.ALT_PERFECT_SHLD_BLOCK < Rnd.get(100))
      {
        damage = 1.0D;
        target.sendPacket(new SystemMessage(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS));
      }
    }
    if ((damage > 0.0D) && (damage < 1.0D))
    {
      damage = 1.0D;
    }
    else if (damage < 0.0D)
    {
      damage = 0.0D;
    }

    if ((((attacker instanceof L2PcInstance)) || ((attacker instanceof L2Summon))) && (((target instanceof L2PcInstance)) || ((target instanceof L2Summon))))
    {
      if (skill == null)
        damage *= attacker.calcStat(Stats.PVP_PHYSICAL_DMG, 1.0D, null, null);
      else
        damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1.0D, null, null);
    }
    if ((attacker instanceof L2PcInstance)) {
      if (((L2PcInstance)attacker).getClassId().isMage())
        damage *= Config.ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
      else damage *= Config.ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI; 
    }
    else if ((attacker instanceof L2Summon))
      damage *= Config.ALT_PETS_PHYSICAL_DAMAGE_MULTI;
    else if ((attacker instanceof L2NpcInstance)) {
      damage *= Config.ALT_NPC_PHYSICAL_DAMAGE_MULTI;
    }
    return damage;
  }

  public static final double calcChargeSkillsDam(L2Character attacker, L2Character target, L2Skill skill, boolean shld, boolean crit, boolean ss, int _numCharges)
  {
    if ((attacker instanceof L2PcInstance))
    {
      L2PcInstance pcInst = (L2PcInstance)attacker;
      if ((pcInst.isGM()) && (pcInst.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)) {
        return 0.0D;
      }
    }
    boolean isPvP = ((attacker instanceof L2PcInstance)) && ((target instanceof L2PcInstance));
    double damage = attacker.getPAtk(target);
    if (Config.DEBUG) {
      _log.info("getPAtk(target) : " + damage);
    }
    double defence = target.getPDef(attacker);

    if (skill != null)
    {
      if (Rnd.chance(target.calcStat(Stats.P_SKILL_EVASION, 0.0D, null, skill)))
      {
        return 0.0D;
      }
      double skillpower = skill.getPower(attacker);
      if (ss)
      {
        damage = 2.0D * (damage + skillpower);
      }

      if (_numCharges >= 1)
      {
        double chargesModifier = 1.0D + 0.2D * (_numCharges - 1);
        if (Config.DEBUG)
          _log.info("chargesModifier : " + chargesModifier);
        damage *= chargesModifier;
      }

    }

    damage = 70.0D * damage / defence;

    L2Weapon weapon = attacker.getActiveWeaponItem();
    Stats stat = null;
    if (weapon != null)
    {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$templates$L2WeaponType[weapon.getItemType().ordinal()])
      {
      case 1:
        stat = Stats.BOW_WPN_VULN;
        break;
      case 2:
      case 3:
        stat = Stats.BLUNT_WPN_VULN;
        break;
      case 4:
        stat = Stats.DAGGER_WPN_VULN;
        break;
      case 5:
        stat = Stats.DUAL_WPN_VULN;
        break;
      case 6:
        stat = Stats.DUALFIST_WPN_VULN;
        break;
      case 7:
        stat = Stats.ETC_WPN_VULN;
        break;
      case 8:
        stat = Stats.FIST_WPN_VULN;
        break;
      case 9:
        stat = Stats.POLE_WPN_VULN;
        break;
      case 10:
        stat = Stats.SWORD_WPN_VULN;
        break;
      case 11:
        stat = Stats.SWORD_WPN_VULN;
      }

    }

    if (stat != null)
      damage = target.calcStat(stat, damage, target, null);
    damage += Rnd.nextDouble() * damage / 10.0D;

    if (shld)
    {
      defence += target.getShldDef();
    }

    if ((target instanceof L2NpcInstance))
    {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$templates$L2NpcTemplate$Race[((L2NpcInstance)target).getTemplate().getRace().ordinal()])
      {
      case 1:
        damage *= attacker.getPAtkUndead(target);
        break;
      case 2:
        damage *= attacker.getPAtkMonsters(target);
        break;
      case 3:
        damage *= attacker.getPAtkAnimals(target);
        break;
      case 4:
        damage *= attacker.getPAtkPlants(target);
        break;
      case 5:
        damage *= attacker.getPAtkDragons(target);
        break;
      case 6:
        damage *= attacker.getPAtkInsects(target);
        break;
      }

    }

    if (shld)
    {
      if (100 - Config.ALT_PERFECT_SHLD_BLOCK < Rnd.get(100))
      {
        damage = 1.0D;
        target.sendPacket(new SystemMessage(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS));
      }
    }

    if ((damage > 0.0D) && (damage < 1.0D))
      damage = 1.0D;
    else if (damage < 0.0D) {
      damage = 0.0D;
    }

    if (isPvP)
    {
      if (skill == null)
        damage *= attacker.calcStat(Stats.PVP_PHYSICAL_DMG, 1.0D, null, null);
      else {
        damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1.0D, null, null);
      }
    }
    if ((attacker instanceof L2PcInstance))
    {
      if (((L2PcInstance)attacker).getClassId().isMage())
        damage *= Config.ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
      else damage *= Config.ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;
    }
    else if ((attacker instanceof L2Summon))
      damage *= Config.ALT_PETS_PHYSICAL_DAMAGE_MULTI;
    else if ((attacker instanceof L2NpcInstance)) {
      damage *= Config.ALT_NPC_PHYSICAL_DAMAGE_MULTI;
    }
    return damage;
  }

  static
  {
    for (int i = 0; i < STRbonus.length; i++)
      STRbonus[i] = (Math.floor(Math.pow(STRCompute[0], i - STRCompute[1]) * 100.0D + 0.5D) / 100.0D);
    for (int i = 0; i < INTbonus.length; i++)
      INTbonus[i] = (Math.floor(Math.pow(INTCompute[0], i - INTCompute[1]) * 100.0D + 0.5D) / 100.0D);
    for (int i = 0; i < DEXbonus.length; i++)
      DEXbonus[i] = (Math.floor(Math.pow(DEXCompute[0], i - DEXCompute[1]) * 100.0D + 0.5D) / 100.0D);
    for (int i = 0; i < WITbonus.length; i++)
      WITbonus[i] = (Math.floor(Math.pow(WITCompute[0], i - WITCompute[1]) * 100.0D + 0.5D) / 100.0D);
    for (int i = 0; i < CONbonus.length; i++)
      CONbonus[i] = (Math.floor(Math.pow(CONCompute[0], i - CONCompute[1]) * 100.0D + 0.5D) / 100.0D);
    for (int i = 0; i < MENbonus.length; i++) {
      MENbonus[i] = (Math.floor(Math.pow(MENCompute[0], i - MENCompute[1]) * 100.0D + 0.5D) / 100.0D);
    }

    _instance = new Formulas();
  }

  static class FuncMaxMpMul extends Func
  {
    static final FuncMaxMpMul _fmmm_instance = new FuncMaxMpMul();

    static Func getInstance()
    {
      return _fmmm_instance;
    }

    private FuncMaxMpMul()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      L2PcInstance p = (L2PcInstance)env.player;
      env.value *= Formulas.MENbonus[p.getMEN()];
    }
  }

  static class FuncMaxMpAdd extends Func
  {
    static final FuncMaxMpAdd _fmma_instance = new FuncMaxMpAdd();

    static Func getInstance()
    {
      return _fmma_instance;
    }

    private FuncMaxMpAdd()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      L2PcTemplate t = (L2PcTemplate)env.player.getTemplate();
      int lvl = env.player.getLevel() - t.classBaseLevel;
      double mpmod = t.lvlMpMod * lvl;
      double mpmax = (t.lvlMpAdd + mpmod) * lvl;
      double mpmin = t.lvlMpAdd * lvl + mpmod;
      env.value += (mpmax + mpmin) / 2.0D;
    }
  }

  static class FuncMaxCpMul extends Func
  {
    static final FuncMaxCpMul _fmcm_instance = new FuncMaxCpMul();

    static Func getInstance()
    {
      return _fmcm_instance;
    }

    private FuncMaxCpMul()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      L2PcInstance p = (L2PcInstance)env.player;
      env.value *= Formulas.CONbonus[p.getCON()];
    }
  }

  static class FuncMaxCpAdd extends Func
  {
    static final FuncMaxCpAdd _fmca_instance = new FuncMaxCpAdd();

    static Func getInstance()
    {
      return _fmca_instance;
    }

    private FuncMaxCpAdd()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      L2PcTemplate t = (L2PcTemplate)env.player.getTemplate();
      int lvl = env.player.getLevel() - t.classBaseLevel;
      double cpmod = t.lvlCpMod * lvl;
      double cpmax = (t.lvlCpAdd + cpmod) * lvl;
      double cpmin = t.lvlCpAdd * lvl + cpmod;
      env.value += (cpmax + cpmin) / 2.0D;
    }
  }

  static class FuncMaxHpMul extends Func
  {
    static final FuncMaxHpMul _fmhm_instance = new FuncMaxHpMul();

    static Func getInstance()
    {
      return _fmhm_instance;
    }

    private FuncMaxHpMul()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      L2PcInstance p = (L2PcInstance)env.player;
      env.value *= Formulas.CONbonus[p.getCON()];
    }
  }

  static class FuncMaxHpAdd extends Func
  {
    static final FuncMaxHpAdd _fmha_instance = new FuncMaxHpAdd();

    static Func getInstance()
    {
      return _fmha_instance;
    }

    private FuncMaxHpAdd()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      L2PcTemplate t = (L2PcTemplate)env.player.getTemplate();
      int lvl = env.player.getLevel() - t.classBaseLevel;
      double hpmod = t.lvlHpMod * lvl;
      double hpmax = (t.lvlHpAdd + hpmod) * lvl;
      double hpmin = t.lvlHpAdd * lvl + hpmod;
      env.value += (hpmax + hpmin) / 2.0D;
    }
  }

  static class FuncHennaWIT extends Func
  {
    static final FuncHennaWIT _fh_instance = new FuncHennaWIT();

    static Func getInstance()
    {
      return _fh_instance;
    }

    private FuncHennaWIT()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      L2PcInstance pc = (L2PcInstance)env.player;
      if (pc != null) env.value += pc.getHennaStatWIT();
    }
  }

  static class FuncHennaCON extends Func
  {
    static final FuncHennaCON _fh_instance = new FuncHennaCON();

    static Func getInstance()
    {
      return _fh_instance;
    }

    private FuncHennaCON()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      L2PcInstance pc = (L2PcInstance)env.player;
      if (pc != null) env.value += pc.getHennaStatCON();
    }
  }

  static class FuncHennaMEN extends Func
  {
    static final FuncHennaMEN _fh_instance = new FuncHennaMEN();

    static Func getInstance()
    {
      return _fh_instance;
    }

    private FuncHennaMEN()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      L2PcInstance pc = (L2PcInstance)env.player;
      if (pc != null) env.value += pc.getHennaStatMEN();
    }
  }

  static class FuncHennaINT extends Func
  {
    static final FuncHennaINT _fh_instance = new FuncHennaINT();

    static Func getInstance()
    {
      return _fh_instance;
    }

    private FuncHennaINT()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      L2PcInstance pc = (L2PcInstance)env.player;
      if (pc != null) env.value += pc.getHennaStatINT();
    }
  }

  static class FuncHennaDEX extends Func
  {
    static final FuncHennaDEX _fh_instance = new FuncHennaDEX();

    static Func getInstance()
    {
      return _fh_instance;
    }

    private FuncHennaDEX()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      L2PcInstance pc = (L2PcInstance)env.player;
      if (pc != null) env.value += pc.getHennaStatDEX();
    }
  }

  static class FuncHennaSTR extends Func
  {
    static final FuncHennaSTR _fh_instance = new FuncHennaSTR();

    static Func getInstance()
    {
      return _fh_instance;
    }

    private FuncHennaSTR()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      L2PcInstance pc = (L2PcInstance)env.player;
      if (pc != null) env.value += pc.getHennaStatSTR();
    }
  }

  static class FuncMAtkSpeed extends Func
  {
    static final FuncMAtkSpeed _fas_instance = new FuncMAtkSpeed();

    static Func getInstance()
    {
      return _fas_instance;
    }

    private FuncMAtkSpeed()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      L2PcInstance p = (L2PcInstance)env.player;
      env.value *= Formulas.WITbonus[p.getWIT()];
    }
  }

  static class FuncPAtkSpeed extends Func
  {
    static final FuncPAtkSpeed _fas_instance = new FuncPAtkSpeed();

    static Func getInstance()
    {
      return _fas_instance;
    }

    private FuncPAtkSpeed()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      L2PcInstance p = (L2PcInstance)env.player;
      env.value *= Formulas.DEXbonus[p.getDEX()];
    }
  }

  static class FuncMoveSpeed extends Func
  {
    static final FuncMoveSpeed _fms_instance = new FuncMoveSpeed();

    static Func getInstance()
    {
      return _fms_instance;
    }

    private FuncMoveSpeed()
    {
      super(48, null);
    }

    public void calc(Env env)
    {
      L2PcInstance p = (L2PcInstance)env.player;
      env.value *= Formulas.DEXbonus[p.getDEX()];
    }
  }

  static class FuncAtkCritical extends Func
  {
    static final FuncAtkCritical _fac_instance = new FuncAtkCritical();

    static Func getInstance()
    {
      return _fac_instance;
    }

    private FuncAtkCritical()
    {
      super(48, null);
    }

    public void calc(Env env)
    {
      L2Character p = env.player;
      if ((p instanceof L2Summon)) { env.value = 40.0D;
      } else if (((p instanceof L2PcInstance)) && (p.getActiveWeaponInstance() == null)) { env.value = 40.0D;
      } else
      {
        env.value *= Formulas.DEXbonus[p.getDEX()];
        env.value *= 10.0D;
      }

      env.baseValue = env.value;
    }
  }

  static class FuncAtkEvasion extends Func
  {
    static final FuncAtkEvasion _fae_instance = new FuncAtkEvasion();

    static Func getInstance()
    {
      return _fae_instance;
    }

    private FuncAtkEvasion()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      L2Character p = env.player;

      env.value += Math.sqrt(p.getDEX()) * 6.0D;
      env.value += p.getLevel();
    }
  }

  static class FuncAtkAccuracy extends Func
  {
    static final FuncAtkAccuracy _faa_instance = new FuncAtkAccuracy();

    static Func getInstance()
    {
      return _faa_instance;
    }

    private FuncAtkAccuracy()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      L2Character p = env.player;

      env.value += Math.sqrt(p.getDEX()) * 6.0D;
      env.value += p.getLevel();
      if ((p instanceof L2Summon)) env.value += (p.getLevel() < 60 ? 4.0D : 5.0D);
    }
  }

  static class FuncBowAtkRange extends Func
  {
    private static final FuncBowAtkRange _fbar_instance = new FuncBowAtkRange();

    static Func getInstance()
    {
      return _fbar_instance;
    }

    private FuncBowAtkRange()
    {
      super(16, null);
      setCondition(new ConditionUsingItemType(L2WeaponType.BOW.mask()));
    }

    public void calc(Env env)
    {
      if (!cond.test(env)) return;
      env.value += 450.0D;
    }
  }

  static class FuncPDefMod extends Func
  {
    static final FuncPDefMod _fmm_instance = new FuncPDefMod();

    static Func getInstance()
    {
      return _fmm_instance;
    }

    private FuncPDefMod()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      if ((env.player instanceof L2PcInstance))
      {
        L2PcInstance p = (L2PcInstance)env.player;
        if (p.getInventory().getPaperdollItem(6) != null) env.value -= 12.0D;
        if (p.getInventory().getPaperdollItem(10) != null)
          env.value -= (p.getClassId().isMage() ? 15 : 31);
        if (p.getInventory().getPaperdollItem(11) != null)
          env.value -= (p.getClassId().isMage() ? 8 : 18);
        if (p.getInventory().getPaperdollItem(9) != null) env.value -= 8.0D;
        if (p.getInventory().getPaperdollItem(12) != null) env.value -= 7.0D;
      }
      env.value *= env.player.getLevelMod();
    }
  }

  static class FuncMDefMod extends Func
  {
    static final FuncMDefMod _fmm_instance = new FuncMDefMod();

    static Func getInstance()
    {
      return _fmm_instance;
    }

    private FuncMDefMod()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      if ((env.player instanceof L2PcInstance))
      {
        L2PcInstance p = (L2PcInstance)env.player;
        if (p.getInventory().getPaperdollItem(4) != null) env.value -= 5.0D;
        if (p.getInventory().getPaperdollItem(5) != null) env.value -= 5.0D;
        if (p.getInventory().getPaperdollItem(1) != null) env.value -= 9.0D;
        if (p.getInventory().getPaperdollItem(2) != null) env.value -= 9.0D;
        if (p.getInventory().getPaperdollItem(3) != null) env.value -= 13.0D;
      }
      env.value *= Formulas.MENbonus[env.player.getMEN()] * env.player.getLevelMod();
    }
  }

  static class FuncMAtkMod extends Func
  {
    static final FuncMAtkMod _fma_instance = new FuncMAtkMod();

    static Func getInstance()
    {
      return _fma_instance;
    }

    private FuncMAtkMod()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      double intb = Formulas.INTbonus[env.player.getINT()];
      double lvlb = env.player.getLevelMod();
      env.value *= lvlb * lvlb * (intb * intb);
    }
  }

  static class FuncPAtkMod extends Func
  {
    static final FuncPAtkMod _fpa_instance = new FuncPAtkMod();

    static Func getInstance()
    {
      return _fpa_instance;
    }

    private FuncPAtkMod()
    {
      super(48, null);
    }

    public void calc(Env env)
    {
      env.value *= Formulas.STRbonus[env.player.getSTR()] * env.player.getLevelMod();
    }
  }

  static class FuncMultRegenResting extends Func
  {
    static final FuncMultRegenResting[] _instancies = new FuncMultRegenResting[Stats.NUM_STATS];

    static Func getInstance(Stats stat)
    {
      int pos = stat.ordinal();

      if (_instancies[pos] == null) _instancies[pos] = new FuncMultRegenResting(stat);

      return _instancies[pos];
    }

    private FuncMultRegenResting(Stats pStat)
    {
      super(32, null);
      setCondition(new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RESTING, true));
    }

    public void calc(Env env)
    {
      if (!cond.test(env))
        return;
      env.value *= 1.5D;
    }
  }

  static class FuncMultLevelMod extends Func
  {
    static final FuncMultLevelMod[] _instancies = new FuncMultLevelMod[Stats.NUM_STATS];

    static Func getInstance(Stats stat)
    {
      int pos = stat.ordinal();
      if (_instancies[pos] == null) _instancies[pos] = new FuncMultLevelMod(stat);
      return _instancies[pos];
    }

    private FuncMultLevelMod(Stats pStat)
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      env.value *= env.player.getLevelMod();
    }
  }

  static class FuncAddLevel3 extends Func
  {
    static final FuncAddLevel3[] _instancies = new FuncAddLevel3[Stats.NUM_STATS];

    static Func getInstance(Stats stat)
    {
      int pos = stat.ordinal();
      if (_instancies[pos] == null) _instancies[pos] = new FuncAddLevel3(stat);
      return _instancies[pos];
    }

    private FuncAddLevel3(Stats pStat)
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      env.value += env.player.getLevel() / 3.0D;
    }
  }
}