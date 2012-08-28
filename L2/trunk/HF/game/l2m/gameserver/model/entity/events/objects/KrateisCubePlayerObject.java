package l2m.gameserver.model.entity.events.objects;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Future;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.events.impl.KrateisCubeEvent;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.utils.Location;

public class KrateisCubePlayerObject
  implements Serializable, Comparable<KrateisCubePlayerObject>
{
  private static final long serialVersionUID = 1L;
  private final Player _player;
  private final long _registrationTime;
  private boolean _showRank;
  private int _points;
  private Future<?> _ressurectTask;

  public KrateisCubePlayerObject(Player player)
  {
    _player = player;
    _registrationTime = System.currentTimeMillis();
  }

  public String getName()
  {
    return _player.getName();
  }

  public boolean isShowRank()
  {
    return _showRank;
  }

  public int getPoints()
  {
    return _points;
  }

  public void setPoints(int points)
  {
    _points = points;
  }

  public void setShowRank(boolean showRank)
  {
    _showRank = showRank;
  }

  public long getRegistrationTime()
  {
    return _registrationTime;
  }

  public int getObjectId()
  {
    return _player.getObjectId();
  }

  public Player getPlayer()
  {
    return _player;
  }

  public void startRessurectTask()
  {
    if (_ressurectTask != null) {
      return;
    }
    _ressurectTask = ThreadPoolManager.getInstance().schedule(new RessurectTask(10), 1000L);
  }

  public void stopRessurectTask()
  {
    if (_ressurectTask != null)
    {
      _ressurectTask.cancel(false);
      _ressurectTask = null;
    }
  }

  public int compareTo(KrateisCubePlayerObject o)
  {
    if (getPoints() == o.getPoints())
      return (int)((getRegistrationTime() - o.getRegistrationTime()) / 1000L);
    return getPoints() - o.getPoints();
  }

  private class RessurectTask extends RunnableImpl
  {
    private int _seconds;

    public RessurectTask(int seconds)
    {
      _seconds = seconds;
    }

    public void runImpl()
      throws Exception
    {
      if (_seconds <= 0)
      {
        KrateisCubeEvent cubeEvent = (KrateisCubeEvent)_player.getEvent(KrateisCubeEvent.class);
        List waitLocs = cubeEvent.getObjects("wait_locs");

        KrateisCubePlayerObject.access$102(KrateisCubePlayerObject.this, null);

        _player.teleToLocation((Location)Rnd.get(waitLocs));
        _player.doRevive();
      }
      else
      {
        _player.sendPacket(new SystemMessage2(SystemMsg.RESURRECTION_WILL_TAKE_PLACE_IN_THE_WAITING_ROOM_AFTER_S1_SECONDS).addInteger(_seconds));
        KrateisCubePlayerObject.access$102(KrateisCubePlayerObject.this, ThreadPoolManager.getInstance().schedule(new RessurectTask(KrateisCubePlayerObject.this, _seconds - 1), 1000L));
      }
    }
  }
}