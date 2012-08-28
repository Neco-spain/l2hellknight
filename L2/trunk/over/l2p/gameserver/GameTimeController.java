package l2p.gameserver;

import java.util.Calendar;
import l2p.commons.listener.Listener;
import l2p.commons.listener.ListenerList;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.listener.GameListener;
import l2p.gameserver.listener.game.OnDayNightChangeListener;
import l2p.gameserver.listener.game.OnStartListener;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.ClientSetTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameTimeController
{
  private static final Logger _log = LoggerFactory.getLogger(GameTimeController.class);
  public static final int TICKS_PER_SECOND = 10;
  public static final int MILLIS_IN_TICK = 100;
  private static final GameTimeController _instance = new GameTimeController();
  private long _gameStartTime;
  private GameTimeListenerList listenerEngine = new GameTimeListenerList();
  private Runnable _dayChangeNotify = new CheckSunState();

  public static final GameTimeController getInstance()
  {
    return _instance;
  }

  private GameTimeController()
  {
    _gameStartTime = getDayStartTime();

    GameServer.getInstance().addListener(new OnStartListenerImpl(null));

    StringBuilder msg = new StringBuilder();
    msg.append("GameTimeController: initialized.").append(" ");
    msg.append("Current time is ");
    msg.append(getGameHour()).append(":");
    if (getGameMin() < 10)
      msg.append("0");
    msg.append(getGameMin());
    msg.append(" in the ");
    if (isNowNight())
      msg.append("night");
    else
      msg.append("day");
    msg.append(".");

    _log.info(msg.toString());

    long nightStart = 0L;
    long dayStart = 3600000L;

    while (_gameStartTime + nightStart < System.currentTimeMillis()) {
      nightStart += 14400000L;
    }
    while (_gameStartTime + dayStart < System.currentTimeMillis()) {
      dayStart += 14400000L;
    }
    dayStart -= System.currentTimeMillis() - _gameStartTime;
    nightStart -= System.currentTimeMillis() - _gameStartTime;

    ThreadPoolManager.getInstance().scheduleAtFixedRate(_dayChangeNotify, nightStart, 14400000L);
    ThreadPoolManager.getInstance().scheduleAtFixedRate(_dayChangeNotify, dayStart, 14400000L);
  }

  private long getDayStartTime()
  {
    Calendar dayStart = Calendar.getInstance();

    int HOUR_OF_DAY = dayStart.get(11);

    dayStart.add(11, -(HOUR_OF_DAY + 1) % 4);
    dayStart.set(12, 0);
    dayStart.set(13, 0);
    dayStart.set(14, 0);

    return dayStart.getTimeInMillis();
  }

  public boolean isNowNight()
  {
    return getGameHour() < 6;
  }

  public int getGameTime()
  {
    return getGameTicks() / 100;
  }

  public int getGameHour()
  {
    return getGameTime() / 60 % 24;
  }

  public int getGameMin()
  {
    return getGameTime() % 60;
  }

  public int getGameTicks()
  {
    return (int)((System.currentTimeMillis() - _gameStartTime) / 100L);
  }

  public GameTimeListenerList getListenerEngine()
  {
    return listenerEngine;
  }

  public <T extends GameListener> boolean addListener(T listener)
  {
    return listenerEngine.add(listener);
  }

  public <T extends GameListener> boolean removeListener(T listener)
  {
    return listenerEngine.remove(listener);
  }

  protected class GameTimeListenerList extends ListenerList<GameServer>
  {
    protected GameTimeListenerList()
    {
    }

    public void onDay()
    {
      for (Listener listener : getListeners())
        if (OnDayNightChangeListener.class.isInstance(listener))
          ((OnDayNightChangeListener)listener).onDay();
    }

    public void onNight()
    {
      for (Listener listener : getListeners())
        if (OnDayNightChangeListener.class.isInstance(listener))
          ((OnDayNightChangeListener)listener).onNight();
    }
  }

  public class CheckSunState extends RunnableImpl
  {
    public CheckSunState()
    {
    }

    public void runImpl()
      throws Exception
    {
      if (isNowNight())
        GameTimeController.getInstance().getListenerEngine().onNight();
      else {
        GameTimeController.getInstance().getListenerEngine().onDay();
      }
      for (Player player : GameObjectsStorage.getAllPlayersForIterate())
      {
        player.checkDayNightMessages();
        player.sendPacket(new ClientSetTime());
      }
    }
  }

  private class OnStartListenerImpl
    implements OnStartListener
  {
    private OnStartListenerImpl()
    {
    }

    public void onStart()
    {
      ThreadPoolManager.getInstance().execute(_dayChangeNotify);
    }
  }
}