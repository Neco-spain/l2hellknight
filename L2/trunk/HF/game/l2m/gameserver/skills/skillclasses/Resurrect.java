package l2m.gameserver.skills.skillclasses;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Skill.SkillTargetType;
import l2m.gameserver.model.base.BaseStats;
import l2m.gameserver.model.entity.events.GlobalEvent;
import l2m.gameserver.model.instances.PetInstance;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.templates.StatsSet;
import org.apache.commons.lang3.tuple.Pair;

public class Resurrect extends Skill
{
  private final boolean _canPet;

  public Resurrect(StatsSet set)
  {
    super(set);
    _canPet = set.getBool("canPet", false);
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    if (!activeChar.isPlayer()) {
      return false;
    }
    if ((target == null) || ((target != activeChar) && (!target.isDead())))
    {
      activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
      return false;
    }

    Player player = (Player)activeChar;
    Player pcTarget = target.getPlayer();

    if (pcTarget == null)
    {
      player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
      return false;
    }

    if ((player.isInOlympiadMode()) || (pcTarget.isInOlympiadMode()))
    {
      player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
      return false;
    }

    for (GlobalEvent e : player.getEvents()) {
      if (!e.canRessurect(player, target, forceUse))
      {
        player.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
        return false;
      }
    }
    if (oneTarget()) {
      if (target.isPet())
      {
        Pair ask = pcTarget.getAskListener(false);
        ReviveAnswerListener reviveAsk = (ask != null) && ((ask.getValue() instanceof ReviveAnswerListener)) ? (ReviveAnswerListener)ask.getValue() : null;
        if (reviveAsk != null)
        {
          if (reviveAsk.isForPet())
            activeChar.sendPacket(Msg.BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED);
          else
            activeChar.sendPacket(Msg.SINCE_THE_MASTER_WAS_IN_THE_PROCESS_OF_BEING_RESURRECTED_THE_ATTEMPT_TO_RESURRECT_THE_PET_HAS_BEEN_CANCELLED);
          return false;
        }
        if ((!_canPet) && (_targetType != Skill.SkillTargetType.TARGET_PET))
        {
          player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
          return false;
        }
      }
      else if (target.isPlayer())
      {
        Pair ask = pcTarget.getAskListener(false);
        ReviveAnswerListener reviveAsk = (ask != null) && ((ask.getValue() instanceof ReviveAnswerListener)) ? (ReviveAnswerListener)ask.getValue() : null;

        if (reviveAsk != null)
        {
          if (reviveAsk.isForPet())
            activeChar.sendPacket(Msg.WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER);
          else
            activeChar.sendPacket(Msg.BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED);
          return false;
        }
        if (_targetType == Skill.SkillTargetType.TARGET_PET)
        {
          player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
          return false;
        }

        if (pcTarget.isFestivalParticipant())
        {
          player.sendMessage(new CustomMessage("l2p.gameserver.skills.skillclasses.Resurrect", player, new Object[0]));
          return false;
        }
      }
    }
    return super.checkCondition(activeChar, target, forceUse, dontMove, first);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    double percent = _power;

    if ((percent < 100.0D) && (!isHandler()))
    {
      double wit_bonus = _power * (BaseStats.WIT.calcBonus(activeChar) - 1.0D);
      percent += (wit_bonus > 20.0D ? 20.0D : wit_bonus);
      if (percent > 90.0D) {
        percent = 90.0D;
      }
    }
    for (Creature target : targets)
      if (target != null)
      {
        if (target.getPlayer() == null) {
          continue;
        }
        Iterator i$ = target.getEvents().iterator();
        while (true) if (i$.hasNext()) { GlobalEvent e = (GlobalEvent)i$.next();
            if (e.canRessurect((Player)activeChar, target, true))
              continue;
          } else {
            if ((target.isPet()) && (_canPet))
            {
              if (target.getPlayer() == activeChar)
                ((PetInstance)target).doRevive(percent);
              else
                target.getPlayer().reviveRequest((Player)activeChar, percent, true);
            } else {
              if ((!target.isPlayer()) || 
                (_targetType == Skill.SkillTargetType.TARGET_PET)) {
                break;
              }
              Player targetPlayer = (Player)target;

              Pair ask = targetPlayer.getAskListener(false);
              ReviveAnswerListener reviveAsk = (ask != null) && ((ask.getValue() instanceof ReviveAnswerListener)) ? (ReviveAnswerListener)ask.getValue() : null;
              if ((reviveAsk != null) || 
                (targetPlayer.isFestivalParticipant())) {
                break;
              }
              targetPlayer.reviveRequest((Player)activeChar, percent, false);
            }

            getEffects(activeChar, target, getActivateRate() > 0, false);
          }
      }
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}