package net.sf.l2j.gameserver.model;

import java.util.Vector;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.RadarControl;

public final class L2Radar
{
  private L2PcInstance _player;
  private Vector<RadarMarker> _markers;

  public L2Radar(L2PcInstance player)
  {
    _player = player;
    _markers = new Vector();
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
    _markers.removeAllElements();
  }

  public void loadMarkers()
  {
  }

  public class RadarOnPlayer
    implements Runnable
  {
    private final L2PcInstance _myTarget;
    private final L2PcInstance _me;

    public RadarOnPlayer(L2PcInstance target, L2PcInstance me)
    {
      _me = me;
      _myTarget = target;
    }
    public void run() {
      try {
        if ((_me == null) || (_me.isOnline() == 0))
          return;
        _me.sendPacket(new RadarControl(1, 1, _me.getX(), _me.getY(), _me.getZ()));
        if ((_myTarget == null) || (_myTarget.isOnline() == 0) || (!_myTarget._haveFlagCTF)) {
          return;
        }
        _me.sendPacket(new RadarControl(0, 1, _myTarget.getX(), _myTarget.getY(), _myTarget.getZ()));
        ThreadPoolManager.getInstance().scheduleGeneral(new RadarOnPlayer(L2Radar.this, _myTarget, _me), 15000L);
      }
      catch (Throwable t)
      {
      }
    }
  }

  public class RadarMarker
  {
    public int _type;
    public int _x;
    public int _y;
    public int _z;

    public RadarMarker(int type, int x, int y, int z)
    {
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