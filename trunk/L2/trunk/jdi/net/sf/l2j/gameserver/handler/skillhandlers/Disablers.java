package net.sf.l2j.gameserver.handler.skillhandlers;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAI;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.instancemanager.DuelManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2CubicInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeSummonInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.util.Rnd;

public class Disablers
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.STUN, L2Skill.SkillType.ROOT, L2Skill.SkillType.SLEEP, L2Skill.SkillType.CONFUSION, L2Skill.SkillType.AGGDAMAGE, L2Skill.SkillType.AGGREDUCE, L2Skill.SkillType.AGGREDUCE_CHAR, L2Skill.SkillType.AGGREMOVE, L2Skill.SkillType.UNBLEED, L2Skill.SkillType.UNPOISON, L2Skill.SkillType.MUTE, L2Skill.SkillType.FAKE_DEATH, L2Skill.SkillType.CONFUSE_MOB_ONLY, L2Skill.SkillType.NEGATE, L2Skill.SkillType.CANCEL, L2Skill.SkillType.PARALYZE, L2Skill.SkillType.ERASE, L2Skill.SkillType.MAGE_BANE, L2Skill.SkillType.WARRIOR_BANE, L2Skill.SkillType.BETRAY };

  protected static final Logger _log = Logger.getLogger(L2Skill.class.getName());
  private String[] _negateStats = null;
  private float _negatePower = 0.0F;
  private int _negateId = 0;

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    L2Skill.SkillType type = skill.getSkillType();

    boolean ss = false;
    boolean sps = false;
    boolean bss = false;

    L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

    if ((activeChar instanceof L2PcInstance))
    {
      if ((weaponInst == null) && (skill.isOffensive()))
      {
        SystemMessage sm2 = new SystemMessage(SystemMessageId.S1_S2);
        sm2.addString("You must equip a weapon before casting a spell.");
        activeChar.sendPacket(sm2);
        return;
      }
    }

    if (weaponInst != null)
    {
      if (skill.isMagic())
      {
        if (weaponInst.getChargedSpiritshot() == 2)
        {
          bss = true;
          if (skill.getId() != 1020)
            weaponInst.setChargedSpiritshot(0);
        }
        else if (weaponInst.getChargedSpiritshot() == 1)
        {
          sps = true;
          if (skill.getId() != 1020) {
            weaponInst.setChargedSpiritshot(0);
          }
        }
      }
      else if (weaponInst.getChargedSoulshot() == 1)
      {
        ss = true;
        if (skill.getId() != 1020) {
          weaponInst.setChargedSoulshot(0);
        }
      }
    }
    else if ((activeChar instanceof L2Summon))
    {
      L2Summon activeSummon = (L2Summon)activeChar;

      if (skill.isMagic())
      {
        if (activeSummon.getChargedSpiritShot() == 2)
        {
          bss = true;
          activeSummon.setChargedSpiritShot(0);
        }
        else if (activeSummon.getChargedSpiritShot() == 1)
        {
          sps = true;
          activeSummon.setChargedSpiritShot(0);
        }

      }
      else if (activeSummon.getChargedSoulShot() == 1)
      {
        ss = true;
        activeSummon.setChargedSoulShot(0);
      }
    }

    for (int index = 0; index < targets.length; index++)
    {
      if (!(targets[index] instanceof L2Character))
        continue;
      L2Character target = (L2Character)targets[index];

      if ((target == null) || (target.isDead())) {
        continue;
      }
      switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[type.ordinal()])
      {
      case 1:
        if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss)) {
          skill.getEffects(activeChar, target);
        }
        else {
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
          sm.addString(target.getName());
          sm.addSkillName(skill.getId());
          activeChar.sendPacket(sm);
        }
        break;
      case 2:
        skill.getEffects(activeChar, target);
        break;
      case 3:
      case 4:
        if (target.reflectSkill(skill)) {
          target = activeChar;
        }
        if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss)) {
          skill.getEffects(activeChar, target);
        }
        else {
          if (!(activeChar instanceof L2PcInstance))
            break;
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
          sm.addString(target.getName());
          sm.addSkillName(skill.getDisplayId());
          activeChar.sendPacket(sm);
        }break;
      case 5:
      case 6:
        if (target.reflectSkill(skill)) {
          target = activeChar;
        }
        if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss)) {
          skill.getEffects(activeChar, target);
        }
        else {
          if (!(activeChar instanceof L2PcInstance))
            break;
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
          sm.addString(target.getName());
          sm.addSkillName(skill.getDisplayId());
          activeChar.sendPacket(sm);
        }break;
      case 7:
      case 8:
        if (target.reflectSkill(skill)) {
          target = activeChar;
        }
        if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
        {
          L2Effect[] effects = target.getAllEffects();
          for (L2Effect e : effects) {
            if (e.getSkill().getSkillType() == type) {
              e.exit();
            }
          }
          if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
          {
            skill.getEffects(activeChar, target);
          }
          else if ((activeChar instanceof L2PcInstance))
          {
            SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
            sm.addString(target.getName());
            sm.addSkillName(skill.getDisplayId());
            activeChar.sendPacket(sm);
          }

        }
        else
        {
          if (!(activeChar instanceof L2PcInstance))
            break;
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
          sm.addString(target.getName());
          sm.addSkillName(skill.getDisplayId());
          activeChar.sendPacket(sm);
        }break;
      case 9:
        if ((target instanceof L2Attackable))
          skill.getEffects(activeChar, target);
        else
          activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
        break;
      case 10:
        if ((target instanceof L2Attackable)) {
          target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, Integer.valueOf((int)(150.0D * skill.getPower() / (target.getLevel() + 7))));
        }
        skill.getEffects(activeChar, target);
        break;
      case 11:
        if (!(target instanceof L2Attackable))
          break;
        skill.getEffects(activeChar, target);

        double aggdiff = ((L2Attackable)target).getHating(activeChar) - target.calcStat(Stats.AGGRESSION, ((L2Attackable)target).getHating(activeChar), target, skill);

        if (skill.getPower() > 0.0D)
          ((L2Attackable)target).reduceHate(null, (int)skill.getPower());
        else if (aggdiff > 0.0D)
          ((L2Attackable)target).reduceHate(null, (int)aggdiff);
        break;
      case 12:
        if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
        {
          if ((target instanceof L2Attackable))
          {
            L2Attackable targ = (L2Attackable)target;
            targ.stopHating(activeChar);
            if (targ.getMostHated() == null)
            {
              ((L2AttackableAI)targ.getAI()).setGlobalAggro(-25);
              targ.clearAggroList();
              targ.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
              targ.setWalking();
            }
          }
          skill.getEffects(activeChar, target);
        }
        else
        {
          if (!(activeChar instanceof L2PcInstance))
            break;
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
          sm.addString(target.getName());
          sm.addSkillName(skill.getId());
          activeChar.sendPacket(sm);
        }break;
      case 13:
        if ((!(target instanceof L2Attackable)) || (target.isRaid()))
          break;
        if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
        {
          if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_UNDEAD)
          {
            if (!target.isUndead()) break;
            ((L2Attackable)target).reduceHate(null, ((L2Attackable)target).getHating(((L2Attackable)target).getMostHated()));
          }
          else {
            ((L2Attackable)target).reduceHate(null, ((L2Attackable)target).getHating(((L2Attackable)target).getMostHated()));
          }
        }
        else {
          if (!(activeChar instanceof L2PcInstance))
            break;
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
          sm.addString(target.getName());
          sm.addSkillName(skill.getId());
          activeChar.sendPacket(sm);
        }break;
      case 14:
        negateEffect(target, L2Skill.SkillType.BLEED, skill.getPower());
        break;
      case 15:
        negateEffect(target, L2Skill.SkillType.POISON, skill.getPower());
        break;
      case 16:
        if ((Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss)) && (!(target instanceof L2SiegeSummonInstance)))
        {
          L2PcInstance summonOwner = null;
          L2Summon summonPet = null;
          summonOwner = ((L2Summon)target).getOwner();
          summonPet = summonOwner.getPet();
          summonPet.unSummon(summonOwner);
          SystemMessage sm = new SystemMessage(SystemMessageId.LETHAL_STRIKE);
          summonOwner.sendPacket(sm);
        }
        else
        {
          if (!(activeChar instanceof L2PcInstance))
            break;
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
          sm.addString(target.getName());
          sm.addSkillName(skill.getId());
          activeChar.sendPacket(sm);
        }break;
      case 17:
        if (target.reflectSkill(skill)) {
          target = activeChar;
        }
        if (!Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
        {
          if (!(activeChar instanceof L2PcInstance))
            continue;
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
          sm.addString(target.getName());
          sm.addSkillName(skill.getId());
          activeChar.sendPacket(sm);
          continue;
        }

        L2Effect[] effects = target.getAllEffects();
        for (L2Effect e : effects)
        {
          if (e == null)
            continue;
          if ((e.getSkill().getId() == 1085) || (e.getSkill().getId() == 1059)) {
            target.stopSkillEffects(e.getSkill().getId());
          }
        }
        break;
      case 18:
        if (target.reflectSkill(skill)) {
          target = activeChar;
        }
        if (!Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
        {
          if (!(activeChar instanceof L2PcInstance))
            continue;
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
          sm.addString(target.getName());
          sm.addSkillName(skill.getId());
          activeChar.sendPacket(sm);
          continue;
        }

        L2Effect[] effects = target.getAllEffects();
        for (L2Effect e : effects)
        {
          if (e == null)
            continue;
          if ((e.getSkill().getId() == 1204) || (e.getSkill().getId() == 1086)) {
            target.stopSkillEffects(e.getSkill().getId());
          }
        }
        break;
      case 19:
        if (target.reflectSkill(skill)) {
          target = activeChar;
        }

        if (skill.getId() == 1056)
        {
          L2Effect[] effects = target.getAllEffects();

          double totalCancelVuln = 1.0D;
          for (L2Effect e : effects)
          {
            switch (e.getSkill().getId())
            {
            case 396:
              return;
            case 110:
              totalCancelVuln *= 0.9D;
              break;
            case 111:
              totalCancelVuln *= 0.9D;
              break;
            case 287:
              totalCancelVuln *= 0.9D;
              break;
            case 341:
              totalCancelVuln *= 0.7D;
              break;
            case 368:
              totalCancelVuln *= 0.9D;
              break;
            case 395:
              totalCancelVuln *= 0.9D;
              break;
            case 1338:
              totalCancelVuln *= 1.3D;
              break;
            case 1354:
              totalCancelVuln *= 0.7D;
              break;
            case 1362:
              totalCancelVuln *= 0.7D;
              break;
            case 1415:
              totalCancelVuln *= 0.7D;
              break;
            case 5125:
              totalCancelVuln *= 0.8D;
              break;
            case 5145:
              totalCancelVuln *= 0.1D;
            }
          }

          int maxfive = 5;
          int DeffTime = Config.SKILL_DURATION_TIME;
          int level = skill.getMagicLevel();
          double power = skill.getPower();
          for (L2Effect e : effects)
          {
            switch (e.getEffectType())
            {
            case SIGNET_GROUND:
            case SIGNET_EFFECT:
              break;
            default:
              if ((e.getSkill().getId() == 4082) || (e.getSkill().getId() == 4215) || (e.getSkill().getId() == 4515) || (e.getSkill().getId() == 110) || (e.getSkill().getId() == 111) || (e.getSkill().getId() == 1323) || (e.getSkill().getId() == 1325))
              {
                continue;
              }
              if (e.getSkill().getSkillType() != L2Skill.SkillType.BUFF) {
                e.exit();
              }
              else {
                double prelim_chance = 2 * (level - e.getSkill().getMagicLevel()) + power + e.getPeriod() / (120 * DeffTime);
                prelim_chance *= totalCancelVuln;
                if (prelim_chance < 5.0D) prelim_chance = 5.0D;
                if (prelim_chance > 95.0D) prelim_chance = 95.0D;

                if (Rnd.get(100) >= prelim_chance)
                  continue;
                if (Config.DEBUG)
                  System.out.println("skill: " + e.getSkill().getName() + " lvl: " + e.getSkill().getMagicLevel() + " time " + e.getPeriod() / DeffTime + " prelim_chance: " + prelim_chance + " totalCancelVuln: " + totalCancelVuln);
                e.exit();
                maxfive--;
                if (maxfive == 0)
                {
                  break label2580;
                }
              }
            }
          }
        }
        else
        {
          int landrate = (int)skill.getPower();
          landrate = (int)target.calcStat(Stats.CANCEL_VULN, landrate, target, null);
          if (Rnd.get(100) < landrate)
          {
            L2Effect[] effects = target.getAllEffects();
            int maxdisp = (int)skill.getNegatePower();
            if (maxdisp == 0)
              maxdisp = Config.BUFFS_MAX_AMOUNT + Config.DEBUFFS_MAX_AMOUNT + 6;
            int exitDisp = Rnd.nextInt(maxdisp);
            if (exitDisp == 0)
              exitDisp = 1;
            for (L2Effect e : effects)
            {
              switch (e.getEffectType())
              {
              case SIGNET_GROUND:
              case SIGNET_EFFECT:
                break;
              default:
                if ((e.getSkill().getId() == 4082) || (e.getSkill().getId() == 4215) || (e.getSkill().getId() == 5182) || (e.getSkill().getId() == 4515) || (e.getSkill().getId() == 110) || (e.getSkill().getId() == 111) || (e.getSkill().getId() == 1323) || (e.getSkill().getId() == 1325))
                  continue;
                if (e.getSkill().getSkillType() != L2Skill.SkillType.BUFF)
                  continue;
                if (exitDisp <= 0)
                  continue;
                e.exit();
                exitDisp--;
                if (exitDisp == 0) {
                  break label2882;
                }
              }
            }
          }
          else
          {
            if (!(activeChar instanceof L2PcInstance))
              break;
            SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
            sm.addString(target.getName());
            sm.addSkillName(skill.getDisplayId());
            activeChar.sendPacket(sm);
            sm = null;
          }
        }break;
      case 20:
        label2580: label2882: if (skill.getId() == 2275) {
          _negatePower = skill.getNegatePower();
          _negateId = skill.getNegateId();

          negateEffect(target, L2Skill.SkillType.BUFF, _negatePower, _negateId);
        }
        else
        {
          _negateStats = skill.getNegateStats();
          _negatePower = skill.getNegatePower();

          for (String stat : _negateStats)
          {
            stat = stat.toLowerCase().intern();
            if (stat == "buff")
            {
              int lvlmodifier = 52 + skill.getMagicLevel() * 2;
              if (skill.getMagicLevel() == 12) lvlmodifier = 80;
              double landrate = skill.getPower();
              if (target.getLevel() - lvlmodifier > 0) landrate = skill.getPower() - 4 * (target.getLevel() - lvlmodifier);

              landrate = (int)activeChar.calcStat(Stats.CANCEL_VULN, landrate, target, null);

              if (Rnd.get(100) < landrate)
                negateEffect(target, L2Skill.SkillType.BUFF, -1.0D);
            }
            if (stat == "debuff") negateEffect(target, L2Skill.SkillType.DEBUFF, -1.0D);
            if (stat == "weakness") negateEffect(target, L2Skill.SkillType.WEAKNESS, -1.0D);
            if (stat == "stun") negateEffect(target, L2Skill.SkillType.STUN, -1.0D);
            if (stat == "root") negateEffect(target, L2Skill.SkillType.ROOT, -1.0D);
            if (stat == "sleep") negateEffect(target, L2Skill.SkillType.SLEEP, -1.0D);
            if (stat == "confusion") negateEffect(target, L2Skill.SkillType.CONFUSION, -1.0D);
            if (stat == "mute") negateEffect(target, L2Skill.SkillType.MUTE, -1.0D);
            if (stat == "fear") negateEffect(target, L2Skill.SkillType.FEAR, -1.0D);
            if (stat == "poison") negateEffect(target, L2Skill.SkillType.POISON, _negatePower);
            if (stat == "bleed") negateEffect(target, L2Skill.SkillType.BLEED, _negatePower);
            if (stat == "paralyze") negateEffect(target, L2Skill.SkillType.PARALYZE, -1.0D);
            if (stat != "heal")
              continue;
            ISkillHandler Healhandler = SkillHandler.getInstance().getSkillHandler(L2Skill.SkillType.HEAL);
            if (Healhandler == null)
            {
              _log.severe("Couldn't find skill handler for HEAL.");
            }
            else {
              L2Object[] tgts = { target };
              try {
                Healhandler.useSkill(activeChar, skill, tgts);
              } catch (IOException e) {
                _log.log(Level.WARNING, "", e);
              }
            }
          }
        }
      }

      Formulas.getInstance().calcLethalHit(activeChar, target, skill);
    }

    L2Effect effect = activeChar.getFirstEffect(skill.getId());
    if ((effect != null) && (effect.isSelfEffect()))
    {
      effect.exit();
    }
    skill.getEffectsSelf(activeChar);
  }

  private void negateEffect(L2Character target, L2Skill.SkillType type, double power)
  {
    negateEffect(target, type, power, 0);
  }

  private void negateEffect(L2Character target, L2Skill.SkillType type, double power, int skillId) {
    L2Effect[] effects = target.getAllEffects();
    for (L2Effect e : effects)
      if (power == -1.0D)
      {
        if ((e.getSkill().getSkillType() == type) || ((e.getSkill().getEffectType() != null) && (e.getSkill().getEffectType() == type)))
          if (skillId != 0)
          {
            if (skillId == e.getSkill().getId())
              e.exit();
          }
          else
            e.exit();
      }
      else {
        if (((e.getSkill().getSkillType() != type) || (e.getSkill().getPower() > power)) && ((e.getSkill().getEffectType() == null) || (e.getSkill().getEffectType() != type) || (e.getSkill().getEffectLvl() > power)))
          continue;
        if (skillId != 0)
        {
          if (skillId == e.getSkill().getId())
            e.exit();
        }
        else
          e.exit();
      }
  }

  public void useCubicSkill(L2CubicInstance activeCubic, L2Skill skill, L2Object[] targets) {
    L2Skill.SkillType type = skill.getSkillType();

    for (int index = 0; index < targets.length; index++)
    {
      if (!(targets[index] instanceof L2Character))
        continue;
      L2Character target = (L2Character)targets[index];

      if ((target == null) || (target.isDead())) {
        continue;
      }
      switch (type)
      {
      case STUN:
        if (Formulas.getInstance().calcCubicSkillSuccess(activeCubic, target, skill))
        {
          if (((target instanceof L2PcInstance)) && (((L2PcInstance)target).isInDuel()) && (skill.getSkillType() == L2Skill.SkillType.DEBUFF) && (activeCubic.getOwner().getDuelId() == ((L2PcInstance)target).getDuelId()))
          {
            DuelManager dm = DuelManager.getInstance();
            for (L2Effect debuff : skill.getEffects(activeCubic.getOwner(), target)) {
              if (debuff == null) continue; dm.onBuff((L2PcInstance)target, debuff);
            }
          } else {
            skill.getEffects(activeCubic, target);
          }
        }
        else {
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
          sm.addString(target.getName());
          sm.addSkillName(skill.getId());
          activeCubic.getOwner().sendPacket(sm);
        }
        break;
      case PARALYZE:
        if (Formulas.getInstance().calcCubicSkillSuccess(activeCubic, target, skill))
        {
          if (((target instanceof L2PcInstance)) && (((L2PcInstance)target).isInDuel()) && (skill.getSkillType() == L2Skill.SkillType.DEBUFF) && (activeCubic.getOwner().getDuelId() == ((L2PcInstance)target).getDuelId()))
          {
            DuelManager dm = DuelManager.getInstance();
            for (L2Effect debuff : skill.getEffects(activeCubic.getOwner(), target)) {
              if (debuff == null) continue; dm.onBuff((L2PcInstance)target, debuff);
            }
          } else {
            skill.getEffects(activeCubic, target);
          }
        }
        else {
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
          sm.addString(target.getName());
          sm.addSkillName(skill.getId());
          activeCubic.getOwner().sendPacket(sm);
        }
        break;
      case CANCEL_DEBUFF:
        L2Effect[] effects = target.getAllEffects();

        if ((effects == null) || (effects.length == 0))
          continue;
        int count = skill.getMaxNegatedEffects() > 0 ? 0 : -2;
        for (L2Effect e : effects)
        {
          if ((e.getSkill().getSkillType() != L2Skill.SkillType.DEBUFF) || (count >= skill.getMaxNegatedEffects()))
            continue;
          e.exit();
          if (count > -1)
            count++;
        }
      }
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}