package net.sf.l2j.gameserver.pathfinding.tools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.pathfinding.AbstractNodeLoc;
import net.sf.l2j.gameserver.pathfinding.GeoNode;

public class CellNodeMap
{
  private final Map<Integer, FastTable<GeoNode>> map = new ConcurrentHashMap();

  public void add(GeoNode n) {
    int y = n.getLoc().getY();
    if (map.containsKey(Integer.valueOf(y))) {
      ((FastTable)map.get(Integer.valueOf(y))).add(n);
    } else {
      FastTable array = new FastTable();
      array.add(n);
      map.put(Integer.valueOf(y), array);
    }
  }

  public boolean contains(GeoNode n) {
    FastTable array = (FastTable)map.get(Integer.valueOf(n.getLoc().getY()));
    if (array == null) {
      return false;
    }

    return array.contains(n);
  }
}