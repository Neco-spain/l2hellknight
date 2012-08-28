package l2m.commons.math.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l2m.commons.util.Rnd;

public class RndSelector<E>
{
  private int totalWeight = 0;
  private final List<RndSelector<E>.RndNode<E>> nodes;

  public RndSelector()
  {
    nodes = new ArrayList();
  }

  public RndSelector(int initialCapacity)
  {
    nodes = new ArrayList(initialCapacity);
  }

  public void add(E value, int weight)
  {
    if ((value == null) || (weight <= 0))
      return;
    totalWeight += weight;
    nodes.add(new RndNode(value, weight));
  }

  public E chance(int maxWeight)
  {
    if (maxWeight <= 0) {
      return null;
    }
    Collections.sort(nodes);

    int r = Rnd.get(maxWeight);
    int weight = 0;
    for (int i = 0; i < nodes.size(); i++)
      if (weight += ((RndNode)nodes.get(i)).weight > r)
        return ((RndNode)nodes.get(i)).value;
    return null;
  }

  public E chance()
  {
    return chance(100);
  }

  public E select()
  {
    return chance(totalWeight);
  }

  public void clear()
  {
    totalWeight = 0;
    nodes.clear();
  }

  private class RndNode<T>
    implements Comparable<RndSelector<E>.RndNode<T>>
  {
    private final T value;
    private final int weight;

    public RndNode(int value)
    {
      this.value = value;
      this.weight = weight;
    }

    public int compareTo(RndSelector<E>.RndNode<T> o)
    {
      return weight - weight;
    }
  }
}