package l2m.gameserver.model;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.List<Ll2p.gameserver.model.Creature;>;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import l2p.commons.collections.LazyArrayList;
import l2p.commons.util.Rnd;
import l2m.gameserver.model.instances.NpcInstance;

public class AggroList
{
  private final NpcInstance npc;
  private final TIntObjectHashMap<AggroInfo> hateList = new TIntObjectHashMap();

  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final Lock readLock = lock.readLock();
  private final Lock writeLock = lock.writeLock();

  public AggroList(NpcInstance npc)
  {
    this.npc = npc;
  }

  public void addDamageHate(Creature attacker, int damage, int aggro)
  {
    damage = Math.max(damage, 0);

    if ((damage == 0) && (aggro == 0)) {
      return;
    }
    writeLock.lock();
    try
    {
      AggroInfo ai;
      if ((ai = (AggroInfo)hateList.get(attacker.getObjectId())) == null) {
        hateList.put(attacker.getObjectId(), ai = new AggroInfo(attacker));
      }
      ai.damage += damage;
      ai.hate += aggro;
      ai.damage = Math.max(ai.damage, 0);
      ai.hate = Math.max(ai.hate, 0);
    }
    finally
    {
      writeLock.unlock();
    }
  }

  public AggroInfo get(Creature attacker)
  {
    readLock.lock();
    try
    {
      AggroInfo localAggroInfo = (AggroInfo)hateList.get(attacker.getObjectId());
      return localAggroInfo; } finally { readLock.unlock(); } throw localObject;
  }

  public void remove(Creature attacker, boolean onlyHate)
  {
    writeLock.lock();
    try
    {
      if (!onlyHate) {
        hateList.remove(attacker.getObjectId());
        return;
      }
      AggroInfo ai = (AggroInfo)hateList.get(attacker.getObjectId());
      if (ai != null)
        ai.hate = 0;
    }
    finally
    {
      writeLock.unlock();
    }
  }

  public void clear()
  {
    clear(false);
  }

  public void clear(boolean onlyHate)
  {
    writeLock.lock();
    try
    {
      if (hateList.isEmpty())
        return;
      if (!onlyHate)
      {
        hateList.clear();
        return;
      }
      for (itr = hateList.iterator(); itr.hasNext(); )
      {
        itr.advance();
        AggroInfo ai = (AggroInfo)itr.value();
        ai.hate = 0;
        if (ai.damage == 0)
          itr.remove();
      }
    }
    finally
    {
      TIntObjectIterator itr;
      writeLock.unlock();
    }
  }

  public boolean isEmpty()
  {
    readLock.lock();
    try
    {
      boolean bool = hateList.isEmpty();
      return bool; } finally { readLock.unlock(); } throw localObject;
  }

  public List<Creature> getHateList() {
    readLock.lock();
    AggroInfo[] hated;
    try {
      if (this.hateList.isEmpty()) {
        List localList1 = Collections.emptyList();
        return localList1;
      }
      hated = (AggroInfo[])this.hateList.getValues(new AggroInfo[this.hateList.size()]);
    }
    finally
    {
      readLock.unlock();
    }

    Arrays.sort(hated, HateComparator.getInstance());
    if (hated[0].hate == 0) {
      return Collections.emptyList();
    }
    Object hateList = new LazyArrayList();
    List chars = World.getAroundCharacters(npc);
    AggroInfo ai;
    for (int i = 0; i < hated.length; i++)
    {
      ai = hated[i];
      if (ai.hate == 0)
        continue;
      for (Creature cha : chars) {
        if (cha.getObjectId() == ai.attackerId)
        {
          ((List)hateList).add(cha);
          break;
        }
      }
    }
    return (List<Creature>)hateList;
  }

