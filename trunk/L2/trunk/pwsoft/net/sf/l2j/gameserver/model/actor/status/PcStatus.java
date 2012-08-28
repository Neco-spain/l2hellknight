package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.stat.PcStat;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.model.entity.Duel.DuelState;
import net.sf.l2j.gameserver.model.entity.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.entity.olympiad.OlympiadGame;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Util;

public class PcStatus extends PlayableStatus
{
  private L2PcInstance _activeChar;

  public PcStatus(L2PcInstance activeChar)
  {
    super(activeChar);
    _activeChar = activeChar;
  }

  public final void reduceHp(double value, L2Character attacker)
  {
    reduceHp(value, attacker, true);
  }

  public final void reduceHp(double value, L2Character attacker, boolean awake)
  {
    reduceHp(value, attacker, awake, false);
  }

  public final void reduceHp(double value, L2Character attacker, boolean awake, boolean hp) {
    if (attacker == null) {
      return;
    }

    if (_activeChar.isInvul()) {
      return;
    }

    if (attacker.isPlayer()) {
      if (_activeChar.isInDuel())
      {
        if (_activeChar.getDuel().getDuelState(_activeChar) == Duel.DuelState.Dead)
          return;
        if (_activeChar.getDuel().getDuelState(_activeChar) == Duel.DuelState.Winner) {
          return;
        }

        if (attacker.getDuel() != _activeChar.getDuel()) {
          _activeChar.getDuel().setDuelState(_activeChar, Duel.DuelState.Interrupted);
        }
      }

      if ((_activeChar.isDead()) && (!_activeChar.isFakeDeath()))
        return;
    }
    else
    {
      if ((_activeChar.isInDuel()) && (!attacker.isSummon())) {
        _activeChar.getDuel().setDuelState(_activeChar, Duel.DuelState.Interrupted);
      }
      if (_activeChar.isDead()) {
        return;
      }
    }

    if (_activeChar.isInOlympiadMode()) {
      OlympiadGame olymp_game = Olympiad.getOlympiadGame(_activeChar.getOlympiadGameId());
      if (olymp_game != null) {
        if (olymp_game.getState() <= 0)
        {
          return;
        }

        if (!_activeChar.equals(attacker)) {
          olymp_game.addDamage(_activeChar, Math.min(_activeChar.getCurrentHp(), value));
        }

        if (value >= _activeChar.getCurrentHp()) {
          olymp_game.setWinner(_activeChar.getOlympiadSide() == 1 ? 2 : 1);
          olymp_game.endGame(20000L, false);
          _activeChar.setCurrentHp(1.0D);
          attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
          attacker.sendActionFailed();
          return;
        }

      }

    }

    int petDmg = 0;
    int fullValue = (int)value;
    if (attacker != _activeChar)
    {
      L2Summon summon = _activeChar.getPet();

      if ((summon != null) && (summon.isSummon()) && (Util.checkIfInRange(900, _activeChar, summon, true))) {
        petDmg = (int)value * (int)_activeChar.getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0.0D, null, null) / 100;
        if (summon.getCurrentHp() < petDmg) {
          _activeChar.stopSkillEffects(1262);
        }

        petDmg = Math.min((int)summon.getCurrentHp() - 1, petDmg);
        if (petDmg > 0) {
          summon.reduceCurrentHp(petDmg, attacker);
          value -= petDmg;
          fullValue = (int)value;
        }

      }

      if ((!hp) && ((attacker.isL2Playable()) || (attacker.isL2SiegeGuard()))) {
        if (getCurrentCp() >= value) {
          setCurrentCp(getCurrentCp() - value);
          value = 0.0D;
        } else {
          value -= getCurrentCp();
          setCurrentCp(0.0D);
        }
      }
    }

    super.reduceHp(value, attacker, awake);

    if ((!_activeChar.isDead()) && (_activeChar.isSitting())) {
      _activeChar.standUp();
    }

    if (_activeChar.isFakeDeath()) {
      _activeChar.stopFakeDeath(null);
    }

    if ((attacker != null) && (attacker != _activeChar) && (fullValue > 0))
    {
      SystemMessage smsg = SystemMessage.id(SystemMessageId.S1_GAVE_YOU_S2_DMG);
      if (attacker.isL2Npc()) {
        int mobId = ((L2NpcInstance)attacker).getTemplate().idTemplate;
        smsg.addNpcName(mobId);
      } else if (attacker.isL2Summon()) {
        int mobId = ((L2Summon)attacker).getTemplate().idTemplate;
        smsg.addNpcName(mobId);
      } else {
        smsg.addString(attacker.getName());
      }

      smsg.addNumber(fullValue);
      _activeChar.sendPacket(smsg);
      smsg = null;

      if (petDmg > 0)
        attacker.sendMessage("\u0412\u044B \u043D\u0430\u043D\u0435\u0441\u043B\u0438 " + fullValue + " \u043F\u043E\u0432\u0440\u0435\u0436\u0434\u0435\u043D\u0438\u0439 \u0432\u0430\u0448\u0435\u0439 \u0446\u0435\u043B\u0438 \u0438 " + petDmg + " \u043F\u043E\u0432\u0440\u0435\u0436\u0434\u0435\u043D\u0438\u0439 \u0441\u043B\u0443\u0433\u0435");
      else if (fullValue > 0)
        attacker.sendUserPacket(SystemMessage.id(SystemMessageId.YOU_DID_S1_DMG).addNumber(fullValue));
    }
  }

  public L2PcInstance getActiveChar()
  {
    return _activeChar;
  }
}