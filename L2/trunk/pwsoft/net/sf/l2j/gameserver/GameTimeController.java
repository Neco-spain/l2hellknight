package net.sf.l2j.gameserver;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.instancemanager.DayNightSpawnManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.util.log.AbstractLogger;

public class GameTimeController
{
  private static final Logger _log = AbstractLogger.getLogger(GameTimeController.class.getName());
  public static final int TICKS_PER_SECOND = 10;
  public static final int MILLIS_IN_TICK = 100;
  private static GameTimeController _ins;
  private static int _gt;
  private static long _gst;
  private static boolean _in = false;
  private static ConcurrentLinkedQueue<L2Character> _movingObjects = new ConcurrentLinkedQueue();
  private static Thread _timer;
  private boolean _interruptRequest = false;

  public static GameTimeController getInstance()
  {
    return _ins;
  }

  public static void init()
  {
    _ins = new GameTimeController();
    _ins.load();
  }

  private void load()
  {
    _gst = System.currentTimeMillis() - 3600000L;
    _gt = 36000;

    _timer = new Thread(new TimerThread());
    _timer.setName("GameTimeController");
    _timer.setDaemon(true);
    _timer.setPriority(10);
    _timer.start();

    ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new BroadcastSunState(), 0L, TimeUnit.MINUTES.toMillis(10L));

    ThreadPoolManager.getInstance().scheduleGeneral(new ZakenDoor(1), TimeUnit.HOURS.toMillis(3L));
  }

  public boolean isNowNight() {
    return _in;
  }

  public int getGameTime() {
    return _gt / 100;
  }

  public static int getGameTicks() {
    return _gt;
  }

  public void registerMovingObject(L2Character cha)
  {
    if (cha == null) {
      return;
    }
    if (_movingObjects.contains(cha)) {
      return;
    }

    _movingObjects.add(cha);
  }

  protected void moveObjects()
  {
    ConcurrentLinkedQueue ended = new ConcurrentLinkedQueue();

    for (L2Character cha : _movingObjects)
    {
      if (cha.updatePosition(_gt)) {
        _movingObjects.remove(cha);
        ended.add(cha);
      }

    }

    if (!ended.isEmpty())
      ThreadPoolManager.getInstance().scheduleGeneral(new MovingObjectArrived(ended), 10L);
  }

  public void stopTimer()
  {
    _interruptRequest = true;
    _timer.interrupt();
  }

  private class ZakenDoor
    implements Runnable
  {
    private int act;

    public ZakenDoor(int act)
    {
      this.act = act;
    }

    public void run()
    {
      switch (act) {
      case 1:
        DoorTable.getInstance().getDoor(Integer.valueOf(21240006)).openMe();
        ThreadPoolManager.getInstance().scheduleGeneral(new ZakenDoor(GameTimeController.this, 2), 120000L);
        ThreadPoolManager.getInstance().scheduleGeneral(new ZakenDoor(GameTimeController.this, 1), TimeUnit.HOURS.toMillis(4L));
        break;
      case 2:
        DoorTable.getInstance().getDoor(Integer.valueOf(21240006)).closeMe();
        DoorTable.getInstance().checkDoorsBetween();
      }
    }
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

      if (tempIsNight != GameTimeController._in) {
        GameTimeController.access$402(tempIsNight);

        DayNightSpawnManager.getInstance().notifyChangeMode();
      }
    }
  }

  static class MovingObjectArrived
    implements Runnable
  {
    private final ConcurrentLinkedQueue<L2Character> ended;

    MovingObjectArrived(ConcurrentLinkedQueue<L2Character> ended)
    {
      this.ended = ended;
    }

    public void run()
    {
      for (L2Character cha : ended)
        try {
          cha.getKnownList().updateKnownObjects();
          cha.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
        } catch (NullPointerException e) {
          e.printStackTrace();
        }
    }
  }

  class TimerThread
    implements Runnable
  {
    public TimerThread()
    {
    }

    public void run()
    {
      while (true)
        try
        {
          int oldTicks = GameTimeController._gt;
          long runtime = System.currentTimeMillis() - GameTimeController._gst;

          GameTimeController.access$002((int)(runtime / 100L));

          if (oldTicks != GameTimeController._gt) {
            moveObjects();
          }

          runtime = System.currentTimeMillis() - GameTimeController._gst - runtime;

          int sleepTime = 101 - (int)runtime % 100;

          if (sleepTime > 0) {
            Thread.sleep(sleepTime);
          }

          continue;
        }
        catch (InterruptedException ie)
        {
          if (_interruptRequest) {
            return;
          }

          GameTimeController._log.warning("" + ie);
          ie.printStackTrace();

          continue;
        }
        catch (Exception e)
        {
          GameTimeController._log.warning("" + e);
          e.printStackTrace();
        }
    }
  }
}