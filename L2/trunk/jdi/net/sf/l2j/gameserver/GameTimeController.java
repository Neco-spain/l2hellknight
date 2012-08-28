package net.sf.l2j.gameserver;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.instancemanager.DayNightSpawnManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;

public class GameTimeController
{
  static final Logger _log = Logger.getLogger(GameTimeController.class.getName());
  public static final int TICKS_PER_SECOND = 10;
  public static final int MILLIS_IN_TICK = 100;
  private static GameTimeController _instance = new GameTimeController();
  protected static int _gameTicks;
  protected static long _gameStartTime;
  protected static boolean _isNight = false;

  private static Map<Integer, L2Character> _movingObjects = new FastMap().setShared(true);
  protected static TimerThread _timer;
  private ScheduledFuture<?> _timerWatcher;

  public static GameTimeController getInstance()
  {
    return _instance;
  }

  private GameTimeController()
  {
    _gameStartTime = System.currentTimeMillis() - 3600000L;
    _gameTicks = 36000;

    _timer = new TimerThread();
    _timer.start();

    _timerWatcher = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new TimerWatcher(), 0L, 1000L);
    ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new BroadcastSunState(), 0L, 600000L);
  }

  public boolean isNowNight()
  {
    return _isNight;
  }

  public int getGameTime()
  {
    return _gameTicks / 100;
  }

  public static int getGameTicks()
  {
    return _gameTicks;
  }

  public void registerMovingObject(L2Character cha)
  {
    if (cha == null) return;
    if (!_movingObjects.containsKey(Integer.valueOf(cha.getObjectId()))) _movingObjects.put(Integer.valueOf(cha.getObjectId()), cha);
  }

  protected void moveObjects()
  {
    FastList ended = null;

    for (L2Character ch : _movingObjects.values())
    {
      if ((ch != null) && 
        (ch.updatePosition(_gameTicks)))
      {
        if (ended == null) {
          ended = new FastList();
        }
        ended.add(ch);
      }
    }
    if (ended != null)
    {
      _movingObjects.values().removeAll(ended);
      for (L2Character ch : ended)
        if (ch != null)
          ThreadPoolManager.getInstance().executeTask(new MovingObjectArrived(ch));
      ended.clear();
    }
  }

  public void stopTimer()
  {
    _timerWatcher.cancel(true);
    _timer.interrupt();
  }

  class BroadcastSunState
    implements Runnable
  {
    BroadcastSunState()
    {
    }

    public void run()
    {
      int h = getGameTime() / 60 % 24;
      boolean tempIsNight = h < 6;

      if (tempIsNight != GameTimeController._isNight) {
        GameTimeController._isNight = tempIsNight;

        DayNightSpawnManager.getInstance().notifyChangeMode();
      }
    }
  }

  class MovingObjectArrived
    implements Runnable
  {
    private final L2Character _ended;

    MovingObjectArrived(L2Character ended)
    {
      _ended = ended;
    }

    public void run()
    {
      try
      {
        if (_ended.hasAI())
        {
          if (Config.MOVE_BASED_KNOWNLIST) _ended.getKnownList().findObjects();
          _ended.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
        }
      }
      catch (NullPointerException e)
      {
        e.printStackTrace();
      }
    }
  }

  class TimerWatcher
    implements Runnable
  {
    TimerWatcher()
    {
    }

    public void run()
    {
      if (!GameTimeController._timer.isAlive())
      {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        GameTimeController._log.warning(time + " TimerThread stop with following error. restart it.");
        if (GameTimeController._timer._error != null) GameTimeController._timer._error.printStackTrace();

        GameTimeController._timer = new GameTimeController.TimerThread(GameTimeController.this);
        GameTimeController._timer.start();
      }
    }
  }

  class TimerThread extends Thread
  {
    protected Exception _error;

    public TimerThread()
    {
      super();
      setDaemon(true);
      setPriority(10);
      _error = null;
    }

    public void run()
    {
      try
      {
        while (true)
        {
          int _oldTicks = GameTimeController._gameTicks;
          long runtime = System.currentTimeMillis() - GameTimeController._gameStartTime;

          GameTimeController._gameTicks = (int)(runtime / 100L);

          if (_oldTicks != GameTimeController._gameTicks) moveObjects();

          runtime = System.currentTimeMillis() - GameTimeController._gameStartTime - runtime;

          int sleepTime = 101 - (int)runtime % 100;

          sleep(sleepTime);
        }

      }
      catch (Exception e)
      {
        _error = e;
      }
    }
  }
}