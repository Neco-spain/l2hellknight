package l2p.gameserver.model.entity.events.impl;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import l2p.commons.collections.MultiValueSet;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.time.cron.SchedulingPattern;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.events.GlobalEvent;

public class UndergroundColiseumEvent extends GlobalEvent
{
  private static final SchedulingPattern DATE_PATTERN = new SchedulingPattern("0 21 * * mon,sat,sun");

  private Calendar _startCalendar = Calendar.getInstance();
  private List<Player> _registeredPlayers = new CopyOnWriteArrayList();
  private final int _minLevel;
  private final int _maxLevel;
  private UndergroundColiseumBattleEvent _battleEvent;

  public UndergroundColiseumEvent(MultiValueSet<String> set)
  {
    super(set);
    _minLevel = set.getInteger("min_level");
    _maxLevel = set.getInteger("max_level");
  }

  public void startEvent()
  {
    super.startEvent();
  }

  public void stopEvent()
  {
    super.stopEvent();
  }

  public void reCalcNextTime(boolean onInit)
  {
    clearActions();

    _startCalendar.setTimeInMillis(DATE_PATTERN.next(System.currentTimeMillis()));

    registerActions();
  }

  protected long startTimeMillis()
  {
    return _startCalendar.getTimeInMillis();
  }

  public List<Player> getRegisteredPlayers()
  {
    return _registeredPlayers;
  }

  public int getMinLevel()
  {
    return _minLevel;
  }

  public int getMaxLevel()
  {
    return _maxLevel;
  }

  private class Timer extends RunnableImpl
  {
    private Timer()
    {
    }

    public void runImpl()
      throws Exception
    {
      if ((_registeredPlayers.size() < 2) || (_battleEvent != null)) {
        return;
      }
      Player player1 = (Player)_registeredPlayers.get(0);
      Player player2 = (Player)_registeredPlayers.get(1);

      UndergroundColiseumEvent.access$102(UndergroundColiseumEvent.this, new UndergroundColiseumBattleEvent(player1, player2));
      _battleEvent.reCalcNextTime(false);
    }
  }
}