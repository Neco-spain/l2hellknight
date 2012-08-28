package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.actor.stat.PcStat;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.StopMove;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;
import scripts.skills.ISkillHandler;

public class Blow
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.BLOW };
  private int _successChance;
  public static final int FRONT = Config.BLOW_CHANCE_FRONT;
  public static final int SIDE = Config.BLOW_CHANCE_SIDE;
  public static final int BEHIND = Config.BLOW_CHANCE_BEHIND;

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if ((activeChar == null) || (activeChar.isAlikeDead())) {
      return;
    }
    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
    {
      L2Character target = (L2Character)n.getValue();
      if ((target == null) || (target.isAlikeDead())) {
        continue;
      }
      if (activeChar.isBehindTarget())
        _successChance = BEHIND;
      else if (activeChar.isFrontTarget())
        _successChance = FRONT;
      else {
        _successChance = SIDE;
      }

      if ((((skill.getCondition() & 0x8) != 0) && (_successChance == BEHIND)) || (((skill.getCondition() & 0x10) != 0) && (Formulas.calcBlow(activeChar, target, skill, _successChance))))
      {
        if (skill.getId() == 321)
        {
          if (target.isPlayer())
          {
            int posX = target.getX();
            int posY = target.getY();
            int posZ = target.getZ();
            int signx = -1;
            int signy = -1;
            if (posX > activeChar.getX())
              signx = 1;
            if (posY > activeChar.getY()) {
              signy = 1;
            }
            posX += signx * 14;
            posY += signy * 14;
            target.setRunning();

            L2CharPosition cp = new L2CharPosition(posX, posY, posZ, target.calcHeading(posX, posY));
            target.stopMove(cp);
            target.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, cp);
            target.setHeading(activeChar.getHeading());
            target.sendPacket(new ValidateLocation(target));
            target.sendPacket(new StopMove(target));
          }
        }

        if (skill.hasEffects())
        {
          if (target.reflectSkill(skill))
          {
            L2Effect scuko = activeChar.getFirstEffect(skill.getId());
            if (scuko == null)
            {
              skill.getEffects(activeChar, target);
              target.sendPacket(SystemMessage.id(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill.getId()));
            }
            else {
              activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2).addString(target.getName()).addSkillName(skill.getDisplayId()));
            }
          }
        }
        L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
        boolean soul = (weapon != null) && (weapon.getChargedSoulshot() == 1) && (weapon.getItemType() == L2WeaponType.DAGGER);
        boolean shld = Formulas.calcShldUse(activeChar, target);

        boolean crit = false;
        if (Formulas.calcCrit(activeChar, skill.getBaseCritRate() * 10 * Formulas.getSTRBonus(activeChar)))
          crit = true;
        double damage = (int)Formulas.calcBlowDamage(activeChar, target, skill, shld, soul);
        if (crit)
        {
          activeChar.sendPacket(Static.CRITICAL_HIT);
          damage *= 1.6D;

          L2Effect vicious = activeChar.getFirstEffect(312);
          if ((vicious != null) && (damage > 1.0D))
          {
            damage += vicious.getLevel() * 30;
          }

          if (activeChar.getFirstEffect(355) != null)
          {
            if (activeChar.isBehindTarget())
              damage *= 1.9D;
            else if (activeChar.isFrontTarget()) {
              damage *= 0.7D;
            }
          }
        }
        if ((soul) && (weapon != null))
          weapon.setChargedSoulshot(0);
        if ((skill.getDmgDirectlyToHP()) && (target.isPlayer()))
        {
          L2PcInstance player = (L2PcInstance)target;
          if (!player.isInvul())
          {
            L2Summon summon = player.getPet();
            if ((summon != null) && ((summon instanceof L2SummonInstance)) && (Util.checkIfInRange(900, player, summon, true)))
            {
              int tDmg = (int)damage * (int)player.getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0.0D, null, null) / 100;

              if (summon.getCurrentHp() < tDmg)
                tDmg = (int)summon.getCurrentHp() - 1;
              if (tDmg > 0)
              {
                summon.reduceCurrentHp(tDmg, activeChar);
                damage -= tDmg;
              }

            }

            player.reduceCurrentHp(damage, activeChar, true, true);
          }

        }
        else
        {
          target.reduceCurrentHp(damage, activeChar);
        }
        if (activeChar.isPlayer())
        {
          SystemMessage sm = SystemMessage.id(SystemMessageId.YOU_DID_S1_DMG);
          sm.addNumber((int)damage);
          activeChar.sendPacket(sm);
        }
        if (target.isSleeping()) {
          target.stopSleeping(null);
        }
        if (target.getFirstEffect(447) != null) {
          activeChar.reduceCurrentHp(damage / 2.7D, target);
        }

        if ((!target.isRaid()) && (!target.isL2Door()) && ((!target.isL2Npc()) || (((L2NpcInstance)target).getNpcId() != 35062)))
        {
          int chance = Rnd.get(100);

          if ((skill.getLethalChance2() > 0) && (chance < 2))
          {
            if (target.isL2Npc()) {
              target.reduceCurrentHp(target.getCurrentHp() - 1.0D, activeChar);
            } else if (target.isPlayer())
            {
              L2PcInstance player = (L2PcInstance)target;
              if (!player.isInvul()) {
                player.setCurrentHp(1.0D);
                player.setCurrentCp(1.0D);
              }
            }
            activeChar.sendPacket(Static.LETHAL_STRIKE);
          }
          else if ((skill.getLethalChance1() > 0) && (chance < 1)) {
            if (target.isPlayer())
            {
              L2PcInstance player = (L2PcInstance)target;
              if (!player.isInvul())
                player.setCurrentCp(1.0D);
            }
            else if (target.isL2Npc()) {
              target.reduceCurrentHp(target.getCurrentHp() / 2.0D, activeChar);
            }activeChar.sendPacket(Static.LETHAL_STRIKE);
          }
        }
      }
      L2Effect effect = activeChar.getFirstEffect(skill.getId());

      if ((effect != null) && (effect.isSelfEffect()))
        effect.exit();
      skill.getEffectsSelf(activeChar);
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}