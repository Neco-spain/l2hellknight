package l2p.gameserver.stats;

import java.util.List;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Skill.SkillType;
import l2p.gameserver.model.base.BaseStats;
import l2p.gameserver.model.base.Element;
import l2p.gameserver.model.base.SkillTrait;
import l2p.gameserver.model.instances.ReflectionBossInstance;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.skills.EffectType;
import l2p.gameserver.skills.effects.EffectTemplate;
import l2p.gameserver.templates.CharTemplate;
import l2p.gameserver.templates.item.WeaponTemplate;
import l2p.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2p.gameserver.utils.PositionUtils;
import l2p.gameserver.utils.PositionUtils.TargetDirection;

public class Formulas
{
  public static double calcHpRegen(Creature cha)
  {
    double init;
    double init;
    if (cha.isPlayer())
      init = (cha.getLevel() <= 10 ? 1.5D + cha.getLevel() / 20.0D : 1.4D + cha.getLevel() / 10.0D) * cha.getLevelMod();
    else {
      init = cha.getTemplate().baseHpReg;
    }
    if (cha.isPlayable())
    {
      init *= BaseStats.CON.calcBonus(cha);
      if (cha.isSummon()) {
        init *= 2.0D;
      }
    }
    return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null);
  }

  public static double calcMpRegen(Creature cha)
  {
    double init;
    double init;
    if (cha.isPlayer())
      init = (0.87D + cha.getLevel() * 0.03D) * cha.getLevelMod();
    else {
      init = cha.getTemplate().baseMpReg;
    }
    if (cha.isPlayable())
    {
      init *= BaseStats.MEN.calcBonus(cha);
      if (cha.isSummon()) {
        init *= 2.0D;
      }
    }
    return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null);
  }

  public static double calcCpRegen(Creature cha)
  {
    double init = (1.5D + cha.getLevel() / 10) * cha.getLevelMod() * BaseStats.CON.calcBonus(cha);
    return cha.calcStat(Stats.REGENERATE_CP_RATE, init, null, null);
  }

  public static AttackInfo calcPhysDam(Creature attacker, Creature target, Skill skill, boolean dual, boolean blow, boolean ss, boolean onCrit)
  {
    AttackInfo info = new AttackInfo();

    info.damage = attacker.getPAtk(target);
    info.defence = target.getPDef(attacker);
    info.crit_static = attacker.calcStat(Stats.CRITICAL_DAMAGE_STATIC, target, skill);
    info.death_rcpt = (0.01D * target.calcStat(Stats.DEATH_VULNERABILITY, attacker, skill));
    info.lethal1 = (skill == null ? 0.0D : skill.getLethal1() * info.death_rcpt);
    info.lethal2 = (skill == null ? 0.0D : skill.getLethal2() * info.death_rcpt);
    info.crit = Rnd.chance(calcCrit(attacker, target, skill, blow));
    info.shld = (((skill == null) || (!skill.getShieldIgnore())) && (calcShldUse(attacker, target)));
    info.lethal = false;
    info.miss = false;
    boolean isPvP = (attacker.isPlayable()) && (target.isPlayable());

    if (info.shld) {
      info.defence += target.getShldDef();
    }
    info.defence = Math.max(info.defence, 1.0D);

    if (skill != null)
    {
      if ((!blow) && (!target.isLethalImmune())) {
        if (Rnd.chance(info.lethal1))
        {
          if (target.isPlayer())
          {
            info.lethal = true;
            info.lethal_dmg = target.getCurrentCp();
            target.sendPacket(Msg.CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL);
          }
          else {
            info.lethal_dmg = (target.getCurrentHp() / 2.0D);
          }attacker.sendPacket(Msg.HALF_KILL);
        }
        else if (Rnd.chance(info.lethal2))
        {
          if (target.isPlayer())
          {
            info.lethal = true;
            info.lethal_dmg = (target.getCurrentHp() + target.getCurrentCp() - 1.1D);
            target.sendPacket(SystemMsg.LETHAL_STRIKE);
          }
          else {
            info.lethal_dmg = (target.getCurrentHp() - 1.0D);
          }attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
        }
      }

      if (skill.getPower(target) == 0.0D)
      {
        info.damage = 0.0D;
        return info;
      }

      if ((blow) && (!skill.isBehind()) && (ss)) {
        info.damage *= 2.04D;
      }
      info.damage += Math.max(0.0D, skill.getPower(target) * attacker.calcStat(Stats.SKILL_POWER, 1.0D, null, null));

      if ((blow) && (skill.isBehind()) && (ss)) {
        info.damage *= 1.5D;
      }

      if (!skill.isChargeBoost()) {
        info.damage *= (1.0D + (Rnd.get() * attacker.getRandomDamage() * 2.0D - attacker.getRandomDamage()) / 100.0D);
      }
      if (blow)
      {
        info.damage *= 0.01D * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill);
        info.damage = target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, info.damage, attacker, skill);
        info.damage += 6.1D * info.crit_static;
      }

      if (skill.isChargeBoost()) {
        info.damage *= (0.8D + 0.2D * attacker.getIncreasedForce());
      }
      if (skill.getSkillType() == Skill.SkillType.CHARGE)
        info.damage *= 2.0D;
      else if (skill.isSoulBoost()) {
        info.damage *= (1.0D + 0.06D * Math.min(attacker.getConsumedSouls(), 5));
      }

      info.damage *= 1.10113D;

      if (info.crit)
      {
        if ((skill.isChargeBoost()) || (skill.getSkillType() == Skill.SkillType.CHARGE))
          info.damage *= 2.0D;
        else
          info.damage = (2.0D * target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, info.damage, attacker, skill));
      }
    }
    else
    {
      info.damage *= (1.0D + (Rnd.get() * attacker.getRandomDamage() * 2.0D - attacker.getRandomDamage()) / 100.0D);

      if (dual) {
        info.damage /= 2.0D;
      }
      if (info.crit)
      {
        info.damage *= 0.01D * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill);
        info.damage = (2.0D * target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, info.damage, attacker, skill));
        info.damage += info.crit_static;
      }
    }

    if (info.crit)
    {
      int chance = attacker.getSkillLevel(Integer.valueOf(467));
      if (chance > 0)
      {
        if (chance >= 21)
          chance = 30;
        else if (chance >= 15)
          chance = 25;
        else if (chance >= 9)
          chance = 20;
        else if (chance >= 4)
          chance = 15;
        if (Rnd.chance(chance)) {
          attacker.setConsumedSouls(attacker.getConsumedSouls() + 1, null);
        }
      }
    }
    switch (1.$SwitchMap$l2p$gameserver$utils$PositionUtils$TargetDirection[PositionUtils.getDirectionTo(target, attacker).ordinal()])
    {
    case 1:
      info.damage *= 1.2D;
      break;
    case 2:
      info.damage *= 1.1D;
    }

    if (ss) {
      info.damage *= (blow ? 1.0D : 2.0D);
    }
    info.damage *= 70.0D / info.defence;
    info.damage = attacker.calcStat(Stats.PHYSICAL_DAMAGE, info.damage, target, skill);

    if ((info.shld) && (Rnd.chance(5))) {
      info.damage = 1.0D;
    }
    if (isPvP)
    {
      if (skill == null)
      {
        info.damage *= attacker.calcStat(Stats.PVP_PHYS_DMG_BONUS, 1.0D, null, null);
        info.damage /= target.calcStat(Stats.PVP_PHYS_DEFENCE_BONUS, 1.0D, null, null);
      }
      else
      {
        info.damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG_BONUS, 1.0D, null, null);
        info.damage /= target.calcStat(Stats.PVP_PHYS_SKILL_DEFENCE_BONUS, 1.0D, null, null);
      }

    }

    if (skill != null)
    {
      if (info.shld) {
        if (info.damage == 1.0D)
          target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
        else {
          target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
        }
      }
      if ((info.damage > 1.0D) && (!skill.hasEffects()) && (Rnd.chance(target.calcStat(Stats.PSKILL_EVASION, 0.0D, attacker, skill))))
      {
        attacker.sendPacket(new SystemMessage(2265).addName(attacker));
        target.sendPacket(new SystemMessage(2264).addName(target).addName(attacker));
        info.damage = 0.0D;
      }

      if ((info.damage > 1.0D) && (skill.isDeathlink())) {
        info.damage *= 1.8D * (1.0D - attacker.getCurrentHpRatio());
      }
      if ((onCrit) && (!calcBlow(attacker, target, skill)))
      {
        info.miss = true;
        info.damage = 0.0D;
        attacker.sendPacket(new SystemMessage(2265).addName(attacker));
      }

      if (blow) {
        if (Rnd.chance(info.lethal1))
        {
          if (target.isPlayer())
          {
            info.lethal = true;
            info.lethal_dmg = target.getCurrentCp();
            target.sendPacket(Msg.CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL);
          }
          else if (target.isLethalImmune()) {
            info.damage *= 2.0D;
          } else {
            info.lethal_dmg = (target.getCurrentHp() / 2.0D);
          }attacker.sendPacket(Msg.HALF_KILL);
        }
        else if (Rnd.chance(info.lethal2))
        {
          if (target.isPlayer())
          {
            info.lethal = true;
            info.lethal_dmg = (target.getCurrentHp() + target.getCurrentCp() - 1.1D);
            target.sendPacket(SystemMsg.LETHAL_STRIKE);
          }
          else if (target.isLethalImmune()) {
            info.damage *= 3.0D;
          } else {
            info.lethal_dmg = (target.getCurrentHp() - 1.0D);
          }attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
        }
      }
      if (info.damage > 0.0D) {
        attacker.displayGiveDamageMessage(target, (int)info.damage, (info.crit) || (blow), false, false, false);
      }
      if ((target.isStunned()) && (calcStunBreak(info.crit))) {
        target.getEffectList().stopEffects(EffectType.Stun);
      }
      if (calcCastBreak(target, info.crit)) {
        target.abortCast(false, true);
      }
    }
    return info;
  }

  public static double calcMagicDam(Creature attacker, Creature target, Skill skill, int sps)
  {
    boolean isPvP = (attacker.isPlayable()) && (target.isPlayable());

    boolean shield = (skill.getShieldIgnore()) && (calcShldUse(attacker, target));

    double mAtk = attacker.getMAtk(target, skill);

    if (sps == 2)
      mAtk *= 4.0D;
    else if (sps == 1) {
      mAtk *= 2.0D;
    }
    double mdef = target.getMDef(null, skill);

    if (shield)
      mdef += target.getShldDef();
    if (mdef == 0.0D) {
      mdef = 1.0D;
    }
    double power = skill.getPower(target);
    double lethalDamage = 0.0D;

    if (Rnd.chance(skill.getLethal1()))
    {
      if (target.isPlayer())
      {
        lethalDamage = target.getCurrentCp();
        target.sendPacket(Msg.CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL);
      }
      else if (!target.isLethalImmune()) {
        lethalDamage = target.getCurrentHp() / 2.0D;
      } else {
        power *= 2.0D;
      }attacker.sendPacket(Msg.HALF_KILL);
    }
    else if (Rnd.chance(skill.getLethal2()))
    {
      if (target.isPlayer())
      {
        lethalDamage = target.getCurrentHp() + target.getCurrentCp() - 1.1D;
        target.sendPacket(SystemMsg.LETHAL_STRIKE);
      }
      else if (!target.isLethalImmune()) {
        lethalDamage = target.getCurrentHp() - 1.0D;
      } else {
        power *= 3.0D;
      }attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
    }

    if (power == 0.0D)
    {
      if (lethalDamage > 0.0D)
        attacker.displayGiveDamageMessage(target, (int)lethalDamage, false, false, false, false);
      return lethalDamage;
    }

    if (skill.isSoulBoost()) {
      power *= (1.0D + 0.06D * Math.min(attacker.getConsumedSouls(), 5));
    }
    double damage = 91.0D * power * Math.sqrt(mAtk) / mdef;

    damage *= (1.0D + (Rnd.get() * attacker.getRandomDamage() * 2.0D - attacker.getRandomDamage()) / 100.0D);

    boolean crit = calcMCrit(attacker.getMagicCriticalRate(target, skill));

    if (crit) {
      damage *= attacker.calcStat(Stats.MCRITICAL_DAMAGE, (attacker.isPlayable()) && (target.isPlayable()) ? 2.5D : 3.0D, target, skill);
    }
    damage = attacker.calcStat(Stats.MAGIC_DAMAGE, damage, target, skill);

    if (shield)
    {
      if (Rnd.chance(5))
      {
        damage = 0.0D;
        target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
        attacker.sendPacket(new SystemMessage(2269).addName(target).addName(attacker));
      }
      else
      {
        target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
        attacker.sendPacket(new SystemMessage(2151));
      }
    }

    int levelDiff = target.getLevel() - attacker.getLevel();

    if ((damage > 1.0D) && (skill.isDeathlink())) {
      damage *= 1.8D * (1.0D - attacker.getCurrentHpRatio());
    }
    if ((damage > 1.0D) && (skill.isBasedOnTargetDebuff())) {
      damage *= (1.0D + 0.05D * target.getEffectList().getAllEffects().size());
    }
    damage += lethalDamage;

    if (skill.getSkillType() == Skill.SkillType.MANADAM) {
      damage = Math.max(1.0D, damage / 4.0D);
    }
    if ((isPvP) && (damage > 1.0D))
    {
      damage *= attacker.calcStat(Stats.PVP_MAGIC_SKILL_DMG_BONUS, 1.0D, null, null);
      damage /= target.calcStat(Stats.PVP_MAGIC_SKILL_DEFENCE_BONUS, 1.0D, null, null);
    }

    double magic_rcpt = target.calcStat(Stats.MAGIC_RESIST, attacker, skill) - attacker.calcStat(Stats.MAGIC_POWER, target, skill);
    double failChance = 4.0D * Math.max(1.0D, levelDiff) * (1.0D + magic_rcpt / 100.0D);
    if (Rnd.chance(failChance))
    {
      if (levelDiff > 9)
      {
        damage = 0.0D;
        SystemMessage msg = new SystemMessage(2269).addName(target).addName(attacker);
        attacker.sendPacket(msg);
        target.sendPacket(msg);
      }
      else
      {
        damage /= 2.0D;
        SystemMessage msg = new SystemMessage(2280).addName(target).addName(attacker);
        attacker.sendPacket(msg);
        target.sendPacket(msg);
      }
    }

    if (damage > 0.0D) {
      attacker.displayGiveDamageMessage(target, (int)damage, crit, false, false, true);
    }
    if (calcCastBreak(target, crit)) {
      target.abortCast(false, true);
    }
    return damage;
  }

  public static boolean calcStunBreak(boolean crit)
  {
    return Rnd.chance(crit ? 75 : 10);
  }

  public static boolean calcBlow(Creature activeChar, Creature target, Skill skill)
  {
    WeaponTemplate weapon = activeChar.getActiveWeaponItem();

    double base_weapon_crit = weapon.getCritical();
    double crit_height_bonus = 0.008D * Math.min(25, Math.max(-25, target.getZ() - activeChar.getZ())) + 1.1D;
    double buffs_mult = activeChar.calcStat(Stats.FATALBLOW_RATE, target, skill);
    double skill_mod = skill.isBehind() ? 5.0D : 4.0D;

    double chance = base_weapon_crit * buffs_mult * crit_height_bonus * skill_mod;

    if (!target.isInCombat()) {
      chance *= 1.1D;
    }
    switch (1.$SwitchMap$l2p$gameserver$utils$PositionUtils$TargetDirection[PositionUtils.getDirectionTo(target, activeChar).ordinal()])
    {
    case 1:
      chance *= 1.3D;
      break;
    case 2:
      chance *= 1.1D;
      break;
    case 3:
      if (!skill.isBehind()) break;
      chance = 3.0D;
    }

    chance = Math.min(skill.isBehind() ? 100.0D : 80.0D, chance);
    return Rnd.chance(chance);
  }

  public static double calcCrit(Creature attacker, Creature target, Skill skill, boolean blow)
  {
    if ((attacker.isPlayer()) && (attacker.getActiveWeaponItem() == null))
      return 0.0D;
    if (skill != null) {
      return skill.getCriticalRate() * (blow ? BaseStats.DEX.calcBonus(attacker) : BaseStats.STR.calcBonus(attacker)) * 0.01D * attacker.calcStat(Stats.SKILL_CRIT_CHANCE_MOD, target, skill);
    }
    double rate = attacker.getCriticalHit(target, null) * 0.01D * target.calcStat(Stats.CRIT_CHANCE_RECEPTIVE, attacker, skill);

    switch (1.$SwitchMap$l2p$gameserver$utils$PositionUtils$TargetDirection[PositionUtils.getDirectionTo(target, attacker).ordinal()])
    {
    case 1:
      rate *= 1.4D;
      break;
    case 2:
      rate *= 1.2D;
    }

    return rate / 10.0D;
  }

  public static boolean calcMCrit(double mRate)
  {
    return Rnd.get() * 100.0D <= Math.min(Config.LIM_MCRIT, mRate);
  }

  public static boolean calcCastBreak(Creature target, boolean crit)
  {
    if ((target == null) || (target.isInvul()) || (target.isRaid()) || (!target.isCastingNow()))
      return false;
    Skill skill = target.getCastingSkill();
    if ((skill != null) && ((skill.getSkillType() == Skill.SkillType.TAKECASTLE) || (skill.getSkillType() == Skill.SkillType.TAKEFORTRESS) || (skill.getSkillType() == Skill.SkillType.TAKEFLAG)))
      return false;
    return Rnd.chance(target.calcStat(Stats.CAST_INTERRUPT, crit ? 75.0D : 10.0D, null, skill));
  }

  public static int calcPAtkSpd(double rate)
  {
    return (int)(500000.0D / rate);
  }

  public static int calcMAtkSpd(Creature attacker, Skill skill, double skillTime)
  {
    if (skill.isMagic())
      return (int)(skillTime * 333.0D / Math.max(attacker.getMAtkSpd(), 1));
    return (int)(skillTime * 333.0D / Math.max(attacker.getPAtkSpd(), 1));
  }

  public static long calcSkillReuseDelay(Creature actor, Skill skill)
  {
    long reuseDelay = skill.getReuseDelay();
    if (actor.isMonster())
      reuseDelay = skill.getReuseForMonsters();
    if ((skill.isReuseDelayPermanent()) || (skill.isHandler()) || (skill.isItemSkill()))
      return reuseDelay;
    if (actor.getSkillMastery(Integer.valueOf(skill.getId())) == 1)
    {
      actor.removeSkillMastery(Integer.valueOf(skill.getId()));
      return 0L;
    }
    if (skill.isMusic())
      return ()actor.calcStat(Stats.MUSIC_REUSE_RATE, reuseDelay, null, skill);
    if (skill.isMagic())
      return ()actor.calcStat(Stats.MAGIC_REUSE_RATE, reuseDelay, null, skill);
    return ()actor.calcStat(Stats.PHYSIC_REUSE_RATE, reuseDelay, null, skill);
  }

  public static boolean calcHitMiss(Creature attacker, Creature target)
  {
    int chanceToHit = 88 + 2 * (attacker.getAccuracy() - target.getEvasionRate(attacker));

    chanceToHit = Math.max(chanceToHit, 28);
    chanceToHit = Math.min(chanceToHit, 98);

    PositionUtils.TargetDirection direction = PositionUtils.getDirectionTo(attacker, target);
    switch (1.$SwitchMap$l2p$gameserver$utils$PositionUtils$TargetDirection[direction.ordinal()])
    {
    case 1:
      chanceToHit = (int)(chanceToHit * 1.2D);
      break;
    case 2:
      chanceToHit = (int)(chanceToHit * 1.1D);
    }

    return !Rnd.chance(chanceToHit);
  }

  public static boolean calcShldUse(Creature attacker, Creature target)
  {
    WeaponTemplate template = target.getSecondaryWeaponItem();
    if ((template == null) || (template.getItemType() != WeaponTemplate.WeaponType.NONE))
      return false;
    int angle = (int)target.calcStat(Stats.SHIELD_ANGLE, attacker, null);
    if (!PositionUtils.isFacing(target, attacker, angle))
      return false;
    return Rnd.chance((int)target.calcStat(Stats.SHIELD_RATE, attacker, null));
  }

  public static boolean calcSkillSuccess(Env env, EffectTemplate et, int spiritshot)
  {
    if (value == -1.0D) {
      return true;
    }
    value = Math.max(Math.min(value, 100.0D), 1.0D);
    double base = value;

    Skill skill = env.skill;
    if (!skill.isOffensive()) {
      return Rnd.chance(value);
    }
    Creature caster = character;
    Creature target = env.target;

    boolean debugCaster = false;
    boolean debugTarget = false;
    boolean debugGlobal = false;
    if (Config.ALT_DEBUG_ENABLED)
    {
      debugCaster = (caster.getPlayer() != null) && (caster.getPlayer().isDebug());

      debugTarget = (target.getPlayer() != null) && (target.getPlayer().isDebug());

      boolean debugPvP = (Config.ALT_DEBUG_PVP_ENABLED) && (debugCaster) && (debugTarget) && ((!Config.ALT_DEBUG_PVP_DUEL_ONLY) || ((caster.getPlayer().isInDuel()) && (target.getPlayer().isInDuel())));

      debugGlobal = (debugPvP) || ((Config.ALT_DEBUG_PVE_ENABLED) && (((debugCaster) && (target.isMonster())) || ((debugTarget) && (caster.isMonster()))));
    }

    double statMod = 1.0D;
    if (skill.getSaveVs() != null)
    {
      statMod = skill.getSaveVs().calcChanceMod(target);
      value *= statMod;
    }

    value = Math.max(value, 1.0D);

    double mAtkMod = 1.0D;
    int ssMod = 0;
    if (skill.isMagic())
    {
      int mdef = Math.max(1, target.getMDef(target, skill));
      double matk = caster.getMAtk(target, skill);

      if (skill.isSSPossible())
      {
        switch (spiritshot)
        {
        case 2:
          ssMod = 4;
          break;
        case 1:
          ssMod = 2;
          break;
        default:
          ssMod = 1;
        }
        matk *= ssMod;
      }

      mAtkMod = Config.SKILLS_CHANCE_MOD * Math.pow(matk, Config.SKILLS_CHANCE_POW) / mdef;

      value *= mAtkMod;
      value = Math.max(value, 1.0D);
    }

    double lvlDependMod = skill.getLevelModifier();
    if (lvlDependMod != 0.0D)
    {
      int attackLevel = skill.getMagicLevel() > 0 ? skill.getMagicLevel() : caster.getLevel();

      lvlDependMod = 1.0D + (attackLevel - target.getLevel()) * 0.03D * lvlDependMod;
      if (lvlDependMod < 0.0D)
        lvlDependMod = 0.0D;
      else if (lvlDependMod > 2.0D) {
        lvlDependMod = 2.0D;
      }
      value *= lvlDependMod;
    }

    double vulnMod = 0.0D;
    double profMod = 0.0D;
    double resMod = 1.0D;
    double debuffMod = 1.0D;
    if (!skill.isIgnoreResists())
    {
      debuffMod = 1.0D - target.calcStat(Stats.DEBUFF_RESIST, caster, skill) / 120.0D;

      if (debuffMod != 1.0D)
      {
        if (debuffMod == (-1.0D / 0.0D))
        {
          if (debugGlobal)
          {
            if (debugCaster)
              caster.getPlayer().sendMessage("Full debuff immunity");
            if (debugTarget)
              target.getPlayer().sendMessage("Full debuff immunity");
          }
          return false;
        }
        if (debuffMod == (1.0D / 0.0D))
        {
          if (debugGlobal)
          {
            if (debugCaster)
              caster.getPlayer().sendMessage("Full debuff vulnerability");
            if (debugTarget)
              target.getPlayer().sendMessage("Full debuff vulnerability");
          }
          return true;
        }

        debuffMod = Math.max(debuffMod, 0.0D);
        value *= debuffMod;
      }

      SkillTrait trait = skill.getTraitType();
      if (trait != null)
      {
        vulnMod = trait.calcVuln(env);
        profMod = trait.calcProf(env);

        double maxResist = 90.0D + profMod * 0.85D;
        resMod = (maxResist - vulnMod) / 60.0D;
      }

      if (resMod != 1.0D)
      {
        if (resMod == (-1.0D / 0.0D))
        {
          if (debugGlobal)
          {
            if (debugCaster)
              caster.getPlayer().sendMessage("Full immunity");
            if (debugTarget)
              target.getPlayer().sendMessage("Full immunity");
          }
          return false;
        }
        if (resMod == (1.0D / 0.0D))
        {
          if (debugGlobal)
          {
            if (debugCaster)
              caster.getPlayer().sendMessage("Full vulnerability");
            if (debugTarget)
              target.getPlayer().sendMessage("Full vulnerability");
          }
          return true;
        }

        resMod = Math.max(resMod, 0.0D);
        value *= resMod;
      }
    }

    double elementMod = 0.0D;
    Element element = skill.getElement();
    if (element != Element.NONE)
    {
      elementMod = skill.getElementPower();
      Element attackElement = getAttackElement(caster, target);
      if (attackElement == element) {
        elementMod += caster.calcStat(element.getAttack(), 0.0D);
      }
      elementMod -= target.calcStat(element.getDefence(), 0.0D);

      elementMod = Math.round(elementMod / 10.0D);

      value += elementMod;
    }

    value = Math.max(value, Math.min(base, Config.SKILLS_CHANCE_MIN));
    value = Math.max(Math.min(value, Config.SKILLS_CHANCE_CAP), 1.0D);
    boolean result = Rnd.chance((int)value);

    if (debugGlobal)
    {
      StringBuilder stat = new StringBuilder(100);
      if (et == null)
        stat.append(skill.getName());
      else
        stat.append(et._effectType.name());
      stat.append(" AR:");
      stat.append((int)base);
      stat.append(" ");
      if (skill.getSaveVs() != null)
      {
        stat.append(skill.getSaveVs().name());
        stat.append(":");
        stat.append(String.format("%1.1f", new Object[] { Double.valueOf(statMod) }));
      }
      if (skill.isMagic())
      {
        stat.append(" ");
        stat.append(" mAtk:");
        stat.append(String.format("%1.1f", new Object[] { Double.valueOf(mAtkMod) }));
      }
      if (skill.getTraitType() != null)
      {
        stat.append(" ");
        stat.append(skill.getTraitType().name());
      }
      stat.append(" ");
      stat.append(String.format("%1.1f", new Object[] { Double.valueOf(resMod) }));
      stat.append("(");
      stat.append(String.format("%1.1f", new Object[] { Double.valueOf(profMod) }));
      stat.append("/");
      stat.append(String.format("%1.1f", new Object[] { Double.valueOf(vulnMod) }));
      if (debuffMod != 0.0D)
      {
        stat.append("+");
        stat.append(String.format("%1.1f", new Object[] { Double.valueOf(debuffMod) }));
      }
      stat.append(") lvl:");
      stat.append(String.format("%1.1f", new Object[] { Double.valueOf(lvlDependMod) }));
      stat.append(" elem:");
      stat.append((int)elementMod);
      stat.append(" Chance:");
      stat.append(String.format("%1.1f", new Object[] { Double.valueOf(value) }));
      if (!result) {
        stat.append(" failed");
      }

      if (debugCaster)
        caster.getPlayer().sendMessage(stat.toString());
      if (debugTarget)
        target.getPlayer().sendMessage(stat.toString());
    }
    return result;
  }

  public static boolean calcSkillSuccess(Creature player, Creature target, Skill skill, int activateRate)
  {
    Env env = new Env();
    env.character = player;
    env.target = target;
    env.skill = skill;
    env.value = activateRate;
    return calcSkillSuccess(env, null, player.getChargedSpiritShot());
  }

  public static void calcSkillMastery(Skill skill, Creature activeChar)
  {
    if (skill.isHandler()) {
      return;
    }

    if (((activeChar.getSkillLevel(Integer.valueOf(331)) > 0) && (activeChar.calcStat(Stats.SKILL_MASTERY, activeChar.getINT(), null, skill) >= Rnd.get(1000))) || ((activeChar.getSkillLevel(Integer.valueOf(330)) > 0) && (activeChar.calcStat(Stats.SKILL_MASTERY, activeChar.getSTR(), null, skill) >= Rnd.get(1000))))
    {
      Skill.SkillType type = skill.getSkillType();
      int masteryLevel;
      int masteryLevel;
      if ((skill.isMusic()) || (type == Skill.SkillType.BUFF) || (type == Skill.SkillType.HOT) || (type == Skill.SkillType.HEAL_PERCENT)) {
        masteryLevel = 2;
      }
      else
      {
        int masteryLevel;
        if (type == Skill.SkillType.HEAL)
          masteryLevel = 3;
        else
          masteryLevel = 1; 
      }
      if (masteryLevel > 0)
        activeChar.setSkillMastery(Integer.valueOf(skill.getId()), masteryLevel);
    }
  }

  public static double calcDamageResists(Skill skill, Creature attacker, Creature defender, double value)
  {
    if (attacker == defender) {
      return value;
    }
    if (attacker.isBoss())
      value *= Config.RATE_EPIC_ATTACK;
    else if ((attacker.isRaid()) || ((attacker instanceof ReflectionBossInstance))) {
      value *= Config.RATE_RAID_ATTACK;
    }
    if (defender.isBoss())
      value /= Config.RATE_EPIC_DEFENSE;
    else if ((defender.isRaid()) || ((defender instanceof ReflectionBossInstance))) {
      value /= Config.RATE_RAID_DEFENSE;
    }
    Player pAttacker = attacker.getPlayer();

    int diff = defender.getLevel() - (pAttacker != null ? pAttacker.getLevel() : attacker.getLevel());
    if ((attacker.isPlayable()) && (defender.isMonster()) && (defender.getLevel() >= 78) && (diff > 2)) {
      value *= 0.7D / Math.pow(diff - 2, 0.25D);
    }
    Element element = Element.NONE;
    double power = 0.0D;

    if (skill != null)
    {
      element = skill.getElement();
      power = skill.getElementPower();
    }
    else
    {
      element = getAttackElement(attacker, defender);
    }
    if (element == Element.NONE) {
      return value;
    }
    if ((pAttacker != null) && (pAttacker.isGM()) && (Config.DEBUG))
    {
      pAttacker.sendMessage(new StringBuilder().append("Element: ").append(element.name()).toString());
      pAttacker.sendMessage(new StringBuilder().append("Attack: ").append(attacker.calcStat(element.getAttack(), power)).toString());
      pAttacker.sendMessage(new StringBuilder().append("Defence: ").append(defender.calcStat(element.getDefence(), 0.0D)).toString());
      pAttacker.sendMessage(new StringBuilder().append("Modifier: ").append(getElementMod(defender.calcStat(element.getDefence(), 0.0D), attacker.calcStat(element.getAttack(), power))).toString());
    }

    return value * getElementMod(defender.calcStat(element.getDefence(), 0.0D), attacker.calcStat(element.getAttack(), power));
  }

  private static double getElementMod(double defense, double attack)
  {
    double diff = attack - defense;
    if (diff <= 0.0D)
      return 1.0D;
    if (diff < 50.0D)
      return 1.0D + diff * 0.003948D;
    if (diff < 150.0D)
      return 1.2D;
    if (diff < 300.0D) {
      return 1.4D;
    }
    return 1.7D;
  }

  public static Element getAttackElement(Creature attacker, Creature target)
  {
    double max = 4.9E-324D;
    Element result = Element.NONE;
    for (Element e : Element.VALUES)
    {
      double val = attacker.calcStat(e.getAttack(), 0.0D, null, null);
      if (val <= 0.0D) {
        continue;
      }
      if (target != null) {
        val -= target.calcStat(e.getDefence(), 0.0D, null, null);
      }
      if (val <= max)
        continue;
      result = e;
      max = val;
    }

    return result;
  }

  public static class AttackInfo
  {
    public double damage = 0.0D;
    public double defence = 0.0D;
    public double crit_static = 0.0D;
    public double death_rcpt = 0.0D;
    public double lethal1 = 0.0D;
    public double lethal2 = 0.0D;
    public double lethal_dmg = 0.0D;
    public boolean crit = false;
    public boolean shld = false;
    public boolean lethal = false;
    public boolean miss = false;
  }
}