package scripts.skills.skillhandlers;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.SkillTable;
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
import net.sf.l2j.gameserver.model.actor.instance.L2BabyPetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2ChestInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeFlagInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeSummonInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2TamedBeastInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.util.Rnd;
import scripts.skills.ISkillHandler;
import scripts.skills.SkillHandler;

public class Disablers
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.STUN, L2Skill.SkillType.ROOT, L2Skill.SkillType.SLEEP, L2Skill.SkillType.FEAR, L2Skill.SkillType.CONFUSION, L2Skill.SkillType.AGGDAMAGE, L2Skill.SkillType.AGGREDUCE, L2Skill.SkillType.AGGREDUCE_CHAR, L2Skill.SkillType.AGGREMOVE, L2Skill.SkillType.UNBLEED, L2Skill.SkillType.UNPOISON, L2Skill.SkillType.MUTE, L2Skill.SkillType.FAKE_DEATH, L2Skill.SkillType.CONFUSE_MOB_ONLY, L2Skill.SkillType.NEGATE, L2Skill.SkillType.CANCEL, L2Skill.SkillType.PARALYZE, L2Skill.SkillType.ERASE, L2Skill.SkillType.MAGE_BANE, L2Skill.SkillType.WARRIOR_BANE, L2Skill.SkillType.BETRAY, L2Skill.SkillType.HOLD_UNDEAD, L2Skill.SkillType.TURN_UNDEAD };

  protected static final Logger _log = Logger.getLogger(L2Skill.class.getName());
  private String[] _negateStats = null;
  private float _negatePower = 0.0F;
  private int _negateId = 0;

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    L2Skill.SkillType type = skill.getSkillType();
    int SkillId = skill.getId();

    boolean ss = false;
    boolean sps = false;
    boolean bss = false;

    L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

    if (weaponInst != null) {
      if (skill.isMagic()) {
        if (weaponInst.getChargedSpiritshot() == 2) {
          bss = true;
          if (skill.getId() != 1020)
          {
            weaponInst.setChargedSpiritshot(0);
          }
        } else if (weaponInst.getChargedSpiritshot() == 1) {
          sps = true;
          if (skill.getId() != 1020)
          {
            weaponInst.setChargedSpiritshot(0);
          }
        }
      } else if (weaponInst.getChargedSoulshot() == 1) {
        ss = true;
        if (skill.getId() != 1020)
        {
          weaponInst.setChargedSoulshot(0);
        }
      }
    }
    else if ((activeChar instanceof L2Summon)) {
      L2Summon activeSummon = (L2Summon)activeChar;

      if (skill.isMagic()) {
        if (activeSummon.getChargedSpiritShot() == 2) {
          bss = true;
          activeSummon.setChargedSpiritShot(0);
        } else if (activeSummon.getChargedSpiritShot() == 1) {
          sps = true;
          activeSummon.setChargedSpiritShot(0);
        }
      } else if (activeSummon.getChargedSoulShot() == 1) {
        ss = true;
        activeSummon.setChargedSoulShot(0);
      }
    }

    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; ) {
      L2Object obj = (L2Object)n.getValue();

      if ((obj == null) || (!obj.isL2Character()))
      {
        continue;
      }
      L2Character target = (L2Character)obj;

      if ((target == null) || (target.isDead()) || 
        (target.isRaid()))
        continue;
      L2Character undead;
      FastList.Node k;
      switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[type.ordinal()]) {
      case 1:
        if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
          skill.getEffects(activeChar, target);
        else {
          unAffected(activeChar, target.getName(), skill.getId());
        }
        break;
      case 2:
        skill.getEffects(activeChar, target);
        break;
      case 3:
        if (target.reflectSkill(skill)) {
          target = activeChar;
        }

        if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
          skill.getEffects(activeChar, target);
        else {
          unAffected(activeChar, target.getName(), skill.getId());
        }
        break;
      case 4:
        if (target.reflectSkill(skill)) {
          target = activeChar;
        }

        if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
          skill.getEffects(activeChar, target);
        else {
          unAffected(activeChar, target.getName(), skill.getId());
        }
        break;
      case 5:
        if (target.reflectSkill(skill)) {
          target = activeChar;
        }

        if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
          skill.getEffects(activeChar, target);
        else {
          unAffected(activeChar, target.getName(), skill.getId());
        }
        break;
      case 6:
        if (target.reflectSkill(skill)) {
          target = activeChar;
        }

        boolean wrong = false;

        if (target.isL2Folk()) {
          wrong = true;
        }
        if (target.isL2SiegeGuard()) {
          wrong = true;
        }

        if ((target instanceof L2SiegeFlagInstance)) {
          wrong = true;
        }
        if ((target instanceof L2SiegeSummonInstance)) {
          wrong = true;
        }
        if ((target.isL2Npc()) && (!target.isMonster())) {
          wrong = true;
        }

        if ((!wrong) && (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss)))
          skill.getEffects(activeChar, target);
        else {
          unAffected(activeChar, target.getName(), skill.getId());
        }
        break;
      case 7:
        if (target.reflectSkill(skill)) {
          target = activeChar;
        }

        if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
          skill.getEffects(activeChar, target);
        else {
          unAffected(activeChar, target.getName(), skill.getId());
        }
        break;
      case 8:
      case 9:
        if (target.reflectSkill(skill)) {
          target = activeChar;
        }

        if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
          skill.getEffects(activeChar, target);
        else {
          unAffected(activeChar, target.getName(), skill.getId());
        }
        break;
      case 10:
        if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss)) {
          L2Effect[] effects = target.getAllEffects();
          for (L2Effect e : effects) {
            if (e.getSkill().getSkillType() == type) {
              e.exit();
            }
          }
          skill.getEffects(activeChar, target);
        } else {
          unAffected(activeChar, target.getName(), skill.getId());
        }
        break;
      case 11:
        if (Rnd.get(100) < 60) {
          if (target.getTarget() != activeChar) {
            target.setTarget(activeChar);
            target.sendPacket(new MyTargetSelected(activeChar.getObjectId(), 0));
            target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, Integer.valueOf((int)(150.0D * skill.getPower() / (target.getLevel() + 7))));
          } else {
            target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
          }

        }
        else
        {
          unAffected(activeChar, target.getName(), skill.getId());
        }

        break;
      case 12:
        if (target.isL2Attackable()) {
          skill.getEffects(activeChar, target);

          double aggdiff = ((L2Attackable)target).getHating(activeChar) - target.calcStat(Stats.AGGRESSION, ((L2Attackable)target).getHating(activeChar), target, skill);

          if (skill.getPower() > 0.0D)
            ((L2Attackable)target).reduceHate(null, (int)skill.getPower());
          else if (aggdiff > 0.0D)
            ((L2Attackable)target).reduceHate(null, (int)aggdiff);
        }
        break;
      case 13:
        switch (SkillId) {
        case 11:
          if ((!target.isPlayer()) && (!target.isL2Monster()) && (!(target instanceof L2SummonInstance))) break;
          if (Rnd.get(100) < 80) {
            target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
            target.setTarget(null);
            target.abortAttack();
            target.abortCast();
          } else {
            unAffected(activeChar, target.getName(), skill.getId()); } break;
        case 5144:
          if ((!target.isPlayer()) && (!target.isL2Monster())) break;
          target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
          target.setTarget(null);
          target.abortAttack();
          target.abortCast(); break;
        case 12:
          if (target.isPlayer()) {
            if (Rnd.get(100) < 80) {
              L2PcInstance xzkaknazvat = (L2PcInstance)target;

              target.abortCast();
              target.abortAttack();

              if (target.getPet() != null) {
                if ((target.getPet() instanceof L2SummonInstance)) {
                  L2SummonInstance switchtarg = (L2SummonInstance)target.getPet();
                  xzkaknazvat.setTarget(switchtarg);
                } else if (target.getPet().isPet()) {
                  L2PetInstance switchtarg = (L2PetInstance)target.getPet();
                  xzkaknazvat.setTarget(switchtarg);
                }
                return;
              }if (activeChar.getPet() != null) {
                if ((activeChar.getPet() instanceof L2SummonInstance)) {
                  L2SummonInstance switchtarg = (L2SummonInstance)activeChar.getPet();
                  xzkaknazvat.setTarget(switchtarg);
                } else if (activeChar.getPet().isPet()) {
                  L2PetInstance switchtarg = (L2PetInstance)activeChar.getPet();
                  xzkaknazvat.setTarget(switchtarg);
                }
                return;
              }

              Collection randomtargets = xzkaknazvat.getKnownList().getKnownCharactersInRadius(300);
              if ((randomtargets == null) || (randomtargets.isEmpty())) {
                return;
              }

              int switched = 0;
              for (L2Character toswitch : randomtargets) {
                if ((toswitch == activeChar) || 
                  (toswitch.isDead()))
                {
                  continue;
                }
                if (toswitch.isPlayer()) {
                  L2PcInstance switchtarg = (L2PcInstance)toswitch;
                  xzkaknazvat.setTarget(switchtarg);
                  switched++;
                } else if (toswitch.isL2Monster()) {
                  L2MonsterInstance switchtarg = (L2MonsterInstance)toswitch;
                  xzkaknazvat.setTarget(switchtarg);
                  switched++;
                } else if ((toswitch instanceof L2SummonInstance)) {
                  L2SummonInstance switchtarg = (L2SummonInstance)toswitch;
                  xzkaknazvat.setTarget(switchtarg);
                  switched++;
                } else if (toswitch.isPet()) {
                  L2PetInstance switchtarg = (L2PetInstance)toswitch;
                  xzkaknazvat.setTarget(switchtarg);
                  switched++;
                } else if ((toswitch instanceof L2BabyPetInstance)) {
                  L2BabyPetInstance switchtarg = (L2BabyPetInstance)toswitch;
                  xzkaknazvat.setTarget(switchtarg);
                  switched++;
                } else if ((toswitch instanceof L2TamedBeastInstance)) {
                  L2TamedBeastInstance switchtarg = (L2TamedBeastInstance)toswitch;
                  xzkaknazvat.setTarget(switchtarg);
                  switched++;
                } else if ((toswitch instanceof L2ChestInstance)) {
                  L2ChestInstance switchtarg = (L2ChestInstance)toswitch;
                  xzkaknazvat.setTarget(switchtarg);
                  switched++;
                } else if ((toswitch instanceof L2GuardInstance)) {
                  L2GuardInstance switchtarg = (L2GuardInstance)toswitch;
                  xzkaknazvat.setTarget(switchtarg);

                  switched++;
                }
                if (switched == 1) {
                  break;
                }
              }
              skill.getEffects(activeChar, target);
            } else {
              unAffected(activeChar, target.getName(), skill.getId());
              target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
            }
          } else {
            if (!target.isL2Monster()) break;
            if (Rnd.get(100) < 80) {
              target.setTarget(null);
              target.abortCast();
              target.abortAttack();
              target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
            } else {
              unAffected(activeChar, target.getName(), skill.getId());
              target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
            }

          }

        }

        break;
      case 14:
        if ((target.isL2Attackable()) && (!target.isRaid()))
          if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss)) {
            if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_UNDEAD) {
              if (target.isUndead())
                ((L2Attackable)target).reduceHate(null, ((L2Attackable)target).getHating(((L2Attackable)target).getMostHated()));
            }
            else
              ((L2Attackable)target).reduceHate(null, ((L2Attackable)target).getHating(((L2Attackable)target).getMostHated()));
          }
          else
            unAffected(activeChar, target.getName(), skill.getId()); 
        break;
      case 15:
        FastList objs = activeChar.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius());
        if ((objs != null) && (!objs.isEmpty()))
        {
          undead = null;
          k = objs.head(); for (FastList.Node kend = objs.tail(); (k = k.getNext()) != kend; ) {
            undead = (L2Character)k.getValue();
            if ((undead == null) || 
              (undead == activeChar) || 
              (undead.isAlikeDead()) || 
              (undead.isRaid()) || 
              (!undead.isUndead()) || 
              (!undead.isL2Attackable())) continue;
            ((L2Attackable)undead).reduceHate(null, ((L2Attackable)undead).getHating(((L2Attackable)undead).getMostHated()));
          }

        }

        break;
      case 16:
        if ((target.isAlikeDead()) || 
          (target.isRaid()) || 
          (!target.isUndead()))
        {
          continue;
        }
        if (Rnd.get(100) < 70) {
          SkillTable.getInstance().getInfo(1092, 19).getEffects(target, target);
          if (Rnd.get(100) < 50) {
            target.setCurrentHp(1.0D);
            activeChar.sendPacket(Static.LETHAL_STRIKE);
          }
        } else {
          unAffected(activeChar, target.getName(), skill.getId());
        }
        break;
      case 17:
        negateEffect(target, L2Skill.SkillType.BLEED, skill.getPower());
        break;
      case 18:
        negateEffect(target, L2Skill.SkillType.POISON, skill.getPower());
        break;
      case 19:
        if (((target instanceof L2SummonInstance)) || (target.isPet()))
          if (Rnd.get(100) < 43) {
            L2PcInstance summonOwner = null;
            L2Summon summonPet = null;
            summonOwner = ((L2Summon)target).getOwner();
            summonPet = summonOwner.getPet();
            summonPet.unSummon(summonOwner);
            activeChar.sendPacket(Static.LETHAL_STRIKE);
          } else {
            unAffected(activeChar, target.getName(), skill.getId());
          } break;
      case 20:
        if (target.reflectSkill(skill)) {
          target = activeChar;
        }

        if (!Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss)) {
          unAffected(activeChar, target.getName(), skill.getId());
          continue;
        }

        FastTable effects = target.getAllEffectsTable();
        int a = 0; for (int z = effects.size(); a < z; a++) {
          L2Effect e = (L2Effect)effects.get(a);
          if (e == null)
          {
            continue;
          }
          switch (e.getSkill().getId()) {
          case 1002:
          case 1004:
          case 1059:
          case 1085:
          case 2053:
          case 2056:
            e.exit();
          }
        }

        break;
      case 21:
        if (target.reflectSkill(skill)) {
          target = activeChar;
        }

        if (!Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss)) {
          unAffected(activeChar, target.getName(), skill.getId());
          continue;
        }

        FastTable effects = target.getAllEffectsTable();
        int s = 0; for (int x = effects.size(); s < x; s++) {
          L2Effect e = (L2Effect)effects.get(s);
          if (e == null)
          {
            continue;
          }
          switch (e.getSkill().getId()) {
          case 1086:
          case 1204:
          case 1251:
          case 1282:
          case 2054:
          case 2058:
            e.exit();
          }
        }

        break;
      case 22:
      case 23:
        if (target.reflectSkill(skill)) {
          target = activeChar;
        }

        if (skill.getId() == 1056) {
          if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss)) {
            int max = Rnd.get(4, 6);
            int canceled = 0;
            int count = 0;
            int finish = target.getBuffCount();
            FastTable effects = target.getAllEffectsTable();
            int d = 0; for (int c = effects.size(); d < c; d++) {
              L2Effect e = (L2Effect)effects.get(d);
              if (e == null)
              {
                continue;
              }
              if (e.getSkill().isCancelProtected())
              {
                continue;
              }
              switch (e.getEffectType()) {
              case SIGNET_GROUND:
              case SIGNET_EFFECT:
                break;
              default:
                if (!e.getSkill().isBuff()) continue;
                if (Rnd.get(100) < finish) {
                  e.exit();
                  canceled++;
                }
                count++;
                if ((canceled >= max) || (count >= finish)) break label2998;
              }
            }
          }
          else {
            unAffected(activeChar, target.getName(), skill.getId());
          }

        }
        else if (skill.getId() == 2275) {
          _negatePower = skill.getNegatePower();
          _negateId = skill.getNegateId();

          negateEffect(target, L2Skill.SkillType.BUFF, _negatePower, _negateId);
        }
        else {
          _negateStats = skill.getNegateStats();
          _negatePower = skill.getNegatePower();

          for (String stat : _negateStats)
          {
            if ("buff".equalsIgnoreCase(stat)) {
              int lvlmodifier = 52 + skill.getMagicLevel() * 2;
              if (skill.getMagicLevel() == 12) {
                lvlmodifier = 80;
              }
              int landrate = 60;
              if (target.getLevel() - lvlmodifier > 0) {
                landrate = 60 - 4 * (target.getLevel() - lvlmodifier);
              }

              landrate = (int)activeChar.calcStat(Stats.CANCEL_VULN, landrate, target, null);

              if (Rnd.get(100) < landrate)
                negateEffect(target, L2Skill.SkillType.BUFF, -1.0D);
            }
            else if ("debuff".equalsIgnoreCase(stat)) {
              negateEffect(target, L2Skill.SkillType.DEBUFF, -1.0D);
            } else if ("weakness".equalsIgnoreCase(stat)) {
              negateEffect(target, L2Skill.SkillType.WEAKNESS, -1.0D);
            } else if ("stun".equalsIgnoreCase(stat)) {
              negateEffect(target, L2Skill.SkillType.STUN, -1.0D);
            } else if ("sleep".equalsIgnoreCase(stat)) {
              negateEffect(target, L2Skill.SkillType.SLEEP, -1.0D);
            } else if ("confusion".equalsIgnoreCase(stat)) {
              negateEffect(target, L2Skill.SkillType.CONFUSION, -1.0D);
            } else if ("mute".equalsIgnoreCase(stat)) {
              negateEffect(target, L2Skill.SkillType.MUTE, -1.0D);
            } else if ("fear".equalsIgnoreCase(stat)) {
              negateEffect(target, L2Skill.SkillType.FEAR, -1.0D);
            } else if ("poison".equalsIgnoreCase(stat)) {
              negateEffect(target, L2Skill.SkillType.POISON, _negatePower);
            } else if ("bleed".equalsIgnoreCase(stat)) {
              negateEffect(target, L2Skill.SkillType.BLEED, _negatePower);
            } else if ("paralyze".equalsIgnoreCase(stat)) {
              negateEffect(target, L2Skill.SkillType.PARALYZE, -1.0D);
            } else if ("root".equalsIgnoreCase(stat)) {
              negateEffect(target, L2Skill.SkillType.ROOT, -1.0D);
            } else if ("heal".equalsIgnoreCase(stat)) {
              ISkillHandler Healhandler = SkillHandler.getInstance().getSkillHandler(L2Skill.SkillType.HEAL);
              if (Healhandler == null) {
                _log.severe("Couldn't find skill handler for HEAL.");
              }
              else {
                FastList tgts = new FastList();
                tgts.add(target);
                try {
                  Healhandler.useSkill(activeChar, skill, tgts);
                } catch (IOException e) {
                  _log.log(Level.WARNING, "", e);
                }
              }
            } else if ("herodebuff".equalsIgnoreCase(stat)) {
              negateHeroEffect(target);
            } else if ("malaria".equalsIgnoreCase(stat)) {
              target.stopSkillEffects(4554);
            } else if ("cholera".equalsIgnoreCase(stat)) {
              target.stopSkillEffects(4552);
            } else if ("flu".equalsIgnoreCase(stat)) {
              target.stopSkillEffects(4553);
            } else if ("rheumatism".equalsIgnoreCase(stat)) {
              target.stopSkillEffects(4551);
            }
          }
        }

      }

    }

    label2998: L2Effect effect = activeChar.getFirstEffect(skill.getId());
    if ((effect != null) && (effect.isSelfEffect()))
    {
      effect.exit();
    }
    skill.getEffectsSelf(activeChar);
  }

  private void negateHeroEffect(L2Character target)
  {
    FastTable effects = target.getAllEffectsTable();
    int i = 0; for (int n = effects.size(); i < n; i++) {
      L2Effect e = (L2Effect)effects.get(i);
      if (e == null)
      {
        continue;
      }
      switch (e.getSkill().getId()) {
      case 3579:
      case 3584:
      case 3586:
      case 3588:
      case 3590:
      case 3594:
        e.exit();
      case 3580:
      case 3581:
      case 3582:
      case 3583:
      case 3585:
      case 3587:
      case 3589:
      case 3591:
      case 3592:
      case 3593: }  }  } 
  private void negateEffect(L2Character target, L2Skill.SkillType type, double power) { negateEffect(target, type, power, 0); }

  private void negateEffect(L2Character target, L2Skill.SkillType type, double power, int skillId)
  {
    FastTable effects = target.getAllEffectsTable();
    int i = 0; for (int n = effects.size(); i < n; i++) {
      L2Effect e = (L2Effect)effects.get(i);
      if (e == null)
      {
        continue;
      }
      if ((e.getSkill().getId() == 4515) || (e.getSkill().getId() == 4215))
      {
        continue;
      }
      if (power == -1.0D)
      {
        if ((e.getSkill().getSkillType() == type) || ((e.getSkill().getEffectType() != null) && (e.getSkill().getEffectType() == type))) {
          if (skillId != 0) {
            if (skillId == e.getSkill().getId())
              e.exit();
          }
          else
            e.exit();
        }
      }
      else if (((e.getSkill().getSkillType() == type) && (e.getSkill().getPower() <= power)) || ((e.getSkill().getEffectType() != null) && (e.getSkill().getEffectType() == type) && (e.getSkill().getEffectLvl() <= power)))
        if (skillId != 0) {
          if (skillId == e.getSkill().getId())
            e.exit();
        }
        else
          e.exit();
    }
  }

  private void unAffected(L2Character activeChar, String targetName, int skillId)
  {
    if (activeChar.isPlayer())
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2).addString(targetName).addSkillName(skillId));
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}