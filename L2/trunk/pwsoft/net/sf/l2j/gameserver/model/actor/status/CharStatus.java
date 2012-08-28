package net.sf.l2j.gameserver.model.actor.status;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.stat.CharStat;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.model.entity.Duel.DuelState;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;

public class CharStatus
{
  protected static final Logger _log = AbstractLogger.getLogger(CharStatus.class.getName());
  private L2Character _activeChar;
  private volatile double _currentCp = 0.0D;
  private volatile double _currentHp = 0.0D;
  private volatile double _currentMp = 0.0D;
  private CopyOnWriteArraySet<L2Character> _statusListener;
  private final Object statusListenerLock = new Object();
  private Future<?> _regTask;
  private byte _flagsRegenActive = 0;
  private static final byte REGEN_FLAG_CP = 4;
  private static final byte REGEN_FLAG_HP = 1;
  private static final byte REGEN_FLAG_MP = 2;

  public CharStatus(L2Character activeChar)
  {
    _activeChar = activeChar;
  }

  public final void addStatusListener(L2Character object)
  {
    if (object.equals(_activeChar))
      return;
    CopyOnWriteArraySet listeners;
    synchronized (statusListenerLock) {
      if (_statusListener == null) {
        _statusListener = new CopyOnWriteArraySet();
      }

      listeners = _statusListener;
    }
    listeners.add(object);
  }

  public final void removeStatusListener(L2Character object) {
    synchronized (statusListenerLock) {
      if (getStatusListener() == null) {
        return;
      }

      getStatusListener().remove(object);
      if ((getStatusListener() != null) && (getStatusListener().isEmpty()))
        setStatusListener(null);
    }
  }

  private void setStatusListener(CopyOnWriteArraySet<L2Character> value)
  {
    _statusListener = value;
  }

  public final void reduceCp(int value) {
    if (getCurrentCp() > value)
      setCurrentCp(getCurrentCp() - value);
    else
      setCurrentCp(0.0D);
  }

  public void reduceHp(double value, L2Character attacker)
  {
    reduceHp(value, attacker, true);
  }

  public void reduceHp(double value, L2Character attacker, boolean awake) {
    if ((_activeChar.isInvul()) || (_activeChar.isDead())) {
      return;
    }

    if (_activeChar.isPlayer()) {
      if (_activeChar.getDuel() != null)
      {
        if (_activeChar.getDuel().getDuelState(_activeChar.getPlayer()) == Duel.DuelState.Dead)
          return;
        if (_activeChar.getDuel().getDuelState(_activeChar.getPlayer()) == Duel.DuelState.Winner) {
          return;
        }

        if ((!attacker.isSummon()) && ((!attacker.isPlayer()) || (attacker.getDuel() != _activeChar.getDuel()))) {
          _activeChar.getDuel().setDuelState(_activeChar.getPlayer(), Duel.DuelState.Interrupted);
        }
      }
    }
    else if ((attacker.isPlayer()) && (attacker.isInDuel()) && ((!_activeChar.isSummon()) || (_activeChar.getOwner().getDuel() != attacker.getDuel())))
    {
      attacker.getDuel().setDuelState(attacker.getPlayer(), Duel.DuelState.Interrupted);
    }

    if ((awake) && (_activeChar.isSleeping())) {
      _activeChar.stopSleeping(null);
    }

    if ((_activeChar.isStunned()) && (Rnd.get(10) == 0)) {
      _activeChar.stopStunning(null);
    }

    if (_activeChar.isL2Npc()) {
      _activeChar.addAttackerToAttackByList(attacker);
    }

    if (value > 0.0D)
    {
      if (_activeChar.isL2Attackable()) {
        if (((L2Attackable)_activeChar).isOverhit())
          ((L2Attackable)_activeChar).setOverhitValues(attacker, value);
        else {
          ((L2Attackable)_activeChar).overhitEnabled(false);
        }
      }
      value = getCurrentHp() - value;
      if (value <= 0.0D)
      {
        if (_activeChar.isPlayer()) {
          boolean pvp = false;
          L2PcInstance player = _activeChar.getPlayer();
          if (player.isInDuel()) {
            player.getDuel().onPlayerDefeat(player);
            pvp = true;
          }

          if (pvp) {
            stopHpMpRegeneration();
            _activeChar.setIsKilledAlready(true);
            _activeChar.setIsPendingRevive(true);
            value = 1.0D;
          } else {
            value = 0.0D;
          }
        } else {
          value = 0.0D;
        }
      }
      setCurrentHp(value);
    }
    else if (_activeChar.isL2Attackable()) {
      ((L2Attackable)_activeChar).overhitEnabled(false);
    }

    if (_activeChar.isDead()) {
      _activeChar.abortAttack();
      _activeChar.abortCast();

      if ((_activeChar.isPlayer()) && 
        (_activeChar.isInOlympiadMode())) {
        stopHpMpRegeneration();
        _activeChar.setIsKilledAlready(true);
        _activeChar.setIsPendingRevive(true);
        if (_activeChar.getPet() != null) {
          _activeChar.getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
        }
        return;
      }

      _activeChar.doDie(attacker);

      setCurrentHp(0.0D);
    }
    else if (_activeChar.isL2Attackable()) {
      ((L2Attackable)_activeChar).overhitEnabled(false);
    }
  }

