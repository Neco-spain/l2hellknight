package net.sf.l2j.gameserver.skills;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.ClanHall.ClanHallFunction;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.SystemMessageId;
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
import net.sf.l2j.util.log.AbstractLogger;

public final class Formulas
{
  protected static final Logger _log = AbstractLogger.getLogger(L2Character.class.getName());
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

  public static void init()
  {
    for (int i = 0; i < STRbonus.length; i++) {
      STRbonus[i] = (Math.floor(Math.pow(STRCompute[0], i - STRCompute[1]) * 100.0D + 0.5D) / 100.0D);
    }
    for (int i = 0; i < INTbonus.length; i++) {
      INTbonus[i] = (Math.floor(Math.pow(INTCompute[0], i - INTCompute[1]) * 100.0D + 0.5D) / 100.0D);
    }
    for (int i = 0; i < DEXbonus.length; i++) {
      DEXbonus[i] = (Math.floor(Math.pow(DEXCompute[0], i - DEXCompute[1]) * 100.0D + 0.5D) / 100.0D);
    }
    for (int i = 0; i < WITbonus.length; i++) {
      WITbonus[i] = (Math.floor(Math.pow(WITCompute[0], i - WITCompute[1]) * 100.0D + 0.5D) / 100.0D);
    }
    for (int i = 0; i < CONbonus.length; i++) {
      CONbonus[i] = (Math.floor(Math.pow(CONCompute[0], i - CONCompute[1]) * 100.0D + 0.5D) / 100.0D);
    }
    for (int i = 0; i < MENbonus.length; i++)
      MENbonus[i] = (Math.floor(Math.pow(MENCompute[0], i - MENCompute[1]) * 100.0D + 0.5D) / 100.0D);
  }

  public static Calculator[] getStdNPCCalculators()
  {
    Calculator[] std = new Calculator[Stats.NUM_STATS];

    std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
    std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());

