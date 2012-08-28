package l2m.gameserver.data;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import java.lang.reflect.Constructor;
import l2p.commons.data.xml.AbstractHolder;
import l2m.gameserver.idfactory.IdFactory;
import l2m.gameserver.model.entity.boat.Boat;
import l2m.gameserver.templates.CharTemplate;

public final class BoatHolder extends AbstractHolder
{
  public static final CharTemplate TEMPLATE = new CharTemplate(CharTemplate.getEmptyStatsSet());

  private static BoatHolder _instance = new BoatHolder();
  private final TIntObjectHashMap<Boat> _boats = new TIntObjectHashMap();

  public static BoatHolder getInstance()
  {
    return _instance;
  }

  public void spawnAll()
  {
    log();
    for (TIntObjectIterator iterator = _boats.iterator(); iterator.hasNext(); )
    {
      iterator.advance();
      ((Boat)iterator.value()).spawnMe();
      info("Spawning: " + ((Boat)iterator.value()).getName());
    }
  }

  public Boat initBoat(String name, String clazz)
  {
    try
    {
      Class cl = Class.forName("l2p.gameserver.model.entity.boat." + clazz);
      Constructor constructor = cl.getConstructor(new Class[] { Integer.TYPE, CharTemplate.class });

      Boat boat = (Boat)constructor.newInstance(new Object[] { Integer.valueOf(IdFactory.getInstance().getNextId()), TEMPLATE });
      boat.setName(name);
      addBoat(boat);
      return boat;
    }
    catch (Exception e)
    {
      error("Fail to init boat: " + clazz, e);
    }

    return null;
  }

  public Boat getBoat(String name)
  {
    for (TIntObjectIterator iterator = _boats.iterator(); iterator.hasNext(); )
    {
      iterator.advance();
      if (((Boat)iterator.value()).getName().equals(name)) {
        return (Boat)iterator.value();
      }
    }
    return null;
  }

  public Boat getBoat(int objectId)
  {
    return (Boat)_boats.get(objectId);
  }

  public void addBoat(Boat boat)
  {
    _boats.put(boat.getObjectId(), boat);
  }

  public void removeBoat(Boat boat)
  {
    _boats.remove(boat.getObjectId());
  }

  public int size()
  {
    return _boats.size();
  }

  public void clear()
  {
    _boats.clear();
  }
}