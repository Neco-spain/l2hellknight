package net.sf.l2j.gameserver.util;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import javolution.util.FastSet;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2MinionData;
import net.sf.l2j.gameserver.model.actor.instance.L2MinionInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;

public class MinionList
{
  private static Logger _log = AbstractLogger.getLogger(L2MonsterInstance.class.getName());
  private final List<L2MinionInstance> minionReferences;
  protected FastMap<Long, Integer> _respawnTasks = new FastMap().shared("MinionList._respawnTasks");
  private final L2MonsterInstance master;

  public MinionList(L2MonsterInstance pMaster)
  {
    minionReferences = new FastList();
    master = pMaster;
  }

  public int countSpawnedMinions()
  {
    synchronized (minionReferences)
    {
      return minionReferences.size();
    }
  }

  public int countSpawnedMinionsById(int minionId)
  {
    int count = 0;
    synchronized (minionReferences)
    {
      for (L2MinionInstance minion : getSpawnedMinions())
      {
        if (minion.getNpcId() == minionId)
        {
          count++;
        }
      }
    }
    return count;
  }

  public boolean hasMinions()
  {
    return getSpawnedMinions().size() > 0;
  }

  public List<L2MinionInstance> getSpawnedMinions()
  {
    return minionReferences;
  }

  public void addSpawnedMinion(L2MinionInstance minion)
  {
    synchronized (minionReferences)
    {
      minionReferences.add(minion);
    }
  }

  public int lazyCountSpawnedMinionsGroups()
  {
    Set seenGroups = new FastSet();
    for (L2MinionInstance minion : getSpawnedMinions())
    {
      seenGroups.add(Integer.valueOf(minion.getNpcId()));
    }
    return seenGroups.size();
  }

  public void removeSpawnedMinion(L2MinionInstance minion)
  {
    synchronized (minionReferences)
    {
      minionReferences.remove(minion);
    }
  }

  public void moveMinionToRespawnList(L2MinionInstance minion)
  {
    Long current = Long.valueOf(System.currentTimeMillis());
    synchronized (minionReferences)
    {
      minionReferences.remove(minion);
      if (_respawnTasks.get(current) == null) {
        _respawnTasks.put(current, Integer.valueOf(minion.getNpcId()));
      }
      else
      {
        for (int i = 1; i < 30; i++)
        {
          if (_respawnTasks.get(Long.valueOf(current.longValue() + i)) != null)
            continue;
          _respawnTasks.put(Long.valueOf(current.longValue() + i), Integer.valueOf(minion.getNpcId()));
          break;
        }
      }
    }
  }

  public void clearRespawnList()
  {
    _respawnTasks.clear();
  }

  public void maintainMinions()
  {
    if ((master == null) || (master.isAlikeDead())) return;
    Long current = Long.valueOf(System.currentTimeMillis());
    FastMap.Entry e;
    if (_respawnTasks != null)
    {
      e = _respawnTasks.head(); for (FastMap.Entry end = _respawnTasks.tail(); (e = e.getNext()) != end; )
      {
        long deathTime = ((Long)e.getKey()).longValue();
        double delay = Config.RAID_MINION_RESPAWN_TIMER;
        if (current.longValue() - deathTime > delay)
        {
          spawnSingleMinion(((Integer)_respawnTasks.get(Long.valueOf(deathTime))).intValue());
          _respawnTasks.remove(Long.valueOf(deathTime));
        }
      }
    }
  }

  public void spawnMinions()
  {
    if ((master == null) || (master.isAlikeDead())) return;
    List minions = master.getTemplate().getMinionData();

    synchronized (minionReferences)
    {
      for (L2MinionData minion : minions)
      {
        int minionCount = minion.getAmount();
        int minionId = minion.getMinionId();

        int minionsToSpawn = minionCount - countSpawnedMinionsById(minionId);

        for (int i = 0; i < minionsToSpawn; i++)
        {
          spawnSingleMinion(minionId);
        }
      }
    }
  }

  public void spawnSingleMinion(int minionid)
  {
    L2NpcTemplate minionTemplate = NpcTable.getInstance().getTemplate(minionid);

    L2MinionInstance monster = new L2MinionInstance(IdFactory.getInstance().getNextId(), minionTemplate);

    monster.setCurrentHpMp(monster.getMaxHp(), monster.getMaxMp());
    monster.setHeading(master.getHeading());

    monster.setLeader(master);

    int randSpawnLim = 170;
    int randPlusMin = 1;
    int spawnConstant = Rnd.nextInt(randSpawnLim);

    randPlusMin = Rnd.nextInt(2);
    if (randPlusMin == 1) spawnConstant *= -1;
    int newX = master.getX() + Math.round(spawnConstant);
    spawnConstant = Rnd.nextInt(randSpawnLim);

    randPlusMin = Rnd.nextInt(2);
    if (randPlusMin == 1) spawnConstant *= -1;
    int newY = master.getY() + Math.round(spawnConstant);

    monster.spawnMe(newX, newY, master.getZ());
  }
}