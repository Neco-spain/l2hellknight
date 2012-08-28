package net.sf.l2j.util;

import java.util.Iterator;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;

public abstract class L2ObjectSet<T extends L2Object>
  implements Iterable<T>
{
  public static L2ObjectSet<L2Object> createL2ObjectSet()
  {
    switch (1.$SwitchMap$net$sf$l2j$Config$ObjectSetType[net.sf.l2j.Config.SET_TYPE.ordinal()])
    {
    case 1:
      return new WorldObjectSet();
    }
    return new L2ObjectHashSet();
  }

  public static L2ObjectSet<L2PlayableInstance> createL2PlayerSet()
  {
    switch (1.$SwitchMap$net$sf$l2j$Config$ObjectSetType[net.sf.l2j.Config.SET_TYPE.ordinal()])
    {
    case 1:
      return new WorldObjectSet();
    }
    return new L2ObjectHashSet();
  }

  public abstract int size();

  public abstract boolean isEmpty();

  public abstract void clear();

  public abstract void put(T paramT);

  public abstract void remove(T paramT);

  public abstract boolean contains(T paramT);

  public abstract Iterator<T> iterator();
}