package net.sf.l2j.gameserver.model;

import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.RadarControl;

public final class L2Radar
{
  private L2PcInstance _player;
  private FastTable<RadarMarker> _markers;

  public L2Radar(L2PcInstance player)
  {
    _player = player;
    _markers = new FastTable();
  }

  public void addMarker(int x, int y, int z)
  {
    RadarMarker newMarker = new RadarMarker(x, y, z);

    _markers.add(newMarker);
    _player.sendPacket(new RadarControl(0, 1, x, y, z));
  }

  public void removeMarker(int x, int y, int z)
  {
    RadarMarker newMarker = new RadarMarker(x, y, z);

    _markers.remove(newMarker);
    _player.sendPacket(new RadarControl(1, 1, x, y, z));
  }

  public void removeAllMarkers()
  {
    for (RadarMarker tempMarker : _markers) {
      _player.sendPacket(new RadarControl(1, tempMarker._type, tempMarker._x, tempMarker._y, tempMarker._z));
    }
    _markers.clear();
  }
  public void loadMarkers() {
  }

  public static class RadarMarker {
    public int _type;
    public int _x;
    public int _y;
    public int _z;

    public RadarMarker(int type, int x, int y, int z) {
      _type = type;
      _x = x;
      _y = y;
      _z = z;
    }

    public RadarMarker(int x, int y, int z)
    {
      _type = 1;
      _x = x;
      _y = y;
      _z = z;
    }

    public boolean equals(Object obj)
    {
      if (obj == null) {
        return false;
      }
      try
      {
        RadarMarker temp = (RadarMarker)obj;

        return (temp._x == _x) && (temp._y == _y) && (temp._z == _z) && (temp._type == _type);
      }
      catch (Exception e)
      {
      }

      return false;
    }
  }
}