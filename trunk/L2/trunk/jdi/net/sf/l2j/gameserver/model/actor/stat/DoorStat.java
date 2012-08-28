package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;

public class DoorStat extends CharStat
{
  public DoorStat(L2DoorInstance activeChar)
  {
    super(activeChar);

    setLevel(1);
  }

  public L2DoorInstance getActiveChar()
  {
    return (L2DoorInstance)super.getActiveChar();
  }
  public final byte getLevel() {
    return 1;
  }
}