package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2SiegeSummonInstance extends L2SummonInstance
{
  public static final int SIEGE_GOLEM_ID = 14737;
  public static final int HOG_CANNON_ID = 14768;
  public static final int SWOOP_CANNON_ID = 14839;

  public L2SiegeSummonInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill)
  {
    super(objectId, template, owner, skill);
  }

  public void onSpawn()
  {
    super.onSpawn();
    if ((!getOwner().isGM()) && (!isInsideZone(4)))
    {
      unSummon(getOwner());
      getOwner().sendMessage("Summon was unsummoned because it exited siege zone");
    }
  }
}