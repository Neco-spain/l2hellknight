package net.sf.l2j.gameserver.model.waypoint;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.poly.ObjectPoly;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.util.Point3D;

public class WayPointNode extends L2Object
{
  private int _id;
  private String _title;
  private String _type;
  private static final String NORMAL = "Node";
  private static final String SELECTED = "Selected";
  private static final String LINKED = "Linked";
  private static int _lineId = 5560;
  private static final String LINE_TYPE = "item";
  private Map<WayPointNode, List<WayPointNode>> _linkLists;

  public WayPointNode(int objectId)
  {
    super(objectId);
    _linkLists = Collections.synchronizedMap(new WeakHashMap());
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    return false;
  }

  public static WayPointNode spawn(String type, int id, int x, int y, int z)
  {
    WayPointNode newNode = new WayPointNode(IdFactory.getInstance().getNextId());
    newNode.getPoly().setPolyInfo(type, id + "");
    newNode.spawnMe(x, y, z);
    return newNode;
  }

  public static WayPointNode spawn(boolean isItemId, int id, L2PcInstance player)
  {
    return spawn(isItemId ? "item" : "npc", id, player.getX(), player.getY(), player.getZ());
  }

  public static WayPointNode spawn(boolean isItemId, int id, Point3D point)
  {
    return spawn(isItemId ? "item" : "npc", id, point.getX(), point.getY(), point.getZ());
  }

  public static WayPointNode spawn(Point3D point)
  {
    return spawn(Config.NEW_NODE_TYPE, Config.NEW_NODE_ID, point.getX(), point.getY(), point.getZ());
  }

  public static WayPointNode spawn(L2PcInstance player)
  {
    return spawn(Config.NEW_NODE_TYPE, Config.NEW_NODE_ID, player.getX(), player.getY(), player.getZ());
  }

  public void onAction(L2PcInstance player)
  {
    if (player.getTarget() != this)
    {
      player.setTarget(this);
      MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
      player.sendPacket(my);
    }
  }

  public void setNormalInfo(String type, int id, String title)
  {
    _type = type;
    changeID(id, title);
  }

  public void setNormalInfo(String type, int id)
  {
    _type = type;
    changeID(id);
  }

  private void changeID(int id)
  {
    _id = id;
    toggleVisible();
    toggleVisible();
  }

  private void changeID(int id, String title)
  {
    setName(title);
    setTitle(title);
    changeID(id);
  }

  public void setLinked()
  {
    changeID(Config.LINKED_NODE_ID, "Linked");
  }

  public void setNormal()
  {
    changeID(Config.NEW_NODE_ID, "Node");
  }

  public void setSelected()
  {
    changeID(Config.SELECTED_NODE_ID, "Selected");
  }

  public boolean isMarker()
  {
    return true;
  }

  public final String getTitle()
  {
    return _title;
  }

  public final void setTitle(String title)
  {
    _title = title;
  }

  public int getId()
  {
    return _id;
  }

  public String getType()
  {
    return _type;
  }

  public void setType(String type)
  {
    _type = type;
  }

  public static void drawLine(WayPointNode nodeA, WayPointNode nodeB)
  {
    int x1 = nodeA.getX(); int y1 = nodeA.getY(); int z1 = nodeA.getZ();
    int x2 = nodeB.getX(); int y2 = nodeB.getY(); int z2 = nodeB.getZ();
    int modX = x1 - x2 > 0 ? -1 : 1;
    int modY = y1 - y2 > 0 ? -1 : 1;
    int modZ = z1 - z2 > 0 ? -1 : 1;

    int diffX = Math.abs(x1 - x2);
    int diffY = Math.abs(y1 - y2);
    int diffZ = Math.abs(z1 - z2);

    int distance = (int)Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);

    int steps = distance / 40;

    List lineNodes = new FastList();

    for (int i = 0; i < steps; i++)
    {
      x1 += modX * diffX / steps;
      y1 += modY * diffY / steps;
      z1 += modZ * diffZ / steps;

      lineNodes.add(spawn("item", _lineId, x1, y1, z1));
    }

    nodeA.addLineInfo(nodeB, lineNodes);
    nodeB.addLineInfo(nodeA, lineNodes);
  }

  public void addLineInfo(WayPointNode node, List<WayPointNode> line)
  {
    _linkLists.put(node, line);
  }

  public static void eraseLine(WayPointNode target, WayPointNode selectedNode)
  {
    List lineNodes = target.getLineInfo(selectedNode);
    if (lineNodes == null) return;
    for (WayPointNode node : lineNodes)
    {
      node.decayMe();
    }
    target.eraseLine(selectedNode);
    selectedNode.eraseLine(target);
  }

  public void eraseLine(WayPointNode target)
  {
    _linkLists.remove(target);
  }

  private List<WayPointNode> getLineInfo(WayPointNode selectedNode)
  {
    return (List)_linkLists.get(selectedNode);
  }

  public static void setLineId(int line_id)
  {
    _lineId = line_id;
  }

  public List<WayPointNode> getLineNodes()
  {
    List list = new FastList();

    for (List points : _linkLists.values())
    {
      list.addAll(points);
    }

    return list;
  }
}