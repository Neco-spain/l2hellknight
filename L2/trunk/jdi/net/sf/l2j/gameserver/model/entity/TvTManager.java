package net.sf.l2j.gameserver.model.entity;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;

public class TvTManager
{
  protected static final Logger _log = Logger.getLogger(TvTManager.class.getName());
  private TvTEventStartTask _task;

  private TvTManager()
  {
    if (Config.TVT_EVENT_ENABLED)
    {
      TvTEvent.init();

      scheduleEventStart();
      _log.info("TvTEventEngine[TvTEventManager.TvTEventManager()]: Started.");
    }
    else
    {
      _log.info("TvTEventEngine[TvTEventManager.TvTEventManager()]: Engine is disabled.");
    }
  }

  public static TvTManager getInstance()
  {
    return SingletonHolder._instance;
  }

  public void scheduleEventStart()
  {
    try
    {
      Calendar currentTime = Calendar.getInstance();
      Calendar nextStartTime = null;
      Calendar testStartTime = null;
      for (String timeOfDay : Config.TVT_EVENT_INTERVAL)
      {
        testStartTime = Calendar.getInstance();
        testStartTime.setLenient(true);
        String[] splitTimeOfDay = timeOfDay.split(":");
        testStartTime.set(11, Integer.parseInt(splitTimeOfDay[0]));
        testStartTime.set(12, Integer.parseInt(splitTimeOfDay[1]));

        if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
        {
          testStartTime.add(5, 1);
        }

        if ((nextStartTime != null) && (testStartTime.getTimeInMillis() >= nextStartTime.getTimeInMillis()))
          continue;
        nextStartTime = testStartTime;
      }

      _task = new TvTEventStartTask(nextStartTime.getTimeInMillis());
      ThreadPoolManager.getInstance().executeTask(_task);
    }
    catch (Exception e)
    {
      _log.warning("TvTEventEngine[TvTEventManager.scheduleEventStart()]: Error figuring out a start time. Check TvTEventInterval in config file.");
    }
  }

  public void startReg()
  {
    if (!TvTEvent.startParticipation())
    {
      Announcements.getInstance().announceToAll("TvT: Event was cancelled.");
      _log.warning("TvTEventEngine[TvTEventManager.run()]: Error spawning event npc for participation.");

      scheduleEventStart();
    }
    else
    {
      Announcements.getInstance().announceToAll("TvT: Registration opened for " + Config.TVT_EVENT_PARTICIPATION_TIME + " minute(s).");

      _task.setStartTime(System.currentTimeMillis() + 60000L * Config.TVT_EVENT_PARTICIPATION_TIME);
      ThreadPoolManager.getInstance().executeTask(_task);
    }
  }

  public void startEvent()
  {
    if (!TvTEvent.startFight())
    {
      Announcements.getInstance().announceToAll("TvT: Event cancelled due to lack of Participation.");

      _log.info("TvTEventEngine[TvTEventManager.run()]: Lack of registration, abort event.");

      scheduleEventStart();
    }
    else
    {
      TvTEvent.sysMsgToAllParticipants("TvT: Teleporting participants to an arena in " + Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY + " second(s).");

      _task.setStartTime(System.currentTimeMillis() + 60000L * Config.TVT_EVENT_RUNNING_TIME);
      ThreadPoolManager.getInstance().executeTask(_task);
    }
  }

  public void endEvent()
  {
    Announcements.getInstance().announceToAll(TvTEvent.calculateRewards());
    TvTEvent.sysMsgToAllParticipants("TvT: Teleporting back to the registration npc in " + Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY + " second(s).");

    TvTEvent.stopFight();

    scheduleEventStart();
  }

  public void skipDelay()
  {
    if (_task.nextRun.cancel(false))
    {
      _task.setStartTime(System.currentTimeMillis());
      ThreadPoolManager.getInstance().executeTask(_task);
    }
  }

  class TvTEventStartTask
    implements Runnable
  {
    private long _startTime;
    public ScheduledFuture<?> nextRun;

    public TvTEventStartTask(long startTime)
    {
      _startTime = startTime;
    }

    public void setStartTime(long startTime)
    {
      _startTime = startTime;
    }

    public void run()
    {
      int delay = (int)Math.round((_startTime - System.currentTimeMillis()) / 1000.0D);

      if (delay > 0)
      {
        announce(delay);
      }

      int nextMsg = 0;
      if (delay > 3600)
      {
        nextMsg = delay - 3600;
      }
      else if (delay > 1800)
      {
        nextMsg = delay - 1800;
      }
      else if (delay > 900)
      {
        nextMsg = delay - 900;
      }
      else if (delay > 600)
      {
        nextMsg = delay - 600;
      }
      else if (delay > 300)
      {
        nextMsg = delay - 300;
      }
      else if (delay > 60)
      {
        nextMsg = delay - 60;
      }
      else if (delay > 5)
      {
        nextMsg = delay - 5;
      }
      else if (delay > 0)
      {
        nextMsg = delay;
      }
      else if (TvTEvent.isInactive())
      {
        startReg();
      }
      else if (TvTEvent.isParticipating())
      {
        startEvent();
      }
      else
      {
        endEvent();
      }

      if (delay > 0)
      {
        nextRun = ThreadPoolManager.getInstance().scheduleGeneral(this, nextMsg * 1000);
      }
    }

    private void announce(long time)
    {
      if ((time >= 3600L) && (time % 3600L == 0L))
      {
        if (TvTEvent.isParticipating())
        {
          Announcements.getInstance().announceToAll("TvT: " + time / 60L / 60L + " hour(s) until registration is closed!");
        }
        else if (TvTEvent.isStarted())
        {
          TvTEvent.sysMsgToAllParticipants("TvT: " + time / 60L / 60L + " hour(s) until event is finished!");
        }
      }
      else if (time >= 60L)
      {
        if (TvTEvent.isParticipating())
        {
          Announcements.getInstance().announceToAll("TvT: " + time / 60L + " minute(s) until registration is closed!");
        }
        else if (TvTEvent.isStarted())
        {
          TvTEvent.sysMsgToAllParticipants("TvT: " + time / 60L + " minute(s) until the event is finished!");
        }

      }
      else if (TvTEvent.isParticipating())
      {
        Announcements.getInstance().announceToAll("TvT: " + time + " second(s) until registration is closed!");
      }
      else if (TvTEvent.isStarted())
      {
        TvTEvent.sysMsgToAllParticipants("TvT: " + time + " second(s) until the event is finished!");
      }
    }
  }

  private static class SingletonHolder
  {
    protected static final TvTManager _instance = new TvTManager(null);
  }
}