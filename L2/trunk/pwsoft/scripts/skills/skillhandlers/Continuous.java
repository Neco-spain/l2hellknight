package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeFlagInstance;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.util.Rnd;
import scripts.skills.ISkillHandler;

public class Continuous
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.BUFF, L2Skill.SkillType.DEBUFF, L2Skill.SkillType.DOT, L2Skill.SkillType.MDOT, L2Skill.SkillType.POISON, L2Skill.SkillType.BLEED, L2Skill.SkillType.HOT, L2Skill.SkillType.CPHOT, L2Skill.SkillType.MPHOT, L2Skill.SkillType.CONT, L2Skill.SkillType.WEAKNESS, L2Skill.SkillType.REFLECT, L2Skill.SkillType.UNDEAD_DEFENSE, L2Skill.SkillType.AGGDEBUFF, L2Skill.SkillType.FORCE_BUFF };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if (skill == null) {
      return;
    }

    L2Character target = null;
    L2PcInstance player = null;
    if (activeChar.isPlayer()) {
      player = activeChar.getPlayer();
    }

    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; ) {
      target = (L2Character)n.getValue();

      if ((target == null) || (target.isDead()))
      {
        continue;
      }
      if ((skill.getSkillType() != L2Skill.SkillType.BUFF) && (skill.getSkillType() != L2Skill.SkillType.HOT) && (skill.getSkillType() != L2Skill.SkillType.CPHOT) && (skill.getSkillType() != L2Skill.SkillType.MPHOT) && (skill.getSkillType() != L2Skill.SkillType.UNDEAD_DEFENSE) && (skill.getSkillType() != L2Skill.SkillType.AGGDEBUFF) && (skill.getSkillType() != L2Skill.SkillType.CONT))
      {
        if (target.reflectSkill(skill)) {
          target = activeChar;
        }

      }

      if ((target.isL2Door()) || (((target instanceof L2SiegeFlagInstance)) && ((skill.getSkillType() == L2Skill.SkillType.BUFF) || (skill.getSkillType() == L2Skill.SkillType.HOT))))
      {
        continue;
      }

      if ((skill.getSkillType() == L2Skill.SkillType.BUFF) && 
        (target != activeChar) && 
        (target.isPlayer())) {
        L2PcInstance targetChar = (L2PcInstance)target;

        if ((targetChar.getFirstEffect(Config.ANTIBUFF_SKILLID) != null) || (target.getFirstEffect(1411) != null) || (targetChar.isCursedWeaponEquiped()) || (targetChar.isOlympiadWait()) || (
          (player != null) && (player.isCursedWeaponEquiped())))
        {
          continue;
        }

      }

      if (skill.isOffensive())
      {
        boolean ss = false;
        boolean sps = false;
        boolean bss = false;
        if (player != null) {
          L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
          if (weaponInst != null)
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
        else if (activeChar.isL2Summon()) {
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

        boolean acted = Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss);
        if (!acted) {
          activeChar.sendPacket(Static.ATTACK_FAILED);
          continue;
        }
      }

      boolean stopped = false;
      FastTable effects = target.getAllEffectsTable();
      if (skill != null) {
        int k = 0; for (int m = effects.size(); k < m; k++) {
          L2Effect e = (L2Effect)effects.get(k);
          if (e == null)
          {
            continue;
          }
          if (e.getSkill().getId() == skill.getId()) {
            e.exit(true);
            stopped = true;
            break;
          }
        }
      }
      if ((skill.isToggle()) && (stopped)) {
        return;
      }

      if ((player != null) && (player.getDuel() != null) && (target.isPlayer()) && (((L2PcInstance)target).getDuel() != null) && ((skill.getSkillType() == L2Skill.SkillType.DEBUFF) || (skill.getSkillType() == L2Skill.SkillType.BUFF)) && (player.getDuel() == ((L2PcInstance)target).getDuel()))
      {
        for (L2Effect buff : skill.getEffects(activeChar, target)) {
          if (buff != null)
            player.getDuel().onBuff((L2PcInstance)target, buff);
        }
      }
      else {
        skill.getEffects(activeChar, target);
      }

      if (skill.getSkillType() == L2Skill.SkillType.AGGDEBUFF) {
        if (target.isL2Attackable())
          target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, Integer.valueOf((int)skill.getPower()));
        else if ((target instanceof L2PlayableInstance)) {
          if (target.getTarget() == activeChar)
            target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
          else {
            target.setTarget(activeChar);
          }
        }
      }

      switch (skill.getId())
      {
      case 342:
        int max = Rnd.get(2, 6);
        int canceled = 0;
        int count = 0;
        int finish = target.getBuffCount();

        int h = 0; for (int j = effects.size(); h < j; h++) {
          L2Effect e = (L2Effect)effects.get(h);
          if (e == null)
          {
            continue;
          }
          if (e.getSkill().isCancelProtected())
          {
            continue;
          }
          switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Effect$EffectType[e.getEffectType().ordinal()]) {
          case 1:
          case 2:
            break;
          default:
            if (!e.getSkill().isBuff()) continue;
            if (Rnd.get(100) < finish) {
              e.exit();
              canceled++;
            }
            count++;
            if ((canceled >= max) || (count >= finish))
            {
              break label1078;
            }

          }

        }

        break;
      case 1358:
      case 1360:
        int a = 0; for (int z = effects.size(); a < z; a++) {
          L2Effect e = (L2Effect)effects.get(a);
          if (e == null) {
            continue;
          }
          switch (e.getSkill().getId()) {
          case 1005:
          case 1009:
          case 1010:
          case 1040:
            e.exit();
          }
        }

        break;
      case 1359:
      case 1361:
        int s = 0; for (int x = effects.size(); s < x; s++) {
          L2Effect e = (L2Effect)effects.get(s);
          if (e == null) {
            continue;
          }
          switch (e.getSkill().getId()) {
          case 230:
          case 1204:
          case 1282:
          case 2011:
            e.exit();
          }
        }

        break;
      case 1363:
        target.setCurrentHp(target.getCurrentHp() * 1.2D);

        break;
      case 1416:
        if (skill.getPower() == 800.0D)
          target.setCurrentCp(target.getMaxCp());
        else {
          target.setCurrentCp(target.getCurrentCp() + skill.getPower());
        }
      }

    }

    label1078: L2Effect effect = activeChar.getFirstEffect(skill.getId());
    if ((effect != null) && (effect.isSelfEffect()))
    {
      effect.exit();
    }
    skill.getEffectsSelf(activeChar);

    if (skill.getId() == 3206)
      activeChar.broadcastPacket(new MagicSkillUser(activeChar, activeChar, Rnd.get(4411, 4417), 1, 1, 0));
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}