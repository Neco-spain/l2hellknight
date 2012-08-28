package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import net.sf.l2j.Config;
import net.sf.l2j.util.Location;

public class ConfirmDlg extends L2GameServerPacket
{
  private int _requestId;
  private int _time;
  private FastMap<Integer, Point> _points = new FastMap();

  public ConfirmDlg(int requestId, String requestorName)
  {
    _requestId = requestId;
    switch (_requestId)
    {
    case 614:
      _time = Config.WEDDING_ANSWER_TIME;
      break;
    case 1510:
      _time = Config.RESURECT_ANSWER_TIME;
      break;
    case 1842:
      _time = Config.SUMMON_ANSWER_TIME;
    }

    _points.put(Integer.valueOf(0), new Point(requestorName));
  }

  public void addLoc(Location loc)
  {
    _points.put(Integer.valueOf(7), new Point(loc));
  }

  public Location getLoc()
  {
    return ((Point)_points.get(Integer.valueOf(7))).loc;
  }

  protected final void writeImpl()
  {
    writeC(237);
    writeD(_requestId);
    writeD(_points.size());

    FastMap.Entry e = _points.head(); for (FastMap.Entry end = _points.tail(); (e = e.getNext()) != end; )
    {
      Integer type = (Integer)e.getKey();
      Point value = (Point)e.getValue();
      if ((type == null) || (value == null)) {
        continue;
      }
      writeD(type.intValue());
      switch (type.intValue())
      {
      case 0:
        writeS(value.name);
        break;
      case 7:
        Location loc = value.loc;
        writeD(loc.x);
        writeD(loc.y);
        writeD(loc.z);
      }

    }

    writeD(_time);
    writeD(0);
  }

  public void clearPoints()
  {
  }

  static class Point
  {
    public String name;
    public Location loc;

    Point(String name)
    {
      this.name = name;
    }

    Point(Location loc)
    {
      this.loc = loc;
    }
  }
}