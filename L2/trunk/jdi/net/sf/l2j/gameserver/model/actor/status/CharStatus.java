package net.sf.l2j.gameserver.model.actor.status;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.instancemanager.DuelManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.actor.stat.CharStat;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.util.Rnd;

public class CharStatus
{
  protected static final Logger _log = Logger.getLogger(CharStatus.class.getName());
  private L2Character _activeChar;
  private double _currentCp = 0.0D;
  private double _currentHp = 0.0D;
  private double _currentMp = 0.0D;
  private Set<L2Character> _StatusListener;
  private Future _regTask;
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
    if (object == getActiveChar()) return;

    synchronized (getStatusListener())
    {
      getStatusListener().add(object);
    }
  }

  public final void reduceCp(int value)
  {
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
    if (getActiveChar().isInvul()) return;

    if ((getActiveChar() instanceof L2PcInstance))
    {
      if (((L2PcInstance)getActiveChar()).isInDuel())
      {
        if (((L2PcInstance)getActiveChar()).getDuelState() == 2) return;
        if (((L2PcInstance)getActiveChar()).getDuelState() == 3) return;

        if ((!(attacker instanceof L2SummonInstance)) && ((!(attacker instanceof L2PcInstance)) || (((L2PcInstance)attacker).getDuelId() != ((L2PcInstance)getActiveChar()).getDuelId())))
        {
          ((L2PcInstance)getActiveChar()).setDuelState(4);
        }
      }
      if ((getActiveChar().isDead()) && (!getActiveChar().isFakeDeath())) return;
    }
    else
    {
      if (getActiveChar().isDead()) return;

      if (((attacker instanceof L2PcInstance)) && (((L2PcInstance)attacker).isInDuel()) && ((!(getActiveChar() instanceof L2SummonInstance)) || (((L2SummonInstance)getActiveChar()).getOwner().getDuelId() != ((L2PcInstance)attacker).getDuelId())))
      {
        ((L2PcInstance)attacker).setDuelState(4);
      }
    }
    if ((awake) && (getActiveChar().isSleeping())) getActiveChar().stopSleeping(null);
    if ((awake) && (getActiveChar().isMeditation())) getActiveChar().stopMeditation(null);
    if ((getActiveChar().isStunned()) && (Rnd.get(10) == 0)) getActiveChar().stopStunning(null);
    if (getActiveChar().isAfraid()) getActiveChar().stopFear(null);

    if ((getActiveChar() instanceof L2NpcInstance)) getActiveChar().addAttackerToAttackByList(attacker);

    if (value > 0.0D)
    {
      if ((getActiveChar() instanceof L2Attackable))
      {
        if (((L2Attackable)getActiveChar()).isOverhit())
          ((L2Attackable)getActiveChar()).setOverhitValues(attacker, value);
        else
          ((L2Attackable)getActiveChar()).overhitEnabled(false);
      }
      value = getCurrentHp() - value;
      if (value <= 0.0D)
      {
        if (((getActiveChar() instanceof L2PcInstance)) && (((L2PcInstance)getActiveChar()).isInDuel()))
        {
          getActiveChar().disableAllSkills();
          stopHpMpRegeneration();
          attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
          attacker.sendPacket(new ActionFailed());

          DuelManager.getInstance().onPlayerDefeat((L2PcInstance)getActiveChar());
          value = 1.0D;
        } else {
          value = 0.0D;
        }
      }
      setCurrentHp(value);
    }
    else if ((getActiveChar() instanceof L2Attackable))
    {
      ((L2Attackable)getActiveChar()).overhitEnabled(false);
    }

    if (getActiveChar().isDead())
    {
      getActiveChar().abortAttack();
      getActiveChar().abortCast();

      if ((getActiveChar() instanceof L2PcInstance))
      {
        if (((L2PcInstance)getActiveChar()).isInOlympiadMode())
        {
          stopHpMpRegeneration();
          return;
        }

      }

      if (Config.DEBUG) _log.fine("char is dead.");

      getActiveChar().doDie(attacker);

      if ((getActiveChar() instanceof L2PcInstance))
      {
        QuestState qs = ((L2PcInstance)getActiveChar()).getQuestState("255_Tutorial");
        if (qs != null) {
          qs.getQuest().notifyEvent("CE30", null, (L2PcInstance)getActiveChar());
        }
      }

      setCurrentHp(0.0D);
    }
    else if ((getActiveChar() instanceof L2Attackable))
    {
      ((L2Attackable)getActiveChar()).overhitEnabled(false);
    }
  }

  public final void reduceMp(double value)
  {
    value = getCurrentMp() - value;
    if (value < 0.0D) value = 0.0D;
    setCurrentMp(value);
  }

  public final void removeStatusListener(L2Character object)
  {
    synchronized (getStatusListener())
    {
      getStatusListener().remove(object);
    }
  }

  public final synchronized void startHpMpRegeneration()
  {
    if ((_regTask == null) && (!getActiveChar().isDead()))
    {
      if (Config.DEBUG) _log.fine("HP/MP/CP regen started");

      int period = Formulas.getInstance().getRegeneratePeriod(getActiveChar());

      _regTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new RegenTask(), period, period);
    }
  }

  public final synchronized void stopHpMpRegeneration()
  {
    if (_regTask != null)
    {
      if (Config.DEBUG) _log.fine("HP/MP/CP regen stop");

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

  public final void setCurrentCp(double newCp, boolean broadcastPacket)
  {
    synchronized (this)
    {
      int maxCp = getActiveChar().getStat().getMaxCp();

      if (newCp < 0.0D) newCp = 0.0D;

      if (newCp >= maxCp)
      {
        _currentCp = maxCp;
        _flagsRegenActive = (byte)(_flagsRegenActive & 0xFFFFFFFB);

        if (_flagsRegenActive == 0) stopHpMpRegeneration();

      }
      else
      {
        _currentCp = newCp;
        _flagsRegenActive = (byte)(_flagsRegenActive | 0x4);

        startHpMpRegeneration();
      }

    }

    if (broadcastPacket)
      getActiveChar().broadcastStatusUpdate(); 
  }

  public final double getCurrentHp() {
    return _currentHp;
  }
  public final void setCurrentHp(double newHp) {
    setCurrentHp(newHp, true);
  }

  public final void setCurrentHp(double newHp, boolean broadcastPacket)
  {
    double maxHp = getActiveChar().getStat().getMaxHp();
    synchronized (this)
    {
      if (newHp >= maxHp)
      {
        _currentHp = maxHp;
        _flagsRegenActive = (byte)(_flagsRegenActive & 0xFFFFFFFE);
        getActiveChar().setIsKilledAlready(false);

        if (_flagsRegenActive == 0) stopHpMpRegeneration();

      }
      else
      {
        _currentHp = newHp;
        _flagsRegenActive = (byte)(_flagsRegenActive | 0x1);
        if (!getActiveChar().isDead()) getActiveChar().setIsKilledAlready(false);

        startHpMpRegeneration();
      }
    }

    if ((getActiveChar() instanceof L2PcInstance))
    {
      if (getCurrentHp() <= maxHp * 0.3D)
      {
        QuestState qs = ((L2PcInstance)getActiveChar()).getQuestState("255_Tutorial");
        if (qs != null) {
          qs.getQuest().notifyEvent("CE45", null, (L2PcInstance)getActiveChar());
        }
      }
    }

    if (broadcastPacket)
      getActiveChar().broadcastStatusUpdate();
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
      int maxMp = getActiveChar().getStat().getMaxMp();

      if (newMp >= maxMp)
      {
        _currentMp = maxMp;
        _flagsRegenActive = (byte)(_flagsRegenActive & 0xFFFFFFFD);

        if (_flagsRegenActive == 0) stopHpMpRegeneration();

      }
      else
      {
        _currentMp = newMp;
        _flagsRegenActive = (byte)(_flagsRegenActive | 0x2);

        startHpMpRegeneration();
      }

    }

    if (broadcastPacket)
      getActiveChar().broadcastStatusUpdate();
  }

  public final Set<L2Character> getStatusListener()
  {
    if (_StatusListener == null) _StatusListener = new CopyOnWriteArraySet();
    return _StatusListener;
  }

  class RegenTask implements Runnable
  {
    RegenTask()
    {
    }

    public void run()
    {
      try {
        CharStat charstat = getActiveChar().getStat();

        if (getCurrentCp() < charstat.getMaxCp()) setCurrentCp(getCurrentCp() + Formulas.getInstance().calcCpRegen(getActiveChar()), false);

        if (getCurrentHp() < charstat.getMaxHp()) setCurrentHp(getCurrentHp() + Formulas.getInstance().calcHpRegen(getActiveChar()), false);

        if (getCurrentMp() < charstat.getMaxMp()) setCurrentMp(getCurrentMp() + Formulas.getInstance().calcMpRegen(getActiveChar()), false);

        if (!getActiveChar().isInActiveRegion().booleanValue())
        {
          if ((getCurrentCp() == charstat.getMaxCp()) && (getCurrentHp() == charstat.getMaxHp()) && (getCurrentMp() == charstat.getMaxMp()))
            stopHpMpRegeneration();
        }
        else
          getActiveChar().broadcastStatusUpdate();
      } catch (Throwable e) {
        CharStatus._log.log(Level.SEVERE, "", e);
      }
    }
  }
}