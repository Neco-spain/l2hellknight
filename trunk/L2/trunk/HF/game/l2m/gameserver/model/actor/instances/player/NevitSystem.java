package l2m.gameserver.model.actor.instances.player;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.serverpackets.ExNavitAdventEffect;
import l2m.gameserver.network.serverpackets.ExNavitAdventPointInfo;
import l2m.gameserver.network.serverpackets.ExNavitAdventTimeChange;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.skills.AbnormalEffect;

public class NevitSystem
{
  public static final int ADVENT_TIME = 14400;
  private static final int MAX_POINTS = 7200;
  private static final int BONUS_EFFECT_TIME = 180;
  private Player _player;
  private int _points = 0;
  private int _time;
  private ScheduledFuture<?> _adventTask;
  private ScheduledFuture<?> _nevitEffectTask;
  private int _percent;
  private boolean _active;

  public NevitSystem(Player player)
  {
    _player = player;
  }

  public void setPoints(int points, int time)
  {
    _points = points;
    _active = false;
    _percent = getPercent(_points);

    Calendar temp = Calendar.getInstance();
    temp.set(11, 6);
    temp.set(12, 30);
    temp.set(13, 0);
    temp.set(14, 0);
    if ((_player.getLastAccess() < temp.getTimeInMillis() / 1000L) && (System.currentTimeMillis() > temp.getTimeInMillis()))
      _time = 14400;
    else
      _time = time;
  }

  public void restartSystem()
  {
    _time = 14400;
    _player.sendPacket(new ExNavitAdventTimeChange(_active, _time));
  }

  public void onEnterWorld()
  {
    _player.sendPacket(new ExNavitAdventPointInfo(_points));
    _player.sendPacket(new ExNavitAdventTimeChange(_active, _time));
    startNevitEffect(_player.getVarInt("nevit", 0));
    if ((_percent >= 45) && (_percent < 50))
      _player.sendPacket(SystemMsg.YOU_ARE_STARTING_TO_FEEL_THE_EFFECTS_OF_NEVITS_BLESSING);
    else if ((_percent >= 50) && (_percent < 75))
      _player.sendPacket(SystemMsg.YOU_ARE_FURTHER_INFUSED_WITH_THE_BLESSINGS_OF_NEVIT_CONTINUE_TO_BATTLE_EVIL_WHEREVER_IT_MAY_LURK);
    else if (_percent >= 75)
      _player.sendPacket(SystemMsg.NEVITS_BLESSING_SHINES_STRONGLY_FROM_ABOVE_YOU_CAN_ALMOST_SEE_HIS_DIVINE_AURA);
  }

  public void startAdventTask()
  {
    if (!_active)
    {
      _active = true;
      if ((_time > 0) && (_adventTask == null)) {
        _adventTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AdventTask(null), 30000L, 30000L);
      }
      _player.sendPacket(new ExNavitAdventTimeChange(_active, _time));
    }
  }

  private void startNevitEffect(int time)
  {
    if (getEffectTime() > 0)
    {
      stopNevitEffectTask(false);
      time += getEffectTime();
    }
    if (time > 0)
    {
      _player.setVar("nevit", time, -1L);
      _player.sendPacket(new ExNavitAdventEffect(time));
      _player.sendPacket(SystemMsg.THE_ANGEL_NEVIT_HAS_BLESSED_YOU_FROM_ABOVE_YOU_ARE_IMBUED_WITH_FULL_VITALITY_AS_WELL_AS_A_VITALITY_REPLENISHING_EFFECT);
      _player.startAbnormalEffect(AbnormalEffect.S_NAVIT);
      _nevitEffectTask = ThreadPoolManager.getInstance().schedule(new NevitEffectEnd(null), time * 1000L);
    }
  }

  public void stopTasksOnLogout()
  {
    stopNevitEffectTask(true);
    stopAdventTask(false);
  }

  public void stopAdventTask(boolean sendPacket)
  {
    if (_adventTask != null)
    {
      _adventTask.cancel(true);
      _adventTask = null;
    }
    _active = false;
    if (sendPacket)
      _player.sendPacket(new ExNavitAdventTimeChange(_active, _time));
  }

  private void stopNevitEffectTask(boolean saveTime)
  {
    if (_nevitEffectTask != null)
    {
      if (saveTime)
      {
        int time = getEffectTime();
        if (time > 0)
          _player.setVar("nevit", time, -1L);
        else
          _player.unsetVar("nevit");
      }
      _nevitEffectTask.cancel(true);
      _nevitEffectTask = null;
    }
  }

  public boolean isActive()
  {
    return _active;
  }

  public int getTime()
  {
    return _time;
  }

  public int getPoints()
  {
    return _points;
  }

  public void addPoints(int val)
  {
    _points += val;
    int percent = getPercent(_points);
    if (_percent != percent)
    {
      _percent = percent;
      if (_percent == 45)
        _player.sendPacket(SystemMsg.YOU_ARE_STARTING_TO_FEEL_THE_EFFECTS_OF_NEVITS_BLESSING);
      else if (_percent == 50)
        _player.sendPacket(SystemMsg.YOU_ARE_FURTHER_INFUSED_WITH_THE_BLESSINGS_OF_NEVIT_CONTINUE_TO_BATTLE_EVIL_WHEREVER_IT_MAY_LURK);
      else if (_percent == 75)
        _player.sendPacket(SystemMsg.NEVITS_BLESSING_SHINES_STRONGLY_FROM_ABOVE_YOU_CAN_ALMOST_SEE_HIS_DIVINE_AURA);
    }
    if (_points > 7200)
    {
      _percent = 0;
      _points = 0;
      startNevitEffect(180);
    }
    _player.sendPacket(new ExNavitAdventPointInfo(_points));
  }

  public int getPercent(int points)
  {
    return (int)(0.01388888888888889D * points);
  }

  public void setTime(int time)
  {
    _time = time;
  }

  public boolean isBlessingActive()
  {
    return getEffectTime() > 0;
  }

  private int getEffectTime()
  {
    if (_nevitEffectTask == null)
      return 0;
    return (int)Math.max(0L, _nevitEffectTask.getDelay(TimeUnit.SECONDS));
  }

  private class NevitEffectEnd extends RunnableImpl
  {
    private NevitEffectEnd()
    {
    }

    public void runImpl()
    {
      _player.sendPacket(new ExNavitAdventEffect(0));
      _player.sendPacket(new ExNavitAdventPointInfo(_points));
      _player.sendPacket(SystemMsg.NEVITS_BLESSING_HAS_ENDED_CONTINUE_YOUR_JOURNEY_AND_YOU_WILL_SURELY_MEET_HIS_FAVOR_AGAIN_SOMETIME_SOON);
      _player.stopAbnormalEffect(AbnormalEffect.S_NAVIT);
      _player.unsetVar("nevit");
      NevitSystem.this.stopNevitEffectTask(false);
    }
  }

  private class AdventTask extends RunnableImpl
  {
    private AdventTask()
    {
    }

    public void runImpl()
    {
      NevitSystem.access$220(NevitSystem.this, 30);
      if (_time <= 0)
      {
        NevitSystem.access$202(NevitSystem.this, 0);
        stopAdventTask(true);
      }
      else
      {
        addPoints(72);
        if (_time % 60 == 0)
          _player.sendPacket(new ExNavitAdventTimeChange(true, _time));
      }
    }
  }
}