  public Creature getMostHated() {
    readLock.lock();
    AggroInfo[] hated;
    try {
      if (hateList.isEmpty()) {
        Object localObject1 = null;
        return localObject1;
      }
      hated = (AggroInfo[])hateList.getValues(new AggroInfo[hateList.size()]);
    }
    finally
    {
      readLock.unlock();
    }

    Arrays.sort(hated, HateComparator.getInstance());
    if (hated[0].hate == 0) {
      return null;
    }
    List chars = World.getAroundCharacters(npc);
    AggroInfo ai;
    for (int i = 0; i < hated.length; i++)
    {
      ai = hated[i];
      if (ai.hate == 0)
        continue;
      for (Creature cha : chars) {
        if (cha.getObjectId() == ai.attackerId)
        {
          if (cha.isDead())
            break;
          return cha;
        }
      }
    }
    return null;
  }
  public Creature getRandomHated() {
    readLock.lock();
    AggroInfo[] hated;
    try {
      if (hateList.isEmpty()) {
        Object localObject1 = null;
        return localObject1;
      }
      hated = (AggroInfo[])hateList.getValues(new AggroInfo[hateList.size()]);
    }
    finally
    {
      readLock.unlock();
    }

    Arrays.sort(hated, HateComparator.getInstance());
    if (hated[0].hate == 0) {
      return null;
    }
    List chars = World.getAroundCharacters(npc);

    LazyArrayList randomHated = LazyArrayList.newInstance();
    AggroInfo ai;
    for (int i = 0; i < hated.length; i++)
    {
      ai = hated[i];
      if (ai.hate == 0)
        continue;
      for (Creature cha : chars)
        if (cha.getObjectId() == ai.attackerId)
        {
          if (cha.isDead())
            break;
          randomHated.add(cha);
          break;
        }
    }
    Creature mostHated;
    Creature mostHated;
    if (randomHated.isEmpty())
      mostHated = null;
    else {
      mostHated = (Creature)randomHated.get(Rnd.get(randomHated.size()));
    }
    LazyArrayList.recycle(randomHated);

    return mostHated;
  }
  public Creature getTopDamager() {
    readLock.lock();
    AggroInfo[] hated;
    try {
      if (hateList.isEmpty()) {
        Object localObject1 = null;
        return localObject1;
      }
      hated = (AggroInfo[])hateList.getValues(new AggroInfo[hateList.size()]);
    }
    finally
    {
      readLock.unlock();
    }

    Creature topDamager = null;
    Arrays.sort(hated, DamageComparator.getInstance());
    if (hated[0].damage == 0) {
      return null;
    }
    List chars = World.getAroundCharacters(npc);
    AggroInfo ai;
    for (int i = 0; i < hated.length; i++)
    {
      ai = hated[i];
      if (ai.damage == 0)
        continue;
      for (Creature cha : chars)
        if (cha.getObjectId() == ai.attackerId)
        {
          topDamager = cha;
          return topDamager;
        }
    }
    return null;
  }

  public Map<Creature, HateInfo> getCharMap()
  {
    if (isEmpty()) {
      return Collections.emptyMap();
    }
    Map aggroMap = new HashMap();
    List chars = World.getAroundCharacters(npc);
    readLock.lock();
    try
    {
      for (itr = hateList.iterator(); itr.hasNext(); )
      {
        itr.advance();
        ai = (AggroInfo)itr.value();
        if ((ai.damage == 0) && (ai.hate == 0))
          continue;
        for (Creature attacker : chars)
          if (attacker.getObjectId() == ai.attackerId)
          {
            aggroMap.put(attacker, new HateInfo(attacker, ai));
            break;
          }
      }
    }
    finally
    {
      TIntObjectIterator itr;
      AggroInfo ai;
      readLock.unlock();
    }

    return aggroMap;
  }

  public Map<Playable, HateInfo> getPlayableMap()
  {
    if (isEmpty()) {
      return Collections.emptyMap();
    }
    Map aggroMap = new HashMap();
    List chars = World.getAroundPlayables(npc);
    readLock.lock();
    try
    {
      for (itr = hateList.iterator(); itr.hasNext(); )
      {
        itr.advance();
        ai = (AggroInfo)itr.value();
        if ((ai.damage == 0) && (ai.hate == 0))
          continue;
        for (Playable attacker : chars)
          if (attacker.getObjectId() == ai.attackerId)
          {
            aggroMap.put(attacker, new HateInfo(attacker, ai));
            break;
          }
      }
    }
    finally
    {
      TIntObjectIterator itr;
      AggroInfo ai;
      readLock.unlock();
    }

    return aggroMap;
  }

  public static class HateComparator
    implements Comparator<AggroList.DamageHate>
  {
    private static Comparator<AggroList.DamageHate> instance = new HateComparator();

    public static Comparator<AggroList.DamageHate> getInstance()
    {
      return instance;
    }

    public int compare(AggroList.DamageHate o1, AggroList.DamageHate o2)
    {
      int diff = o2.hate - o1.hate;
      return diff == 0 ? o2.damage - o1.damage : diff;
    }
  }

  public static class DamageComparator
    implements Comparator<AggroList.DamageHate>
  {
    private static Comparator<AggroList.DamageHate> instance = new DamageComparator();

    public static Comparator<AggroList.DamageHate> getInstance()
    {
      return instance;
    }

    public int compare(AggroList.DamageHate o1, AggroList.DamageHate o2)
    {
      return o2.damage - o1.damage;
    }
  }

  public class AggroInfo extends AggroList.DamageHate
  {
    public final int attackerId;

    AggroInfo(Creature attacker)
    {
      super(null);
      attackerId = attacker.getObjectId();
    }
  }

  public class HateInfo extends AggroList.DamageHate
  {
    public final Creature attacker;

    HateInfo(Creature attacker, AggroList.AggroInfo ai)
    {
      super(null);
      this.attacker = attacker;
      hate = ai.hate;
      damage = ai.damage;
    }
  }

  private abstract class DamageHate
  {
    public int hate;
    public int damage;

    private DamageHate()
    {
    }
  }
}