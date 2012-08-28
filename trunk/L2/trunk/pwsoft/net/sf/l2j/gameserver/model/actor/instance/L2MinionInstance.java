package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ai.L2AttackableAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2MinionInstance extends L2MonsterInstance
{
  private L2MonsterInstance _master;

  public L2MinionInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public boolean isRaid()
  {
    return getLeader() instanceof L2RaidBossInstance;
  }

  public L2MonsterInstance getLeader()
  {
    return _master;
  }

  public void onSpawn()
  {
    super.onSpawn();

    getLeader().notifyMinionSpawned(this);

    L2WorldRegion region = L2World.getInstance().getRegion(getX(), getY());
    if ((region != null) && (!region.isActive().booleanValue()))
      ((L2AttackableAI)getAI()).stopAITask();
  }

  public void setLeader(L2MonsterInstance leader)
  {
    _master = leader;
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer))
      return false;
    _master.notifyMinionDied(this);
    return true;
  }
}