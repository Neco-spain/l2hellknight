package l2m.gameserver.model.instances;

import l2m.gameserver.model.Creature;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.utils.Location;

public class MinionInstance extends MonsterInstance
{
  public static final long serialVersionUID = 1L;
  private MonsterInstance _master;

  public MinionInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public void setLeader(MonsterInstance leader)
  {
    _master = leader;
  }

  public MonsterInstance getLeader()
  {
    return _master;
  }

  public boolean isRaidFighter()
  {
    return (getLeader() != null) && (getLeader().isRaid());
  }

  protected void onDeath(Creature killer)
  {
    if (getLeader() != null) {
      getLeader().notifyMinionDied(this);
    }
    super.onDeath(killer);
  }

  protected void onDecay()
  {
    decayMe();

    _spawnAnimation = 2;
  }

  public boolean isFearImmune()
  {
    return isRaidFighter();
  }

  public Location getSpawnedLoc()
  {
    return getLeader() != null ? getLeader().getLoc() : getLoc();
  }

  public boolean canChampion()
  {
    return false;
  }

  public boolean isMinion()
  {
    return true;
  }
}