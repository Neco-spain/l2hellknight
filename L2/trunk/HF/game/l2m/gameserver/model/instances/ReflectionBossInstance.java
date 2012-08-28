package l2m.gameserver.model.instances;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.MinionList;
import l2m.gameserver.model.entity.Reflection;
import l2m.gameserver.templates.npc.NpcTemplate;

public class ReflectionBossInstance extends RaidBossInstance
{
  public static final long serialVersionUID = 1L;
  private static final int COLLAPSE_AFTER_DEATH_TIME = 5;

  public ReflectionBossInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  protected void onDeath(Creature killer)
  {
    getMinionList().unspawnMinions();
    super.onDeath(killer);
    clearReflection();
  }

  protected void clearReflection()
  {
    getReflection().clearReflection(5, true);
  }
}