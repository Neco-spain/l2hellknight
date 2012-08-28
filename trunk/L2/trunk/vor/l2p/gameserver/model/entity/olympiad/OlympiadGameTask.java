package l2p.gameserver.model.entity.olympiad;

import java.util.concurrent.ScheduledFuture;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OlympiadGameTask extends RunnableImpl
{
  private static final Logger _log = LoggerFactory.getLogger(OlympiadGameTask.class);
  private OlympiadGame _game;
  private BattleStatus _status;
  private int _count;
  private long _time;
  private boolean _terminated = false;

  public boolean isTerminated()
  {
    return _terminated;
  }

  public BattleStatus getStatus()
  {
    return _status;
  }

  public int getCount()
  {
    return _count;
  }

  public OlympiadGame getGame()
  {
    return _game;
  }

  public long getTime()
  {
    return _count;
  }

  public ScheduledFuture<?> shedule()
  {
    return ThreadPoolManager.getInstance().schedule(this, _time);
  }

  public OlympiadGameTask(OlympiadGame game, BattleStatus status, int count, long time)
  {
    _game = game;
    _status = status;
    _count = count;
    _time = time;
  }

  public void runImpl()
    throws Exception
  {
    if ((_game == null) || (_terminated)) {
      return;
    }
    OlympiadGameTask task = null;

    int gameId = _game.getId();
    try
    {
      if (!Olympiad.inCompPeriod()) {
        return;
      }

      if ((!_game.checkPlayersOnline()) && (_status != BattleStatus.ValidateWinner) && (_status != BattleStatus.Ending))
      {
        Log.add("Player is offline for game " + gameId + ", status: " + _status, "olympiad");
        _game.endGame(1000L, true);
        return;
      }

      switch (1.$SwitchMap$l2p$gameserver$model$entity$olympiad$BattleStatus[_status.ordinal()])
      {
      case 1:
        _game.broadcastPacket(new SystemMessage(1492).addNumber(120), true, false);
        task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, 60, 60000L);
        break;
      case 2:
        _game.broadcastPacket(new SystemMessage(1492).addNumber(_count), true, false);
        if (_count == 60) {
          task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, 30, 30000L);
        } else if (_count == 30) {
          task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, 15, 15000L);
        } else if (_count == 15) {
          task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, 5, 10000L);
        } else if ((_count < 6) && (_count > 1)) {
          task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, _count - 1, 1000L); } else {
          if (_count != 1) break;
          task = new OlympiadGameTask(_game, BattleStatus.PortPlayers, 0, 1000L); } break;
      case 3:
        _game.portPlayersToArena();
        _game.managerShout();
        task = new OlympiadGameTask(_game, BattleStatus.Started, 60, 1000L);
        break;
      case 4:
        if (_count == 60)
        {
          _game.setState(1);
          _game.preparePlayers();
          _game.addBuffers();
        }

        _game.broadcastPacket(new SystemMessage(1495).addNumber(_count), true, true);
        _count -= 10;

        if (_count > 0)
        {
          task = new OlympiadGameTask(_game, BattleStatus.Started, _count, 10000L);
        }
        else
        {
          _game.openDoors();

          task = new OlympiadGameTask(_game, BattleStatus.CountDown, 5, 5000L);
        }break;
      case 5:
        _game.broadcastPacket(new SystemMessage(1495).addNumber(_count), true, true);
        _count -= 1;
        if (_count <= 0)
          task = new OlympiadGameTask(_game, BattleStatus.StartComp, 36, 1000L);
        else
          task = new OlympiadGameTask(_game, BattleStatus.CountDown, _count, 1000L);
        break;
      case 6:
        _game.deleteBuffers();
        if (_count == 36)
        {
          _game.setState(2);
          _game.broadcastPacket(Msg.STARTS_THE_GAME, true, true);
          _game.broadcastInfo(null, null, false);
        }

        _count -= 1;
        if (_count == 0)
          task = new OlympiadGameTask(_game, BattleStatus.ValidateWinner, 0, 10000L);
        else
          task = new OlympiadGameTask(_game, BattleStatus.StartComp, _count, 10000L);
        break;
      case 7:
        try
        {
          _game.validateWinner(_count > 0);
        }
        catch (Exception e)
        {
          _log.error("", e);
        }
        task = new OlympiadGameTask(_game, BattleStatus.Ending, 0, 20000L);
        break;
      case 8:
        _game.collapse();
        _terminated = true;
        if (Olympiad._manager != null)
          Olympiad._manager.freeOlympiadInstance(_game.getId());
        return;
      }

      if (task == null)
      {
        Log.add("task == null for game " + gameId, "olympiad");
        Thread.dumpStack();
        _game.endGame(1000L, true);
        return;
      }

      _game.sheduleTask(task);
    }
    catch (Exception e)
    {
      _log.error("", e);
      _game.endGame(1000L, true);
    }
  }
}