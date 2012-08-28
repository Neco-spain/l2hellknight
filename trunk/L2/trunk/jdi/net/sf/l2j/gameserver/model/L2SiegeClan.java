package net.sf.l2j.gameserver.model;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;

public class L2SiegeClan
{
  private int _clanId = 0;
  private List<L2NpcInstance> _flag = new FastList();
  private int _numFlagsAdded = 0;
  private SiegeClanType _type;

  public L2SiegeClan(int clanId, SiegeClanType type)
  {
    _clanId = clanId;
    _type = type;
  }

  public int getNumFlags()
  {
    return _numFlagsAdded;
  }

  public void addFlag(L2NpcInstance flag)
  {
    _numFlagsAdded += 1;
    getFlag().add(flag);
  }

  public boolean removeFlag(L2NpcInstance flag)
  {
    if (flag == null) return false;
    boolean ret = getFlag().remove(flag);

    while ((ret) && 
      (getFlag().remove(flag)));
    boolean more = true;
    while (more)
    {
      more = false;
      int n = getFlag().size();
      if (n > 0) {
        for (int i = 0; i < n; i++) {
          if (getFlag().get(i) != null)
            continue;
          getFlag().remove(i);
          more = true;
          break;
        }
      }
    }
    flag.deleteMe();
    return ret;
  }

  public void removeFlags()
  {
    for (L2NpcInstance flag : getFlag())
      removeFlag(flag);
  }

  public final int getClanId()
  {
    return _clanId;
  }

  public final List<L2NpcInstance> getFlag() {
    if (_flag == null) _flag = new FastList();
    return _flag;
  }
  public SiegeClanType getType() {
    return _type;
  }
  public void setType(SiegeClanType setType) { _type = setType;
  }

  public static enum SiegeClanType
  {
    OWNER, 
    DEFENDER, 
    ATTACKER, 
    DEFENDER_PENDING;
  }
}