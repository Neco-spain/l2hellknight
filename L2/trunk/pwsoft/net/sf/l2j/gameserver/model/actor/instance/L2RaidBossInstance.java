package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager.StatusEnum;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.MinionList;
import net.sf.l2j.util.Rnd;

public class L2RaidBossInstance extends L2MonsterInstance
{
  private static final int RAIDBOSS_MAINTENANCE_INTERVAL = 30000;
  private RaidBossSpawnManager.StatusEnum _raidStatus;

  public L2RaidBossInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public boolean isRaid()
  {
    return true;
  }

  protected int getMaintenanceInterval()
  {
    return 30000;
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer)) {
      return false;
    }

    L2PcInstance player = killer.getPlayer();
    if (player != null) {
      if ((Config.RAID_CUSTOM_DROP) && (getTemplate().npcId != 25325)) {
        dropRaidCustom(player);
      }

      if ((Config.RAID_CLANPOINTS_REWARD > 0) && 
        (player.getClan() != null)) {
        player.getClan().addPoints(Config.RAID_CLANPOINTS_REWARD);
      }

      broadcastPacket(Static.RAID_WAS_SUCCESSFUL);
    }

    RaidBossSpawnManager.getInstance().updateStatus(this, true);
    return true;
  }

  protected void manageMinions()
  {
    _minionList.spawnMinions();
    _minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable()
    {
      public void run()
      {
        L2Spawn bossSpawn = getSpawn();
        if (!isInsideRadius(bossSpawn.getLocx(), bossSpawn.getLocy(), bossSpawn.getLocz(), 5000, true, false)) {
          teleToLocation(bossSpawn.getLocx(), bossSpawn.getLocy(), bossSpawn.getLocz(), true);
          healFull();
        }
        _minionList.maintainMinions();
      }
    }
    , 60000L, getMaintenanceInterval() + Rnd.get(5000));
  }

  public void setRaidStatus(RaidBossSpawnManager.StatusEnum status)
  {
    _raidStatus = status;
  }

  public RaidBossSpawnManager.StatusEnum getRaidStatus() {
    return _raidStatus;
  }

  public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
  {
    super.reduceCurrentHp(damage, attacker, awake);
  }

  public void healFull() {
    super.setCurrentHp(super.getMaxHp());
    super.setCurrentMp(super.getMaxMp());
  }
}