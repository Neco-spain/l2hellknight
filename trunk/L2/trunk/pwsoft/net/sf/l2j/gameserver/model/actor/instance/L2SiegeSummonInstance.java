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
      getOwner().sendMessage("\u0412\u044B \u043D\u0435 \u0432 \u0437\u043E\u043D\u0435 \u043E\u0441\u0430\u0434\u044B, \u0441\u0430\u043C\u043E\u043D \u043E\u0442\u043E\u0437\u0432\u0430\u043D.");
    }
  }

  public boolean isDebuffProtected()
  {
    return true;
  }
}