package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class NpcStat extends CharStat
{
  public NpcStat(L2NpcInstance activeChar)
  {
    super(activeChar);

    setLevel(getActiveChar().getTemplate().level);
  }

  public L2NpcInstance getActiveChar()
  {
    return (L2NpcInstance)super.getActiveChar();
  }
  public final int getMaxHp() {
    return (int)calcStat(Stats.MAX_HP, getActiveChar().getTemplate().baseHpMax, null, null);
  }
}