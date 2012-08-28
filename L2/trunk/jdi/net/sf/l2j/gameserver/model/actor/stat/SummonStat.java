package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.model.L2Summon;

public class SummonStat extends PlayableStat
{
  public SummonStat(L2Summon activeChar)
  {
    super(activeChar);
  }

  public L2Summon getActiveChar()
  {
    return (L2Summon)super.getActiveChar();
  }
}