package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import net.sf.l2j.Config;
import net.sf.l2j.Config.PvpColor;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.knownlist.MonsterKnownList;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.MinionList;
import net.sf.l2j.util.Rnd;

public class L2MonsterInstance extends L2Attackable
{
  protected final MinionList _minionList;
  protected ScheduledFuture<?> _minionMaintainTask = null;
  private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;
  private int _weaponEnch = 0;

  public L2MonsterInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
    getKnownList();
    _minionList = new MinionList(this);

    if ((Config.ENCH_MONSTER_CAHNCE > 0) && (Rnd.get(100) < Config.ENCH_MONSTER_CAHNCE))
      _weaponEnch = Rnd.get(Config.ENCH_MONSTER_MINMAX.nick, Config.ENCH_MONSTER_MINMAX.title);
  }

  public final MonsterKnownList getKnownList()
  {
    if ((super.getKnownList() == null) || (!(super.getKnownList() instanceof MonsterKnownList))) {
      setKnownList(new MonsterKnownList(this));
    }
    return (MonsterKnownList)super.getKnownList();
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    if (attacker.isL2Monster()) {
      return false;
    }

    return !isEventMob;
  }

  public boolean isAggressive()
  {
    return (getTemplate().aggroRange > 0) && (!isEventMob);
  }

  public void onSpawn()
  {
    super.onSpawn();

    if (getTemplate().getMinionData() != null)
      try {
        for (L2MinionInstance minion : getSpawnedMinions()) {
          if (minion == null)
          {
            continue;
          }
          minion.deleteMe();
        }
        _minionList.clearRespawnList();

        manageMinions();
      } catch (NullPointerException e) {
        e.printStackTrace();
      }
  }

  protected int getMaintenanceInterval()
  {
    return 1000;
  }

  protected void manageMinions()
  {
    _minionMaintainTask = ThreadPoolManager.getInstance().scheduleAi(new Runnable()
    {
      public void run() {
        _minionList.spawnMinions();
      }
    }
    , getMaintenanceInterval(), false);
  }

  public void callMinions()
  {
    if (_minionList.hasMinions())
      for (L2MinionInstance minion : _minionList.getSpawnedMinions())
      {
        if (!isInsideRadius(minion, 200, false, false))
        {
          int masterX = getX();
          int masterY = getY();
          int masterZ = getZ();

          int minionX = masterX + (Rnd.nextInt(401) - 200);
          int minionY = masterY + (Rnd.nextInt(401) - 200);
          int minionZ = masterZ;
          while (((minionX != masterX + 30) && (minionX != masterX - 30)) || ((minionY != masterY + 30) && (minionY != masterY - 30))) {
            minionX = masterX + (Rnd.nextInt(401) - 200);
            minionY = masterY + (Rnd.nextInt(401) - 200);
          }

          if ((!minion.isInCombat()) && (!minion.isDead()) && (!minion.isMovementDisabled()))
            minion.moveToLocation(minionX, minionY, minionZ, 0);
        }
      }
  }

  public void callMinionsToAssist(L2Character attacker)
  {
    if (_minionList.hasMinions()) {
      List spawnedMinions = _minionList.getSpawnedMinions();
      if ((spawnedMinions != null) && (spawnedMinions.size() > 0)) {
        Iterator itr = spawnedMinions.iterator();

        while (itr.hasNext()) {
          L2MinionInstance minion = (L2MinionInstance)itr.next();

          if ((minion != null) && (!minion.isDead())) {
            if ((this instanceof L2RaidBossInstance)) {
              minion.addDamage(attacker, 100); continue;
            }
            minion.addDamage(attacker, 1);
          }
        }
      }
    }
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer)) {
      return false;
    }

    if (_minionMaintainTask != null) {
      _minionMaintainTask.cancel(true);
    }
    if (((this instanceof L2RaidBossInstance)) || ((this instanceof L2GrandBossInstance))) {
      deleteSpawnedMinions();
    }
    return true;
  }

  public List<L2MinionInstance> getSpawnedMinions() {
    return _minionList.getSpawnedMinions();
  }

  public int getTotalSpawnedMinionsInstances() {
    return _minionList.countSpawnedMinions();
  }

  public int getTotalSpawnedMinionsGroups() {
    return _minionList.lazyCountSpawnedMinionsGroups();
  }

  public void notifyMinionDied(L2MinionInstance minion) {
    _minionList.moveMinionToRespawnList(minion);
  }

  public void notifyMinionSpawned(L2MinionInstance minion) {
    _minionList.addSpawnedMinion(minion);
  }

  public boolean hasMinions() {
    return _minionList.hasMinions();
  }

  public void addDamageHate(L2Character attacker, int damage, int aggro)
  {
    super.addDamageHate(attacker, damage, aggro);
  }

  public void deleteMe()
  {
    if (hasMinions()) {
      if (_minionMaintainTask != null) {
        _minionMaintainTask.cancel(true);
      }

      deleteSpawnedMinions();
    }
    super.deleteMe();
  }

  public void deleteSpawnedMinions() {
    for (L2MinionInstance minion : getSpawnedMinions()) {
      if (minion == null) {
        continue;
      }
      minion.abortAttack();
      minion.abortCast();
      minion.deleteMe();
      getSpawnedMinions().remove(minion);
    }
    _minionList.clearRespawnList();
  }

  public boolean isEnemyForMob(L2Attackable mob)
  {
    return (mob.isL2Guard()) && (isAggressive());
  }

  public int getWeaponEnchant()
  {
    return _weaponEnch;
  }

  public boolean isAngel()
  {
    switch (getTemplate().npcId) {
    case 20830:
    case 20831:
    case 20858:
    case 20859:
    case 20860:
    case 21062:
    case 21063:
    case 21067:
    case 21068:
    case 21070:
    case 21071:
    case 21081:
    case 29021:
      return true;
    }
    return false;
  }

  public boolean isL2Monster()
  {
    return true;
  }
}