package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.gameserver.model.L2Summon;

public class SummonStatus extends PlayableStatus
{
  private L2Summon _activeChar;

  public SummonStatus(L2Summon activeChar)
  {
    super(activeChar);
    _activeChar = activeChar;
  }

  public L2Summon getActiveChar()
  {
    return _activeChar;
  }
}