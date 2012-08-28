package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.knownlist.FriendlyMobKnownList;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2FriendlyMobInstance extends L2Attackable
{
  public L2FriendlyMobInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
    getKnownList();
  }

  public final FriendlyMobKnownList getKnownList()
  {
    if ((super.getKnownList() == null) || (!(super.getKnownList() instanceof FriendlyMobKnownList)))
      setKnownList(new FriendlyMobKnownList(this));
    return (FriendlyMobKnownList)super.getKnownList();
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    if ((attacker instanceof L2PcInstance))
      return ((L2PcInstance)attacker).getKarma() > 0;
    return false;
  }

  public boolean isAggressive()
  {
    return true;
  }
}