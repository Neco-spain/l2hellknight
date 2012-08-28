package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2BossInstance extends L2MonsterInstance
{
  private static final int BOSS_MAINTENANCE_INTERVAL = 10000;

  public L2BossInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  protected int getMaintenanceInterval() {
    return 10000;
  }

  public void onSpawn()
  {
    super.onSpawn();
  }

  public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
  {
    super.reduceCurrentHp(damage, attacker, awake);
  }

  public boolean isRaid()
  {
    return true;
  }
}