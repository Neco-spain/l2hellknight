package net.sf.l2j.gameserver.model.actor.status;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;

public class NpcStatus extends CharStatus
{
  private L2NpcInstance _activeChar;

  public NpcStatus(L2NpcInstance activeChar)
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
    if (_activeChar.isDead()) {
      return;
    }

    if (attacker != null) {
      if (!_activeChar.isMonster()) {
        if ((Config.KILL_NPC_ATTACKER) && (!Config.NPC_HIT_PROTECTET.contains(Integer.valueOf(_activeChar.getNpcId()))) && 
          (!attacker.teleToLocation(Config.NPC_HIT_LOCATION))) {
          attacker.reduceCurrentHp(999999.0D, _activeChar);
        }

        return;
      }

      if ((Config.PROTECT_MOBS_ITEMS) && (_activeChar.isMonster()) && (attacker.hasItems(_activeChar.getPenaltyItems()))) {
        attacker.teleToLocation(_activeChar.getPenaltyLoc());
      }

      _activeChar.addAttackerToAttackByList(attacker);
    }

    super.reduceHp(value, attacker, awake);
  }

  public final void reduceNpcHp(double value, L2Character attacker, boolean awake)
  {
    if (_activeChar.isDead()) {
      return;
    }

    super.reduceHp(value, attacker, awake);
  }

  public L2NpcInstance getActiveChar()
  {
    return _activeChar;
  }
}