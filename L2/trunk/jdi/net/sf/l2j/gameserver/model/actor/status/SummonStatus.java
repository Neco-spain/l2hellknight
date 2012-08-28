package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.gameserver.model.L2Summon;

public class SummonStatus extends PlayableStatus
{
  public SummonStatus(L2Summon activeChar)
  {
    super(activeChar);
  }

  public L2Summon getActiveChar()
  {
    return (L2Summon)super.getActiveChar();
  }
}