  public final void reduceMp(double value)
  {
    value = getCurrentMp() - value;
    if (value < 0.0D) {
      value = 0.0D;
    }
    setCurrentMp(value);
  }

  public final synchronized void startHpMpRegeneration()
  {
    if ((_regTask == null) && (!getActiveChar().isDead()))
    {
      _regTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new RegenTask(), getActiveChar().getRegeneratePeriod(), getActiveChar().getRegeneratePeriod());
    }
  }

  public final synchronized void stopHpMpRegeneration()
  {
    if (_regTask != null)
    {
      _regTask.cancel(false);
      _regTask = null;

      _flagsRegenActive = 0;
    }
  }

  public L2Character getActiveChar()
  {
    return _activeChar;
  }

  public final double getCurrentCp() {
    return _currentCp;
  }

  public final void setCurrentCp(double newCp) {
    setCurrentCp(newCp, true);
  }

  public final void setCurrentCp(double newCp, boolean broadcastPacket) {
    synchronized (this)
    {
      int maxCp = _activeChar.getStat().getMaxCp();

      if (newCp < 0.0D) {
        newCp = 0.0D;
      }

      if (newCp >= maxCp)
      {
        _currentCp = maxCp;

        _flagsRegenActive = (byte)(_flagsRegenActive & 0xFFFFFFFB);

        if (_flagsRegenActive == 0)
          stopHpMpRegeneration();
      }
      else
      {
        _currentCp = newCp;

        _flagsRegenActive = (byte)(_flagsRegenActive | 0x4);

        startHpMpRegeneration();
      }

    }

    if (broadcastPacket)
      _activeChar.broadcastStatusUpdate();
  }

  public final double getCurrentHp()
  {
    return _currentHp;
  }

  public final void setCurrentHp(double newHp) {
    setCurrentHp(newHp, true);
  }

  public final void setCurrentHp(double newHp, boolean broadcastPacket) {
    newHp = Math.min(_activeChar.getMaxHp(), Math.max(0.0D, newHp));

    if (_currentHp == newHp) {
      return;
    }

    double startHp = _currentHp;
    synchronized (this)
    {
      double maxHp = _activeChar.getMaxHp();

      if (newHp >= maxHp)
      {
        _currentHp = maxHp;

        _flagsRegenActive = (byte)(_flagsRegenActive & 0xFFFFFFFE);
        _activeChar.setIsKilledAlready(false);

        if (_flagsRegenActive == 0)
          stopHpMpRegeneration();
      }
      else
      {
        _currentHp = newHp;

        _flagsRegenActive = (byte)(_flagsRegenActive | 0x1);
        if (!_activeChar.isDead()) {
          _activeChar.setIsKilledAlready(false);
        }

        startHpMpRegeneration();
      }
    }

    _activeChar.checkHpMessages(startHp, newHp);

    _activeChar.broadcastStatusUpdate();
  }

  public final void setCurrentHpMp(double newHp, double newMp)
  {
    setCurrentHp(newHp, false);
    setCurrentMp(newMp, true);
  }

  public final double getCurrentMp() {
    return _currentMp;
  }

  public final void setCurrentMp(double newMp) {
    setCurrentMp(newMp, true);
  }

  public final void setCurrentMp(double newMp, boolean broadcastPacket) {
    synchronized (this)
    {
      int maxMp = _activeChar.getStat().getMaxMp();

      if (newMp >= maxMp)
      {
        _currentMp = maxMp;
        _flagsRegenActive = (byte)(_flagsRegenActive & 0xFFFFFFFD);

        if (_flagsRegenActive == 0)
          stopHpMpRegeneration();
      }
      else
      {
        _currentMp = newMp;
        _flagsRegenActive = (byte)(_flagsRegenActive | 0x2);

        startHpMpRegeneration();
      }

    }

    if (broadcastPacket)
      _activeChar.broadcastStatusUpdate();
  }

  public final CopyOnWriteArraySet<L2Character> getStatusListener()
  {
    if (_statusListener == null) {
      _statusListener = new CopyOnWriteArraySet();
    }
    return _statusListener;
  }
  public void reduceNpcHp(double value, L2Character attacker, boolean awake) {
  }
  class RegenTask implements Runnable {
    RegenTask() {
    }

    public void run() {
      try {
        CharStat charstat = _activeChar.getStat();

        if (getCurrentCp() < charstat.getMaxCp()) {
          setCurrentCp(getCurrentCp() + Formulas.calcCpRegen(_activeChar), false);
        }

        if (getCurrentHp() < charstat.getMaxHp()) {
          setCurrentHp(getCurrentHp() + Formulas.calcHpRegen(_activeChar), false);
        }

        if (getCurrentMp() < charstat.getMaxMp()) {
          setCurrentMp(getCurrentMp() + Formulas.calcMpRegen(_activeChar), false);
        }

        if (!_activeChar.isInActiveRegion().booleanValue())
        {
          if ((getCurrentCp() == charstat.getMaxCp()) && (getCurrentHp() == charstat.getMaxHp()) && (getCurrentMp() == charstat.getMaxMp()))
            stopHpMpRegeneration();
        }
        else {
          _activeChar.broadcastStatusUpdate();
          _activeChar.sendChanges();
        }
      } catch (Throwable e) {
        CharStatus._log.log(Level.SEVERE, "class RegenTask implements Runnable", e);
        e.printStackTrace();
      }
    }
  }
}