    std[Stats.EVASION_RATE.ordinal()] = new Calculator();
    std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());
    return std;
  }

  public static void addFuncsToNewCharacter(L2Character cha)
  {
    if (cha.isPlayer()) {
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
      cha.addStatFunc(FuncMAtkCritical.getInstance());
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
    } else if (cha.isPet()) {
      cha.addStatFunc(FuncPAtkMod.getInstance());
      cha.addStatFunc(FuncMAtkMod.getInstance());
      cha.addStatFunc(FuncPDefMod.getInstance());
      cha.addStatFunc(FuncMDefMod.getInstance());
    } else if (cha.isL2Summon())
    {
      cha.addStatFunc(FuncAtkCritical.getInstance());
      cha.addStatFunc(FuncMAtkCritical.getInstance());
      cha.addStatFunc(FuncAtkAccuracy.getInstance());
      cha.addStatFunc(FuncAtkEvasion.getInstance());
    }
  }

  public static double calcHpRegen(L2Character cha)
  {
    double value = cha.getTemplate().baseHpReg;
    double hpRegenMultiplier = cha.isRaid() ? Config.RAID_HP_REGEN_MULTIPLIER : Config.HP_REGEN_MULTIPLIER;
    double hpRegenBonus = 0.0D;

    if ((Config.L2JMOD_CHAMPION_ENABLE) && (cha.isChampion())) {
      hpRegenMultiplier *= Config.L2JMOD_CHAMPION_HP_REGEN;
    }

    if (cha.isPlayer()) {
      L2PcInstance player = cha.getPlayer();

      value += (player.getLevel() > 10 ? (player.getLevel() - 1) / 10.0D : 0.5D);

      if ((SevenSignsFestival.getInstance().isFestivalInProgress()) && (player.isFestivalParticipant())) {
        hpRegenMultiplier *= calcFestivalRegenModifier(player);
      } else {
        double siegeModifier = calcSiegeRegenModifer(player);
        if (siegeModifier > 0.0D) {
          hpRegenMultiplier *= siegeModifier;
        }
      }

      if ((player.isInsideZone(16)) && (player.getClan() != null)) {
        int clanHallIndex = player.getClan().getHasHideout();
        if (clanHallIndex > 0) {
          ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
          if ((clansHall != null) && 
            (clansHall.getFunction(3) != null)) {
            hpRegenMultiplier *= (1.0D + clansHall.getFunction(3).getLvl() / 100.0D);
          }

        }

      }

      if (player.isInsideZone(8)) {
        hpRegenBonus += 2.0D;
      }

      if (player.isSitting())
        hpRegenMultiplier *= 1.5D;
      else if (!player.isMoving())
        hpRegenMultiplier *= 1.1D;
      else if (player.isRunning()) {
        hpRegenMultiplier *= 0.7D;
      }

      value *= cha.getLevelMod() * CONbonus[cha.getCON()];
    }

    return cha.calcStat(Stats.REGENERATE_HP_RATE, Math.max(value, 1.0D), null, null) * hpRegenMultiplier + hpRegenBonus;
  }

  public static double calcMpRegen(L2Character cha)
  {
    double value = cha.getTemplate().baseMpReg;
    double mpRegenMultiplier = cha.isRaid() ? Config.RAID_MP_REGEN_MULTIPLIER : Config.MP_REGEN_MULTIPLIER;
    double mpRegenBonus = 0.0D;

    if (cha.isPlayer()) {
      L2PcInstance player = cha.getPlayer();

      value += 0.3D * ((player.getLevel() - 1) / 10.0D);

      if ((SevenSignsFestival.getInstance().isFestivalInProgress()) && (player.isFestivalParticipant())) {
        mpRegenMultiplier *= calcFestivalRegenModifier(player);
      }

      if (player.isInsideZone(8)) {
        mpRegenBonus += 1.0D;
      }

      if ((player.isInsideZone(16)) && (player.getClan() != null)) {
        int clanHallIndex = player.getClan().getHasHideout();
        if (clanHallIndex > 0) {
          ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
          if ((clansHall != null) && 
            (clansHall.getFunction(4) != null)) {
            mpRegenMultiplier *= (1 + clansHall.getFunction(4).getLvl() / 100);
          }

        }

      }

      if (player.isSitting())
        mpRegenMultiplier *= 1.5D;
      else if (!player.isMoving())
        mpRegenMultiplier *= 1.1D;
      else if (player.isRunning()) {
        mpRegenMultiplier *= 0.7D;
      }

      value *= cha.getLevelMod() * MENbonus[cha.getMEN()];
    }

    return cha.calcStat(Stats.REGENERATE_MP_RATE, Math.max(value, 1.0D), null, null) * mpRegenMultiplier + mpRegenBonus;
  }

  public static double calcCpRegen(L2Character cha)
  {
    double value = cha.getTemplate().baseHpReg;
    double cpRegenMultiplier = Config.CP_REGEN_MULTIPLIER;
    double cpRegenBonus = 0.0D;

    if (cha.isPlayer()) {
      L2PcInstance player = cha.getPlayer();

      value += (player.getLevel() > 10 ? (player.getLevel() - 1) / 10.0D : 0.5D);

      if (player.isSitting())
        cpRegenMultiplier *= 1.5D;
      else if (!player.isMoving())
        cpRegenMultiplier *= 1.1D;
      else if (player.isRunning()) {
        cpRegenMultiplier *= 0.7D;
      }

    }
    else if (!cha.isMoving()) {
      cpRegenMultiplier *= 1.1D;
    } else if (cha.isRunning()) {
      cpRegenMultiplier *= 0.7D;
    }

    value *= cha.getLevelMod() * CONbonus[cha.getCON()];

    return cha.calcStat(Stats.REGENERATE_CP_RATE, Math.max(value, 1.0D), null, null) * cpRegenMultiplier + cpRegenBonus;
  }

  public static double calcFestivalRegenModifier(L2PcInstance activeChar)
  {
    int[] festivalInfo = SevenSignsFestival.getInstance().getFestivalForPlayer(activeChar);
    int oracle = festivalInfo[0];
    int festivalId = festivalInfo[1];

    if (festivalId < 0)
      return 0.0D;
    int[] festivalCenter;
    int[] festivalCenter;
    if (oracle == 2)
      festivalCenter = SevenSignsFestival.FESTIVAL_DAWN_PLAYER_SPAWNS[festivalId];
    else {
      festivalCenter = SevenSignsFestival.FESTIVAL_DUSK_PLAYER_SPAWNS[festivalId];
    }

    double distToCenter = activeChar.getDistance(festivalCenter[0], festivalCenter[1]);

    return 1.0D - distToCenter * 0.0005D;
  }

  public static double calcSiegeRegenModifer(L2PcInstance activeChar) {
    if ((activeChar == null) || (activeChar.getClan() == null)) {
      return 0.0D;
    }

    Siege siege = SiegeManager.getInstance().getSiege(activeChar.getPosition().getX(), activeChar.getPosition().getY(), activeChar.getPosition().getZ());
    if ((siege == null) || (!siege.getIsInProgress())) {
      return 0.0D;
    }

    L2SiegeClan siegeClan = siege.getAttackerClan(activeChar.getClan().getClanId());
    if ((siegeClan == null) || (siegeClan.getNumFlags() == 0) || (!Util.checkIfInRange(200, activeChar, siegeClan.getFlag(), true))) {
      return 0.0D;
    }

    return 1.5D;
  }

  public static double calcBlowDamage(L2Character attacker, L2Character target, L2Skill skill, boolean shld, boolean ss)
  {
    if (target.getFirstEffect(446) != null) {
      return 0.0D;
    }

    double power = skill.getPower();
    if ((ss) && (skill.getSSBoost() > 0.0F)) {
      power *= skill.getSSBoost();
    }

    double defence = target.getPDef(attacker);
    if (shld) {
      defence += target.getShldDef();
    }

    double damage = attacker.calcStat(Stats.CRITICAL_DAMAGE, power, target, skill);

    if (ss) {
      damage *= 2.0D;
    }

    if (calcPrWindBonus(attacker)) {
      damage *= 1.2D;
    }

    if (target.isL2Npc()) {
      damage *= ((L2NpcInstance)target).getTemplate().getVulnerability(Stats.DAGGER_WPN_VULN);
    }

    damage = target.calcStat(Stats.DAGGER_WPN_VULN, damage, target, null);
    damage *= 70.0D / defence;
    damage += Rnd.get() * attacker.getRandomDamage(target);

    damage *= target.calcBlowDamageMul();
    if (damage > 0.0D) {
      target.stopSleeping(null);
    }

    return Math.max(damage, 1.0D);
  }

  public static double calcPhysDam(L2Character attacker, L2Character target, L2Skill skill, boolean shld, boolean crit, boolean dual, boolean ss)
  {
    if (target.getFirstEffect(446) != null) {
      return 0.0D;
    }

    if ((shld) && (Rnd.get(100) < 5)) {
      target.sendPacket(Static.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
      return 1.0D;
    }

    double damage = attacker.getPAtk(target);
    double defence = target.getPDef(attacker);

    if (skill != null)
    {
      double skillpower = skill.getPower();
      float ssboost = skill.getSSBoost();
      if (ssboost <= 0.0F)
        damage += skillpower;
      else if (ssboost > 0.0F) {
        if (ss) {
          skillpower *= ssboost;
          damage += skillpower;
        } else {
          damage += skillpower;
        }

      }

    }
    else if (ss) {
      damage *= 2.0D;
    }

    boolean heavy = false;
    boolean heavym = false;
    boolean light = false;
    boolean magic = false;
    if (attacker.isPlayer()) {
      switch (attacker.getClassId().getId())
      {
      case 48:
      case 114:
        if (skill == null) break;
        damage *= 1.5D;
      }

      L2Armor armor = attacker.getActiveChestArmorItem();
      if ((armor != null) && 
        (attacker.isWearingHeavyArmor())) {
        heavym = true;
      }

      if (target.isPlayer()) {
        L2Armor armort = target.getActiveChestArmorItem();
        if (armort != null) {
          if (target.isWearingHeavyArmor()) {
            heavy = true;
          }
          if (target.isWearingLightArmor()) {
            light = true;
          }
          if (target.isWearingMagicArmor()) {
            magic = true;
          }
        }
      }

    }

    if ((attacker.isL2Summon()) && (target.isPlayer())) {
      damage *= 0.9D;
    }

    L2Weapon weapon = attacker.getActiveWeaponItem();
    Stats stat = null;
    if (weapon != null) {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$templates$L2WeaponType[weapon.getItemType().ordinal()]) {
      case 1:
        stat = Stats.BOW_WPN_VULN;
        if ((!crit) || (skill != null)) break;
        if (heavy) {
          damage *= 0.8D; } else {
          if (!magic) break;
          damage *= Config.MAGIC_PDEF_EXP; } break;
      case 2:
      case 3:
        stat = Stats.BLUNT_WPN_VULN;
        break;
      case 4:
        stat = Stats.DAGGER_WPN_VULN;
        if (skill != null) {
          damage *= 0.55D;
        }
        if (!heavym) break;
        damage *= 0.6D; break;
      case 5:
        stat = Stats.DUAL_WPN_VULN;
        if (skill == null) break;
        damage *= 1.05D; break;
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

    if (crit) {
      damage += attacker.getCriticalDmg(target, damage);
      if (attacker.getFirstEffect(357) != null) {
        damage *= 0.7D;
      }
    }
    if ((shld) && (!Config.ALT_GAME_SHIELD_BLOCKS)) {
      defence += target.getShldDef();
    }

    damage = 70.0D * damage / defence;

    if (stat != null)
    {
      damage = target.calcStat(stat, damage, target, null);
      if (target.isL2Npc())
      {
        damage *= ((L2NpcInstance)target).getTemplate().getVulnerability(stat);
      }
    }

    if (calcPrWindBonus(attacker)) {
      damage *= 1.2D;
    }

    if (attacker.getFirstEffect(423) != null) {
      switch (attacker.getFirstEffect(423).getLevel()) {
      case 1:
        damage *= 1.15D;
        break;
      case 2:
        damage *= 1.23D;
        break;
      case 3:
        damage *= 1.3D;
      }

      damage *= target.calcStat(Stats.FIRE_VULN, target.getTemplate().baseFireVuln, target, null);
    }

    damage += Rnd.nextDouble() * damage / 10.0D;

    if ((shld) && (Config.ALT_GAME_SHIELD_BLOCKS)) {
      damage -= target.getShldDef();
      damage = Math.max(damage, 0.0D);
    }

    if (attacker.isL2Npc())
    {
      if (((L2NpcInstance)attacker).getTemplate().getRace() == L2NpcTemplate.Race.UNDEAD) {
        damage /= attacker.getPDefUndead(target);
      }
      if (((L2NpcInstance)attacker).getTemplate().npcId == 29028) {
        damage /= target.getPDefValakas(attacker);
      }
    }

    if (target.isL2Npc()) {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$templates$L2NpcTemplate$Race[((L2NpcInstance)target).getTemplate().getRace().ordinal()]) {
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
      }

      if (((L2NpcInstance)target).getTemplate().npcId == 29028) {
        damage *= attacker.getPAtkValakas(target);
      }

    }

    if (((attacker.isPlayer()) || (attacker.isL2Summon())) && ((target.isPlayer()) || (target.isL2Summon()))) {
      if (skill == null)
        damage *= attacker.calcStat(Stats.PVP_PHYSICAL_DMG, 1.0D, null, null);
      else {
        damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1.0D, null, null);
      }
    }

    return Math.max(damage, 1.0D);
  }

  public static double calcViciousDam(L2Character attacker, double damage, boolean skill) {
    if (skill) {
      damage *= 2.0D;
    }

    return damage;
  }

  public static double calcMagicDam(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean bss, boolean mcrit) {
    double mAtk = attacker.getMAtk(target, skill);
    double mDef = target.getMDef(attacker, skill);
    if (bss)
      mAtk *= 4.0D;
    else if (ss) {
      mAtk *= 2.0D;
    }

    double damage = 91.0D * Math.sqrt(mAtk) / mDef * skill.getPower(attacker) * calcSkillVulnerability(target, skill);

    if ((attacker.isL2Summon()) && (target.isPlayer())) {
      damage *= 0.9D;
    }

    if (!attacker.isInOlympiadMode()) {
      damage *= Config.MAGIC_DAM_EXP;
    }

    if ((Config.ALT_GAME_MAGICFAILURES) && (!calcMagicSuccess(attacker, target, skill))) {
      if (attacker.isPlayer()) {
        if ((calcMagicSuccess(attacker, target, skill)) && (target.getLevel() - attacker.getLevel() <= 9)) {
          if (skill.getSkillType() == L2Skill.SkillType.DRAIN)
            attacker.sendPacket(Static.DRAIN_HALF_SUCCESFUL);
          else {
            attacker.sendPacket(Static.ATTACK_FAILED);
          }

          damage /= 2.0D;
        } else {
          damage = 1.0D;
          attacker.sendPacket(SystemMessage.id(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2).addString(target.getName()).addSkillName(skill.getId()));
        }
      }

      if (target.isPlayer()) {
        if (skill.getSkillType() == L2Skill.SkillType.DRAIN)
          target.sendPacket(SystemMessage.id(SystemMessageId.RESISTED_S1_DRAIN).addString(attacker.getName()));
        else
          target.sendPacket(SystemMessage.id(SystemMessageId.RESISTED_S1_MAGIC).addString(attacker.getName()));
      }
    }
    else if (mcrit) {
      damage *= Config.MAGIC_CRIT_EXP;
    }

    if (((attacker.isPlayer()) || (attacker.isL2Summon())) && ((target.isPlayer()) || (target.isL2Summon()))) {
      if (skill.isMagic())
        damage *= attacker.calcStat(Stats.PVP_MAGICAL_DMG, 1.0D, null, null);
      else {
        damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1.0D, null, null);
      }
    }

    switch (skill.getId()) {
    case 1231:
    case 1245:
      damage *= 0.7D;
    }

    return damage;
  }

  public static boolean calcCrit(double rate)
  {
    return rate > Rnd.get(1000);
  }

  public static boolean calcCrit(L2Character activeChar, double rate) {
    if (activeChar.isBehindTarget()) {
      rate *= 1.6D;
      if (activeChar.getFirstEffect(1357) != null)
        rate *= 1.2D;
    }
    else if (!activeChar.isFrontTarget()) {
      rate *= 1.3D;
    }
    else if (activeChar.getFirstEffect(356) != null) {
      rate *= 0.7D;
    }

    return Rnd.get(4000) < rate;
  }

  public static boolean calcPrWindBonus(L2Character activeChar) {
    return (activeChar.isBehindTarget()) && (activeChar.getFirstEffect(1357) != null);
  }

  public static boolean calcBlow(L2Character activeChar, L2Character target, L2Skill skill, int chance)
  {
    switch (skill.getId()) {
    case 30:
      if (!activeChar.isFrontTarget()) break;
      return false;
    case 263:
    case 344:
      if (Rnd.get(100) >= 25) break;
      return true;
    }

    int blowChance = (int)activeChar.calcStat(Stats.BLOW_RATE, chance * (1.0D + (activeChar.getDEX() - 20) / 100), target, null);
    return Rnd.get(290) < blowChance;
  }

  public static double calcLethal(L2Character activeChar, L2Character target, int baseLethal)
  {
    return activeChar.calcStat(Stats.LETHAL_RATE, baseLethal * (activeChar.getLevel() / target.getLevel()), target, null);
  }

  public static boolean calcMCrit(double mRate) {
    return Rnd.poker(mRate) < mRate;
  }

  public static boolean calcAtkBreak(L2Character target, double dmg)
  {
    if (target.getForceBuff() != null) {
      return true;
    }

    double init = 0.0D;

    if ((Config.ALT_GAME_CANCEL_CAST) && (target.isCastingNow())) {
      init = 15.0D;
    }

    if ((Config.ALT_GAME_CANCEL_BOW) && (target.isAttackingNow())) {
      L2Weapon wpn = target.getActiveWeaponItem();
      if ((wpn != null) && (wpn.getItemType() == L2WeaponType.BOW)) {
        init = 15.0D;
      }
    }

    if (init <= 0.0D) {
      return false;
    }

    init += Math.sqrt(13.0D * dmg);

    init -= MENbonus[target.getMEN()] * 100.0D - 100.0D;

    double rate = target.calcStat(Stats.ATTACK_CANCEL, init, null, null);

    rate = Math.max(rate, 1.0D);
    rate = Math.min(rate, 99.0D);
    return Rnd.get(100) < rate;
  }

  public static int calcPAtkSpd(L2Character attacker, L2Character target, double rate)
  {
    return (int)(500000.0D / rate);
  }

  public static int calcMAtkSpd(L2Character attacker, L2Character target, L2Skill skill, double skillTime)
  {
    if (skill.isMagic()) {
      return (int)(skillTime * 333.0D / attacker.getMAtkSpd());
    }
    return (int)(skillTime * 333.0D / attacker.getPAtkSpd());
  }

  public static int calcMAtkSpd(L2Character attacker, L2Skill skill, double skillTime)
  {
    if (skill.isMagic()) {
      return (int)(skillTime * 333.0D / attacker.getMAtkSpd());
    }
    return (int)(skillTime * 333.0D / attacker.getPAtkSpd());
  }

  public static boolean calcHitMiss(L2Character attacker, L2Character target)
  {
    int d = 85 + attacker.getAccuracy() - target.getEvasionRate(attacker);
    if (attacker.isBehindTarget())
      d = (int)(d * 1.2D);
    else if (!attacker.isFrontTarget()) {
      d = (int)(d * 1.1D);
    }

    return d < Rnd.get(100);
  }

  public static boolean calcShldUse(L2Character attacker, L2Character target)
  {
    double shldRate = target.calcStat(Stats.SHIELD_RATE, 0.0D, attacker, null) * DEXbonus[target.getDEX()];

    if (shldRate == 0.0D) {
      return false;
    }

    if (((target.getKnownSkill(316) == null) || (target.getFirstEffect(318) == null)) && (!target.isFront(attacker))) {
      return false;
    }

    L2Weapon at_weapon = attacker.getActiveWeaponItem();
    if ((at_weapon != null) && (at_weapon.getItemType() == L2WeaponType.BOW)) {
      shldRate *= 1.3D;
    }

    return shldRate > Rnd.get(100);
  }

  public static boolean calcMagicAffected(L2Character actor, L2Character target, L2Skill skill) {
    if (protectBoss(skill, target)) {
      return false;
    }

    double defence = 0.0D;

    if ((skill.isActive()) && (skill.isOffensive())) {
      defence = target.getMDef(actor, skill);
    }
    double attack = 2 * actor.getMAtk(target, skill) * calcSkillVulnerability(target, skill);
    double d = attack - defence;
    d /= (attack + defence);
    d += 0.5D * Rnd.nextGaussian();
    return d > 0.0D;
  }

  private static boolean protectBoss(L2Skill skill, L2Character target) {
    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[skill.getSkillType().ordinal()]) {
    case 1:
    case 2:
    case 3:
    case 4:
    case 5:
    case 6:
    case 7:
    case 8:
    case 9:
      if (!target.isRaid()) break;
      return true;
    }

    return false;
  }

  public static double calcSkillVulnerability(L2Character target, L2Skill skill) {
    double multiplier = 1.0D;

    if (skill != null)
    {
      multiplier = calcTemplateVuln(skill.getStat(), multiplier, target);

      multiplier = calcElementVuln(skill, target, multiplier);

      multiplier = calcSkillTypeVuln(skill.getSkillType(), skill, target, multiplier);
    }
    return multiplier;
  }

  private static double calcTemplateVuln(Stats stat, double multiplier, L2Character target) {
    if (stat != null) {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$skills$Stats[stat.ordinal()]) {
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

    return multiplier;
  }

  private static double calcSkillTypeVuln(L2Skill.SkillType type, L2Skill skill, L2Character target, double multiplier)
  {
    if ((type != null) && ((type == L2Skill.SkillType.PDAM) || (type == L2Skill.SkillType.MDAM))) {
      type = skill.getEffectType();
    }
    if (type != null) {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[type.ordinal()]) {
      case 10:
        multiplier = target.calcStat(Stats.BLEED_VULN, multiplier, target, null);
        break;
      case 11:
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
      case 12:
      case 13:
        multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
        break;
      case 1:
        multiplier = target.calcStat(Stats.CONFUSION_VULN, multiplier, target, null);
        break;
      case 8:
      case 14:
        multiplier = target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null);
        break;
      case 9:
      }
    }

    return multiplier;
  }

  private static double calcElementVuln(L2Skill skill, L2Character target, double multiplier) {
    switch (skill.getElement()) {
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

    return multiplier;
  }

  public static double calcSkillResistans(L2Character target, L2Skill skill, L2Skill.SkillType type, L2Skill.SkillType deftype) {
    double multiplier = 1.0D;

    Stats stat = skill.getStat();
    if (stat != null) {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$skills$Stats[stat.ordinal()]) {
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

    switch (skill.getElement()) {
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

    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[type.ordinal()]) {
    case 10:
      multiplier = target.calcStat(Stats.BLEED_VULN, multiplier, target, null);
      break;
    case 11:
      multiplier = target.calcStat(Stats.POISON_VULN, multiplier, target, null);
      break;
    case 7:
      multiplier = target.calcStat(Stats.STUN_VULN, multiplier, target, null);
      break;
    case 3:
      multiplier = target.calcStat(Stats.PARALYZE_VULN, multiplier, target, null);
      break;
    case 4:
      switch (deftype) {
      case MUTE:
      case FEAR:
      case CANCEL:
        return target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
      }
      multiplier = target.calcStat(Stats.ROOT_VULN, multiplier, target, null);
      break;
    case 6:
      multiplier = target.calcStat(Stats.SLEEP_VULN, multiplier, target, null);
      break;
    case 2:
    case 5:
    case 12:
    case 13:
      multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
      break;
    case 1:
      multiplier = target.calcStat(Stats.CONFUSION_VULN, multiplier, target, null);
      break;
    case 8:
    case 14:
      multiplier = target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null);
    case 9:
    }
    return multiplier;
  }

  public static double calcCONModifier(L2Character target)
  {
    double multiplier = 2.0D - Math.sqrt(CONbonus[target.getCON()]);
    return Math.max(0.0D, multiplier);
  }

  public static double calcMENModifier(L2Character target) {
    double multiplier = 2.0D - Math.sqrt(MENbonus[target.getMEN()]);
    return Math.max(0.0D, multiplier);
  }

  public static boolean calcSkillSuccess(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean sps, boolean bss) {
    if (target.isDebuffImmun(skill)) {
      return false;
    }

    double rate = skill.calcActivateRate(attacker.getMAtk(), target.getMDef(), target, skill.useAltFormula(attacker));

    rate = Math.max(rate, Math.min(skill.getBaseLandRate(), Config.SKILLS_CHANCE_MIN));
    rate = Math.min(rate, Config.SKILLS_CHANCE_MAX);

    double rnd = Rnd.poker(rate);

    if (attacker.getShowSkillChances()) {
      attacker.sendMessage(skill.getName() + ", \u0448\u0430\u043D\u0441: " + Math.round(rate) + "%; \u0432\u044B\u043F\u0430\u043B\u043E: " + Math.round(rnd));
    }
    return rnd < rate;
  }

  public static boolean calcMagicSuccess(L2Character attacker, L2Character target, L2Skill skill)
  {
    if (skill.isAugment()) {
      return true;
    }

    return target.getLevel() - skill.getMagicLevel() < 18;
  }

  public static boolean calculateUnlockChance(L2Skill skill)
  {
    int level = skill.getLevel();
    int chance = 0;
    switch (level) {
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

  public static double calcManaDam(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean bss)
  {
    double mAtk = attacker.getMAtk(target, skill);
    if (bss)
      mAtk *= 9.0D;
    else {
      mAtk *= 4.0D;
    }

    double damage = Math.sqrt(mAtk) * skill.getPower(attacker) * (target.getMaxMp() / 97) / target.getMDef(attacker, skill);

    damage *= calcSkillVulnerability(target, skill);
    return damage;
  }

  public static double calculateSkillResurrectRestorePercent(double baseRestorePercent, int casterWIT) {
    double restorePercent = baseRestorePercent;
    if ((restorePercent != 100.0D) && (restorePercent != 0.0D))
    {
      restorePercent = baseRestorePercent * WITbonus[casterWIT];

      if (restorePercent - baseRestorePercent > 20.0D) {
        restorePercent = baseRestorePercent + 20.0D;
      }
    }

    restorePercent = Math.min(restorePercent, 100.0D);
    restorePercent = Math.max(restorePercent, baseRestorePercent);
    return restorePercent;
  }

  public static double getSTRBonus(L2Character activeChar) {
    return STRbonus[activeChar.getSTR()];
  }

  public static double getINTBonus(L2Character activeChar) {
    return INTbonus[activeChar.getINT()];
  }

  public static boolean calcSkillMastery(L2Character actor) {
    if ((actor == null) || (actor.isInOlympiadMode()))
    {
      return false;
    }
    return Rnd.get(200) < 2;
  }

  static class FuncMaxMpMul extends Func
  {
    static final FuncMaxMpMul _fmmm_instance = new FuncMaxMpMul();

    static Func getInstance() {
      return _fmmm_instance;
    }

    private FuncMaxMpMul() {
      super(32, null);
    }

    public void calc(Env env)
    {
      env.value *= Formulas.MENbonus[env.cha.getMEN()];
    }
  }

  static class FuncMaxMpAdd extends Func
  {
    static final FuncMaxMpAdd _fmma_instance = new FuncMaxMpAdd();

    static Func getInstance() {
      return _fmma_instance;
    }

    private FuncMaxMpAdd() {
      super(16, null);
    }

    public void calc(Env env)
    {
      L2PcTemplate t = (L2PcTemplate)env.cha.getTemplate();
      int lvl = env.cha.getLevel() - t.classBaseLevel;
      double mpmod = t.lvlMpMod * lvl;
      double mpmax = (t.lvlMpAdd + mpmod) * lvl;
      double mpmin = t.lvlMpAdd * lvl + mpmod;
      env.value += (mpmax + mpmin) / 2.0D;
    }
  }

  static class FuncMaxCpMul extends Func
  {
    static final FuncMaxCpMul _fmcm_instance = new FuncMaxCpMul();

    static Func getInstance() {
      return _fmcm_instance;
    }

    private FuncMaxCpMul() {
      super(32, null);
    }

    public void calc(Env env)
    {
      env.value *= Formulas.CONbonus[env.cha.getCON()];
    }
  }

  static class FuncMaxCpAdd extends Func
  {
    static final FuncMaxCpAdd _fmca_instance = new FuncMaxCpAdd();

    static Func getInstance() {
      return _fmca_instance;
    }

    private FuncMaxCpAdd() {
      super(16, null);
    }

    public void calc(Env env)
    {
      L2PcTemplate t = (L2PcTemplate)env.cha.getTemplate();
      int lvl = env.cha.getLevel() - t.classBaseLevel;
      double cpmod = t.lvlCpMod * lvl;
      double cpmax = (t.lvlCpAdd + cpmod) * lvl;
      double cpmin = t.lvlCpAdd * lvl + cpmod;
      env.value += (cpmax + cpmin) / 2.0D;
    }
  }

  static class FuncMaxHpMul extends Func
  {
    static final FuncMaxHpMul _fmhm_instance = new FuncMaxHpMul();

    static Func getInstance() {
      return _fmhm_instance;
    }

    private FuncMaxHpMul() {
      super(32, null);
    }

    public void calc(Env env)
    {
      env.value *= Formulas.CONbonus[env.cha.getCON()];
    }
  }

  static class FuncMaxHpAdd extends Func
  {
    static final FuncMaxHpAdd _fmha_instance = new FuncMaxHpAdd();

    static Func getInstance() {
      return _fmha_instance;
    }

    private FuncMaxHpAdd() {
      super(16, null);
    }

    public void calc(Env env)
    {
      L2PcTemplate t = (L2PcTemplate)env.cha.getTemplate();
      int lvl = env.cha.getLevel() - t.classBaseLevel;
      double hpmod = t.lvlHpMod * lvl;
      double hpmax = (t.lvlHpAdd + hpmod) * lvl;
      double hpmin = t.lvlHpAdd * lvl + hpmod;
      env.value += (hpmax + hpmin) / 2.0D;
    }
  }

  static class FuncHennaWIT extends Func
  {
    static final FuncHennaWIT _fh_instance = new FuncHennaWIT();

    static Func getInstance() {
      return _fh_instance;
    }

    private FuncHennaWIT() {
      super(16, null);
    }

    public void calc(Env env)
    {
      env.value += env.cha.getHennaStatWIT();
    }
  }

  static class FuncHennaCON extends Func
  {
    static final FuncHennaCON _fh_instance = new FuncHennaCON();

    static Func getInstance() {
      return _fh_instance;
    }

    private FuncHennaCON() {
      super(16, null);
    }

    public void calc(Env env)
    {
      env.value += env.cha.getHennaStatCON();
    }
  }

  static class FuncHennaMEN extends Func
  {
    static final FuncHennaMEN _fh_instance = new FuncHennaMEN();

    static Func getInstance() {
      return _fh_instance;
    }

    private FuncHennaMEN() {
      super(16, null);
    }

    public void calc(Env env)
    {
      env.value += env.cha.getHennaStatMEN();
    }
  }

  static class FuncHennaINT extends Func
  {
    static final FuncHennaINT _fh_instance = new FuncHennaINT();

    static Func getInstance() {
      return _fh_instance;
    }

    private FuncHennaINT() {
      super(16, null);
    }

    public void calc(Env env)
    {
      env.value += env.cha.getHennaStatINT();
    }
  }

  static class FuncHennaDEX extends Func
  {
    static final FuncHennaDEX _fh_instance = new FuncHennaDEX();

    static Func getInstance() {
      return _fh_instance;
    }

    private FuncHennaDEX() {
      super(16, null);
    }

    public void calc(Env env)
    {
      env.value += env.cha.getHennaStatDEX();
    }
  }

  static class FuncHennaSTR extends Func
  {
    static final FuncHennaSTR _fh_instance = new FuncHennaSTR();

    static Func getInstance() {
      return _fh_instance;
    }

    private FuncHennaSTR() {
      super(16, null);
    }

    public void calc(Env env)
    {
      env.value += env.cha.getHennaStatSTR();
    }
  }

  static class FuncMAtkSpeed extends Func
  {
    static final FuncMAtkSpeed _fas_instance = new FuncMAtkSpeed();

    static Func getInstance() {
      return _fas_instance;
    }

    private FuncMAtkSpeed() {
      super(32, null);
    }

    public void calc(Env env)
    {
      env.value *= Formulas.WITbonus[env.cha.getWIT()];
    }
  }

  static class FuncPAtkSpeed extends Func
  {
    static final FuncPAtkSpeed _fas_instance = new FuncPAtkSpeed();

    static Func getInstance() {
      return _fas_instance;
    }

    private FuncPAtkSpeed() {
      super(32, null);
    }

    public void calc(Env env)
    {
      env.value *= Formulas.DEXbonus[env.cha.getDEX()];
    }
  }

  static class FuncMoveSpeed extends Func
  {
    static final FuncMoveSpeed _fms_instance = new FuncMoveSpeed();

    static Func getInstance() {
      return _fms_instance;
    }

    private FuncMoveSpeed() {
      super(48, null);
    }

    public void calc(Env env)
    {
      env.value *= Formulas.DEXbonus[env.cha.getDEX()];
    }
  }

  static class FuncMAtkCritical extends Func
  {
    static final FuncMAtkCritical _fac_instance = new FuncMAtkCritical();

    static Func getInstance() {
      return _fac_instance;
    }

    private FuncMAtkCritical() {
      super(48, null);
    }

    public void calc(Env env)
    {
      env.value = env.cha.calcMAtkCritical(env.value, Formulas.DEXbonus[env.cha.getWIT()]);
    }
  }

  static class FuncAtkCritical extends Func
  {
    static final FuncAtkCritical _fac_instance = new FuncAtkCritical();

    static Func getInstance() {
      return _fac_instance;
    }

    private FuncAtkCritical() {
      super(48, null);
    }

    public void calc(Env env)
    {
      env.value = env.cha.calcAtkCritical(env.value, Formulas.DEXbonus[env.cha.getDEX()]);
      env.value = Math.min(env.value, Config.MAX_PCRIT_RATE);
    }
  }

  static class FuncAtkEvasion extends Func
  {
    static final FuncAtkEvasion _fae_instance = new FuncAtkEvasion();

    static Func getInstance() {
      return _fae_instance;
    }

    private FuncAtkEvasion() {
      super(16, null);
    }

    public void calc(Env env)
    {
      env.value += Math.sqrt(env.cha.getDEX()) * 6.0D;
      env.value += env.cha.getLevel();
    }
  }

  static class FuncAtkAccuracy extends Func
  {
    static final FuncAtkAccuracy _faa_instance = new FuncAtkAccuracy();

    static Func getInstance() {
      return _faa_instance;
    }

    private FuncAtkAccuracy() {
      super(16, null);
    }

    public void calc(Env env)
    {
      env.value += Math.sqrt(env.cha.getDEX()) * 6.0D;
      env.value += env.cha.getLevel();
      env.value = env.cha.calcAtkAccuracy(env.value);
    }
  }

  static class FuncBowAtkRange extends Func
  {
    private static final FuncBowAtkRange _fbar_instance = new FuncBowAtkRange();

    static Func getInstance() {
      return _fbar_instance;
    }

    private FuncBowAtkRange() {
      super(16, null);
      setCondition(new ConditionUsingItemType(L2WeaponType.BOW.mask()));
    }

    public void calc(Env env)
    {
      if (!cond.test(env)) {
        return;
      }
      env.value += 450.0D;
    }
  }

  static class FuncPDefMod extends Func
  {
    static final FuncPDefMod _fmm_instance = new FuncPDefMod();

    static Func getInstance() {
      return _fmm_instance;
    }

    private FuncPDefMod() {
      super(32, null);
    }

    public void calc(Env env)
    {
      env.value = env.cha.calcPDefMod(env.value);
      env.value *= env.cha.getLevelMod();
    }
  }

  static class FuncMDefMod extends Func
  {
    static final FuncMDefMod _fmm_instance = new FuncMDefMod();

    static Func getInstance() {
      return _fmm_instance;
    }

    private FuncMDefMod() {
      super(32, null);
    }

    public void calc(Env env)
    {
      env.value = env.cha.calcMDefMod(env.value);
      env.value *= Formulas.MENbonus[env.cha.getMEN()] * env.cha.getLevelMod();
    }
  }

  static class FuncMAtkMod extends Func
  {
    static final FuncMAtkMod _fma_instance = new FuncMAtkMod();

    static Func getInstance() {
      return _fma_instance;
    }

    private FuncMAtkMod() {
      super(32, null);
    }

    public void calc(Env env)
    {
      double intb = Formulas.INTbonus[env.cha.getINT()];
      double lvlb = env.cha.getLevelMod();
      env.value *= lvlb * lvlb * (intb * intb);
      if (env.value > 36000.0D)
        env.value = 36000.0D;
    }
  }

  static class FuncPAtkMod extends Func
  {
    static final FuncPAtkMod _fpa_instance = new FuncPAtkMod();

    static Func getInstance() {
      return _fpa_instance;
    }

    private FuncPAtkMod() {
      super(48, null);
    }

    public void calc(Env env)
    {
      env.value *= Formulas.STRbonus[env.cha.getSTR()] * env.cha.getLevelMod();
    }
  }

  static class FuncMultRegenResting extends Func
  {
    static final FuncMultRegenResting[] _instancies = new FuncMultRegenResting[Stats.NUM_STATS];

    static Func getInstance(Stats stat)
    {
      int pos = stat.ordinal();

      if (_instancies[pos] == null) {
        _instancies[pos] = new FuncMultRegenResting(stat);
      }

      return _instancies[pos];
    }

    private FuncMultRegenResting(Stats pStat)
    {
      super(32, null);
      setCondition(new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RESTING, true));
    }

    public void calc(Env env)
    {
      if (!cond.test(env)) {
        return;
      }

      env.value *= 1.45D;
    }
  }

  static class FuncMultLevelMod extends Func
  {
    static final FuncMultLevelMod[] _instancies = new FuncMultLevelMod[Stats.NUM_STATS];

    static Func getInstance(Stats stat) {
      int pos = stat.ordinal();
      if (_instancies[pos] == null) {
        _instancies[pos] = new FuncMultLevelMod(stat);
      }
      return _instancies[pos];
    }

    private FuncMultLevelMod(Stats pStat) {
      super(32, null);
    }

    public void calc(Env env)
    {
      env.value *= env.cha.getLevelMod();
    }
  }

  static class FuncAddLevel3 extends Func
  {
    static final FuncAddLevel3[] _instancies = new FuncAddLevel3[Stats.NUM_STATS];

    static Func getInstance(Stats stat) {
      int pos = stat.ordinal();
      if (_instancies[pos] == null) {
        _instancies[pos] = new FuncAddLevel3(stat);
      }
      return _instancies[pos];
    }

    private FuncAddLevel3(Stats pStat) {
      super(16, null);
    }

    public void calc(Env env)
    {
      env.value += env.cha.getLevel() / 3.0D;
    }
  }
}