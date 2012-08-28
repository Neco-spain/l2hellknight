package l2p.gameserver.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import l2p.gameserver.data.xml.holder.NpcHolder;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.model.instances.MinionInstance;
import l2p.gameserver.model.instances.MonsterInstance;
import l2p.gameserver.templates.npc.MinionData;
import l2p.gameserver.templates.npc.NpcTemplate;

public class MinionList
{
  private final Set<MinionData> _minionData;
  private final Set<MinionInstance> _minions;
  private final Lock lock;
  private final MonsterInstance _master;

  public MinionList(MonsterInstance master)
  {
    _master = master;
    _minions = new HashSet();
    _minionData = new HashSet();
    _minionData.addAll(_master.getTemplate().getMinionData());
    lock = new ReentrantLock();
  }

  public boolean addMinion(MinionData m)
  {
    lock.lock();
    try
    {
      boolean bool = _minionData.add(m);
      return bool; } finally { lock.unlock(); } throw localObject;
  }

  public boolean addMinion(MinionInstance m)
  {
    lock.lock();
    try
    {
      boolean bool = _minions.add(m);
      return bool; } finally { lock.unlock(); } throw localObject;
  }

  public boolean hasAliveMinions()
  {
    lock.lock();
    try
    {
      for (MinionInstance m : _minions)
        if ((m.isVisible()) && (!m.isDead())) {
          int i = 1;
          return i;
        }  } finally {
      lock.unlock();
    }
    return false;
  }

  public boolean hasMinions()
  {
    return _minionData.size() > 0;
  }

  public List<MinionInstance> getAliveMinions()
  {
    List result = new ArrayList(_minions.size());
    lock.lock();
    try
    {
      for (MinionInstance m : _minions)
        if ((m.isVisible()) && (!m.isDead()))
          result.add(m);
    }
    finally
    {
      lock.unlock();
    }
    return result;
  }

  public void spawnMinions()
  {
    lock.lock();
    try
    {
      for (MinionData minion : _minionData)
      {
        int minionId = minion.getMinionId();
        int minionCount = minion.getAmount();

        for (MinionInstance m : _minions)
        {
          if (m.getNpcId() == minionId)
            minionCount--;
          if ((m.isDead()) || (!m.isVisible()))
          {
            m.refreshID();
            m.stopDecay();
            _master.spawnMinion(m);
          }
        }

        for (int i = 0; i < minionCount; i++)
        {
          MinionInstance m = new MinionInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(minionId));
          m.setLeader(_master);
          _master.spawnMinion(m);
          _minions.add(m);
        }
      }
    }
    finally
    {
      lock.unlock();
    }
  }

  public void unspawnMinions()
  {
    lock.lock();
    try
    {
      for (MinionInstance m : _minions)
        m.decayMe();
    }
    finally
    {
      lock.unlock();
    }
  }

  public void deleteMinions()
  {
    lock.lock();
    try
    {
      for (MinionInstance m : _minions)
        m.deleteMe();
      _minions.clear();
    }
    finally
    {
      lock.unlock();
    }
  }
}