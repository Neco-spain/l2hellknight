package net.sf.l2j.util;

import java.util.Iterator;
import net.sf.l2j.gameserver.model.L2Object;

public abstract class L2ObjectMap<T extends L2Object>
  implements Iterable<T>
{
  public abstract int size();

  public abstract boolean isEmpty();

  public abstract void clear();

  public abstract void put(T paramT);

  public abstract void remove(T paramT);

  public abstract T get(int paramInt);

  public abstract boolean contains(T paramT);

  public abstract Iterator<T> iterator();

  public static L2ObjectMap<L2Object> createL2ObjectMap()
  {
    switch (1.$SwitchMap$net$sf$l2j$Config$ObjectMapType[net.sf.l2j.Config.MAP_TYPE.ordinal()])
    {
    case 1:
      return new WorldObjectMap();
    }
    return new WorldObjectTree();
  }
}