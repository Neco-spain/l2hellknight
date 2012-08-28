package l2m.gameserver.instancemanager;

import gnu.trove.TIntObjectHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import l2m.gameserver.data.xml.holder.DoorHolder;
import l2m.gameserver.data.xml.holder.ZoneHolder;
import l2m.gameserver.model.entity.Reflection;
import l2m.gameserver.utils.Location;

public class ReflectionManager
{
  public static final Reflection DEFAULT = Reflection.createReflection(0);
  public static final Reflection PARNASSUS = Reflection.createReflection(-1);
  public static final Reflection GIRAN_HARBOR = Reflection.createReflection(-2);
  public static final Reflection JAIL = Reflection.createReflection(-3);

  private static final ReflectionManager _instance = new ReflectionManager();

  private final TIntObjectHashMap<Reflection> _reflections = new TIntObjectHashMap();

  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final Lock readLock = lock.readLock();
  private final Lock writeLock = lock.writeLock();

  public static ReflectionManager getInstance()
  {
    return _instance;
  }

  private ReflectionManager()
  {
    add(DEFAULT);
    add(PARNASSUS);
    add(GIRAN_HARBOR);
    add(JAIL);

    DEFAULT.init(DoorHolder.getInstance().getDoors(), ZoneHolder.getInstance().getZones());

    JAIL.setCoreLoc(new Location(-114648, -249384, -2984));
  }

  public Reflection get(int id)
  {
    readLock.lock();
    try
    {
      Reflection localReflection = (Reflection)_reflections.get(id);
      return localReflection; } finally { readLock.unlock(); } throw localObject;
  }

  public Reflection add(Reflection ref)
  {
    writeLock.lock();
    try
    {
      Reflection localReflection = (Reflection)_reflections.put(ref.getId(), ref);
      return localReflection; } finally { writeLock.unlock(); } throw localObject;
  }

  public Reflection remove(Reflection ref)
  {
    writeLock.lock();
    try
    {
      Reflection localReflection = (Reflection)_reflections.remove(ref.getId());
      return localReflection; } finally { writeLock.unlock(); } throw localObject;
  }

  public Reflection[] getAll()
  {
    readLock.lock();
    try
    {
      Reflection[] arrayOfReflection = (Reflection[])_reflections.getValues(new Reflection[_reflections.size()]);
      return arrayOfReflection; } finally { readLock.unlock(); } throw localObject;
  }

  public int getCountByIzId(int izId)
  {
    readLock.lock();
    try
    {
      int i = 0;
      for (Reflection r : getAll())
        if (r.getInstancedZoneId() == izId)
          i++;
      ??? = i;
      return ???; } finally { readLock.unlock(); } throw localObject;
  }

  public int size()
  {
    return _reflections.size();
  }
}