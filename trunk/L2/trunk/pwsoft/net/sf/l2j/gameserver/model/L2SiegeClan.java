package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;

public class L2SiegeClan
{
  private int _clanId = 0;
  private L2NpcInstance _flag = null;
  private int _numFlagsAdded = 0;
  private SiegeClanType _type;

  public L2SiegeClan(int clanId, SiegeClanType type)
  {
    _clanId = clanId;
    _type = type;
  }

  public int getNumFlags()
  {
    if (_flag != null) {
      return 1;
    }
    return 0;
  }

  public void addFlag(L2NpcInstance flag)
  {
    _flag = flag;
  }

  public boolean removeFlag()
  {
    if (_flag == null) {
      return false;
    }
    _flag.deleteMe();
    _flag = null;
    return true;
  }

  public final int getClanId()
  {
    return _clanId;
  }

  public final L2NpcInstance getFlag()
  {
    return _flag;
  }

  public SiegeClanType getType()
  {
    return _type;
  }

  public void setType(SiegeClanType setType)
  {
    _type = setType;
  }

  public static enum SiegeClanType
  {
    OWNER, 
    DEFENDER, 
    ATTACKER, 
    DEFENDER_PENDING;
  }
}